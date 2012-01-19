<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE xsl:stylesheet [
    <!ENTITY xsd            "http://www.w3.org/2001/XMLSchema#" >
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
    <!ENTITY dc             "http://purl.org/dc/elements/1.1/" >
    <!ENTITY dcterms        "http://purl.org/dc/terms/">
    <!ENTITY dctype         "http://purl.org/dc/dcmitype/">
    <!ENTITY oai_dc         "http://www.openarchives.org/OAI/2.0/oai_dc/">
    <!ENTITY bibtex         "http://purl.org/net/nknouf/ns/bibtex#">
    <!ENTITY prism          "http://prismstandard.org/namespaces/1.2/basic/">
    <!ENTITY foaf           "http://xmlns.com/foaf/0.1/">
    <!ENTITY nlmpub         "http://dtd.nlm.nih.gov/publishing/">
    <!ENTITY plos           "http://rdf.plos.org/RDF/">
    <!ENTITY plosct         "http://rdf.plos.org/RDF/citation/type#">
    <!ENTITY plostmp        "http://rdf.plos.org/RDF/temporal#">
    <!ENTITY topaz          "http://rdf.topazproject.org/RDF/">
]>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:rdf="&rdf;"
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

  <xsl:output name="nlm-1.1"
      doctype-public="-//NLM//DTD Journal Publishing DTD v1.1 20031101//EN"
      doctype-system="&nlmpub;1.1/journalpublishing.dtd"/>
  <xsl:output name="nlm-2.0"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.0 20040830//EN"
      doctype-system="&nlmpub;2.0/journalpublishing.dtd"/>
  <xsl:output name="nlm-2.1"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.1 20050630//EN"
      doctype-system="&nlmpub;2.1/journalpublishing.dtd"/>
  <xsl:output name="nlm-2.2"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.2 20060430//EN"
      doctype-system="&nlmpub;2.2/journalpublishing.dtd"/>
  <xsl:output name="nlm-2.3"
      doctype-public="-//NLM//DTD Journal Publishing DTD v2.3 20070202//EN"
      doctype-system="&nlmpub;2.3/journalpublishing.dtd"/>

  <xsl:param name="output-loc"     select="''"                    as="xs:string"/>
  <xsl:param name="doi-url-prefix" select="'http://dx.plos.org/'" as="xs:string"/>

  <xsl:variable name="file-entries"  select="/ZipInfo/ZipEntry[not(@isDirectory)]"
      as="element(ZipEntry)*"/>
  <xsl:variable name="article-entry" select="my:find-pmc-xml(/ZipInfo)"
      as="element(ZipEntry)"/>
  <xsl:variable name="orig-article"  select="document($article-entry/@name, .)/article"
      as="element(article)"/>
  <xsl:variable name="meta"          select="$orig-article/front/article-meta"
      as="element(article-meta)"/>
  <xsl:variable name="jnl-meta"      select="$orig-article/front/journal-meta"
      as="element(journal-meta)"/>
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

  <xsl:variable name="contrib-editors"
      select="(for $u in $meta/contrib-group/contrib[@contrib-type = 'editor']/name
               return my:create-user($u, '')) union ()"
      as="element(user)*"/>
  <xsl:variable name="contrib-authors"
      select="(for $u in $meta/contrib-group/contrib[@contrib-type = 'author']/name
               return my:create-user($u, '')) union ()"
      as="element(user)*"/>
  <xsl:variable name="citation-editors"
      select="(for $u in $orig-article/back/ref-list/ref/citation/person-group[@person-group-type = 'editor']/name
               return my:create-user($u, $u/../../../@id)) union ()"
      as="element(user)*"/>
  <xsl:variable name="citation-authors"
      select="(for $u in $orig-article/back/ref-list/ref/citation/person-group[@person-group-type = 'author']/name
               return my:create-user($u, $u/../../../@id)) union ()"
      as="element(user)*"/>

  <xsl:variable name="initial-state" select="1" as="xs:integer"/>

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
      <DC xmlns:dc="&dc;">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="&topaz;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="main-ds"/>
    </Object>

    <rdf:RDF xmlns:topaz="&topaz;" xmlns:dc="&dc;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;">
      <rdf:Description rdf:about="{my:doi-to-uri($article-doi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>

    <rdf:RDF xmlns:topaz="&topaz;">
      <rdf:Description rdf:about="{my:doi-to-uri($article-doi)}">
        <xsl:call-template name="main-new-rdf"/>
      </rdf:Description>
    </rdf:RDF>

    <rdf:RDF xmlns:topaz="&topaz;" model="profiles">
      <xsl:copy-of select="$contrib-editors/rdf:Description"/>
      <xsl:copy-of select="$contrib-authors/rdf:Description"/>
      <xsl:copy-of select="$citation-editors/rdf:Description"/>
      <xsl:copy-of select="$citation-authors/rdf:Description"/>
    </rdf:RDF>

    <rdf:RDF xmlns:topaz="&topaz;" model="pp">
      <rdf:Description rdf:about="{my:doi-to-uri($article-doi)}">
        <xsl:call-template name="main-pp-rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for the article -->
  <xsl:template name="main-rdf" xmlns:dc="&dc;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;"
                xmlns:topaz="&topaz;">
    <rdf:type rdf:resource="&topaz;Article"/>
    <rdf:type rdf:resource="&plos;articleType/{$fixed-article/@article-type}"/>
    <xsl:for-each select="$meta/article-categories/subj-group[@subj-group-type = 'heading']/subject">
      <rdf:type rdf:resource="&plos;articleType/{encode-for-uri(.)}"/>
    </xsl:for-each>

    <dc:identifier><xsl:value-of select="my:doi-to-uri($article-doi)"/></dc:identifier>
    <xsl:if test="$jnl-meta/issn[@pub-type = 'ppub']">
      <prism:issn><xsl:value-of select="$jnl-meta/issn[@pub-type = 'ppub']"/></prism:issn>
    </xsl:if>
    <xsl:if test="$jnl-meta/issn[@pub-type = 'epub']">
      <prism:eIssn><xsl:value-of select="$jnl-meta/issn[@pub-type = 'epub']"/></prism:eIssn>
    </xsl:if>
    <dc:title rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/title-group/article-title"/></xsl:call-template></dc:title>
    <dc:type rdf:resource="&dctype;Text"/>
    <dc:format>text/xml</dc:format>
    <dc:language>en</dc:language>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
      <dc_terms:issued rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:issued>
      <dc_terms:available rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc_terms:available>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'received']">
      <dc_terms:dateSubmitted rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'received'])"/></dc_terms:dateSubmitted>
    </xsl:if>
    <xsl:if test="$meta/history/date[@date-type = 'accepted']">
      <dc_terms:dateAccepted rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date($meta/history/date[@date-type = 'accepted'])"/></dc_terms:dateAccepted>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-contrib-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-contrib-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:for-each
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
        <dc:subject rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="."/></xsl:call-template></dc:subject>
    </xsl:for-each>
    <xsl:if test="$meta/abstract">
      <dc:description rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="my:select-abstract($meta/abstract)"/></xsl:call-template></dc:description>
    </xsl:if>
    <xsl:if test="$jnl-meta/publisher/publisher-name">
      <dc:publisher rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$jnl-meta/publisher/publisher-name"/></xsl:call-template></dc:publisher>
    </xsl:if>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/copyright-statement"/></xsl:call-template></dc:rights>
    </xsl:if>

    <xsl:for-each select="$sec-dois">
      <dc_terms:hasPart rdf:resource="{my:doi-to-uri(.)}"/>
    </xsl:for-each>

    <dc_terms:conformsTo
        rdf:resource="&nlmpub;{$fixed-article/@dtd-version}/journalpublishing.dtd"/>
    <xsl:if test="$meta/copyright-year">
      <dc_terms:dateCopyrighted rdf:datatype="&xsd;int"><xsl:value-of select="$meta/copyright-year"/></dc_terms:dateCopyrighted>
    </xsl:if>

    <xsl:for-each 
        select="$meta/article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
      <topaz:hasCategory
          rdf:resource="{my:doi-to-uri(my:doi-to-aux($article-doi, 'category', position()))}"/>
    </xsl:for-each>

    <xsl:if test="$sec-obj-refs">
      <topaz:nextObject rdf:resource="{$sec-obj-refs[1]/@xlink:href}"/>
    </xsl:if>

    <topaz:isPID><xsl:value-of select="my:doi-to-pid($article-doi)"/></topaz:isPID>
    <topaz:articleState rdf:datatype="&xsd;int"><xsl:value-of select="$initial-state"/></topaz:articleState>

    <xsl:apply-templates select="$file-entries[my:is-main(@name)]" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate new stuff - should eventually be in main-rdf when that doesn't get stored
     - in Fedora anymore
     -->
  <xsl:template name="main-new-rdf" xmlns:topaz="&topaz;" xmlns:bibtex="&bibtex;"
                xmlns:dc_terms="&dcterms;">
    <xsl:call-template name="gen-bib-cit"/>
    <xsl:call-template name="gen-ref"/>
    <xsl:call-template name="gen-lic"/>
  </xsl:template>

  <xsl:template name="gen-bib-cit" xmlns:topaz="&topaz;" xmlns:bibtex="&bibtex;"
                xmlns:dc_terms="&dcterms;">
    <dc_terms:bibliographicCitation>
      <xsl:variable name="pub-date" as="xs:string?"
          select="if ($meta/pub-date) then my:format-date(my:select-date($meta/pub-date)) else ()"/>
      <xsl:call-template name="gen-citation">
        <xsl:with-param name="id"
            select="xs:anyURI(concat(my:doi-to-uri($article-doi), '/bibliographicCitation'))"/>
        <xsl:with-param name="type"
            select="xs:anyURI('&bibtex;Article')"/>
        <xsl:with-param name="key"
            select="()"/>
        <xsl:with-param name="year"
            select="if ($pub-date) then xs:integer(substring($pub-date, 1, 4)) else ()"/>
        <xsl:with-param name="dispYear"
            select="if ($pub-date) then substring($pub-date, 1, 4) else ()"/>
        <xsl:with-param name="month"
            select="if ($pub-date) then substring($pub-date, 6, 2) else ()"/>
        <xsl:with-param name="volume"
            select="$meta/volume"/>
        <xsl:with-param name="volNum"
            select="xs:integer($meta/volume)"/>
        <xsl:with-param name="issue"
            select="$meta/issue"/>
        <xsl:with-param name="title"
            select="$meta/title-group/article-title"/>
        <xsl:with-param name="pub-loc"
            select="$jnl-meta/publisher/publisher-loc"/>
        <xsl:with-param name="pub-name"
            select="$jnl-meta/publisher/publisher-name"/>
        <xsl:with-param name="pages"
            select="if ($meta/counts/page-count) then concat('1-', $meta/counts/page-count/@count)
                    else ()"/>
        <xsl:with-param name="journal"
            select="$meta/journal-meta/journal-title"/>
        <xsl:with-param name="note"
            select="$meta/author-notes/fn[1]"/>
        <xsl:with-param name="editors"
            select="$contrib-editors/rdf:Description/@rdf:about"/>
        <xsl:with-param name="authors"
            select="$contrib-authors/rdf:Description/@rdf:about"/>
        <xsl:with-param name="url"
            select="xs:anyURI(concat($doi-url-prefix, encode-for-uri($article-doi)))"/>
        <xsl:with-param name="summary"
            select="if ($meta/abstract) then my:select-abstract($meta/abstract) else ()"/>
      </xsl:call-template>
    </dc_terms:bibliographicCitation>
  </xsl:template>

  <xsl:template name="gen-ref" xmlns:topaz="&topaz;" xmlns:bibtex="&bibtex;"
                xmlns:dc_terms="&dcterms;">
    <xsl:for-each select="$orig-article/back/ref-list/ref">
      <xsl:variable name="id" select="@id"/>
      <dc_terms:references>
        <xsl:call-template name="gen-citation">
          <xsl:with-param name="id"
              select="xs:anyURI(concat(my:doi-to-uri($article-doi), '/reference#', $id))"/>
          <xsl:with-param name="type"
              select="if (citation/@citation-type) then my:map-cit-type(citation/@citation-type)
                      else ()"/>
          <xsl:with-param name="key"      select="label"/>
          <xsl:with-param name="year"     select="my:find-int(citation/year[1], 4)"/>
          <xsl:with-param name="dispYear" select="citation/year[1]"/>
          <xsl:with-param name="month"    select="citation/month[1]"/>
          <xsl:with-param name="volume"   select="citation/volume[1]"/>
          <xsl:with-param name="volNum"   select="my:find-int(citation/volume[1], 1)"/>
          <xsl:with-param name="issue"    select="citation/issue[1]"/>
          <xsl:with-param name="title"
              select="if (citation/article-title) then citation/article-title[1]
                      else if (citation/source)   then citation/source[1]
                      else ()"/>
          <xsl:with-param name="pub-loc"  select="citation/publisher-loc[1]"/>
          <xsl:with-param name="pub-name" select="citation/publisher-name[1]"/>
          <xsl:with-param name="pages"
              select="if (citation/page-range) then citation/page-range[1]
                      else if (citation/lpage) then concat(citation/fpage, '-', citation/lpage)
                      else citation/fpage"/>
          <xsl:with-param name="journal"
              select="if (citation/@citation-type = 'journal' or
                          citation/@citation-type = 'confproc')
                        then citation/source[1] else ()"/>
          <xsl:with-param name="note"     select="citation/comment[1]"/>
          <xsl:with-param name="editors"
              select="$citation-editors[@cit-id = $id]/rdf:Description/@rdf:about"/>
          <xsl:with-param name="authors"
              select="$citation-authors[@cit-id = $id]/rdf:Description/@rdf:about"/>
          <xsl:with-param name="url"      select="citation/@xlink:role"/>
          <xsl:with-param name="summary"  select="()"/>
        </xsl:call-template>
      </dc_terms:references>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="gen-citation" xmlns:topaz="&topaz;" xmlns:plos="&plos;" xmlns:ptmp="&plostmp;"
                xmlns:bibtex="&bibtex;" xmlns:prism="&prism;" xmlns:dc="&dc;"
                xmlns:dc_terms="&dcterms;">
    <xsl:param name="id"       as="xs:anyURI"/>
    <xsl:param name="type"     as="xs:anyURI?"/>
    <xsl:param name="key"      as="xs:string?"/>
    <xsl:param name="year"     as="xs:integer?"/>
    <xsl:param name="dispYear" as="xs:string?"/>
    <xsl:param name="month"    as="xs:string?"/>
    <xsl:param name="volume"   as="xs:string?"/>
    <xsl:param name="volNum"   as="xs:integer?"/>
    <xsl:param name="issue"    as="xs:string?"/>
    <xsl:param name="title"/>
    <xsl:param name="pub-loc"  as="xs:string?"/>
    <xsl:param name="pub-name" as="xs:string?"/>
    <xsl:param name="pages"    as="xs:string?"/>
    <xsl:param name="journal"  as="xs:string?"/>
    <xsl:param name="note"     as="xs:string?"/>
    <xsl:param name="editors"  as="xs:anyURI*"/>
    <xsl:param name="authors"  as="xs:anyURI*"/>
    <xsl:param name="url"      as="xs:anyURI?"/>
    <xsl:param name="summary"/>

    <rdf:Description rdf:about="{$id}">
      <rdf:type rdf:resource="&bibtex;Entry"/>
      <xsl:if test="$type">
        <rdf:type rdf:resource="{$type}"/>
      </xsl:if>

      <xsl:if test="$key">
        <bibtex:hasKey rdf:datatype="&xsd;string"><xsl:value-of select="$key"/></bibtex:hasKey>
      </xsl:if>

      <xsl:if test="$year">
        <bibtex:hasYear rdf:datatype="&xsd;double"><xsl:value-of select="$year"/></bibtex:hasYear>
      </xsl:if>
      <xsl:if test="$dispYear">
        <ptmp:displayYear rdf:datatype="&xsd;string"><xsl:value-of select="$dispYear"/></ptmp:displayYear>
      </xsl:if>
      <xsl:if test="$month">
        <bibtex:hasMonth rdf:datatype="&xsd;string"><xsl:value-of select="$month"/></bibtex:hasMonth>
      </xsl:if>
      <xsl:if test="$volume">
        <prism:volume rdf:datatype="&xsd;string"><xsl:value-of select="$volume"/></prism:volume>
      </xsl:if>
      <xsl:if test="$volNum">
        <bibtex:hasVolume rdf:datatype="&xsd;double"><xsl:value-of select="$volNum"/></bibtex:hasVolume>
      </xsl:if>
      <xsl:if test="$issue">
        <bibtex:hasNumber rdf:datatype="&xsd;string"><xsl:value-of select="$issue"/></bibtex:hasNumber>
      </xsl:if>

      <xsl:if test="$title">
        <dc:title rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$title"/></xsl:call-template></dc:title>
      </xsl:if>

      <xsl:if test="$pub-loc">
        <bibtex:hasAddress rdf:datatype="&xsd;string"><xsl:value-of select="$pub-loc"/></bibtex:hasAddress>
      </xsl:if>
      <xsl:if test="$pub-name">
        <bibtex:hasPublisher rdf:datatype="&xsd;string"><xsl:value-of select="$pub-name"/></bibtex:hasPublisher>
      </xsl:if>
      <xsl:if test="$pages">
        <bibtex:hasPages rdf:datatype="&xsd;string"><xsl:value-of select="$pages"/></bibtex:hasPages>
      </xsl:if>
      <xsl:if test="$journal">
        <bibtex:hasJournal rdf:datatype="&xsd;string"><xsl:value-of select="$journal"/></bibtex:hasJournal>
      </xsl:if>
      <xsl:if test="$note">
        <bibtex:hasNote rdf:datatype="&xsd;string"><xsl:value-of select="$note"/></bibtex:hasNote>
      </xsl:if>
      <xsl:if test="$summary">
        <bibtex:hasAbstract rdf:datatype="&xsd;string"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$summary"/></xsl:call-template></bibtex:hasAbstract>
      </xsl:if>
      <xsl:if test="$url">
        <bibtex:hasURL rdf:datatype="&xsd;string"><xsl:value-of select="$url"/></bibtex:hasURL>
      </xsl:if>

      <xsl:if test="exists($editors)">
        <plos:hasEditorList>
          <rdf:Seq>
            <xsl:for-each select="$editors">
              <rdf:li rdf:resource="{.}"/>
            </xsl:for-each>
          </rdf:Seq>
        </plos:hasEditorList>
      </xsl:if>

      <xsl:if test="exists($authors)">
        <plos:hasAuthorList>
          <rdf:Seq>
            <xsl:for-each select="$authors">
              <rdf:li rdf:resource="{.}"/>
            </xsl:for-each>
          </rdf:Seq>
        </plos:hasAuthorList>
      </xsl:if>
    </rdf:Description>
  </xsl:template>

  <xsl:template name="gen-lic" xmlns:dc_terms="&dcterms;">
    <!-- Need OTM cascade="no-delete" for this
    <dc_terms:license>
      <rdf:Description rdf:about="http://creativecommons.org/licenses/by-sa/3.0/">
        <rdf:type rdf:resource="http://web.resource.org/cc/License"/>
      </rdf:Description>
    </dc_terms:license>
    -->
  </xsl:template>

  <!-- generate the propagate-permissions rdf statements for the article -->
  <xsl:template name="main-pp-rdf" xmlns:topaz="&topaz;">
    <topaz:propagate-permissions-to rdf:resource="{my:doi-to-fedora-uri($article-doi)}"/>
    <xsl:for-each select="$sec-dois">
      <topaz:propagate-permissions-to rdf:resource="{my:doi-to-uri(.)}"/>
    </xsl:for-each>
  </xsl:template>

  <!-- generate the article object's datastream definitions -->
  <xsl:template name="main-ds">
    <xsl:variable name="art-ext" as="xs:string" select="my:get-ext($article-entry/@name)"/>
    <xsl:variable name="loc"     as="xs:string"
        select="concat($output-loc, encode-for-uri(my:basename($article-entry/@name)))"/>

    <xsl:result-document href="{$loc}" method="xml" format="nlm-{$fixed-article/@dtd-version}">
      <xsl:sequence select="$fixed-article"/>
    </xsl:result-document>

    <Datastream contLoc="{$loc}" id="{my:name-to-ds-id($article-entry/@name)}"
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
      <DC xmlns:dc="&dc;">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="&topaz;" xmlns:dc_terms="&dcterms;">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
    </Object>

    <rdf:RDF xmlns:topaz="&topaz;" xmlns:dc="&dc;" xmlns:dc_terms="&dcterms;">
      <rdf:Description rdf:about="{my:doi-to-uri($cat-doi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for an auxiliary object -->
  <xsl:template name="cat-aux-rdf" xmlns:topaz="&topaz;">
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
      <DC xmlns:dc="&dc;">
        <xsl:sequence select="my:filter-dc($rdf, true())"/>
      </DC>
      <RELS-EXT xmlns:topaz="&topaz;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;">
        <xsl:sequence select="my:filter-dt(my:filter-dc($rdf, false()))"/>
      </RELS-EXT>
      <xsl:call-template name="sec-ds"/>
    </Object>

    <rdf:RDF xmlns:topaz="&topaz;" xmlns:dc="&dc;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;">
      <rdf:Description rdf:about="{my:doi-to-uri($sdoi)}">
        <xsl:sequence select="$rdf"/>
      </rdf:Description>
    </rdf:RDF>

    <rdf:RDF xmlns:topaz="&topaz;" model="pp">
      <rdf:Description rdf:about="{my:doi-to-uri($sdoi)}">
        <xsl:call-template name="sec-pp-rdf">
          <xsl:with-param name="sdoi" select="$sdoi"/>
        </xsl:call-template>
      </rdf:Description>
    </rdf:RDF>
  </xsl:template>

  <!-- generate the rdf statements for the secondary object -->
  <xsl:template name="sec-rdf" xmlns:dc="&dc;" xmlns:dc_terms="&dcterms;" xmlns:prism="&prism;"
                xmlns:topaz="&topaz;">
    <xsl:variable name="sdoi" select="my:fname-to-doi(@name)"/>

    <dc:identifier><xsl:value-of select="my:doi-to-uri($sdoi)"/></dc:identifier>
    <xsl:if test="$jnl-meta/issn[@pub-type = 'ppub']">
      <prism:issn><xsl:value-of select="$jnl-meta/issn[@pub-type = 'ppub']"/></prism:issn>
    </xsl:if>
    <xsl:if test="$jnl-meta/issn[@pub-type = 'epub']">
      <prism:eIssn><xsl:value-of select="$jnl-meta/issn[@pub-type = 'epub']"/></prism:eIssn>
    </xsl:if>
    <xsl:if test="$meta/pub-date">
      <dc:date rdf:datatype="&xsd;date"><xsl:value-of select="my:format-date(my:select-date($meta/pub-date))"/></dc:date>
    </xsl:if>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'author']">
      <dc:creator><xsl:value-of select="my:format-contrib-name(.)"/></dc:creator>
    </xsl:for-each>
    <xsl:for-each select="$meta/contrib-group/contrib[@contrib-type = 'contributor']">
      <dc:contributor><xsl:value-of select="my:format-contrib-name(.)"/></dc:contributor>
    </xsl:for-each>
    <xsl:if test="$meta/copyright-statement">
      <dc:rights rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$meta/copyright-statement"/></xsl:call-template></dc:rights>
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
        <dc:title rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$ctxt-obj/label"/></xsl:call-template></dc:title>
      </xsl:if>
      <xsl:if test="$ctxt-obj/caption">
        <dc:description rdf:datatype="&rdf;XMLLiteral"><xsl:call-template name="xml-to-str"><xsl:with-param name="xml" select="$ctxt-obj/caption"/></xsl:call-template></dc:description>
      </xsl:if>
    </xsl:if>

    <topaz:isPID><xsl:value-of select="my:doi-to-pid($sdoi)"/></topaz:isPID>
    <topaz:articleState rdf:datatype="&xsd;int"><xsl:value-of select="$initial-state"/></topaz:articleState>

    <xsl:apply-templates select="current-group()" mode="ds-rdf"/>
  </xsl:template>

  <!-- generate the propagate-permissions rdf statements for the article -->
  <xsl:template name="sec-pp-rdf" xmlns:topaz="&topaz;">
    <xsl:param name="sdoi" as="xs:string"/>
    <topaz:propagate-permissions-to rdf:resource="{my:doi-to-fedora-uri($sdoi)}"/>
  </xsl:template>

  <!-- generate the object's datastream definitions for the secondary object -->
  <xsl:template name="sec-ds">
    <xsl:apply-templates select="current-group()" mode="ds"/>
  </xsl:template>

  <!-- common templates for all datastream definitions -->
  <xsl:template match="ZipEntry" mode="ds-rdf" xmlns:topaz="&topaz;">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>
    <xsl:variable name="rep-name" select="my:name-to-ds-id(@name)"/>

    <topaz:hasRepresentation><xsl:value-of select="$rep-name"/></topaz:hasRepresentation>
    <xsl:element name="topaz:{$rep-name}-objectSize">
      <xsl:attribute name="rdf:datatype">&xsd;int</xsl:attribute>
      <xsl:value-of select="@size"/>
    </xsl:element>
    <xsl:element name="topaz:{$rep-name}-contentType">
      <xsl:value-of select="my:ext-to-mime($ext)"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="ZipEntry" mode="ds">
    <xsl:variable name="ext" select="my:get-ext(@name)"/>

    <Datastream contLoc="{encode-for-uri(@name)}" id="{my:name-to-ds-id(@name)}"
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
        concat($article-doi, replace(substring($froot, string-length(my:get-root(my:basename($article-entry/@name))) + 1), '-.*', ''))
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
          xmlns:dc="&dc;"
          xmlns:oai_dc="&oai_dc;">
        <xsl:sequence select="."/>
      </xsl:if>
    </xsl:for-each>
  </xsl:function>

  <!-- remove xsd:date datatype attributes for fedora unsupported datatypes -->
  <xsl:function name="my:filter-dt" as="element()*">
    <xsl:param name="rdf" as="element()*"/>

    <xsl:for-each select="$rdf">
      <xsl:choose>
        <xsl:when test="@rdf:datatype and
                        @rdf:datatype != '&xsd;int' and
                        @rdf:datatype != '&xsd;long' and
                        @rdf:datatype != '&xsd;float' and
                        @rdf:datatype != '&xsd;double'">
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
  <xsl:function name="my:format-contrib-name" as="xs:string">
    <xsl:param name="contrib" as="element(contrib)"/>

    <xsl:choose>
      <xsl:when test="$contrib/name">
        <xsl:value-of select="my:format-name($contrib/name)"/>
      </xsl:when>

      <xsl:when test="$contrib/collab">
        <xsl:value-of select="$contrib/collab"/>
      </xsl:when>

      <xsl:when test="$contrib/string-name">
        <xsl:value-of select="$contrib/string-name"/>
      </xsl:when>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="my:format-name" as="xs:string">
    <xsl:param name="name" as="element(name)"/>
    <xsl:value-of select="
      if ($name/given-names) then
        if ($name/@name-style = 'eastern') then
          concat($name/surname, ' ', $name/given-names)
        else
          concat($name/given-names, ' ', $name/surname)
      else
        $name/surname
      "/>
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

  <!-- Filename to datastream-id -->
  <xsl:function name="my:name-to-ds-id" as="xs:string">
    <xsl:param name="name" as="xs:string"/>
    <xsl:variable name="root" as="xs:string" select="my:get-root(my:basename($name))"/>
    <xsl:variable name="mod"  as="xs:string"
        select="if (contains($root, '-')) then replace($root, '.*-', '-') else ''"/>
    <xsl:value-of select="upper-case(concat(my:get-ext($name), $mod))"/>
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
      if      ($media-type = 'image') then xs:anyURI('&dctype;StillImage')
      else if ($media-type = 'video') then xs:anyURI('&dctype;MovingImage')
      else if ($media-type = 'audio') then xs:anyURI('&dctype;Sound')
      else if ($media-type = 'text')  then xs:anyURI('&dctype;Text')
      else if ($mime-type = 'application/vnd.ms-excel') then xs:anyURI('&dctype;Dataset')
      else ()
      "/>
  </xsl:function>

  <!-- NLM citation-type to (bibtex or PLoS) URI mapping -->
  <xsl:function name="my:map-cit-type" as="xs:anyURI">
    <xsl:param name="cit-type" as="xs:string"/>
    <xsl:variable name="uri" as="xs:string" select="
      if      ($cit-type = 'book')       then '&bibtex;Book'
      else if ($cit-type = 'commun')     then '&plosct;Informal'
      else if ($cit-type = 'confproc')   then '&bibtex;Conference'
      else if ($cit-type = 'discussion') then '&plosct;Discussion'
      else if ($cit-type = 'gov')        then '&plosct;Government'
      else if ($cit-type = 'journal')    then '&bibtex;Article'
      else if ($cit-type = 'list')       then '&plosct;List'
      else if ($cit-type = 'other')      then '&bibtex;Misc'
      else if ($cit-type = 'patent')     then '&plosct;Patent'
      else if ($cit-type = 'thesis')     then '&plosct;Thesis'
      else if ($cit-type = 'web')        then '&plosct;Web'
      else '&bibtex;Misc'
      "/>
    <xsl:sequence select="xs:anyURI($uri)"/>
  </xsl:function>

  <!-- Find the first integer with given minimal length in the string -->
  <xsl:function name="my:find-int" as="xs:integer?">
    <xsl:param name="str" as="xs:string?"/>
    <xsl:param name="min" as="xs:integer"/>
    <!-- this should simply be replace($str, '(.*?(\d{$min,}))?.*', '$2', 's') but that is not
       - allowed in xpath because the regexp can match the zero-length string. So we have to do
       - this in two steps. -->
    <xsl:variable name="tmp" as="xs:string"
        select="replace($str, concat('.*?(\d{', $min, ',})'), '$1', 's')"/>
    <xsl:variable name="num" as="xs:string"
        select="replace($tmp, '\D.*', '', 's')"/>
    <xsl:sequence select="if ($num) then xs:integer($num) else ()"/>
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

  <!-- create a user-profile -->
  <xsl:function name="my:create-user" as="element(user)" xmlns:foaf="&foaf;">
    <xsl:param name="u"  as="element(name)"/>
    <xsl:param name="id" as="xs:string?"/>
    <user cit-id="{$id}">
      <rdf:Description rdf:about="{concat('info:doi/10.1371/profile/', my:gen-uuid())}">
        <rdf:type rdf:resource="&foaf;Person"/>
        <foaf:name><xsl:value-of select="my:format-name($u)"/></foaf:name>
        <xsl:if test="$u/given-names">
          <foaf:givenname><xsl:value-of select="$u/given-names"/></foaf:givenname>
        </xsl:if>
        <xsl:if test="$u/surname">
          <foaf:surname><xsl:value-of select="$u/surname"/></foaf:surname>
        </xsl:if>
      </rdf:Description>
    </user>
  </xsl:function>

  <xsl:function name="my:gen-uuid" as="xs:string">
    <!-- no rng in xslt or xpath, so we have to cheat... -->
    <xsl:value-of xmlns:uuid="java:java.util.UUID" select="uuid:randomUUID()"/>
  </xsl:function>
</xsl:stylesheet>
