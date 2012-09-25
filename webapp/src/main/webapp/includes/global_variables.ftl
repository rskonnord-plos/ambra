<#assign pgTitleOrig = freemarker_config.getTitle(templateFile, journalContext)>
<#assign pgTitle = pgTitleOrig>

<#if pgTitleOrig = "CODE_ARTICLE_TITLE" && articleInfoX??> <#--to get article title in w/o a new template for now-->
  <#assign pgTitle = freemarker_config.getArticleTitlePrefix(journalContext) + " " + articleInfoX.unformattedTitle>
</#if>

<@s.url id="pgURL" includeParams="get" includeContext="true" encode="false"/>
<@s.url id="homeURL" includeParams="none" includeContext="true" namespace="/" action="home"/>

<#if pgURL?contains('fetchArticle.action') && articleInfoX??>
  <#--
    Do not mess with the whitespace in the following tag!
    It is specified by http://www.hixie.ch/specs/pingback/pingback-1.0#TOC2.2
    There should be exactly one space before the closing slash. If an auto-formatter ate it, please put it back.
    TODO: Move to article pages!
    -->
  <link rel="pingback" href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}/pingback" />
</#if>

<#assign rdfPgURL = pgURL?replace("&amp;", "&")>

<#--
  When the current page is displaying article data, add this extra info for Google Scholar
  TODO: Move to article pages!
-->
<#if articleInfoX??>
  <meta name="citation_publisher" content="${articleInfoX.publisher}" />
  <meta name="citation_doi" content="${articleInfoX.doi?replace('info:doi/','')}" />
  <#if articleInfoX.unformattedTitle??>
    <meta name="citation_title" content="${articleInfoX.unformattedTitle}"/>
    <meta itemprop="name" content="${articleInfoX.unformattedTitle}"/>
  </#if>

  <#if authorExtras?? >
    <#list authorExtras as author>
      <meta name="citation_author" content="${author.authorName}" />
      <#if author.affiliations?? >
        <#list author.affiliations as affiliation>
          <#if affiliation?? >
            <meta name="citation_author_institution" content="${affiliation?trim}" />
          </#if>
        </#list>
      </#if>
    </#list>
  </#if>

  <#if articleInfoX.date??>
  <meta name="citation_date" content="${articleInfoX.date?date?string("yyyy/M/d")}"/>
  </#if>

  <#assign pdfURL = "${freemarker_config.doiResolverURL}${articleInfoX.doi?replace('info:doi/','')}" + ".pdf" />
  <meta name="citation_pdf_url" content="${pdfURL}" />

  <#if articleInfoX??>
    <#if publishedJournal??>
      <meta name="citation_journal_title" content="${publishedJournal}" />
    </#if>
    <meta name="citation_firstpage" content="${articleInfoX.eLocationId!}"/>
    <meta name="citation_issue" content="${articleInfoX.issue}"/>
    <meta name="citation_volume" content="${articleInfoX.volume}"/>
    <meta name="citation_issn" content="${articleInfoX.eIssn}"/>
  </#if>

  <#if journalAbbrev??>
    <meta name="citation_journal_abbrev" content="${journalAbbrev}" />
  </#if>

  <#if references??>
    <#list references as reference>
    <meta name="citation_reference" content="${reference.referenceContent}" />
    </#list>
  </#if>

</#if><!--end articleInfoX-->