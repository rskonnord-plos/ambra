<div id="content">
	<h1>View and Join Ongoing Discussions</h1>
	<@s.url namespace="/article" includeParams="none" id="articleURL" action="fetchArticle" articleURI="${articleInfo.id}"/>
	<@s.url namespace="/annotation/secure" includeParams="none" id="startDiscussionUrl" action="startDiscussion" target="${articleInfo.id}"/>

	<div class="source">
		<span>Original Article</span><a href="${articleURL}" title="Back to original article" class="article icon">${articleInfo.dublinCore.title}</a>
	</div>
	<table class="directory" cellpadding="0" cellspacing="0">
	<#if commentary?size == 0>
	<p>There are currently no notes or comments yet on this article. 
	You can <a href="${startDiscussionUrl}" title="Click to make a new comment on this article" class="discuss icon">add a comment</a> or return to the original article to add a note.<p>
	<#else>


	<#list commentary as comment>
    <#if ((comment.annotation.context)!"")?length == 0>
   		<#assign class="discuss"/>
	 	<#else>
  		<#assign class="annotation"/>
	 	</#if>
	 	<#assign numReplies = comment.numReplies>
	 	<#if numReplies != 1>
	 		<#assign label = "responses">
	 	<#else>
	 		<#assign label = "response">
	 	</#if>
  	<@s.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${comment.annotation.id}" inReplyTo="${comment.annotation.id}"/>
  	<@s.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${comment.annotation.creator}"/>
		<td class="replies">${comment.numReplies} ${label}<br /></td>
		<td class="title"><a href="${listThreadURL}" title="View Full Discussion Thread" class="${class} icon">${comment.annotation.commentTitle}</a></td>
		<td class="info">Posted by <a href="${showUserURL}" title="Discussion Author" class="user icon">${comment.annotation.creatorName}</a> on <strong>${comment.annotation.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></td>
	</tr>
	<tr><td colspan="4" class="last">Most recent response on <strong>${comment.lastModifiedAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></td>
	<tr>
	</#list>
	</#if>
	</table>
	
	<#if commentary?size gt 0>
	<p>You can also <a href="${startDiscussionUrl}" title="Click to make a new comment on this article" class="discuss icon">make a new comment</a> on this article.</p>
  </#if>
</div>
