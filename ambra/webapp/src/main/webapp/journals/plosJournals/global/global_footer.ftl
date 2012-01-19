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
	<p><span>All site content, except where otherwise noted, is licensed under a <a href="http://creativecommons.org/licenses/by/2.5/" title="Creative Commons Attribution License 2.5" tabindex="200">Creative Commons Attribution License</a>.</span></p>
	<ul>
		<@s.url namespace="/static" action="privacy" includeParams="none" id="privacyURL" />
		<li><a href="${privacyURL}" title="PLoS Privacy Statement" tabindex="501">Privacy Statement</a></li>
		<@s.url namespace="/static" action="terms" includeParams="none" id="termsURL" />
		<li><a href="${termsURL}" title="PLoS Terms of Use" tabindex="502">Terms of Use</a></li>
		<li><a href="http://www.plos.org/advertise/" title="Advertise With PLoS" tabindex="503">Advertise</a></li>
		<li><a href="http://www.plos.org/journals/embargopolicy.html" title="PLoS Embargo Policy" tabindex="504">Media Inquiries</a></li>
		<li><a href="http://www.plos.org/journals/print.html" title="PLoS in Print" tabindex="505">PLoS in Print</a></li>
		<@s.url namespace="/static" action="sitemap" includeParams="none" id="siteMapURL" />
		<li><a href="${siteMapURL}" title="Site Map" tabindex="506">Site Map</a></li>
		<li><a href="http://www.plos.org" title="PLoS.org" tabindex="507">PLoS.org</a></li>
	</ul>
	<div class="powered">
	<ul>
		<li><a href="/static/releaseNotes.action" title="Topaz | Release Notes">RC 0.8.2.1 beta</a></li>
		<li>Managed Colocation provided by <a href="http://www.unitedlayer.com/" title="UnitedLayer: Built on IP Services">UnitedLayer</a>.</li>

	</ul>
	</div>
