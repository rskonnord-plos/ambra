<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="linkPrefix">http://www.plosone.org/article/fetchArticle.action?articleURI=</xsl:param>

  <xsl:output method="xhtml" doctype-system="http://www.w3.org/TR/xhtml1/DTD/strict.dtd" 
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>PLoS ONE List of Articles</title>
      </head>
      <body>
        <xsl:apply-templates select="articles/article"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="article">
    <a>
      <xsl:attribute name="href"><xsl:value-of select="$linkPrefix"/><xsl:value-of select="uri"/></xsl:attribute> 
      <xsl:value-of select="uri"/>
    </a>
    <p/>
  </xsl:template>

</xsl:stylesheet>
