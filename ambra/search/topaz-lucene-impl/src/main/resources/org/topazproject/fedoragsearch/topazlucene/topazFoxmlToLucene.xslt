<?xml version="1.0" encoding="UTF-8"?> 
<!--
  $HeadURL::                                                                                      $
  $Id$

  Copyright (c) 2006-2008 by Topaz, Inc.
  http://topazproject.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
  xmlns:my="my:lucene.ingest#"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:exts="xalan://dk.defxws.fedoragsearch.server.XsltExtensions"
  exclude-result-prefixes="my exts"
  xmlns:zs="http://www.loc.gov/zing/srw/"
  xmlns:foxml="info:fedora/fedora-system:def/foxml#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
  xmlns:uvalibdesc="http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd"
  xmlns:uvalibadmin="http://dl.lib.virginia.edu/bin/admin/admin.dtd/">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:param name="fedoraBaseURL"/>
  <xsl:param name="articleDS"/>

  <xsl:template name="uvalibdesc">
    <xsl:for-each select="foxml:datastream/foxml:datastreamVersion/foxml:xmlContent/uvalibdesc:desc/*">
      <xsl:if test="name()='uvalibdesc:agent'">
        <IndexField IFname="uva.agent" index="TOKENIZED" store="YES" termVector="NO">
          <xsl:value-of select="text()"/>
        </IndexField>
      </xsl:if>
      <xsl:if test="name()='uvalibdesc:culture'">
        <IndexField IFname="uva.culture" index="TOKENIZED" store="YES" termVector="NO">
          <xsl:value-of select="text()"/>
        </IndexField>
      </xsl:if>
      <xsl:if test="name()='uvalibdesc:place'">
        <IndexField IFname="uva.place" index="TOKENIZED" store="YES" termVector="NO">
          <xsl:value-of select="./*/text()"/>
        </IndexField>
      </xsl:if>
    </xsl:for-each>

  </xsl:template>

  <!--
  This xslt stylesheet generates the IndexDocument consisting of IndexFields
  from a FOXML record. The IndexFields are:
  - from the root element = PID
  - from foxml:property   = type, state, contentModel, ...
  - from article:xml      = title, creator, ...

  Options for tailoring:
  - IndexField types, see Lucene javadoc for Field.Store, Field.Index, Field.TermVector
  - IndexField boosts, see Lucene documentation for explanation
  - IndexDocument boosts, see Lucene documentation for explanation
  - generation of IndexFields from other XML metadata streams than DC
  - e.g. as for uvalibdesc included above and called below, the XML is inline
  - for not inline XML, the datastream may be fetched with the document() function,
  see the example below (however, none of the demo objects can test this)
  - generation of IndexFields from other datastream types than XML
  - from datastream by ID, text fetched, if mimetype can be handled
  - from datastream by sequence of mimetypes, 
  text fetched from the first mimetype that can be handled,
  default sequence given in properties
  - currently only the mimetype application/pdf can be handled.
  -->

  <xsl:variable name="PID" select="/foxml:digitalObject/@PID"/>
  <xsl:variable name="docBoost" select="1.4*2.5"/> <!-- or any other calculation, default boost is 1.0 -->

  <xsl:template match="/">
    <IndexDocument> 
      <xsl:attribute name="boost">
        <xsl:value-of select="$docBoost"/>
      </xsl:attribute>

      <xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='info:fedora/fedora-system:def/model#state' and @VALUE='Active']">
        <xsl:if test="foxml:digitalObject/foxml:objectProperties/foxml:property[@NAME='http://www.w3.org/1999/02/22-rdf-syntax-ns#type' and @VALUE='FedoraObject']">
          <xsl:apply-templates mode="activeFedoraObject"/>
        </xsl:if>
      </xsl:if>
    </IndexDocument>
  </xsl:template>

  <!-- Index the actual content -->
  <xsl:template match="/foxml:digitalObject" mode="activeFedoraObject">

    <!-- The PID field must exist and be UN_TOKENIZED -->
    <IndexField IFname="PID" index="UN_TOKENIZED" store="YES" termVector="NO" boost="2.5">
      <xsl:value-of select="$PID"/>
    </IndexField>

    <xsl:for-each select="foxml:objectProperties/foxml:property">
      <IndexField index="UN_TOKENIZED" store="YES" termVector="NO">
        <xsl:attribute name="IFname"> 
          <xsl:value-of select="concat('property.', substring-after(@NAME,'#'))"/>
        </xsl:attribute>
        <xsl:value-of select="@VALUE"/>
      </IndexField>
    </xsl:for-each>

    <!-- For XML that is not inline, the datastream may be fetched with the document() function -->
    <xsl:call-template name="topaz-xml"/>
  </xsl:template>

  <!-- Template to index our XML content -->
  <xsl:template name="topaz-xml">
    <xsl:apply-templates mode="topaz-xml"
      select="document(concat($fedoraBaseURL, 'get/', $PID, '/', $articleDS))"/>
  </xsl:template>

  <!-- topaz-xml templates to index Article -->
  <xsl:template match="article/front/journal-meta" mode="topaz-xml">
    <IndexField IFname="journal-title" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="journal-title" mode="value-of"/>
    </IndexField>
  </xsl:template>

  <xsl:template match="article/front/article-meta" mode="topaz-xml">
    <IndexField IFname= "identifier" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:value-of select="concat('info:doi/', article-id[@pub-id-type='doi'])"/>
    </IndexField>
    <xsl:for-each select="article-categories/subj-group[@subj-group-type = 'Discipline']/subject">
      <IndexField IFname= "subject" index="TOKENIZED" store="YES" termVector="NO">
        <xsl:call-template name="xml-to-str">
          <xsl:with-param name="xml" select="."/>
        </xsl:call-template>
      </IndexField>
    </xsl:for-each>
    <xsl:if test="title-group/article-title">
      <IndexField IFname= "title" index="TOKENIZED" store="YES" termVector="NO">
        <xsl:call-template name="xml-to-str">
          <xsl:with-param name="xml" select="title-group/article-title"/>
        </xsl:call-template>
      </IndexField>
    </xsl:if>
    <xsl:if test="abstract">
      <IndexField IFname= "abstract" index="TOKENIZED" store="YES" termVector="NO">
        <xsl:call-template name="xml-to-str">
          <xsl:with-param name="xml" select="my:select-abstract(abstract)"/>
        </xsl:call-template>
      </IndexField>
    </xsl:if>
    <xsl:if test="pub-date">
      <IndexField IFname= "date" index="TOKENIZED" store="YES" termVector="NO">
        <xsl:value-of select="my:format-date(my:select-date(pub-date))"/>
      </IndexField>
    </xsl:if>
    <xsl:for-each select="contrib-group/contrib[@contrib-type = 'author']/name">
      <IndexField IFname= "creator" index="TOKENIZED" store="YES" termVector="NO">
        <xsl:value-of select="concat(given-names,' ',surname)"/>
      </IndexField>
    </xsl:for-each>
    <IndexField IFname="aff" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="aff" mode="value-of"/>
    </IndexField>
    <IndexField IFname="editor" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="contrib-group/contrib[@contrib-type='editor']" mode="value-of"/>
    </IndexField>
    <IndexField IFname="volume" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="volume" mode="value-of"/>
    </IndexField>
    <IndexField IFname="issue" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="issue" mode="value-of"/>
    </IndexField>
    <IndexField IFname="elocation-id" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="elocation-id" mode="value-of"/>
    </IndexField>
  </xsl:template>

  <xsl:template match="article/body" mode="topaz-xml">
    <IndexField IFname="body" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates mode="value-of"/>
    </IndexField>
  </xsl:template>

  <xsl:template match="article/back" mode="topaz-xml">
    <IndexField IFname="citation" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates select="ref-list" mode="value-of"/>
    </IndexField>
  </xsl:template>

  <!-- consume anything that's not explicitly indexed -->
  <xsl:template match="*" mode="topaz-xml">
    <xsl:apply-templates mode="topaz-xml"/>
  </xsl:template>
  <xsl:template match="text()" mode="topaz-xml"/>

  <!-- value-of templates to output value of contents -->
  <xsl:template match="*" mode="value-of">
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*|text()" mode="value-of"/>
  </xsl:template>

  <xsl:template match="text()" mode="value-of">
    <xsl:value-of select="."/>
  </xsl:template>

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

  <xsl:function name="my:xml-escape" as="xs:string">
    <xsl:param name="str" as="xs:string"/>
    <xsl:value-of select="replace(replace(replace($str, '&amp;', '&amp;amp;'),
      '&lt;', '&amp;lt;'), '&gt;', '&amp;gt;')"/>
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
      else if ($abstracts[not(@abstract-type)])
      then $abstracts[not(@abstract-type)]
      else $abstracts[1]
      "/>
  </xsl:function>

</xsl:stylesheet> 
