<#include "/global/global_config.ftl">
<#include "/global/global_top.ftl">

<!-- begin : main content -->
<div id="content">
<h1>Sign Up for a PLoS Journals Profile</h1>
	<p>
		<strong>Thanks for registering!</strong>
		Please check your e-mail inbox to confirm your registration.  
  </p>
  <@s.url id="resendURL" includeParams="none"  action="resendRegistration"/>

 	<p>If you do not receive the e-mail, please add <strong>${registrationVerificationMailer.fromEmailAddress}</strong> to your allowed 
	senders list and <a href="${resendURL}">request</a> that a new e-mail be sent.</p>
	<br/>
	<p>Continue to <a href="http://www.plosjournals.org" title="PLoS Journals: Peer-reviewed open-access journals from the Public Library of Science">PLoS Journals</a> or to <a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a>.</p>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">