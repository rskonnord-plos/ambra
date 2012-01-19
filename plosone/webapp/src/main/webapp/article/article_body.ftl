<div id="content" class="article">
	<#include "article_rhc.ftl">

	<!-- begin : research article -->
	<form name="articleInfo" id="articleInfo" method="" action="">
		<input type="hidden" name="isAuthor" value="true" />
		<input type="hidden" name="authorIdList" value="" />
		<input type="hidden" name="userIdList" value="" />
		<input type="hidden" name="otherIdList" value="" />
		<input type="hidden" name="annotationId" value="${annotationId}" />
	</form>

	<div id="articleContainer">
		<#include "article_content.ftl">
	</div>

	<#include "/widget/annotation_add.ftl">

	<#include "/widget/commentDialog.ftl">
	
	<#include "/widget/loadingCycle.ftl">
</div>


