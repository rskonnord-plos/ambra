<#--
  Copyright (c) 2007-2013 by Public Library of Science
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

<form action="/user/secure/profile/alerts/search/save" method="post" class="ambra-form" title="Search Alert Form" name="userSearchAlerts">
  <fieldset id="alert-form">
    <legend>Manage your search alert emails</legend>
    <div id="saOL"<#if !userSearchAlerts?has_content> style="display:none;"</#if>>
      <ol>
        <#if userSearchAlerts?has_content>
          <li>
            <span class="search-alerts-title">&nbsp;</span>
            <ol>
              <li class="search-alerts-weekly">
                <label for="checkAllWeeklySavedSearch">
                  <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" id="checkAllWeeklySavedSearch"/> Select All
                </label>
              </li>
              <li class="search-alerts-monthly">
                <label for="checkAllMonthlySavedSearch">
                  <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" id="checkAllMonthlySavedSearch"/>
                  Select All
                </label>
              </li>
              <li class="search-alerts-delete">
                <label for="checkAllDeleteSavedSearch">
                  <input type="checkbox" value="checkAllDelete" name="checkAllDelete" id="checkAllDeleteSavedSearch"/> Select All
                </label>
              </li>
            </ol>
          </li>
        </#if>
        <#list userSearchAlerts as ua>
          <li id="saID${ua.savedSearchId?c}" class="saID">
            <span class="search-alerts-title">${ua.searchName}</span>
            <ol>
              <li class="search-alerts-weekly">
                <label>
                  <@s.checkbox name="weeklyAlerts" fieldValue="${ua.savedSearchId?c}" value="${ua.weekly?string}"/>
                  Weekly </label>
              </li>

              <li class="search-alerts-monthly">
                <label>
                  <@s.checkbox name="monthlyAlerts" fieldValue="${ua.savedSearchId?c}" value="${ua.monthly?string}"/>
                  Monthly </label>
              </li>

              <li class="search-alerts-delete">
                <label>
                  <@s.checkbox name="deleteAlerts" fieldValue="${ua.savedSearchId?c}" value="false"/>
                  Delete </label>
              </li>
            </ol>
          </li>
        </#list>
      </ol>
      <div id="sa_save_btn" class="btnwrap"><input type="submit" id="formSubmit" class="btn primary" name="formSubmit" value="Save" tabindex="99"/></div>
    </div>

    <div id="sa_none_defined" style="<#if userSearchAlerts?has_content>display:none;</#if>">
      You have no search alerts defined.  You can create one by performing a <a href="/search/advanced">search</a> on our site and save it by clicking on the "Search Alert" button.
    </div>
  </fieldset>

</form>

