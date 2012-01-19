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
<#include "/journals/plosJournals/global/user_top.ftl">

<!-- begin : main content -->
<#include "${templateFile}">
<!-- end : main contents -->

</div>
<!-- end : container -->	

<!-- begin : footer -->
<div id="ftr">
<#include "/journals/plosJournals/global/user_footer.ftl">
</div>
<!-- end : footer -->
<#include "/javascript/global_js.ftl">
<#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
<#if journalContext = "PLoSClinicalTrials" >
  <#include "/journals/clinicalTrials/global/google.ftl">
<#elseif journalContext = "PLoSCompBiol" >
  <#include "/journals/compbiol/global/google.ftl">
<#elseif journalContext = "PLoSGenetics" >
  <#include "/journals/genetics/global/google.ftl">
<#elseif journalContext = "PLoSNTD" >
  <#include "/journals/ntd/global/google.ftl">
<#elseif journalContext = "PLoSONE" >
  <#include "/journals/plosone/global/google.ftl">
<#elseif journalContext = "PLoSPathogens" >
  <#include "/journals/pathogens/global/google.ftl">
<#else>
  <#include "/global/google.ftl">
</#if>
<#-- END MAJOR HACK -->
</body>
</html>


