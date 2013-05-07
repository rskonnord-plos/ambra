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
<form action="/user/secure/profile/alerts/journal/save" method="post" class="ambra-form" method="post"
      name="userAlerts">
    <fieldset id="alert-form">
        <legend>Manage your journal alert emails</legend>
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
            <li<#if ua.hasSubjectFilter() && permissions?seq_contains("BETA_FEATURES")> class="filtered"</#if>>
                <span class="alerts-title">${ua.name} <#if ua.hasSubjectFilter() && permissions?seq_contains("BETA_FEATURES")>
                  <span class="alertToggle<#if journalSubjectFilters[ua.key]??> alertToggleOn<#else> alertToggleOff</#if>"></span>
                  </#if></span>
                <ol>
                    <li class="alerts-weekly">
                      <#if ua.weeklyAvailable>
                        <label>
                          <@s.checkbox accessKey="${ua.key}" name="weeklyAlerts" fieldValue="${ua.key}" value="${isFound(weeklyAlerts, ua.key)}"/>
                            Weekly </label>
                      </#if>
                    </li>

                    <li class="alerts-monthly">
                      <#if ua.monthlyAvailable>
                          <label>
                            <@s.checkbox accessKey="${ua.key}" name="monthlyAlerts" fieldValue="${ua.key}" value="${isFound(monthlyAlerts, ua.key)}"/>
                              Monthly </label>
                      <#else>
                        &nbsp;&nbsp;
                      </#if>
                    </li>
                </ol>
            </li>
            <#if ua.hasSubjectFilter() && permissions?seq_contains("BETA_FEATURES")>
              <#if journalSubjectFilters[ua.key]??>
                <#list journalSubjectFilters[ua.key] as subject>
                  <input type="hidden" name="journalSubjectFilters['${ua.key}']" value="${subject}"/>
                </#list>
              </#if>
              <li class="subjectAreaSelector" journal="${ua.key}"<#if !journalSubjectFilters[ua.key]??> style="display:none"</#if>>
                <div class="row">
                  <label>
                    <input id="subjectAll_${ua.key}" type="radio" name="filterSpecified['${ua.key}']" value="none"/>All subject areas (Sent as an
                    uncategorized article list, ordered by publication date)
                  </label>
                  <label>
                    <input id="subjectSome_${ua.key}" type="radio" name="filterSpecified['${ua.key}']" value="subjects"<#if journalSubjectFilters[ua.key]??> checked="true"</#if>/>Specify Subject areas (12 maximum)
                  </label>
                </div>
                <div class="row">
                  <input type="text" name="searchSubject" class="subjectSearchInput"/><div class="clearFilter"></div><input type="button" name="searchSubject_btn" value="Search" class="btn primary"/>
                </div>
                <div class="row">
                  <div class="selectSubjectAreas">
                    <ol id="subjectAreaSelector" class="selector" />
                  </div>
                  <div class="selectedSubjectAreas">
                    <div class="subjectsSelected"<#if !journalSubjectFilters[ua.key]??> style="display:none"</#if>>
                        <h3>Specified subject areas</h3>
                        <#if journalSubjectFilters[ua.key]??>
                          <ol>
                            <#list journalSubjectFilters[ua.key] as subject>
                              <li><div class="filter-item">${subject}&nbsp;<img src="/images/btn.close.png"></div></li>
                            </#list>
                          </ol>
                        <#else>
                          <#-- Javascript needs an empty list to add items to -->
                          <ol></ol>
                        </#if>
                      <input type="submit" class="btn primary" id="subjectSave" name="formSubmit" value="Save"/>
                    </div>
                    <div class="noSubjectsSelected"<#if journalSubjectFilters[ua.key]??> style="display:none;"</#if>>
                      <h3>No subject areas specified</h3>
                      Select <b>up to 12</b> subject areas from the list on the left.
                    </div>
                  </div>
                </div>
              </li>
            </#if>
        </#list>
        </ol>

        <div class="btnwrap"><input type="submit" class="btn primary" id="formSubmit" name="formSubmit" value="Save" tabindex="99"/></div>

    </fieldset>
</form>