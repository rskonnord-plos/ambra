<#list freemarker_config.getCss(templateFile, journalContext) as cssFile>
  <#--
    If we are not in debug mode, or the filename points to a journal.css
    Do not try to use the minified version.

    TODO: Instead of hard coding journal.css here.  Perhaps use a different
    naming schema, perhaps [colorSet].css. Then each journal.xml can
    reference the minified color specific override.
    Perhaps a new folder called "override_css"?  All css defined there
    Is not minified?
  -->
  <#if freemarker_config.debug || cssFile?contains("journal.css")>
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
