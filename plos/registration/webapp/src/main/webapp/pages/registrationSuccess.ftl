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
	<p>Continue to:</p>
	<ul>
		<li><a href="http://biology.plosjournals.org/">PLoS Biology</a></li>
		<li><a href="http://medicine.plosjournals.org/">PLoS Medicine</a></li>
		<li><a href="http://compbiol.plosjournals.org/">PLoS Computational Biology</a></li>
		<li><a href="http://genetics.plosjournals.org/">PLoS Genetics</a></li>
		<li><a href="http://pathogens.plosjournals.org/">PLoS Pathogens</a></li>
		<li><a href="${plosOneUrl}${plosOneContext}">PLoS ONE</a></li>
		<li><a href="http://www.plosntds.org/">PLoS Neglected Tropical Diseases</a></li>
		<li><a href="http://clinicaltrials.ploshubs.org/">PLoS Hub for Clinical Trials</a></li>
	</ul>
</div>
<!-- end : main contents -->

<#include "/global/global_bottom.ftl">