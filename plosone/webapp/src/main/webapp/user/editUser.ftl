<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>
<#if displayName?exists>
   <#assign username = displayName>
<#else>
   <#assign username = "">
</#if>


<div id="content">
	<h1>PLoS Profile: ${username}</h1>
	
	<div class="horizontalTabs">
		<ul id="tabsContainer">
		</ul>
		
		<div id="tabPaneSet" class="contentwrap">
		  <#if tabId == "alerts">
				<#include "alerts.ftl">
		  <#else>
				<#include "user.ftl">
			</#if>
		</div>
	</div>
	
</div>

<#include "/widget/loadingCycle.ftl">
