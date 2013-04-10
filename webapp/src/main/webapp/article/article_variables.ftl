<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>

<#if articleInfoX??>
  <#assign shortDOI = "${articleInfoX.doi?replace('info:doi/','')}" />
  <#assign docURL = freemarker_config.doiResolverURL + shortDOI />
  <#assign jDocURL = freemarker_config.getJournalUrl(journalContext) + "/article/" + articleInfoX.doi?url />
  <#assign docTitle><@compress single_line=true>${articleInfoX.title}</@compress></#assign>
  <#assign date = articleInfoX.date?string("yyyy-MM-dd") />
  <#assign articlePublisher = articleInfoX.publisher!"PLOS" />

<#-- This is article description content without any markup -->
  <#if articleDescription?? && (articleDescription?length > 0) >
    <#assign description = articleDescription />
  </#if>
<#else>
  <#assign shortDOI = "" />
  <#assign docURL = "" />
  <#assign jDocURL = "" />
  <#assign docTitle = "" />
  <#assign date = "" />
  <#assign articlePublisher = "" />
</#if>

<#assign plainDocTitle><@articleFormat><@simpleText>${docTitle}</@simpleText></@articleFormat></#assign>
<#-- Remove ALL remaining HTML codes -->
<#assign plainDocTitle = plainDocTitle?replace('<.+?>','','r') />
<#assign noHTMLDocTitle = docTitle?replace('<.+?>','','r') />

<#--EZ Reprint Data -->

<#assign ezReprintJournalID = "0" />


<#if articleIssues?? && articleIssues?size gt 0>
  <#list articleIssues as oneIssue>
    <#if (freemarker_config.getDisplayName(oneIssue[1])?lower_case?index_of("collections") gt -1)>
      <#if !collections??>
        <#assign collections = "">
      </#if>

      <#assign collections = collections + "<dd><a href=\"" + freemarker_config.getJournalUrl(oneIssue[1]) +
      freemarker_config.context + "/article/browse/issue/" + oneIssue[4]?url +
      "\" title=\"Browse the Collection\">" + oneIssue[5] + "</a></dd>" />

    <#else>
      <#if !issues??>
        <#assign issues = "">
      </#if>
      <#assign issues = issues + "<a href=\"" + freemarker_config.getJournalUrl(oneIssue[1]) + freemarker_config
      .context + "/article/browse/issue/" + oneIssue[4]?url +
      "\" title=\"Browse the Issue\">" + oneIssue[5] + " " + oneIssue[3] + " Issue of <em>" +
      freemarker_config.getDisplayName(oneIssue[1]) + "</em></a>" />
    </#if>
  </#list>
</#if>

<#assign publisher = "">

<#list journalList as jour>
<#-- Special Case -->
  <#if (journalList?size == 1) && (jour.journalKey == journalContext)>
  <#-- Normal Case -->
  <#elseif jour.journalKey != journalContext>
  <#-- Article is originally published elsewhere -->
    <#if articleInfoX.eIssn = jour.eIssn>
      <#assign publisher = "Published in <em><a href=\"" + freemarker_config.getJournalUrl(jour.journalKey)
      + "\">"+ jour.title + "</a></em>" />
      <#break/>
    <#-- Article is additionally published elsewhere -->
    <#else>
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.journalKey) + "\">"/>
      <#assign title = jour.title>
      <#if publisher?length gt 0>
        <#assign publisher = publisher + ", " + jourAnchor + title + "</a>" />
      <#else>
        <#assign publisher = "Featured in " + jourAnchor + title + "</a>" />
      </#if>
    </#if>
  </#if>
</#list>

<@s.url id="articleCitationURL" namespace="/article" action="citationList" includeParams="none" articleURI="${articleURI}"/>
<@s.url id="articleXMLURL" namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
  <@s.param name="representation" value="%{'XML'}"/>
</@s.url>
<@s.url id="articlePDFURL" namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
  <@s.param name="representation" value="%{'PDF'}"/>
</@s.url>