<@s.url id="facetMoreURL" includeParams="none" namespace="/search" action="listFacet" />

<#if searchType == "simple">
  <@s.url id="searchURL" includeParams="none" namespace="/search" action="simpleSearch" />
<#elseif searchType == "unformatted">
  <@s.url id="searchURL" includeParams="none" namespace="/search" action="advancedSearch" />
<#elseif searchType == "findAnArticle">
  <@s.url id="searchURL" includeParams="none" namespace="/search" action="findAnArticleSearch" />
<#else>
<#--  TODO: Set this default to something reasonable and set "noSearchFlag = true" and probably give a good error message.
-->
  <@s.url id="searchURL" includeParams="none" namespace="/search" action="simpleSearch" />
</#if>

<@s.url id="advancedSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />
<@s.url id="rssSearchURL" includeParams="none" namespace="/article/feed" action="executeFeedSearch" />