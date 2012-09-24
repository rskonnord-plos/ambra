<#list freemarker_config.getJavaScript(templateFile, journalContext) as jsFile>
  <#if jsFile?contains("ambra.js")>
    <#if freemarker_config.debug>
      <@versionedJS file="${freemarker_config.context}/javascript/jquery-1.8.1.js" />
      <@versionedJS file="${freemarker_config.context}/javascript/global.js" />
    <#else>
      <@versionedJS file="${freemarker_config.context}/javascript/ambra.js" />
    </#if>
  <#else>
    <@versionedJS file="${jsFile}" />
  </#if>
</#list>
