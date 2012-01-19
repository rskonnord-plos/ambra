<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Resend Registration E-mail</h1>
	<!--<p><strong>Instruction Title   Text.</strong> Additional Instructoins here.</p>-->
	<p>Fields marked with <span class="required">*</span> are required. </p>
  <@ww.form cssClass="pone-form" method="post" name="resendRegistrationForm" id="resendRegistrationForm" action="resendRegistrationSubmit" title="Resend Registration Form">
		
	<fieldset>
		<legend>Resend Registration</legend>
		<ol class="field-list">
    	<@ww.textfield name="loginName" label="E-mail " required="true" id="email" tabindex="1" maxlength="256"/>
		</ol>
  	<div class="btnwrap">
	  <@ww.submit name="submit" id="submit" value="Submit" tabindex="2"/>
	</div>
	</fieldset>

  </@ww.form>
  
  <ul>
          <li><a href="http://journals.plos.org/help.php">Help</a></li>
	  <li>Already registered? <a href="${plosOneUrl}${plosOneContext}/profile">Login</a>.</li>
          <li><a href="/plos-registration/register.action">Register for a New Account</a></li>
          <li><a href="/plos-registration/forgotPassword.action" title="Click here if you forgot your password">Forgotten Password?</a></li>
  </ul>
  
</div>
<!-- end : main contents -->
<#include "/global/global_bottom.ftl">