<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>

<!--include the headers-->
<#include "/includes/header.ftl">
<!--include the template-->

<#--
  This template is called only for static content.  If something fails, assume it's a 404
  and handle it gracefully

  TODO: There should be away to check for the files existence in the virturalJournalContext.
  Handling the error here results in a 400 response code instead of a 404
-->
<#attempt>
  <#include "${templateFile}">
  <#recover>
    <#include "/static/pageNotFound.ftl">
</#attempt>
<!--include the footer-->
<#include "/includes/footer.ftl">