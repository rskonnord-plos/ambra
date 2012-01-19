<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8"
    doctype-system="http://java.sun.com/dtd/web-app_2_3.dtd"  
    doctype-public="-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="auth-method">
    <auth-method>CAS-BASIC</auth-method>
  </xsl:template>

</xsl:stylesheet>


