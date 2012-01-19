<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:foxml="info:fedora/fedora-system:def/foxml#"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:my="my:ingest.obj2foxml#"
    exclude-result-prefixes="my">

  <!--
    - This converts an Object and its DC and RELS-EXT children (see fedora.dtd) to a foxml
    - document suitable for use in Fedora's ingest() method.
    -->

  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="datastream-loc" as="xs:string*"/>

  <xsl:template match="Object">
    <foxml:digitalObject
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:foxml="info:fedora/fedora-system:def/foxml#"
      xsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-0.xsd"
      PID="{@pid}">

      <foxml:objectProperties>
        <foxml:property NAME="http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
            VALUE="FedoraObject"/>
        <xsl:if test="@label">
          <foxml:property NAME="info:fedora/fedora-system:def/model#label" VALUE="{@label}"/>
        </xsl:if>
        <xsl:if test="@cModel">
          <foxml:property NAME="info:fedora/fedora-system:def/model#contentModel"
              VALUE="{@cModel}"/>
        </xsl:if>
        <foxml:property NAME="info:fedora/fedora-system:def/model#state"
            VALUE="{my:get-state(@state)}"/>
      </foxml:objectProperties>

      <xsl:apply-templates select="DC"/>
      <xsl:apply-templates select="RELS-EXT"/>

      <xsl:apply-templates select="Datastream"/>
    </foxml:digitalObject>
  </xsl:template>

  <xsl:template match="DC">
    <foxml:datastream ID="DC" STATE="A" CONTROL_GROUP="X" VERSIONABLE="true">
      <foxml:datastreamVersion ID="DC.0" MIMETYPE="text/xml" LABEL="Default Dublin Core Record">
        <foxml:xmlContent>
          <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/">
            <xsl:copy-of select="node()"/>
          </oai_dc:dc>
        </foxml:xmlContent>
      </foxml:datastreamVersion>
    </foxml:datastream>
  </xsl:template>

  <xsl:template match="RELS-EXT">
    <foxml:datastream ID="RELS-EXT" STATE="A" CONTROL_GROUP="X" VERSIONABLE="true">
      <foxml:datastreamVersion ID="RELS-EXT.0" MIMETYPE="text/xml"
          LABEL="Fedora Object-to-Object Relationship Metadata">
        <foxml:xmlContent>
          <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
            <rdf:Description rdf:about="info:fedora/{../@pid}">
              <xsl:copy-of select="node()"/>
            </rdf:Description>
          </rdf:RDF>
        </foxml:xmlContent>
      </foxml:datastreamVersion>
    </foxml:datastream>
  </xsl:template>

  <xsl:template match="Datastream">
    <foxml:datastream ID="{@id}" STATE="{my:get-state(@state)}" VERSIONABLE="true"
        CONTROL_GROUP="{my:get-ctrl-grp(@controlGroup)}">
      <foxml:datastreamVersion ID="{@id}.0" MIMETYPE="{@mimeType}">
        <xsl:if test="@label">
          <xsl:attribute name="LABEL" select="@label"/>
        </xsl:if>
        <xsl:if test="@formatUri">
          <xsl:attribute name="FORMAT_URI" select="@formatUri"/>
        </xsl:if>
        <xsl:if test="@altIds">
          <xsl:attribute name="ALT_IDS" select="@altIds"/>
        </xsl:if>
        <foxml:contentLocation TYPE="URL"
            REF="{$datastream-loc[count(current()/preceding-sibling::Datastream) + 1]}"/>
      </foxml:datastreamVersion>
    </foxml:datastream>
  </xsl:template>

  <xsl:function name="my:get-state" as="xs:string">
    <xsl:param name="state" as="xs:string?"/>

    <xsl:variable name="s" as="xs:string" select="
      if (not($state) or $state = 'Active') then 'A'
      else if ($state = 'Inactive') then 'I'
      else if ($state = 'Deleted') then 'D'
      else ''
      "/>
    <xsl:if test="$s">
      <xsl:value-of select="$s"/>
    </xsl:if>
    <xsl:if test="not($s)">
      <xsl:message>Warning: unrecognized state '<xsl:value-of select="$state"/>'</xsl:message>
      <xsl:value-of select="'A'"/>
    </xsl:if>
  </xsl:function>

  <xsl:function name="my:get-ctrl-grp" as="xs:string">
    <xsl:param name="grp" as="xs:string"/>

    <xsl:value-of select="
      if ($grp = 'External') then 'E'
      else if ($grp = 'Redirected') then 'R'
      else if ($grp = 'Managed') then 'M'
      else if ($grp = 'XML') then 'X'
      else error((), concat('Invalid Control-Group ''', $grp, ''''))
      "/>
  </xsl:function>
</xsl:stylesheet>
