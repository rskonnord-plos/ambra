<div id="discussionContainer">
	<!-- begin : main content -->
	<div id="content">
		<h1>Start a Discussion</h1>
	
		<div class="source">
			<span>On the Article</span>
      <@s.url id="articlePageURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
      <@s.a href="%{articlePageURL}" title="Back to original article" cssClass="article icon">${articleInfo.dublinCore.title}</@s.a>
		</div>
	
		<div class="original response">
		
	
	<!-- begin : posting response -->
			<div class="posting pane" id="DiscussionPanel">
				<h5>Post Your Discussion Comment</h5>
				<div class="close btn" id="btnCancelResponse"><@s.a href="%{articlePageURL}" title="Cancel and go back to original article">Cancel</@s.a></div>
				<form name="discussionResponse" method="post" action="">
					<input type="hidden" name="target" value="${articleInfo.id}" />	
					<input type="hidden" name="commentTitle" value="" />
					<input type="hidden" name="comment" value="" />
          <input type="hidden" name="isPublic" value="true" />
					
					<div id="responseSubmitMsg" class="error"></div>
					
					<fieldset>
						<legend>Compose Your Response</legend>
					
						<label for="responseTitle"><span class="none">Enter your comment title</span><!-- error message text <em>A title is required for all public comments</em>--></label>
						<input type="text" name="responseTitle" id="responseTitle" value="Enter your comment title..." class="title" alt="Enter your comment title..." />
						
						<label for="responseArea"><span class="none">Enter your comment</span><!-- error message style <em>Please enter your response</em>--></label>
						<textarea id="responseArea" title="Enter your comment..." class="response" name="responseArea" >Enter your comment...</textarea>
						
						<div class="btnwrap"><input name="post" value="Post" type="button" id="btnPostResponse" title="Click to Post Your Response"/></div>
						
					</fieldset>
				</form>
				
			</div>
		</div>
		<!-- end : posting response -->
	</div>
	<!-- end : main contents -->
</div>

<#include "/widget/loadingCycle.ftl">

<#include "/widget/errorConsole.ftl">
