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
<strong>Select which E-mail content alerts you would like to receive.</strong>
	<p>Fields marked with <span class="required">*</span> are required. </p>

<#if editedByAdmin>
  <#assign actionValue="saveAlertsByAdmin"/>
  <#assign namespaceValue="/admin"/>
<#else>
  <#assign actionValue="saveUserAlerts"/>
  <#assign namespaceValue="/user/secure"/>
</#if>
  <@ww.form action="${actionValue}" namespace="${namespaceValue}" method="post" cssClass="pone-form" method="post" title="Alert Form" name="userAlerts">
  <fieldset id="alert-form">
		<legend>Choose your alerts</legend>
		<ol>
			<li><em>Check back soon for more PLoS ONE alerts</em></li>
      		<@ww.textfield name="alertEmailAddress" label="E-mail address for alerts" required="true"/>
        	<li><ol>
        		<li class="alerts-title">&nbsp;</li>
        		<li>
        			<label for="checkAllWeekly">
        			<#if tabId?has_content>
        				<input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.weeklyAlerts); topaz.horizontalTabs.checkValue(this);" /> Check all weekly alerts
        			<#else>
        				<input type="checkbox" value="checkAllWeekly" name="checkAllWeekly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.weeklyAlerts);" /> Check all weekly alerts
        			</#if>
        			</label>
        		</li>
        		<li>
        			<label for="checkAllMonthly">
         			<#if tabId?has_content>
        				<input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.monthlyAlerts); topaz.horizontalTabs.checkValue(this);" /> Check all monthly alerts
        			<#else>
        				<input type="checkbox" value="checkAllMonthly" name="checkAllMonthly" onclick="topaz.formUtil.selectAllCheckboxes(this, document.userAlerts.monthlyAlerts);" /> Check all monthly alerts
        			</#if>
        			</label>
        		</li>
        	</ol>
			</li>
      <#list categoryBeans as category>
        <li>
          <ol>
            <li class="alerts-title">${category.name}</li>
            <li>
              <#if category.weeklyAvailable>
                <label for="${category.key}">
				<#if tabId?has_content>
	              <@ww.checkbox name="weeklyAlerts" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this); topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" onchange="topaz.horizontalTabs.checkValue(this);" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
				<#else>
	              <@ww.checkbox name="weeklyAlerts" onclick="topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllWeekly, this.form.weeklyAlerts);" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
				</#if>
                Weekly </label>
              </#if>
            </li>

            <li>
              <#if category.monthlyAvailable>
                <label for="${category.key}">
    			<#if tabId?has_content>
	              <@ww.checkbox name="monthlyAlerts" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this); topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);" onchange="topaz.horizontalTabs.checkValue(this);"  fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
    			<#else>
                  <@ww.checkbox name="monthlyAlerts" onclick="topaz.formUtil.selectCheckboxPerCollection(this.form.checkAllMonthly, this.form.monthlyAlerts);"  fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
    			</#if>
                  Monthly </label>
              <#else>
              </#if>
            </li>
          </ol>
      </#list>
	          </li>

		</ol>
		<br clear="all" />

<#include "submit.ftl">

	</fieldset>
  </@ww.form>


