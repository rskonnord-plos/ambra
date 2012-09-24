<#list freemarker_config.getCss(templateFile, journalContext) as cssFile>
  <#--
    If we are not in debug mode, or the filename points to an external reference
    Do not try to use the minified version
  -->
  <#if !freemarker_config.debug || cssFile?contains("http://")>
    <#assign cssFile = "${cssFile}" />
  <#else>
    <#assign cssFile = "${cssFile?replace('.css','-min.css')}" />
  </#if>

  <#if cssFile?contains("_ie7")>
    <!--[if lte IE 7]>
    <@versionedCSS file="${cssFile}" />
    <![endif]-->
  <#else>
    <@versionedCSS file="${cssFile}" />
  </#if>
</#list>
