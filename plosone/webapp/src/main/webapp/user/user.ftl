
<#if Parameters.tabId?exists>
   <#assign tabId = Parameters.tabId>
<#else>
   <#assign tabId = "">
</#if>

<@ww.form name="userForm" id="userForm"  method="post" title="User Information Form" cssClass="pone-form" enctype="multipart/form-data">

  <fieldset>
  <legend>Your Private Information</legend>
  <ol>
      <li><p><em>Your E-mail address will always be kept private. See the the <a href="http://www.plos.org/privacy.html" title="PLoS Privacy Statement">PLoS Privacy Statement</a> for more information.</em></p>
    <strong>${email}</strong><br />
<a href="${freemarker_config.changePasswordURL}" title="Click here to change your password">Change your password</a>
    </li>
  </ol>
  </fieldset>
  <fieldset>
  <legend>Your Public Profile</legend>
  <ol>
	<li>Fields marked with <span class="required">*</span> are required.</li>
  <li><em>The following required fields will always appear publicly.</em></li>

   	<#if !isDisplayNameSet>
      <!--after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)"-->
        <@ww.textfield name="displayName" label="Username" required="true" tabindex="101" after="(Usernames are <strong>permanent</strong> and must be between 4 and 18 characters)" />
	  </#if>
   	<#if tabId?has_content>
          <@ww.textfield name="givenNames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="First/Given Name" required="true" tabindex="102" />
	  <#else>
          <@ww.textfield name="givenNames" label="First/Given Name" required="true" tabindex="102" />
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.textfield name="surnames" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Last/Family Name" required="true" tabindex="103"/>
	  <#else>
          <@ww.textfield name="surnames" label="Last/Family Name" required="true" tabindex="103"/>
	  </#if>
		<br />
   	  <#if tabId?has_content>	
          <@ww.textfield name="city" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="City" required="true" tabindex="104"/>
	  <#else>
          <@ww.textfield name="city" label="City" required="true" tabindex="104"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.textfield name="country" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Country" required="true" tabindex="105"/>
	  <#else>
          <@ww.textfield name="country" label="Country" required="true" tabindex="105"/>
	  </#if>

			</li>
		</ol>
	</fieldset>
	<fieldset>
	<legend>Your Extended Profile</legend>
		<ol>
   	  <#if tabId?has_content>	
        <@ww.textarea name="postalAddress" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="106" />
	  <#else>
        <@ww.textarea name="postalAddress" label="Address" cssClass="long-input"  rows="5" cols="50" tabindex="106" />
	  </#if></li>
		<li>
				<fieldset class="public-private">
				<legend>Would you like your address to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@ww.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" checked="true" tabindex="107" cssClass="radio" class="radio"/>
	  <#else>
          <@ww.radio name="extendedVisibility" label="Public" list="{'public'}" checked="true" tabindex="107" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.radio name="extendedVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="108" cssClass="radio" class="radio"/>
	  <#else>
          <@ww.radio name="extendedVisibility" label="Private" list="{'private'}" tabindex="108" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
			</li>
			<li class="form-last-item">
				<ol>
          <@ww.action name="selectList" namespace="" id="selectList"/>
   	  <#if tabId?has_content>	
          <@ww.select label="Organization Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onselect="topaz.horizontalTabs.checkValue(this);" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="109" />
	  <#else>
          <@ww.select label="Organization Type" name="organizationType" value="organizationType"
          list="%{#selectList.allOrganizationTypes}" tabindex="109" />
	  </#if>
          <@ww.textfield name="organizationName" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Organization Name" tabindex="110" />
				</ol>
				<ol>
            <@ww.select label="Title" name="title" value="title"
            list="%{#selectList.allTitles}" tabindex="110" />

   	  <#if tabId?has_content>	
            <@ww.select label="Position Type" onfocus="topaz.horizontalTabs.setTempValue(this);" onselect="topaz.horizontalTabs.checkValue(this);" name="positionType" value="positionType"
            list="%{#selectList.allPositionTypes}" tabindex="111" />
	  <#else>
            <@ww.select label="Position Type" name="positionType" value="positionType" list="%{#selectList.allPositionTypes}" tabindex="111" />
	  </#if>
				</ol>
				<fieldset class="public-private">
				<legend>Would you like your organization information and title to appear publicly or privately?</legend>
   	  <#if tabId?has_content>	
          <@ww.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Public" list="{'public'}" tabindex="112" cssClass="radio" class="radio"/>
	  <#else>
          <@ww.radio name="orgVisibility" label="Public" list="{'public'}" tabindex="112" cssClass="radio" class="radio"/>
	  </#if>
   	  <#if tabId?has_content>	
          <@ww.radio name="orgVisibility" onfocus="topaz.horizontalTabs.setTempValue(this);" onclick="topaz.horizontalTabs.checkValue(this);" label="Private" list="{'private'}" tabindex="113" cssClass="radio" class="radio"/>
	  <#else>
          <@ww.radio name="orgVisibility" label="Private" list="{'private'}" tabindex="113" cssClass="radio" class="radio"/>
	  </#if>
				</fieldset>
		  </li>
		</ol>
	</fieldset>
	<fieldset>
		<legend>Optional Public Information</legend>
		<ol>
   	  <#if tabId?has_content>	
	      <@ww.textarea name="biographyText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="About Me" rows="5" cols="50" tabindex="114"/>
	  <#else>
	      <@ww.textarea name="biographyText" label="About Me" rows="5" cols="50" tabindex="114"/>
	  </#if>
   	  <#if tabId?has_content>	
	      <@ww.textfield name="researchAreasText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Research Areas" cssClass="long-input" tabindex="115" />
	  <#else>
	      <@ww.textfield name="researchAreasText" label="Research Areas" cssClass="long-input" tabindex="115" />
	  </#if>
   	  <#if tabId?has_content>	
	      <@ww.textfield name="interestsText" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Interests"  cssClass="long-input" tabindex="116" />
	  <#else>
	      <@ww.textfield name="interestsText" label="Interests"  cssClass="long-input" tabindex="116" />
	  </#if>
			<li>
   	  <#if tabId?has_content>	
        <@ww.textfield name="homePage" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Home page"  cssClass="long-input" tabindex="117" />
	  <#else>
        <@ww.textfield name="homePage" label="Home page"  cssClass="long-input" tabindex="117" />
	  </#if>
   	  <#if tabId?has_content>	
        <@ww.textfield name="weblog" onfocus="topaz.horizontalTabs.setTempValue(this);" onchange="topaz.horizontalTabs.checkValue(this);" label="Weblog"  cssClass="long-input" tabindex="118" />
	  <#else>
        <@ww.textfield name="weblog" label="Weblog"  cssClass="long-input" tabindex="118" />
	  </#if>
			</li>
		</ol>
      <div class="btnwrap"><input type="button" id="formSubmit" name="formSubmit" value="Save" tabindex="119"/></div>
	</fieldset>

</@ww.form>

