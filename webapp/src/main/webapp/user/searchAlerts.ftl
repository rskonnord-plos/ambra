<#include "initForEditedBy.ftl">

<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>
<#if Parameters.tabId?exists>
  <#assign tabId = Parameters.tabId>
<#else>
  <#assign tabId = "">
</#if>

<#if editedByAdmin>
  <#assign actionValue="saveSearchAlertsByAdmin"/>
  <#assign namespaceValue="/admin"/>
<#else>
  <#assign actionValue="saveUserSearchAlerts"/>
  <#assign namespaceValue="/user/secure"/>
</#if>
<@s.form action="${actionValue}" namespace="${namespaceValue}" method="post" cssClass="ambra-form" method="post" title="Search Alert Form" name="userSearchAlerts">
<fieldset id="alert-form">
  <legend><strong>Manage your search alert emails</strong></legend>
  <ol>
    <#if userSearchAlerts?has_content>
      <li>
        <span class="search-alerts-title">&nbsp;</span>
        <ol>
          <li class="search-alerts-weekly">
            <label for="checkAllWeekly">
              <#if tabId?has_content>
                <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.weeklyAlerts); ambra.horizontalTabs.checkValue(this);" /> Select All
              <#else>
                <input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.weeklyAlerts);" /> Select All
              </#if>
            </label>
          </li>
          <li class="search-alerts-monthly">
            <label for="checkAllMonthly">
              <#if tabId?has_content>
                <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.monthlyAlerts); ambra.horizontalTabs.checkValue(this);" /> Select All
              <#else>
                <input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.monthlyAlerts);" /> Select All
              </#if>
            </label>
          </li>
          <li class="search-alerts-delete">
            <label for="checkAllDelete">
              <#if tabId?has_content>
                <input type="checkbox" value="checkAllDelete" name="checkAllDelete" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.deleteAlerts); ambra.horizontalTabs.checkValue(this);" /> Select All
              <#else>
                <input type="checkbox" value="checkAllDelete" name="checkAllDelete" onclick="ambra.formUtil.selectAllCheckboxes(this, document.userSearchAlerts.deleteAlerts);" /> Select All
              </#if>
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
            <#if tabId?has_content>
              <@s.checkbox name="weeklyAlerts" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.horizontalTabs.checkValue(this); ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" onchange="ambra.horizontalTabs.checkValue(this);" fieldValue="${ua.savedSearchId}" value="${ua.weekly?string}"/>
            <#else>
              <@s.checkbox name="weeklyAlerts" onclick="ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" fieldValue="${ua.savedSearchId}" value="${ua.weekly?string}"/>
            </#if>
            Weekly </label>
        </li>

        <li class="search-alerts-monthly">
          <label for="${ua.savedSearchId}">
            <#if tabId?has_content>
              <@s.checkbox name="monthlyAlerts" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.horizontalTabs.checkValue(this); ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);" onchange="ambra.horizontalTabs.checkValue(this);"  fieldValue="${ua.savedSearchId}" value="${ua.monthly?string}"/>
            <#else>
              <@s.checkbox name="monthlyAlerts" onclick="ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);"  fieldValue="${ua.savedSearchId}" value="${ua.monthly?string}"/>
            </#if>
            Monthly </label>
        </li>
        <li class="search-alerts-delete">
          <label for="${ua.savedSearchId}">
            <#if tabId?has_content>
              <@s.checkbox name="deleteAlerts" onfocus="ambra.horizontalTabs.setTempValue(this);" onclick="ambra.horizontalTabs.checkValue(this); ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllDelete, this.form.deleteAlerts);" onchange="ambra.horizontalTabs.checkValue(this);"  fieldValue="${ua.savedSearchId}" value="false"/>
            <#else>
              <@s.checkbox name="deleteAlerts" onclick="ambra.formUtil.selectCheckboxPerCollection(this.form.checkAllDelete, this.form.deleteAlerts);"  fieldValue="${ua.savedSearchId}" value="false"/>
            </#if>
            Delete </label>
        </li>
      </ol>
    </#list>
  </li>

  </ol>
  <br clear="all" />

  <#include "submit.ftl">

</fieldset>
</@s.form>

