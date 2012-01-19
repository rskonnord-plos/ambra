</div>
<!-- end : container -->	

<!-- begin : footer -->
<div id="ftr">
<#include "global_footer.ftl">
</div>
<!-- end : footer -->
<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-338393-1";
  <#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
  <#if journalContext = "PLoSClinicalTrials" >
_udn = "clinicaltrials.ploshubs.org";
  <#elseif journalContext = "PLoSCompBiol" >
_udn = "www.ploscompbiol.org";
  <#elseif journalContext = "PLoSGenetics" >
_udn = "www.plosgenetics.org";
  <#elseif journalContext = "PLoSNTD" >
_udn = "www.plosntds.org";
  <#elseif journalContext = "PLoSPathogens" >
_udn = "www.plospathogens.org";
  <#else>
_udn = "www.plosone.org";
  </#if>
  <#-- END HACK -->
urchinTracker();
</script>
</body>
</html>
