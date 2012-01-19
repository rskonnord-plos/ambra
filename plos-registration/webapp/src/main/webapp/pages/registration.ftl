<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Sign Up for a PLoS Journals Profile</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required.</p>

  <@ww.form cssClass="pone-form" method="post" name="registrationFormPart1" action="registerSubmit" title="Registration Form">

  <fieldset>
	<legend>Registration</legend>
	<ol>
	<@ww.textfield label="E-mail " name="loginName1" required="true" tabindex="1" maxlength="256"/>
	<@ww.textfield label="Please re-type your e-mail " name="loginName2" required="true" tabindex="2" maxlength="256"/>
	<@ww.password label="Password " name="password1" required="true" tabindex="3" maxlength="255" after=" (Password must be at least 6 characters)"/>
	<@ww.password label="Please re-type your password " name="password2" required="true" tabindex="4" maxlength="128"/>
	</ol>
	<div class="btnwrap">
		<@ww.submit name="submit" value="Submit" tabindex="5"/>
	</div>
	</fieldset>
  </@ww.form>
  <ul>
      <li>Already registered? <a href="${plosOneUrl}${plosOneContext}/profile">Login</a>.</li>
      <li><a href="${plosOneUrl}${plosOneContext}/static/help.action#account">Help</a></li>
  </ul>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">
