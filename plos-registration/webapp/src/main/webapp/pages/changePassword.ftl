<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Change Your Password</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required. </p>
  <@s.form cssClass="pone-form" method="post" name="changePasswordForm" id="changePasswordForm" action="changePasswordSubmit" title="Change Password Form">

	<fieldset>
		<legend>Change your password</legend>
		<ol class="field-list">
    	<@s.textfield name="loginName" label="E-mail " required="true" id="email" tabindex="1" maxlength="256"/>
      <@s.password name="oldPassword" label="Old password " required="true" id="oldPassword" tabindex="2" maxlength="255"/>
      <@s.password name="newPassword1" label="New password " required="true" id="newPassword1" tabindex="3" maxlength="255" after=" (Password must be at least 6 characters)"/>
      <@s.password name="newPassword2" label="Please re-type your new password " required="true" id="newPassword2" tabindex="4" maxlength="255" />
		</ol>
	<div class="btnwrap">
	  <@s.submit name="submit" id="submit" value="Submit" tabindex="5"/>
	</div>
	</fieldset>
	
	</@s.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">