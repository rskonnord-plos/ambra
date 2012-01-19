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
<#-- depending on the current page, set banner zones -->

<#assign topRight = 35>

<#if pgURL?contains('browse.action')>
	<#if pgURL?contains('field=date')>
		<#assign topRight = 176>
	<#else>
		<#assign topRight = 208>
	</#if>
<#elseif pgURL?contains('browseIssue.action') || pgURL?contains('browseVolume.action')>
	<#assign topRight = 175>
<#elseif pgURL?contains('advancedSearch.action') || pgURL?contains('simpleSearch.action')>
	<#assign topRight = 211>
<#elseif pgURL?contains('article')>
	<#assign topRight = 40>
</#if>

<!-- begin : left banner slot -->
<div class="left">
  <iframe frameborder="0" scrolling="no" width="468px" height="60px" marginheight="0" marginwidth="0" src="http://comparenetworks.com/partnerAds/IFrameAd.aspx?adid=0&adtypeid=2&siteid=35&subsiteid=0&pagetypeid=0&pageid=0&subpageid=0">
  </iframe>
</div>
<!-- end : left banner slot -->
<!-- begin : right banner slot -->
<div class="right">
<script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
<script language='JavaScript' type='text/javascript'>
<!--
   if (!document.phpAds_used) document.phpAds_used = ',';
   phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
   
   document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
   document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
   document.write ("&amp;what=zone:${topRight}&amp;source=ONE&amp;target=_top&amp;block=1&amp;blockcampaign=1");
   document.write ("&amp;exclude=" + document.phpAds_used);
   if (document.referrer)
      document.write ("&amp;referer=" + escape(document.referrer));
   document.write ("'><" + "/script>");
//-->
</script><noscript><a href='http://ads.plos.org/adclick.php?n=acfd0f5a' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:${topRight}&amp;source=ONE&amp;n=acfd0f5a' border='0' alt='' /></a></noscript>	
</div>
<!-- end : right banner slot -->
