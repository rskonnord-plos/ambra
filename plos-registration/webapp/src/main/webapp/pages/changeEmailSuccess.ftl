<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Change E-mail Address</h1>
	An e-mail has been sent to <@s.property value="newLogin1"/> with instructions on how to finish changing your e-mail address.
	</p>
	<p>If you do not receive the e-mail, please add <strong>${registrationVerificationMailer.fromEmailAddress}</strong> to your allowed 
	senders list and try again.</p>
	<br/>
	<p>Return to <a href="${plosOneUrl}${plosOneContext}/user/secure/editProfile.action">your profile</a> or <a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a></p>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">