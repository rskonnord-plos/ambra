<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:my="my:ingest.pmc#"
    exclude-result-prefixes="my">

  <!--
    - Convert a ZipInfo (zip.dtd) to an ObjectList (fedora.dtd). This contains the main 
    - object generation logic for ingest.
    -
    - This converter handles zip's according to TOPAZ's specs and zip's from AP, both of
    - which use PMC 2.0 for the main article description.
    -->

  <xsl:include href="validate_pmc.xslt"/>
  <xsl:preserve-space elements="article"/>

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:output name="pmc-1.1"
      doctype-public="-//NLM//DTD Journal Publishing DTD v1.1 20031101//EN"
      doctype-system="http://dtd.nlm.nih.gov/publishing/1.1/journalpublishing.dtd"/>
  <xsl:output name="pmc-2.0"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.0 20040830//EN"
      doctype-system="http://dtd.nlm.nih.gov/publishing/2.0/journalpublishing.dtd"/>
  <xsl:output name="pmc-2.1"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.1 20050630//EN"
      doctype-system="http://dtd.nlm.nih.gov/publishing/2.1/journalpublishing.dtd"/>
  <xsl:output name="pmc-2.2"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.2 20060430//EN"
      doctype-system="http://dtd.nlm.nih.gov/publishing/2.2/journalpublishing.dtd"/>

  <xsl:param name="output-loc" select="''" as="xs:string"/>

  <xsl:variable name="file-entries"  select="/ZipInfo/ZipEntry[not(@isDirectory)]"
      as="element(ZipEntry)*"/>
  <xsl:variable name="article-entry" select="my:find-pmc-xml(/ZipInfo)"
      as="element(ZipEntry)"/>
  <xsl:variable name="orig-article"  select="document($article-entry/@name, .)/article"
      as="element(article)"/>
  <xsl:variable name="meta"          select="$orig-article/front/article-meta"
      as="element(article-meta)"/>
  <xsl:variable name="article-doi"   select="$meta/article-id[@pub-id-type = 'doi']"
      as="xs:string"/>
  <xsl:variable name="zip-fmt"
      select="if (my:basename($article-entry/@name) = 'pmc.xml') then 'TPZ' else 'AP'"
      as="xs:string"/>
  <xsl:variable name="sec-dois"
      select="distinct-values(
            for $ent in $file-entries[my:is-secondary(@name)] return my:fname-to-doi($ent/@name))"
      as="xs:string*"/>

  <xsl:variable name="fixed-article" select="my:fixup-article($orig-article)"
      as="element(article)"/>

  <xsl:variable name="sec-obj-refs"
      select="(for $doi in $sec-dois return my:links-for-doi($doi)[1]) union ()"
      as="element()*"/>

  <xsl:variable name="initial-state" select="1" as="xs:integer"/>
  <xsl:variable name="rdf-ns" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
      as="xs:string"/>
  <xsl:variable name="xs-ns" select="'http://www.w3.org/2001/XMLSchema#'" as="xs:string"/>

  <!-- top-level template - do some checks, and then run the production templates -->
  <xsl:template match="/">
    <xsl:call-template name="validate-pmc"/>

    <xsl:apply-templates/>
  </xsl:template>

  <!-- generate the ObjectList -->
  <xsl:template match="/ZipInfo">
    <ObjectList logMessage="Ingest of article '{$meta/title-group/article-title}'"
                articleId="{my:doi-to-uri($article-doi)}">
      <xsl:call-template name="main-entry"/>

      <xsl:for-each-group select="$file-entries[my:is-secondary(@name)]"
                          group-by="my:fname-to-doi(@name)">
        <xsl:apply-templates select="." mode="sec"/>
      </xsl:for-each-group>

      <xsl:for-each 
          select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
        <xsl:call-template name="cat-aux"/>
      </xsl:for-each>
    </ObjectList>
  </xsl:template>

  <!-- templates for the main (pmc) entry -->

  <!-- generate the object and rdf for the article -->
  <xsl:template name="main-entry">
    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="main-rdf"/>
    </xsl:variable>

    <Object pid="{my:doi-to-pid($article-doi)}" cModel="PlosArticle" doIndex="true">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="main-ds"/>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:doi-to-uri($article-doi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>

    <Permissions>
      <xsl:call-template name="main-permissions"/>
    </Permissions>
  </xsl:template>

  <!-- generate the rdf statements for the article -->
  <xsl:template name="main-rdf" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dc_terms="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <rdf:type rdf:resource="http://rdf.topazproject.org/RDF/Article"/>

    <dc:identifier><xsl:value-of select="my:doi-to-uri($article-doi)"/></dc:identifier>
    <dc:title rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/title-group/article-title"/></xsl:call-template></dc:title>
    <dc:type rdf:resource="http://purl.org/dc/dcmitype/Text"/>
    <dc:format>text/xml</dc:format>
    <dc:language>en</dc:language>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
      <dc_terms:issued rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:issued>
      <dc_terms:available rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:available>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'received']">
      <dc_terms:dateSubmitted rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'received'])"/></dc_terms:dateSubmitted>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'accepted']">
      <dc_terms:dateAccepted rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'accepted'])"/></dc_terms:dateAccepted>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:for-each
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
        <dc:subject rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="."/></xsl:call-template></dc:subject>
    </xsl:for-each>
    <xsl:if test="$meta/abstract">
      <dc:description rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="my:select-abstract($meta/abstract)"/></xsl:call-template></dc:description>
    </xsl:if>
    <xsl:if test="$fixed-article/front/journal-meta/publisher">
      <dc:publisher rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$fixed-article/front/journal-meta/publisher/publisher-name"/></xsl:call-template></dc:publisher>
    </xsl:if>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/copyright-statement"/></xsl:call-template></dc:rights>
    </xsl:if>

    <xsl:for-each select="$sec-dois">
      <dc_terms:hasPart rdf:resource="{my:doi-to-uri(.)}"/>
    </xsl:for-each>

    <xsl:for-each 
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
      <topaz:hasCategory
          rdf:resource="{my:doi-to-uri(my:doi-to-aux($article-doi, 'category', position()))}"/>
    </xsl:for-each>

    <xsl:if test="$sec-obj-refs">
      <topaz:nextObject rdf:resource="{$sec-obj-refs[1]/@xlink:href}"/>
    </xsl:if>

    <topaz:isPID><xsl:value-of select="my:doi-to-pid($article-doi)"/></topaz:isPID>
    <topaz:articleState rdf:datatype="{$xs-ns}int"><xsl:value-of select="$initial-state"/></topaz:articleState>

    <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate the propagate-permissions for the article -->
  <xsl:template name="main-permissions">
    <propagate from="{my:doi-to-uri($article-doi)}">
      <to><xsl:value-of select="my:doi-to-fedora-uri($article-doi)"/></to>
      <xsl:for-each select="$sec-dois">
        <to><xsl:value-of select="my:doi-to-uri(.)"/></to>
      </xsl:for-each>
    </propagate>
  </xsl:template>

  <!-- generate the article object's datastream definitions -->
  <xsl:template name="main-ds">
    <xsl:variable name="art-ext" as="xs:string" select="my:get-ext($article-entry/@name)"/>
    <xsl:variable name="loc"     as="xs:string"
        select="concat($output-loc, encode-for-uri(my:basename($article-entry/@name)))"/>

    <xsl:result-document href="{$loc}" method="xml" format="pmc-{$fixed-article/@dtd-version}">
      <xsl:sequence select="$fixed-article"/>
    </xsl:result-document>

    <Datastream contLoc="{$loc}" id="{my:ext-to-ds-id($art-ext)}"
                controlGroup="{my:ext-to-ctrlgrp($art-ext)}" mimeType="{my:ext-to-mime($art-ext)}"/>

    <xsl:apply-templates select="$file-entries[not(. is $article-entry)][my:is-main(@name)]"
        mode="ds"/>
  </xsl:template>

  <!-- generate the auxiliary object definitions (objects not directly present in the pmc) -->
  <xsl:template name="cat-aux">
    <xsl:variable name="cat-doi" as="xs:string"
        select="my:doi-to-aux($article-doi, 'category', position())"/>

    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="cat-aux-rdf">
        <xsl:with-param name="cat-doi" select="$cat-doi"/>
      </xsl:call-template>
    </xsl:variable>

    <Object pid="{my:doi-to-pid($cat-doi)}" cModel="PlosCategory">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:doi-to-uri($cat-doi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for an auxiliary object -->
  <xsl:template name="cat-aux-rdf" xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <xsl:param name="cat-doi"/>

    <xsl:variable name="main-cat" as="xs:string"
        select="if (contains(., '/')) then substring-before(., '/') else ."/>
    <xsl:variable name="sub-cat" as="xs:string" select="substring-after(., '/')"/>

    <topaz:mainCategory><xsl:value-of select="$main-cat"/></topaz:mainCategory>
    <xsl:if test="$sub-cat">
      <topaz:subCategory><xsl:value-of select="$sub-cat"/></topaz:subCategory>
    </xsl:if>

    <topaz:isPID><xsl:value-of select="my:doi-to-pid($cat-doi)"/></topaz:isPID>
  </xsl:template>

  <!-- templates for all secondary entries -->

  <!-- generate the object and rdf for a secondary object -->
  <xsl:template match="ZipEntry" mode="sec">
    <xsl:variable name="sdoi" select="my:fname-to-doi(@name)"/>
    <xsl:variable name="rdf" as="element()*">
      <xsl:call-template name="sec-rdf"/>
    </xsl:variable>


    <Object pid="{my:doi-to-pid($sdoi)}" cModel="PlosArticleSecObj">
      <DC xmlns:dc="http://purl.org/dc/elements/1.1/">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="sec-ds"/>
    </Object>

    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
             xmlns:topaz="http://rdf.topazproject.org/RDF/"
             xmlns:dc="http://purl.org/dc/elements/1.1/"
             xmlns:dc_terms="http://purl.org/dc/terms/">
      <rdf:Description rdf:about="{my:doi-to-uri($sdoi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>

    <Permissions>
      <xsl:call-template name="sec-permissions">
        <xsl:with-param name="sdoi" select="$sdoi"/>
      </xsl:call-template>
    </Permissions>
  </xsl:template>

  <!-- generate the rdf statements for the secondary object -->
  <xsl:template name="sec-rdf" xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:dc_terms="http://purl.org/dc/terms/"
                xmlns:topaz="http://rdf.topazproject.org/RDF/">
    <xsl:variable name="sdoi" select="my:fname-to-doi(@name)"/>

    <dc:identifier><xsl:value-of select="my:doi-to-uri($sdoi)"/></dc:identifier>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="{$xs-ns}date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/copyright-statement"/></xsl:call-template></dc:rights>
    </xsl:if>

    <xsl:variable name="dc-types" as="xs:anyURI*"
        select="distinct-values(
                      for $n in current-group() return my:ext-to-dctype(my:get-ext($n/@name)))"/>
    <xsl:for-each select="$dc-types">
      <dc:type rdf:resource="{.}"/>
    </xsl:for-each>

    <dc_terms:isPartOf rdf:resource="{my:doi-to-uri($article-doi)}"/>

    <xsl:variable name="idx" as="xs:integer?"
        select="index-of(for $uri in $sec-obj-refs/@xlink:href return my:uri-to-doi($uri),
                         my:fname-to-doi(./@name))"/>
    <xsl:variable name="next" as="xs:string?" select="$sec-obj-refs[$idx + 1]/@xlink:href"/>
    <xsl:if test="$next">
      <topaz:nextObject rdf:resource="{$next}"/>
    </xsl:if>

    <xsl:variable name="ctxt-obj" as="element()?"
        select="$sec-obj-refs[$idx]/(parent::* | self::supplementary-material)[last()]"/>
    <xsl:if test="$ctxt-obj">
      <topaz:contextElement><xsl:value-of select="local-name($ctxt-obj)"/></topaz:contextElement>
      <xsl:if test="$ctxt-obj/label">
        <dc:title rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$ctxt-obj/label"/></xsl:call-template></dc:title>
      </xsl:if>
      <xsl:if test="$ctxt-obj/caption">
        <dc:description rdf:datatype="{$rdf-ns}XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$ctxt-obj/caption"/></xsl:call-template></dc:description>
      </xsl:if>
    </xsl:if>

    <topaz:isPID><xsl:value-of select="my:doi-to-pid($sdoi)"/></topaz:isPID>
    <topaz:articleState rdf:datatype="{$xs-ns}int"><xsl:value-of select="$initial-state"/></topaz:articleState>

    <xsl:apply-templates select="current-group()" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate the propagate-permissions for the article -->
  <xsl:template name="sec-permissions">
    <xsl:param name="sdoi"/>

    <propagate from="{my:doi-to-uri($sdoi)}">
      <to><xsl:value-of select="my:doi-to-fedora-uri($sdoi)"/></to>
    </propagate>
  </xsl:template>

  <!-- generate the object's datastream definitions for the secondary object -->
  <xsl:template name="sec-ds">
    <xsl:apply-templates select="current-group()" mode="ds"/>
  </xsl:template>

  <!-- common templates for all datastream definitions -->
  <xsl:template match="ZipEntry" mode="ds-rdf" xmlns:topaz="http://rdf.topazproject.org/RDF/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>
    <xsl:variable name="rep-name" select="my:ext-to-ds-id($ext)"/>

    <topaz:hasRepresentation><xsl:value-of select="$rep-name"/></topaz:hasRepresentation>
    <xsl:element name="topaz:{$rep-name}-objectSize">
      <xsl:attribute name="rdf:datatype"><xsl:value-of select="$xs-ns"/>int</xsl:attribute>
      <xsl:value-of select="@size"/>
    </xsl:element>
    <xsl:element name="topaz:{$rep-name}-contentType">
      <xsl:value-of select="my:ext-to-mime($ext)"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ZipEntry" mode="ds">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>

    <Datastream contLoc="{encode-for-uri(@name)}" id="{my:ext-to-ds-id($ext)}"
                controlGroup="{my:ext-to-ctrlgrp($ext)}" mimeType="{my:ext-to-mime($ext)}"/>
  </xsl:template>


  <!-- Article Mods -->

  <xsl:function name="my:fixup-article" as="element(article)">
    <xsl:param name="article" as="element(article)"/>
    <xsl:apply-templates select="$article" mode="article-fixup"/>
  </xsl:function>

  <xsl:template match="@xlink:href" mode="article-fixup" priority="5">
    <xsl:attribute name="xlink:href"><xsl:value-of select="my:fixup-link(.)"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@*|node()" mode="article-fixup">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="article-fixup"/>
    </xsl:copy>
  </xsl:template>

  <xsl:function name="my:fixup-link" as="xs:string">
    <xsl:param name="href" as="xs:string"/>
    <xsl:sequence select="
      if (my:uri-is-absolute($href)) then
        (: doi-uri normalization: 'doi:/DOI' -> 'info:doi/DOI' :)
        if (starts-with($href, 'doi:')) then
          concat('info:doi/', substring($href, 5))
        else
          $href
      else if (starts-with(my:basename($href), my:get-root(my:basename($article-entry/@name)))) then
        my:doi-to-uri(my:fname-to-doi($href))
      else
        $href (: don't generate an error as the validation will later catch this :)
      "/>
  </xsl:function>

  <!-- Helper functions -->

  <!-- Try to figure out which entry is the xml article -->
  <xsl:function name="my:find-pmc-xml" as="element(ZipEntry)">
    <xsl:param name="zip-info" as="element(ZipInfo)"/>
    <xsl:variable name="base-zip-name" as="xs:string?"
              select="if ($zip-info/@name) then my:get-root(my:basename($zip-info/@name)) else ()"/>

    <xsl:sequence select="
      if ($file-entries[my:basename(@name) = 'pmc.xml']) then
        $file-entries[my:basename(@name) = 'pmc.xml'][1]
      else if ($base-zip-name and
               $file-entries[my:basename(@name) = concat($base-zip-name, '.xml')]) then
        $file-entries[my:basename(@name) = concat($base-zip-name, '.xml')][1]
      else if ($file-entries[ends-with(@name, '.xml')]) then
        $file-entries[@name =
                  min(for $n in $file-entries/@name[ends-with(., '.xml')] return xs:string($n))][1]
      else
        error((), 'Couldn''t find article entry in zip file')
      "/>
  </xsl:function>

  <!-- Parse Filename into root, ext -->
  <xsl:function name="my:parse-filename" as="xs:string+">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:sequence select="(my:urldecode(replace($fname, '(.*)\..*', '$1')),
                           my:urldecode(replace($fname, '.*\.', '')))"/>
  </xsl:function>

  <!-- remove any directories from the filename -->
  <xsl:function name="my:basename" as="xs:string">
    <xsl:param name="path" as="xs:string"/>
    <xsl:value-of select="replace($path, '.*/', '')"/>
  </xsl:function>

  <!-- Get DOI from filename -->
  <xsl:function name="my:fname-to-doi" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:variable name="froot" select="my:get-root(my:basename($name))"/>
    <xsl:value-of select="
      if ($zip-fmt = 'TPZ') then
        $froot
      else if ($zip-fmt = 'AP') then
        concat($article-doi, substring($froot, string-length(my:get-root(my:basename($article-entry/@name))) + 1))
      else
        error((), concat('internal error: unknown format ''', $zip-fmt, ''' in fct fname-to-doi'))
      "/>
  </xsl:function>

  <!-- Get root of filename -->
  <xsl:function name="my:get-root" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:value-of select="my:parse-filename($name)[1]"/>
  </xsl:function>

  <!-- Get extension from filename -->
  <xsl:function name="my:get-ext" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:value-of select="my:parse-filename($name)[last()]"/>
  </xsl:function>

  <!-- DOI to Fedora-PID mapping -->
  <xsl:function name="my:doi-to-pid" as="xs:string">
    <xsl:param name="doi" as="xs:string"/>
    <xsl:value-of select="concat('doi:', my:pidencode($doi))"/>
  </xsl:function>

  <!-- Fedora-PID to DOI mapping -->
  <xsl:function name="my:pid-to-doi" as="xs:string">
    <xsl:param name="pid" as="xs:string"/>
    <xsl:value-of select="
      if (starts-with($pid, 'doi:')) then
        my:urldecode(substring($pid, 5))
      else
        error((), concat('cannot convert pid ''', $pid, ''' to doi'))"/>
  </xsl:function>

  <!-- DOI to URI mapping -->
  <xsl:function name="my:doi-to-uri" as="xs:string">
    <xsl:param name="doi" as="xs:string"/>
    <xsl:value-of select="concat('info:doi/', my:doiencode($doi))"/>
  </xsl:function>

  <!-- URI to DOI mapping -->
  <xsl:function name="my:uri-to-doi" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:value-of select="
      if (my:is-doi-uri($uri)) then
        my:urldecode(substring($uri, 10))
      else
        error((), concat('cannot convert uri ''', $uri, ''' to doi'))"/>
  </xsl:function>

  <!-- DOI to fedora-URI mapping -->
  <xsl:function name="my:doi-to-fedora-uri" as="xs:string">
    <xsl:param name="doi" as="xs:string"/>
    <xsl:value-of select="concat('info:fedora/', my:doi-to-pid($doi))"/>
  </xsl:function>

  <!-- test for doi-URI's -->
  <xsl:function name="my:is-doi-uri" as="xs:boolean">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:value-of select="starts-with($uri, 'info:doi/')"/>
  </xsl:function>

  <!-- article-DOI to auxiliary-DOI mapping -->
  <xsl:function name="my:doi-to-aux" as="xs:string">
    <xsl:param name="doi"  as="xs:string"/>
    <xsl:param name="type" as="xs:string"/>
    <xsl:param name="cnt"  as="xs:integer"/>
    <xsl:value-of select="concat($doi, '/', $type, '/', $cnt)"/>
  </xsl:function>

  <!-- determines if the filename is that of a secondary object or not -->
  <xsl:function name="my:is-main" as="xs:boolean">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:value-of select="$fname = $article-entry/@name or my:fname-to-doi($fname) = $article-doi"/>
  </xsl:function>

  <xsl:function name="my:is-secondary" as="xs:boolean">
    <xsl:param name="fname" as="xs:string"/>
    <xsl:value-of select="not(my:is-main($fname))"/>
  </xsl:function>

  <!-- Check if the URI is absolute -->
  <xsl:function name="my:uri-is-absolute" as="xs:boolean">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:sequence select="matches($uri, '^[^:/?#]+:')"/>
  </xsl:function>

  <!-- doi-encode a string (replace reserved chars with %HH). Note this is the
     - same as the built-in encode-for-uri, except that it only encodes % ? # [ and ]
     -->
  <xsl:function name="my:doiencode" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="iri-to-uri(string-join(my:doiencode-seq($str), ''))"/>
  </xsl:function>

  <xsl:function name="my:doiencode-seq" as="xs:string+">
    <xsl:param name="str" as="xs:string"/>

    <xsl:analyze-string select="$str" regex="[%?#\[\]]">
      <xsl:matching-substring>
        <xsl:value-of select="concat('%', my:hex(string-to-codepoints(.)))"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <!-- pid-encode a string (replace reserved chars with %HH). Note this is similar
     - to the built-in encode-for-uri, except that it encodes all non-fedora-pid chars
     -->
  <xsl:function name="my:pidencode" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="string-join(my:pidencode-seq($str), '')"/>
  </xsl:function>

  <xsl:function name="my:pidencode-seq" as="xs:string+">
    <xsl:param name="str" as="xs:string"/>

    <xsl:analyze-string select="$str" regex="[^A-Za-z0-9.~_-]">
      <xsl:matching-substring>
        <xsl:value-of select="concat('%', my:hex(string-to-codepoints(.)))"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <xsl:function name="my:hex" as="xs:string">
    <xsl:param name="char" as="xs:integer"/>
    <xsl:value-of select="concat(my:hexChar($char idiv 16), my:hexChar($char mod 16))"/>
  </xsl:function>

  <xsl:function name="my:hexChar" as="xs:string">
    <xsl:param name="char" as="xs:integer"/>
    <xsl:variable name="cp" as="xs:integer" select="if ($char &gt; 9) then $char + 7 else $char"/>
    <xsl:value-of select="codepoints-to-string($cp + 48)"/>
  </xsl:function>

  <!-- url-decode a string (resolve the %HH) -->
  <xsl:function name="my:urldecode" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="string-join(my:decodeSeq($str), '')"/>
  </xsl:function>

  <xsl:function name="my:decodeSeq" as="xs:string*">
    <xsl:param name="str" as="xs:string"/>

    <xsl:analyze-string select="$str" regex="%[0-9A-Fa-f][0-9A-Fa-f]">
      <xsl:matching-substring>
        <xsl:value-of select="codepoints-to-string(my:unhex(substring(., 2)))"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:function>

  <xsl:function name="my:unhex" as="xs:integer">
    <xsl:param name="hex" as="xs:string"/>
    <xsl:variable name="u" as="xs:string" select="substring($hex, 1, 1)"/>
    <xsl:variable name="l" as="xs:string" select="substring($hex, 2, 1)"/>
    <xsl:value-of select="my:unhexChar($u) * 16 + my:unhexChar($l)"/>
  </xsl:function>

  <xsl:function name="my:unhexChar" as="xs:integer">
    <xsl:param name="hex" as="xs:string"/>
    <xsl:variable name="v" as="xs:integer" select="string-to-codepoints(upper-case($hex)) - 48"/>
    <xsl:value-of select="if ($v &gt; 9) then $v - 7 else $v"/>
  </xsl:function>

  <!-- separate out dublic-core from non-dublin-core -->
  <xsl:function name="my:filter-dc" as="element()*">
    <xsl:param name="rdf"    as="element()*"/>
    <xsl:param name="inc-dc" as="xs:boolean"/>

    <xsl:for-each select="$rdf">
      <xsl:if test="(self::dc:* or self::oai_dc:*) and $inc-dc or
                    not(self::dc:* or self::oai_dc:*) and not($inc-dc)"
          xmlns:dc="http://purl.org/dc/elements/1.1/"
          xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>

  <!-- remove xsd:date datatype attributes for fedora unsupported datatypes -->
  <xsl:function name="my:filter-dt" as="element()*">
    <xsl:param name="rdf" as="element()*"/>

    <xsl:for-each select="$rdf" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
      <xsl:choose>
        <xsl:when test="@rdf:datatype and
                          @rdf:datatype != concat($xs-ns, 'int') and
                          @rdf:datatype != concat($xs-ns, 'long') and
                          @rdf:datatype != concat($xs-ns, 'float') and
                          @rdf:datatype != concat($xs-ns, 'double')">
          <xsl:copy>
            <xsl:sequence select="not(@rdf:datatype)"/>
          </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>

  <!-- pmc structured name to simple string (for dc:creator etc) -->
  <xsl:function name="my:format-name" as="xs:string">
    <xsl:param name="contrib" as="element(contrib)"/>

    <xsl:choose>
      <xsl:when test="$contrib/name">
        <xsl:value-of select="
          if ($contrib/name/given-names) then
            if ($contrib/name/@name-style = 'eastern') then
              concat($contrib/name/surname, ' ', $contrib/name/given-names)
            else
              concat($contrib/name/given-names, ' ', $contrib/name/surname)
          else
            $contrib/name/surname
          "/>
      </xsl:when>

      <xsl:when test="$contrib/collab">
        <xsl:value-of select="$contrib/collab"/>
      </xsl:when>

      <xsl:when test="$contrib/string-name">
        <xsl:value-of select="$contrib/string-name"/>
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <!-- Select the date to use for dc:date. The order of preference is:
     - 'epub', 'epub-ppub', 'ppub', 'ecorrected', 'pcorrected', no-type, first -->
  <xsl:function name="my:select-date" as="element(pub-date)">
    <xsl:param name="date" as="element(pub-date)+"/>

    <xsl:variable name="pref-date" select="(
      for $t in ('epub', 'epub-ppub', 'ppub', 'ecorrected', 'pcorrected')
        return $date[@pub-type = $t]
      )[1]"/>

    <xsl:sequence select="
      if ($pref-date) then $pref-date
      else if ($date[not(@pub-type)]) then $date[not(@pub-type)]
      else $date[1]
      "/>
  </xsl:function>

  <!-- pmc structured date to ISO-8601 (YYYY-MM-DD); seasons results in first day of the season,
     - or Jan 1st in the case of winter (to get the year right); missing fields are defaulted
     - from the current time -->
  <xsl:function name="my:format-date" as="xs:string">
    <xsl:param name="date" as="element()"/>

    <xsl:variable name="year" as="xs:integer" select="
        if ($date/year) then $date/year else year-from-date(current-date())"/>

    <xsl:value-of select="concat(
      $year,
      '-',
      if ($date/season) then
        if (lower-case($date/season) = 'spring') then '03-21'
        else if (lower-case($date/season) = 'summer') then '06-21'
        else if (lower-case($date/season) = 'fall') then '09-23'
        else if (lower-case($date/season) = 'winter') then '01-01'
        else ''
      else
        concat(
          my:twochar(if ($date/month) then $date/month
                     else if ($year != year-from-date(current-date())) then 1
                     else month-from-date(current-date())),
          '-',
          my:twochar(if ($date/day) then $date/day
                     else if ($year = year-from-date(current-date()) and
                              $date/month and $date/month = month-from-date(current-date())) then
                         day-from-date(current-date())
                     else 1)
        )
      )"/>
  </xsl:function>

  <xsl:function name="my:twochar" as="xs:string">
    <xsl:param    name="str" as="xs:integer"/>
    <xsl:variable name="s" select="xs:string($str)"/>
    <xsl:value-of select="
        if (string-length($s) = 1) then concat('0', $s) else $s
      "/>
  </xsl:function>

  <!-- Select the abstract to use for dc:description. The order of preference is:
     - 'short', 'web-summary', 'toc', 'summary', 'ASCII', no-type, first -->
  <xsl:function name="my:select-abstract" as="element(abstract)">
    <xsl:param name="abstracts" as="element(abstract)+"/>

    <xsl:variable name="pref-abstract" select="(
      for $t in ('short', 'web-summary', 'toc', 'summary', 'ASCII')
        return $abstracts[@abstract-type = $t]
      )[1]"/>

    <xsl:sequence select="
      if ($pref-abstract) then $pref-abstract
      else if ($abstracts[not(@abstract-type)]) then $abstracts[not(@abstract-type)]
      else $abstracts[1]
      "/>
  </xsl:function>

  <!-- Filename extension to datastream-id -->
  <xsl:function name="my:ext-to-ds-id" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:value-of select="upper-case($ext)"/>
  </xsl:function>

  <!-- Filename extension to Fedora control-group mapping: everything is 'Managed'. Note:
       don't use 'XML' because Fedora messes with it then. -->
  <xsl:function name="my:ext-to-ctrlgrp" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:value-of select="'Managed'"/>
  </xsl:function>

  <!-- Filename extension to mime-type mapping; defaults to application/octet-stream if extension
     - is not recognized -->
  <xsl:function name="my:ext-to-mime" as="xs:string">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:variable name="e" as="xs:string" select="lower-case($ext)"/>
    <xsl:value-of select="
      if ($e = 'xml') then 'text/xml'
      else if ($e = 'htm' or $e = 'html') then 'text/html'
      else if ($e = 'txt') then 'text/plain'
      else if ($e = 'rtf') then 'text/rtf'
      else if ($e = 'pdf') then 'application/pdf'
      else if ($e = 'zip') then 'application/zip'
      else if ($e = 'dvi') then 'application/x-dvi'
      else if ($e = 'latex') then 'application/x-latex'
      else if ($e = 'swf') then 'application/x-shockwave-flash'
      else if ($e = 'png') then 'image/png'
      else if ($e = 'gif') then 'image/gif'
      else if ($e = 'tif' or $e = 'tiff') then 'image/tiff'
      else if ($e = 'jpg' or $e = 'jpeg' or $e = 'jpe') then 'image/jpeg'
      else if ($e = 'bmp') then 'image/bmp'
      else if ($e = 'xpm') then 'image/x-xpixmap'
      else if ($e = 'pnm') then 'image/x-portable-anymap'
      else if ($e = 'ief') then 'image/ief'
      else if ($e = 'ras') then 'image/x-cmu-raster'
      else if ($e = 'doc') then 'application/msword'
      else if ($e = 'xls') then 'application/vnd.ms-excel'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'ppt') then 'application/vnd.ms-powerpoint'
      else if ($e = 'ps' or $e = 'eps') then 'application/postscript'
      else if ($e = 'mpg' or $e = 'mpeg') then 'video/mpeg'
      else if ($e = 'mp4' or $e = 'mpg4') then 'video/mp4'
      else if ($e = 'mov' or $e = 'qt') then 'video/quicktime'
      else if ($e = 'avi') then 'video/x-msvideo'
      else if ($e = 'wmv') then 'video/x-ms-wmv'
      else if ($e = 'asf' or $e = 'asx') then 'video/x-ms-asf'
      else if ($e = 'divx') then 'video/x-divx'
      else if ($e = 'wav') then 'audio/x-wav'
      else if ($e = 'au' or $e = 'snd') then 'audio/basic'
      else if ($e = 'mp2' or $e = 'mp3') then 'audio/mpeg'
      else if ($e = 'ram' or $e = 'rm') then 'audio/x-pn-realaudio'
      else if ($e = 'ra') then 'audio/x-realaudio'
      else if ($e = 'aif' or $e = 'aiff') then 'audio/x-aiff'
      else if ($e = 'mid' or $e = 'midi' or $e = 'rmi') then 'audio/midi'
      else if ($e = 'wma') then 'audio/x-ms-wma'
      else 'application/octet-stream'
      "/>
  </xsl:function>

  <!-- Filename extension to dublin-core type mapping -->
  <xsl:function name="my:ext-to-dctype" as="xs:anyURI?">
    <xsl:param name="ext" as="xs:string"/>
    <xsl:variable name="mime-type"  as="xs:string" select="my:ext-to-mime($ext)"/>
    <xsl:variable name="media-type" as="xs:string" select="substring-before($mime-type, '/')"/>
    <xsl:sequence select="
      if      ($media-type = 'image') then xs:anyURI('http://purl.org/dc/dcmitype/StillImage')
      else if ($media-type = 'video') then xs:anyURI('http://purl.org/dc/dcmitype/MovingImage')
      else if ($media-type = 'audio') then xs:anyURI('http://purl.org/dc/dcmitype/Sound')
      else if ($media-type = 'text')  then xs:anyURI('http://purl.org/dc/dcmitype/Text')
      else if ($mime-type = 'application/vnd.ms-excel') then xs:anyURI('http://purl.org/dc/dcmitype/Dataset')
      else ()
      "/>
  </xsl:function>

  <!-- find all the hyperlinks pointing to the given doi -->
  <xsl:function name="my:links-for-doi" as="element()*">
    <xsl:param    name="doi" as="xs:string"/>
    <xsl:sequence select="$fixed-article/body//*[@xlink:href and my:is-doi-uri(@xlink:href) and
                                                 my:uri-to-doi(@xlink:href) = $doi]"/>
  </xsl:function>

  <!-- serialize an xml string -->
  <xsl:template name="xml-to-str">
    <xsl:param name="xml"/>
    <xsl:apply-templates mode="serialize" select="$xml/node()"/>
  </xsl:template>

  <xsl:template match="*" mode="serialize">
    <xsl:text/>&lt;<xsl:value-of select="name()"/>
    <xsl:variable name="attr-ns-uris" as="xs:anyURI*"
        select="for $attr in (@*) return namespace-uri($attr)"/>
    <xsl:for-each select="namespace::*[name() != 'xml']">
      <xsl:if test=". = namespace-uri(..) or . = $attr-ns-uris">
        <xsl:text> xmlns</xsl:text>
        <xsl:if test="name()">
          <xsl:text />:<xsl:value-of select="name()" />
        </xsl:if>
        <xsl:value-of select="concat('=&quot;', ., '&quot;')"/>
      </xsl:if>
    </xsl:for-each>
    <xsl:for-each select="@*">
      <xsl:value-of select="concat(' ', name(), '=&quot;', my:xml-escape(.), '&quot;')"/>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="node()">
        <xsl:text>></xsl:text>
        <xsl:apply-templates select="node()" mode="serialize"/>
        <xsl:text/>&lt;/<xsl:value-of select="name()"/><xsl:text>></xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/></xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text()" mode="serialize">
    <xsl:value-of select="my:xml-escape(.)"/>
  </xsl:template>

  <xsl:function name="my:xml-escape" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="replace(replace(replace($str, '&amp;', '&amp;amp;'), '&lt;', '&amp;lt;'), '&gt;', '&amp;gt;')"/>
  </xsl:function>
</xsl:stylesheet>
