<#--
	Only show message if errors are available.
	This will be done if ActionSupport is used.
-->
<#assign hasFieldErrors = parameters.name?exists && fieldErrors?exists && fieldErrors[parameters.name]?exists/>
<#--
	if the label position is top,
	then give the label it's own row in the table
-->
<#if hasFieldErrors>
  <li class="form-error">
<#else>
  <li>
</#if>
<#if parameters.label?exists>
    <label <#rt/>
<#if parameters.id?exists>
        for="${parameters.id?html}"<#t/>
</#if>
    ><#t/>
<#if parameters.required?default(false) && parameters.requiredposition?default("left") != 'left'>
        <span class="required">*</span><#t/>
</#if>
${parameters.label?html} <#t/>
<#if parameters.required?default(false) && parameters.requiredposition?default("left") == 'left'>
 <span class="required">*</span><#t/>
</#if>
 <#t/>
<#include "/${parameters.templateDir}/${parameters.theme}/tooltip.ftl" /> 
</label><#lt/>
</#if>
<#-- add the extra row -->
<#if parameters.labelposition?default("") == 'top'>
</#if>
