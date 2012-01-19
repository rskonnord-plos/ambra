<#-- depending on the current page, set banner zones -->

<#assign topLeft = 182>
<#assign topRight = 183>

<#if pgURL?contains('browse.action')>
	<#if pgURL?contains('field=date')>
		<#assign topRight = 219>
	<#else>
		<#assign topRight = 220>
	</#if>
<#elseif pgURL?contains('advancedSearch.action') || pgURL?contains('simpleSearch.action')>
	<#assign topLeft = 215>
	<#assign topRight = 216>
<#elseif pgURL?contains('article')>
	<#assign topLeft = 213>
	<#assign topRight = 214>
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
       document.write ("&#38;what=zone:${topLeft}&#38;source=PHUBCT&#38;block=1&#38;blockcampaign=1");
       document.write ("&#38;exclude=" + document.phpAds_used);
       if (document.referrer)
          document.write ("&#38;referer=" + escape(document.referrer));
       document.write ("'><" + "/script>");
    //-->
    </script><noscript><a href='http://ads.plos.org/adclick.php?n=a25fe88d' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:${topLeft}&#38;source=PHUBCT&#38;n=a25fe88d' border='0' alt=''/></a></noscript>
  </div>
  <!-- end : left banner slot -->
  <!-- begin : right banner slot -->
  <div class="right">
    <script language='JavaScript' type='text/javascript'>
    <!--
       if (!document.phpAds_used) document.phpAds_used = ',';
       phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

       document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
       document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
       document.write ("&#38;what=zone:${topRight}&#38;source=PHUBCT&#38;block=1&#38;blockcampaign=1");
       document.write ("&#38;exclude=" + document.phpAds_used);
       if (document.referrer)
          document.write ("&#38;referer=" + escape(document.referrer));
       document.write ("'><" + "/script>");
    //-->
    </script><noscript><a href='http://ads.plos.org/adclick.php?n=abc935c6' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:${topRight}&#38;source=PHUBCT&#38;n=abc935c6' border='0' alt=''/></a></noscript>
</div>
  <!-- end : right banner slot -->