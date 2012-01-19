<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no"/>
  <xsl:strip-space elements="abstract p sec title" />

  <xsl:template match="/">
    <org.plos.article.service.CitationInfo>
      <DOI><xsl:value-of select="//article-id[@pub-id-type='doi'][1]"/></DOI>
      <publicationDate>
        <xsl:choose>
          <xsl:when test="article/front/article-meta/pub-date[@pub-type='ppub']/year">
            <xsl:call-template name="makeDate">
              <xsl:with-param name="dateSelector">ppub</xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="makeDate">
              <xsl:with-param name="dateSelector">epub</xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </publicationDate>
      <articleTitle><xsl:value-of select="article/front/article-meta/title-group/article-title"/></articleTitle>
      <journalName><xsl:value-of select="article/front/journal-meta/journal-id[@journal-id-type='nlm-ta']"/></journalName>
      <journalTitle><xsl:value-of select="article/front/journal-meta/journal-title[1]"/></journalTitle>
      <publisherName><xsl:value-of select="article/front/journal-meta/publisher/publisher-name"/></publisherName>
      <startPage><xsl:value-of select="article/front/article-meta/elocation-id"/></startPage>
      <volume><xsl:value-of select="article/front/article-meta/volume"/></volume>
      <issue><xsl:value-of select="article/front/article-meta/issue"/></issue>
      <xsl:apply-templates select="article/front/article-meta/abstract[not(@abstract-type)]" />
      <authors>
        <xsl:for-each select="article/front/article-meta/contrib-group/contrib[@contrib-type='author']">
          <org.plos.article.service.Author>
            <xsl:choose>
              <xsl:when test="position() = 1 or @equal-contrib='yes'">
                <isPrimary>true</isPrimary>
              </xsl:when>
              <xsl:otherwise>
                <isPrimary>false</isPrimary>
              </xsl:otherwise>
            </xsl:choose>
            <givenNames><xsl:value-of select="name/given-names"/></givenNames>
            <surname><xsl:value-of select="name/surname"/></surname>
            <xsl:if test="name/suffix">
              <suffix><xsl:value-of select="name/suffix"/></suffix>
            </xsl:if>
          </org.plos.article.service.Author>
        </xsl:for-each>
      </authors>
      <collaborativeAuthors>
        <xsl:for-each select="//article/front/article-meta/contrib-group/contrib[@contrib-type='author']/collab[@collab-type='authors']">
          <org.plos.article.service.CollaborativeAuthor>
            <nameRef><xsl:value-of select="."/></nameRef>
          </org.plos.article.service.CollaborativeAuthor>
        </xsl:for-each>
      </collaborativeAuthors>
    </org.plos.article.service.CitationInfo>
  </xsl:template>

  <xsl:template match="abstract">
    <articleAbstract>
      <xsl:apply-templates />
    </articleAbstract>
  </xsl:template>

  <xsl:template match="p">
    <xsl:apply-templates/><xsl:text> </xsl:text>
  </xsl:template>
  
  <xsl:template match="sec">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="title">
    <xsl:apply-templates /><xsl:text>: </xsl:text>
  </xsl:template>
  
  <xsl:template name="makeDate">
    <xsl:param name="dateSelector" />
    <xsl:value-of select="article/front/article-meta/pub-date[@pub-type=$dateSelector]/year"/>
    <xsl:choose>
      <xsl:when test="article/front/article-meta/pub-date[@pub-type='epub']/month">
        <xsl:text>-</xsl:text><xsl:value-of select="article/front/article-meta/pub-date[@pub-type=$dateSelector]/month"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>-01</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="article/front/article-meta/pub-date[@pub-type='epub']/day">
        <xsl:text>-</xsl:text><xsl:value-of select="article/front/article-meta/pub-date[@pub-type=$dateSelector]/day"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>-01</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
