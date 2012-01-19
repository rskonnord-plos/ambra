<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2010 by Public Library of Science
  http://plos.org
  http://ambraproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
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
