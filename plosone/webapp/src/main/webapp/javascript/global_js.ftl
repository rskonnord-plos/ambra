<script type="text/javascript">
  var _namespace="${freemarker_config.getContext()}";
	<#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
		var loggedIn = true;
	<#else>
  	var loggedIn = false;
	</#if>

	var djConfig = {
		isDebug: false,
		debugContainerId : "dojoDebug",
		debugAtAllCosts: false,
  	bindEncoding: "UTF-8",
  	baseScriptUri: "${freemarker_config.context}/javascript/dojo/"
	};
</script>
<#list freemarker_config.getJavaScript(templateFile, journalContext) as x>
	<#if x?ends_with(".ftl")>
<script type="text/javascript">
<#include "${x}">
</script>	
	<#else>
<script type="text/javascript" src="${x}"></script>	
	</#if>
</#list>