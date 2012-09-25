<#assign topRight = 35>
<#assign topLeft = 280>

<#if pgURL?contains('browse.action')>
  <#if pgURL?contains('field=date')>
    <#assign topRight = 176>
    <#assign topLeft = 285>
  <#else>
    <#assign topRight = 208>
    <#assign topLeft = 286>
  </#if>
<#elseif pgURL?contains('browseIssue.action') || pgURL?contains('browseVolume.action')>
  <#assign topRight = 175>
<#elseif pgURL?contains('advancedSearch.action') || pgURL?contains('simpleSearch.action')>
  <#assign topRight = 211>
  <#assign topLeft = 288>
<#elseif pgURL?contains('article')>
  <#assign topRight = 40>
  <#assign topLeft = 287>
</#if>

<!-- begin : left banner slot -->
<div class="left">
<@iFrameAd zone=topLeft id="aa97ff20" height="60" width="468" />
</div> <!-- end : left banner slot -->

<!-- begin : right banner slot -->
<div class="right">
<@iFrameAd zone=topRight id="acfd0f5a" height="60" width="468" />
</div> <!-- end : right banner slot -->