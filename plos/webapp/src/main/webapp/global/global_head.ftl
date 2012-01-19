<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>
<#assign pgTitleOrig = freemarker_config.getTitle(templateFile, journalContext)>
<#assign pgTitle = pgTitleOrig>
<#if pgTitleOrig = "CODE_ARTICLE_TITLE"> <#--to get article title in w/o a new template for now-->
  <#assign pgTitle = freemarker_config.getArticleTitlePrefix(journalContext) + " " + articleInfoX.unformattedTitle>
</#if>
  <title>${pgTitle}</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="shortcut icon" href="${freemarker_config.context}/images/pone_favicon.ico" type="image/x-icon" />
<@s.url id="homeURL" includeParams="none" includeContext="true" namespace="/" action="home"/>
<link rel="home" title="home" href="${homeURL}" />
<link rel="alternate" type="application/rss+xml"
  title="${freemarker_config.getArticleTitlePrefix(journalContext)} ${rssName?html}"
  href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}" />

<#include "../css/global_css.ftl">
<#include "../javascript/global_js.ftl">

<meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}" />

<meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}" />

<@s.url id="pgURL" includeParams="get" includeContext="true" encode="false"/>
<#assign rdfPgURL = pgURL?replace("&amp;", "&")>

<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="${Request[freemarker_config.journalContextAttributeKey].baseHostUrl}${rdfPgURL}">
   <license rdf:resource="http://creativecommons.org/licenses/by/2.5/" />
</Work>
<License rdf:about="http://creativecommons.org/licenses/by/2.5/">
   <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
   <permits rdf:resource="http://web.resource.org/cc/Distribution" />
   <requires rdf:resource="http://web.resource.org/cc/Notice" />
   <requires rdf:resource="http://web.resource.org/cc/Attribution" />
   <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
</License>
<rdf:Description
     rdf:about="${Request[freemarker_config.journalContextAttributeKey].baseHostUrl}${rdfPgURL}"
     dc:identifier="${Request[freemarker_config.journalContextAttributeKey].baseHostUrl}${rdfPgURL}"
     dc:title="${pgTitle}"
     <#if pgTitleOrig = "CODE_ARTICLE_TITLE">
       <@s.url id="trackbackURL" namespace="/" action="trackback" includeParams="none" trackbackId="${articleURI}"/>
       trackback:ping="${Request[freemarker_config.journalContextAttributeKey].baseHostUrl}${trackbackURL}"
     </#if>/>
</rdf:RDF>

-->
