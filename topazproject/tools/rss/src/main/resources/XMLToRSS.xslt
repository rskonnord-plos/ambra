<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="rssTitle">PLoS ONE Alerts</xsl:param>
  <xsl:param name="rssLink">http://www.plosone.org</xsl:param>
  <xsl:param name="rssImage">http://www.plosone.org/images/pone_favicon.ico</xsl:param>
  <xsl:param name="rssDescription">PLoS ONE Journal</xsl:param>
  <xsl:param name="linkPrefix">http://www.plosone.org/article/fetchArticle.action?articleURI=</xsl:param>

  <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

  <xsl:template match="/">
    <rss version="2.0">
      <channel>
        <title><xsl:value-of select="$rssTitle"/></title>
        <link><xsl:value-of select="$rssLink"/></link>
        <image><xsl:value-of select="$rssImage"/></image>
        <description><xsl:value-of select="$rssDescription"/></description>
        <xsl:apply-templates select="articles/article"/>
      </channel>
    </rss>
  </xsl:template>

  <xsl:template match="article">
    <item>
      <title><xsl:value-of select="title"/></title>
      <pubDate>
        <xsl:value-of select="format-date(date, '[FNn,*-3], [D01] [MNn,*-3] [Y0001] 00:00:00 GMT')"/>
      </pubDate>
      <link><xsl:value-of select="$linkPrefix"/><xsl:value-of select="uri"/></link>
      <description><xsl:value-of select="description"/></description>
      <xsl:apply-templates select="authors/author"/>
      <guid><xsl:value-of select="uri"/></guid>
    </item>
  </xsl:template>

  <xsl:template match="author">
    <author><xsl:value-of select="."/></author>
  </xsl:template>

</xsl:stylesheet>
