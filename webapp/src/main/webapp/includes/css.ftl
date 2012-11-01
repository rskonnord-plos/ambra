<#list freemarker_config.getCss(templateFile, journalContext) as cssFile>
  <#--
    If we are not in debug mode, or the filename points to an external reference
    Do not try to use the minified version
  -->
  <#if freemarker_config.debug>
    <#assign cssFileName = "${cssFile}" />
  <#else>
    <#assign cssFileName = "${cssFile?replace('.css$','-min.css','r')}" />
  </#if>

  <#if cssFile?contains("_ie7")>
    <!--[if lte IE 7]>
    <@versionedCSS file="${cssFileName}" />
    <![endif]-->
  <#else>
    <@versionedCSS file="${cssFileName}" />
  </#if>

</#list>
