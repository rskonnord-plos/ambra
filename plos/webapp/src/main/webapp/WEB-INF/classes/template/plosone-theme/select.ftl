<#include "/${parameters.templateDir}/${parameters.theme}/controlheader.ftl" />
<#include "/${parameters.templateDir}/simple/select.ftl" />
<#if hasFieldErrors>
<#list fieldErrors[parameters.name] as error>
${error?html}
</#list>
</#if>
<#include "/${parameters.templateDir}/${parameters.theme}/controlfooter.ftl" />
