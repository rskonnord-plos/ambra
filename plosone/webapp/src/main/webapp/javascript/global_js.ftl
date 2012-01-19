<script type="text/javascript">
  var namespace="${freemarker_config.getContext()}";
	<#if Session.PLOS_ONE_USER?exists>
		var loggedIn = true;
	<#else>
  	var loggedIn = false;
	</#if>

	var djConfig = {
		isDebug: false,
		debugContainerId : "dojoDebug",
		debugAtAllCosts: false,
  	bindEncoding: "UTF-8",
  	baseScriptUri: "<@ww.url includeParams="false" value='/javascript/dojo/'/>"
	};
</script>
<#list freemarker_config.getJavaScript(templateFile) as x>
	<#if x?ends_with(".ftl")>
<script type="text/javascript">
<#include "${x}">
</script>	
	<#else>
<script type="text/javascript" src="${x}"></script>	
	</#if>
</#list>