<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Change Your E-mail Address</h1>
	<p>Fields marked with <span class="required">*</span> are required. </p>
  <@s.form cssClass="pone-form" method="post" name="changeEmailForm" id="changeEmailForm" action="changeEmailSubmit" title="Change E-mail Address Form">

	<fieldset>
		<legend>Change your e-mail address</legend>
		<ol class="field-list">
    	<@s.textfield name="login" label="E-mail address " required="true" id="email" tabindex="1" maxlength="256"/>
      <@s.password name="password" label="Password " required="true" id="password" tabindex="2" maxlength="255"/>
      <@s.textfield name="newLogin1" label="New e-mail address " required="true" id="newLogin1" tabindex="3" maxlength="256" />
      <@s.textfield name="newLogin2" label="Please re-type your new e-mail address " required="true" id="newLogin2" tabindex="4" maxlength="256" />
		</ol>
	<div class="btnwrap">
	  <@s.submit name="submit" id="submit" value="Submit" tabindex="5"/>
	</div>
	</fieldset>
	
	</@s.form>

</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">