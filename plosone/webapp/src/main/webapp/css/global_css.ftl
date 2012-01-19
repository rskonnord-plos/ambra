<!-- global_css.ftl -->

<#list freemarker_config.getCss(templateFile) as x>
<style type="text/css" media="all"> @import "${x}";</style>
</#list> 
<style type="text/css" media="print"> @import "${freemarker_config.context}/css/pone_print.css";</style>