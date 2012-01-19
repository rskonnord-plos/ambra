<div id="discussionContainer">
  <#include "/rating/ratedComments.ftl">
</div>

<!-- TODO: need a refactor to work with all Anotea v. subclasses -->
<!-- need discussionResponse to avoid js errors -->
<#include "/widget/discussionResponse.ftl">
<#include "/widget/discussionFlag.ftl">
<#include "/widget/loadingCycle.ftl">

<#include "/widget/errorConsole.ftl">
