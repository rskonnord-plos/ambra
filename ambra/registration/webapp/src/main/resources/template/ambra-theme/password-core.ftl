<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
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
<#if parameters.beforeText?exists>
 ${parameters.beforeText?html}<#rt/>
</#if>
<input type="password"<#rt/>
 name="${parameters.name?default("")?html}"<#rt/>
<#if parameters.get("size")?exists>
 size="${parameters.get("size")?html}"<#rt/>
</#if>
<#if parameters.maxlength?exists>
 maxlength="${parameters.maxlength?html}"<#rt/>
</#if>
<#if parameters.nameValue?exists && parameters.showPassword?default(false)>
 value="<@s.property value="parameters.nameValue"/>"<#rt/>
</#if>
<#if parameters.disabled?default(false)>
 disabled="disabled"<#rt/>
</#if>
<#if parameters.readonly?default(false)>
 readonly="readonly"<#rt/>
</#if>
<#if parameters.tabindex?exists>
 tabindex="${parameters.tabindex?html}"<#rt/>
</#if>
<#if parameters.id?exists>
 id="${parameters.id?html}"<#rt/>
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
/>