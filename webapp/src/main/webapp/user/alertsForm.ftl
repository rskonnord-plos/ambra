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
<#include "/includes/global_variables.ftl">

<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>
<@s.form action="saveUserAlerts" namespace="/user/secure" method="post" cssClass="ambra-form" method="post"
title="Alert Form" name="userAlerts">
<fieldset id="alert-form">
  <legend><strong>Email Alerts</strong></legend>
  <ol>
    <li>
      <span class="alerts-title">&nbsp;</span>
      <ol>
        <li class="alerts-weekly">
          <label for="checkAllWeekly">
              <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" id="checkAllWeekly"/> Select All
          </label>
        </li>
        <li class="alerts-monthly">
          <label for="checkAllMonthly">
              <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly"  id="checkAllMonthly"/> Select All
          </label>
        </li>
      </ol>
    </li>
    <#list userAlerts as ua>
    <li>
      <span class="alerts-title">${ua.name}</span>
      <ol>
        <li class="alerts-weekly">
          <#if ua.weeklyAvailable>
            <label for="${ua.key}">
                <@s.checkbox name="weeklyAlerts" fieldValue="${ua.key}" value="${isFound(weeklyAlerts, ua.key)}"/>
              Weekly </label>
          </#if>
        </li>

        <li class="alerts-monthly">
          <#if ua.monthlyAvailable>
            <label for="${ua.key}">
                <@s.checkbox name="monthlyAlerts" fieldValue="${ua.key}" value="${isFound(monthlyAlerts, ua.key)}"/>
              Monthly </label>
          </#if>
        </li>
      </ol>
    </#list>
  </li>

  </ol>
  <div class="btnwrap"><input type="submit" id="formSubmit" name="formSubmit" value="Save" tabindex="99"/></div>

</fieldset>
</@s.form>