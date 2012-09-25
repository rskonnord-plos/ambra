<#list freemarker_config.getJavaScript(templateFile, journalContext) as jsFile>
  <#if freemarker_config.debug>
    <#assign jsFileName = "${jsFile}" />
  <#else>
    <#assign jsFileName = "${jsFile?replace('.js','-min.js')}" />
  </#if>
  <@versionedJS file="${jsFileName}" />
</#list>