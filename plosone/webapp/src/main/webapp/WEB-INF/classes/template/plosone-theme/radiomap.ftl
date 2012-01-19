<#if parameters.label?exists>
    <label <#rt/>
<#if parameters.id?exists>
        for="${parameters.id?html}"<#t/>
</#if>
    ><#t/>
<#if parameters.required?default(false) && parameters.requiredposition?default("left") != 'left'>
        <span class="required">*</span><#t/>
</#if>
<#if parameters.required?default(false) && parameters.requiredposition?default("left") == 'left'>
 <span class="required">*</span><#t/>
</#if>
 <#t/>
<#include "/${parameters.templateDir}/${parameters.theme}/tooltip.ftl" />
</#if>
<#-- add the extra row -->
<#if parameters.labelposition?default("") == 'top'>
</#if>

<@s.iterator value="parameters.list">
    <#if parameters.listKey?exists>
        <#assign itemKey = stack.findValue(parameters.listKey)/>
    <#else>
        <#assign itemKey = stack.findValue('top')/>
    </#if>
    <#assign itemKeyStr = itemKey.toString() />
    <#if parameters.listValue?exists>
        <#assign itemValue = stack.findString(parameters.listValue)/>
    <#else>
        <#assign itemValue = stack.findString('top')/>
    </#if>
<input type="radio" name="${parameters.name?html}" id="${parameters.id?html}${itemKeyStr?html}"<#rt/>
<#if tag.contains(parameters.nameValue, itemKeyStr)>
 checked="checked"<#rt/>
</#if>
<#if itemKey?exists>
 value="${itemKeyStr?html}"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.tabindex?exists>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.cssClass?exists>
 class="${parameters.cssClass?html}"<#rt/>
</#if>
<#if parameters.cssStyle?exists>
 style="${parameters.cssStyle?html}"<#rt/>
</#if>
<#if parameters.title?exists>
 title="${parameters.title?html}"<#rt/>
</#if>
<#include "/${parameters.templateDir}/simple/scripting-events.ftl" />
<#include "/${parameters.templateDir}/simple/common-attributes.ftl" />
/><#rt/>
    ${parameters.label?html} <#t/>
</label>
</@s.iterator>