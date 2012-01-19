<#-- depending on the current page, set banner zones -->

<#assign topLeft = 14>
<#assign topRight = 16>

<#if pgURL?contains('browse.action')>
	<#if pgURL?contains('field=date')>
		<#assign topRight = 170>
	<#else>
		<#assign topRight = 229>
	</#if>
<#elseif pgURL?contains('browseIssue.action') || pgURL?contains('browseVolume.action')>
	<#assign topRight = 169>
<#elseif pgURL?contains('advancedSearch.action') || pgURL?contains('simpleSearch.action')>
	<#assign topLeft = 234>
	<#assign topRight = 235>
<#elseif pgURL?contains('article')>
	<#assign topLeft = 100>
	<#assign topRight = 101>
</#if>

<!-- begin : left banner slot -->
<div class="left">
  <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
  <script language='JavaScript' type='text/javascript'>
  <!--
     if (!document.phpAds_used) document.phpAds_used = ',';
     phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
     
     document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
     document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
     document.write ("&#38;what=zone:14&#38;source=GEN&#38;block=1&#38;blockcampaign=1");
     document.write ("&#38;exclude=" + document.phpAds_used);
     if (document.referrer)
        document.write ("&#38;referer=" + escape(document.referrer));
     document.write ("'><" + "/script>");
  //-->
  </script><noscript><a href='http://ads.plos.org/adclick.php?n=a7ebb8d3' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:14&#38;source=GEN&#38;n=a7ebb8d3' border='0' alt=''/></a></noscript>
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
     document.write ("&#38;what=zone:16&#38;source=GEN&#38;block=1&#38;blockcampaign=1");
     document.write ("&#38;exclude=" + document.phpAds_used);
     if (document.referrer)
        document.write ("&#38;referer=" + escape(document.referrer));
     document.write ("'><" + "/script>");
  //-->
  </script><noscript><a href='http://ads.plos.org/adclick.php?n=acdf9a4b' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:16&#38;source=GEN&#38;n=acdf9a4b' border='0' alt=''/></a></noscript>
</div>
<!-- end : right banner slot -->
