<#assign pgTitleOrig = freemarker_config.getTitle(templateFile, journalContext)>
<#assign pgTitle = pgTitleOrig>

<#if pgTitleOrig = "CODE_ARTICLE_TITLE" && articleInfoX??> <#--to get article title in w/o a new template for now-->
  <#assign pgTitle = freemarker_config.getArticleTitlePrefix(journalContext) + " " + articleInfoX.unformattedTitle>
</#if>

<@s.url id="pgURL" includeParams="get" includeContext="true" encode="false"/>
<@s.url id="homeURL" includeParams="none" includeContext="true" namespace="/" action="home"/>

<#assign rdfPgURL = pgURL?replace("&amp;", "&")>
