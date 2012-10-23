<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>

<#--include the header-->
<#include "/includes/generic_header.ftl">

<div id="pagebdy-wrap">
  <div id="pagebdy">
    <!--include the template-->
    <#include "${templateFile}">
  </div>
</div>
<!--include the footer-->
<#include "/global/footer.ftl">
