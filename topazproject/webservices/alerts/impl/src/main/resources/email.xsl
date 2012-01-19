<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" encoding="UTF-8" media-type="text/plain" />
  <xsl:template match="/">
<xsl:text>PLEASE DO NOT REPLY DIRECTLY TO THIS E-MAIL
For assistance with this alert, email webmaster@plos.org

New articles in PLoS-One
</xsl:text>
    
    <xsl:for-each select="articles/article">
      <xsl:text>
Published </xsl:text>
      
      <xsl:value-of select="date"/>
      <xsl:text>:
</xsl:text>
      
      <xsl:value-of select="title"/>
<xsl:text> --
</xsl:text>

      <xsl:value-of select="description"/>
      <xsl:if test="authors/author">
        <xsl:text>
    by </xsl:text>
        
        <xsl:for-each select="authors/author">
          <xsl:value-of select="text()"/>
        </xsl:for-each>
      </xsl:if>
      <xsl:text>
----
</xsl:text>

    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
