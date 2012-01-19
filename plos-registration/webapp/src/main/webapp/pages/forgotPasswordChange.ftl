<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<#if Parameters.loginName?exists>
	<#assign thisLoginName = Parameters.loginName>
<#elseif loginName?exists>
	<#assign thisLoginName = loginName>
<#else>
	<#assign thisLoginName = "">
</#if>

<#if Parameters.resetPasswordToken?exists>
	<#assign thisPasswordToken = Parameters.resetPasswordToken>
<#elseif resetPasswordToken?exists>
	<#assign thisPasswordToken = resetPasswordToken>
<#else>
	<#assign thisPasswordToken = "">
</#if>

<!-- begin : main content -->
<div id="content">
<h1>Change Your Password</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required. </p>
  <@ww.form cssClass="pone-form" method="post" name="changePasswordForm" id="changePasswordForm" action="forgotPasswordChangeSubmit" title="Change Password Form">
	  <@ww.hidden name="loginName" value="${thisLoginName}"/>
    <@ww.hidden name="resetPasswordToken" value="${thisPasswordToken}" />
	<fieldset>
		<legend>Change your password</legend>
		<ol class="field-list">
      <@ww.password name="password1" label="New password " required="true" id="newPassword1" tabindex="101" maxlength="255" after=" (Password must be at least 6 characters)"/>
      <@ww.password name="password2" label="Please re-type your new password " required="true" id="newPassword2" tabindex="102" maxlength="255" />
		</ol>
	  <@ww.submit name="submit" id="submit" value="Submit" tabindex="103"/>
	</fieldset>
	
	</@ww.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">