<#assign hasFieldErrors = fieldErrors?exists && fieldErrors[parameters.name]?exists/>

<#if hasFieldErrors>
<#list fieldErrors[parameters.name] as error>
<#if parameters.id?exists>
 errorFor="${parameters.id}"<#rt/>
</#if>
>
</#list>
</#if>
<#if parameters.labelposition?default("") == 'top'>
<#if parameters.label?exists> <label<#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}"<#rt/>
</#if>
>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") != 'right'>
        <span class="required">*</span><#t/>
</#if>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") == 'right'>
 <span class="required">*</span><#t/>
</#if>
<#t/>
<#if parameters.tooltip?exists>
    <img src='<@ww.url value="/webwork/tooltip/tooltip.gif" encode='false' includeParams='none' />' alt="${parameters.tooltip}" title="${parameters.tooltip}" onmouseover="return escape('${parameters.tooltip?js_string}');" />
</#if>
</#if>

        <#include "/${parameters.templateDir}/${parameters.theme}/checkbox-core.ftl" />
<#if parameters.label?exists>
${parameters.label?html}<#t/>
</label><#t/>
</#if>

<#else>

<#if parameters.labelposition?default("") == 'left'>

<#if parameters.label?exists> <label<#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}"<#rt/>
</#if>
<#if hasFieldErrors>
 class="checkboxErrorLabel"<#rt/>
<#else>
 class="checkboxLabel"<#rt/>
</#if>
>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") != 'right'>
        <span class="required">*</span><#t/>
</#if>
${parameters.label?html}<#t/>
<#if parameters.required?default(false) && parameters.requiredposition?default("right") == 'right'>
 <span class="required">*</span><#t/>
</#if>
<#t/>
<#if parameters.tooltip?exists>
    <img src='<@ww.url value="/webwork/tooltip/tooltip.gif" encode="false" includeParams='none'/>' alt="${parameters.tooltip}" title="${parameters.tooltip}" onmouseover="return escape('${parameters.tooltip?js_string}');" />
</#if>
</label><#t/>
</#if>
</#if>

<#if parameters.labelposition?default("") == 'right'>
    <#if parameters.required?default(false)>
        <span class="required">*</span><#t/>
    </#if>
    <#if parameters.tooltip?exists>
        <img src='<@ww.url value="/webwork/tooltip/tooltip.gif" encode="false" includeParams='none'/>' alt="${parameters.tooltip}" title="${parameters.tooltip}" onmouseover="return escape('${parameters.tooltip?js_string}');" />
    </#if>
</#if>

<#if parameters.labelposition?default("") != 'top'>
                	<#include "/${parameters.templateDir}/${parameters.theme}/checkbox-core.ftl" />
</#if>                    

<#if parameters.labelposition?default("") != 'top' && parameters.labelposition?default("") != 'left'>
<#if parameters.label?exists> <label<#t/>
<#if parameters.id?exists>
 for="${parameters.id?html}"<#rt/>
</#if>
<#if hasFieldErrors>
 class="checkboxErrorLabel"<#rt/>
<#else>
 class="checkboxLabel"<#rt/>
</#if>
>${parameters.label?html}</label><#rt/>
</#if>
</#if>
</#if>
