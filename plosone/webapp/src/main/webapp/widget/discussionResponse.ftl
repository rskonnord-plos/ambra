<!-- begin : posting response -->
<div class="posting pane" id="DiscussionPanel">
	<h5>Post Your Response</h5>
	<div class="close btn" id="btnCancelResponse"><a href="#" title="Close this dialogue box">Cancel</a></div>
	<form name="discussionResponse" method="post" action="">
		<input type="hidden" name="commentTitle" value="" />
		<input type="hidden" name="comment" value="" />

		<div id="responseSubmitMsg" class="error"></div>
		
		<fieldset>
			<legend>Compose Your Response</legend>
		
			<label for="responseTitle"><span class="none">Enter your response title</span><!-- error message text <em>A title is required for all public comments</em>--></label>
			<input type="text" name="responseTitle" id="responseTitle" value="Enter your response title..." class="title" alt="Enter your response title..." />
			
			<label for="reponseArea"><span class="none">Enter your response</span><!-- error message style <em>Please enter your response</em>--></label>
			<textarea id="responseArea" title="Enter your response..." class="response" name="responseArea" >Enter your response...</textarea>
			
			<div class="btnwrap"><input name="post" value="Post" type="button" id="btnPostResponse" title="Click to Post Your Response"/></div>
			
		</fieldset>
	</form>
</div>
<!-- end : posting response -->
