<#include "initForEditedBy.ftl">

<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>

<#if editedByAdmin>
  <#assign actionValue="saveProfileByAdmin"/>
  <#assign namespaceValue="/admin"/>
<#else>
  <#assign actionValue="saveProfile"/>
  <#assign namespaceValue="/user/secure"/>
</#if>
<@s.form name="userForm" id="userForm" action="${actionValue}" namespace="${namespaceValue}" method="post" title="User Information Form" cssClass="pone-form" enctype="multipart/form-data">

  <fieldset>
  <legend>${addressingUser} Private Information</legend>
  <ol>
      <li><p><em>Your E-mail address will always be kept private. See the the <a href="http://www.plos.org/privacy.html" title="PLoS Privacy Statement">PLoS Privacy Statement</a> for more information.</em></p>
    <strong>${email}</strong><br />
<a href="${freemarker_config.changeEmailURL}" title="Click here to change your e-mail address">Change your e-mail address</a><br/>
<a href="${freemarker_config.changePasswordURL}" title="Click here to change your password">Change your password</a>
    </li>
  </ol>
  </fieldset>
  <fieldset>
  <legend>${addressingUser} Public Profile</legend>
  <ol>
	<li>Fields marked with <span class="required">*</span> are required.</li>
  <li><em>The following required fields will always appear publicly.</em></li>

   	<#if !isDisplayNameSet>
      <!--after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)"-->
        <@s.textfield name="displayName" label="Username" required="true" tabindex="1" maxlength="18" after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)" />
	  </#if>
   	<#if tabId?has_content>
          <@s.textfield name="givenNames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="First/Given Name" required="true" tabindex="2" />
	  <#else>
          <@s.textfield name="givenNames" label="First/Given Name" required="true" tabindex="2" />
	  </#if>
   	  <#if tabId?has_content>	
          <@s.textfield name="surnames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Last/Family Name" required="true" tabindex="3"/>
	  <#else>
          <@s.textfield name="surnames" label="Last/Family Name" required="true" tabindex="3"/>
	  </#if>
		<br />
   	  <#if tabId?has_content>	
          <@s.textfield name="city" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="City" required="true" tabindex="4"/>
	  <#else>
          <@s.textfield name="city" label="City" required="true" tabindex="4"/>
	  </#if>
    <@s.action name="selectList" namespace="" id="selectList"/>
    <#if tabId?has_content>	
          <@s.select label="Country" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" name="country" value="country"
          list="%{#selectList.get('countries')}" tabindex="5" required="true" />
	  <#else>
          <@s.select label="Country" name="country" value="country"
          list="%{#selectList.get('countries')}" tabindex="5" required="true" />
    </#if>

			</li>
		</ol>
	</fieldset>
	<fieldset>
	<legend>${addressingUser} Extended Profile</legend>
		<ol>
   	  <#if tabId?has_content>	
        <@s.textarea name="postalAddress" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="6" />
	  <#else>
        <@s.textarea name="postalAddress" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="6" />
	  </#if></li>
		<li>
				<fieldset class="public-private">
				<legend>Would you like your address to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@s.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" checked="true" tabindex="7" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="extendedVisibility" label="Public" list="{'public'}" checked="true" tabindex="7" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@s.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="8" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="extendedVisibility" label="Private" list="{'private'}" tabindex="8" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
			</li>
			<li class="form-last-item">
				<ol>
   	  <#if tabId?has_content>
          <@s.select label="Organization Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="9" />
	  <#else>
          <@s.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="9" />
	  </#if>
          <@s.textfield name="organizationName" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Organization Name" tabindex="10" />
				</ol>
				<ol>
   	  <#if tabId?has_content>	
            <@s.select label="Title" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);"  name="title" value="title"
            list="%{#selectList.allTitles}" tabindex="10" />
	  <#else>
            <@s.select label="Title" name="title" value="title" list="%{#selectList.allTitles}" tabindex="10" />
	  </#if>

   	  <#if tabId?has_content>	
            <@s.select label="Position Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" name="positionType" value="positionType"
            list="%{#selectList.allPositionTypes}" tabindex="11" />
	  <#else>
            <@s.select label="Position Type" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" tabindex="11" />
	  </#if>
				</ol>
				<fieldset class="public-private">
				<legend>Would you like your organization information and title to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@s.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" tabindex="12" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="orgVisibility" label="Public" list="{'public'}" tabindex="12" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@s.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="13" cssClass="radio" class="radio"/>
	  <#else>
          <@s.radio name="orgVisibility" label="Private" list="{'private'}" tabindex="13" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
		  </li>
		</ol>
	</fieldset>
	<fieldset>
		<legend>Optional Public Information</legend>
		<ol>
   	  <#if tabId?has_content>	
	      <@s.textarea name="biographyText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="About Me" rows="5" cols="50" tabindex="14"/>
	  <#else>
	      <@s.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="14"/>
	  </#if>
   	  <#if tabId?has_content>	
	      <@s.textfield name="researchAreasText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Research Areas" cssClass="long-input" tabindex="15" />
	  <#else>
	      <@s.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="15" />
	  </#if>
   	  <#if tabId?has_content>	
	      <@s.textfield name="interestsText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Interests"  cssClass="long-input" tabindex="16" />
	  <#else>
	      <@s.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="16" />
	  </#if>
			<li>
   	  <#if tabId?has_content>	
        <@s.textfield name="homePage" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Home page"  cssClass="long-input" tabindex="17" />
	  <#else>
        <@s.textfield name="homePage" label="Home page"  cssClass="long-input" tabindex="17" />
	  </#if>
   	  <#if tabId?has_content>	
        <@s.textfield name="weblog" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Weblog"  cssClass="long-input" tabindex="18" />
	  <#else>
        <@s.textfield name="weblog" label="Weblog"  cssClass="long-input" tabindex="18" />
	  </#if>
			</li>
		</ol>

    <#include "submit.ftl">

	</fieldset>

</@s.form>

