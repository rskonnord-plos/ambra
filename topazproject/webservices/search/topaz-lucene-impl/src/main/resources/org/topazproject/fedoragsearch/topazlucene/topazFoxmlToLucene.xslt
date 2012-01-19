<?xml version="1.0" encoding="UTF-8"?> 
<!--
  $HeadURL::                                                                                      $
  $Id$

  Copyright (c) 2006 by Topaz, Inc.
  http://topazproject.org

  Licensed under the Educational Community License version 1.0
  http://opensource.org/licenses/ecl1.php
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"   
        xmlns:exts="xalan://dk.defxws.fedoragsearch.server.XsltExtensions"
                exclude-result-prefixes="exts"
                xmlns:zs="http://www.loc.gov/zing/srw/"
                xmlns:foxml="info:fedora/fedora-system:def/foxml#"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                xmlns:uvalibdesc="http://dl.lib.virginia.edu/bin/dtd/descmeta/descmeta.dtd"
                xmlns:uvalibadmin="http://dl.lib.virginia.edu/bin/admin/admin.dtd/">
  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <!--
  <xsl:include href="@fedoragsearchPath@/WEB-INF/classes/config/index/DemoOnLucene/demoUvalibdescToLucene.xslt"/>
  -->

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
       - from oai_dc:dc        = title, creator, ...
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
    
    <xsl:for-each select="foxml:datastream/foxml:datastreamVersion/foxml:xmlContent/oai_dc:dc/*">
      <IndexField index="TOKENIZED" store="YES" termVector="YES">
        <xsl:attribute name="IFname">
          <xsl:value-of select="local-name()"/>
        </xsl:attribute>
        <xsl:value-of select="replace(text(), '&lt;.*?&gt;', ' ')"/>
      </IndexField>
    </xsl:for-each>
      
    <!-- When dsMimetypes is present, then the datastream is fetched 
         whose mimetype is found first in the list of mimeTypes.
         If mimeTypes is empty, then it is taken from properties.
         Use *either* this or the inline XML stuff below. -->
    <!--
    <IndexField IFname="DS.first.text" dsMimetypes="" index="TOKENIZED" store="YES" termVector="NO">
    </IndexField>
    -->

    <!-- For XML that is not inline, the datastream may be fetched with the document() function -->
    <xsl:call-template name="topaz-xml"/>
  </xsl:template>
  
  <!-- Template to index our XML content -->
  <xsl:template name="topaz-xml">
    <IndexField IFname="body" index="TOKENIZED" store="YES" termVector="NO">
      <xsl:apply-templates mode="value-of"
          select="document(concat($fedoraBaseURL, 'get/', $PID, '/', $articleDS))/article/body"/>
    </IndexField>
  </xsl:template>

  <xsl:template match="*" mode="value-of">
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="*|text()" mode="value-of"/>
  </xsl:template>

  <xsl:template match="text()" mode="value-of">
    <xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet> 
