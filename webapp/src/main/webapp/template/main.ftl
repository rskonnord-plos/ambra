<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>

<!--include the headers-->
<#include "/global/global_header.ftl">
<!--include the template-->
<#include "${templateFile}">
<!--include the footer-->
<#include "/global/global_footer.ftl">