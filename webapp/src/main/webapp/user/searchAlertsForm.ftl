<#include "/includes/global_variables.ftl">

<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>

<@s.form action="saveUserSearchAlerts" namespace="/user/secure" method="post" cssClass="ambra-form" method="post" title="Search Alert Form" name="userSearchAlerts">
<fieldset id="alert-form">
  <legend><strong>Manage your search alert emails</strong></legend>
  <ol>
    <#if userSearchAlerts?has_content>
      <li>
        <span class="search-alerts-title">&nbsp;</span>
        <ol>
          <li class="search-alerts-weekly">
            <label for="checkAllWeekly">
              <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" id="checkAllWeekly"/> Select All
            </label>
          </li>
          <li class="search-alerts-monthly">
            <label for="checkAllMonthly">
              <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" id="checkAllMonthly"/> Select All
            </label>
          </li>
          <li class="search-alerts-delete">
            <label for="checkAllDelete">
              <input type="checkbox" value="checkAllDelete" name="checkAllDelete" id="checkAllDelete"/> Select All
            </label>
          </li>
        </ol>
      </li>
    </#if>
    <#list userSearchAlerts as ua>
    <li>
      <span class="search-alerts-title">${ua.searchName}</span>
      <ol>
        <li class="search-alerts-weekly">
          <label for="${ua.savedSearchId}">
            <@s.checkbox name="weeklyAlerts" fieldValue="${ua.savedSearchId}" value="${ua.weekly?string}"/>
            Weekly </label>
        </li>

        <li class="search-alerts-monthly">
          <label for="${ua.savedSearchId}">
            <@s.checkbox name="monthlyAlerts" fieldValue="${ua.savedSearchId}" value="${ua.monthly?string}"/>
            Monthly </label>
        </li>
        <li class="search-alerts-delete">
          <label for="${ua.savedSearchId}">
            <@s.checkbox name="deleteAlerts" fieldValue="${ua.savedSearchId}" value="false"/>
            Delete </label>
        </li>
      </ol>
    </#list>
  </li>

  </ol>
  <#if userSearchAlerts?has_content>
    <div class="btnwrap"><input type="submit" id="formSubmit" name="formSubmit" value="Save" tabindex="99"/></div>
  </#if>
</fieldset>
</@s.form>

