<#if Session.PLOS_ONE_USER?exists>
	<#assign loginURL = "#">
<#else>
	<#assign loginURL = "${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}">
</#if>
	<#macro writeReplyDetails reply replyToAuthorId replyToAuthorName>
  	<@ww.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${reply.creator}"/>
  	<@ww.url namespace="/user" includeParams="none" id="authorURL" action="showUser" userId="${replyToAuthorId}"/>
			<div class="response">
				<div class="hd">
					<!-- begin response title -->
					<h3>${reply.commentTitle}</h3>
					<!-- end : response title -->
					<!-- begin : response poster details -->
					<div class="detail">
						<a href="${showUserURL}" class="user icon">${reply.creatorName}</a> replied to <a href="${authorURL}" class="user icon">${replyToAuthorName}</a> on <strong>${reply.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></div>
					<!-- end : response poster details -->
				</div>
				<!-- begin : response body text -->
				<blockquote>
					${reply.commentWithUrlLinking}
				</blockquote>
				<!-- end : response body text -->
				<!-- begin : toolbar options -->
				<div class="toolbar">
					<ul>
						<li>
						<#if Session.PLOS_ONE_USER?exists>
							<a href="${loginURL}" onclick="topaz.responsePanel.show(this, _dcf, 'toolbar', '${reply.id}', null, null, 1); return false;" class="flag tooltip" title="Request Review">Request review</a>
						<#else>							
							<a href="${loginURL}" class="flag tooltip" title="Request review">Request review</a>						
						</#if>
						</li>
						<li>
						<#if Session.PLOS_ONE_USER?exists>
							<a href="${loginURL}" onclick="topaz.responsePanel.show(this, _dcr, 'toolbar', '${baseAnnotation.id}', '${reply.id}', '${reply.commentTitle?js_string}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
						<#else>
							<a href="${loginURL}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>						
						</#if>
						</li>
					</ul>
				</div>
				<!-- end : toolbar options -->
	      <#list reply.replies as subReply>
	        <@writeReplyDetails reply=subReply replyToAuthorId=reply.creator replyToAuthorName=reply.creatorName/>
	      </#list>
			</div>
	</#macro>
	
	<!-- begin : main content -->
	<div id="content">
		<h1>Annotation and Responses</h1>
		<div class="source">
			<span>Original Article</span>
			<a href="${freemarker_config.context}/article/fetchArticle.action?articleURI=${baseAnnotation.annotates}&annotationId=${baseAnnotation.id}" title="Back to original article" class="article icon">${articleInfo.title}</a>
		</div>
		<div class="response original">
			<div class="hd">
				<!-- begin response title -->
				<h3>${baseAnnotation.commentTitle}</h3>
				<!-- end : response title -->
				<!-- begin : response poster detail -->
				<@ww.url namespace="/user" includeParams="none" id="baseAuthorURL" action="showUser" userId="${baseAnnotation.creator}"/>
				
				<div class="detail">Posted by <a href="${baseAuthorURL}" title="Annotation Author" class="user icon">${baseAnnotation.creatorName}</a> on <strong>${baseAnnotation.createdAsDate?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong>
				</div>
				<!-- end : response poster details -->
			</div>
			<!-- begin : response body text -->
			<blockquote>
				${baseAnnotation.commentWithUrlLinking}
			</blockquote>
			<!-- end : response body text -->
			<!-- begin : toolbar options -->
			<div class="toolbar">
				<ul>
					<li>
					<#if Session.PLOS_ONE_USER?exists>
						<a href="${loginURL}" onclick="topaz.responsePanel.show(this, _dcf, 'toolbar', '${baseAnnotation.id}', null, null, 0); return false;" class="flag tooltip" title="Request review">Request review</a>
					<#else>
						<a href="${loginURL}" class="flag tooltip" title="Request review">Request review</a>					
					</#if>
					</li>
					<li>
					<#if Session.PLOS_ONE_USER?exists>
						<a href="${loginURL}" onclick="topaz.responsePanel.show(this, _dcr, 'toolbar', '${baseAnnotation.id}', '${baseAnnotation.id}', '${baseAnnotation.commentTitle?js_string}'); return false;" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					<#else>
						<a href="${loginURL}" class="respond tooltip" title="Click to respond">Respond to this Posting</a>
					</#if>
					</li>
				</ul>
			</div>
			<!-- end : toolbar options -->
		</div>
		<!-- begin : response note that all responses TO this response get enclosed within this response container  -->
	  <#list replies as reply>
		  <@writeReplyDetails reply=reply replyToAuthorId=baseAnnotation.creator replyToAuthorName=baseAnnotation.creatorName/>
	  </#list>
		<!-- end : response -->
	</div>
	
			    <@ww.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${baseAnnotation.annotates}"/>
				<p><a href="${commentsURL}" class="commentary icon">See all commentary</a> on this article</p>
	
	<!-- end : main contents -->




