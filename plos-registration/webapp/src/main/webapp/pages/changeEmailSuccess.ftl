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
	<p>Return to <a href="${plosOneUrl}${plosOneContext}/user/secure/editProfile.action">your profile</a>, or visit:</p>
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