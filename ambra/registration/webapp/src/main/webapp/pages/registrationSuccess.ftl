<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
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