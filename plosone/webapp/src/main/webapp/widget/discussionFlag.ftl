<!-- begin : post response -->
<div class="flag pane" id="FlaggingPanel" >
	<div id="flagForm">
		<h5>Why should this posting be reviewed?</h5>
		<p>See also <a href="${freemarker_config.getContext()}/static/commentGuidelines.action" class="instructions">guidelines for commenting</a>.</p>
		<div class="close btn" id="btnCloseFlag"><a href="#" title="Close this dialogue box">Cancel</a></div>
		<form name="discussionFlag" method="post" action="">
			<input type="hidden" name="urlParam" value="" />
			<input type="hidden" name="comment" value="" />
			
			<div id="flagSubmitMsg" class="error"></div>
			
			<fieldset>
				<legend>Identify Reason for Flagging</legend>
				<ol class="radio">
					<li><label for="spam">Spam</label>
						<input type="radio" value="spam" name="reasonCode" checked /></li>
					<li><label for="offensive">Offensive</label>
						<input type="radio" value="offensive" name="reasonCode" /></li>
					<li><label for="inappropriate">Inappropriate</label>
						<input type="radio" value="inappropriate" name="reasonCode" /></li>
					<li><label for="other">Other</label>
						<input type="radio" value="other" name="reasonCode" /></li>
			
				</ol>
			
				<label for="reponse"><span class="none">Enter your response</span><!-- error message style <em>Please enter your response</em> --></label>
	
				<textarea id="responseArea" title="Add any additional information here..." class="response" name="response" >Add any additional information here...</textarea>
				
				
				
				<div class="btnwrap"><input name="submit" value="Submit" type="button" id="btnSubmit" title="Click to Submit Your Response"/></div>
				
			</fieldset>
		</form>
	</div>
	<div id="flagConfirm">
		<h5 class="flag icon">Thank You!</h5>
		<div class="close btn" id="btnFlagConfirmClose"><a href="#" title="Close this dialogue box">Close</a></div>
		<p>Thank you for taking the time to flag this posting; we review flagged postings on a regular basis.</p>
	</div>	
	
</div>
<!-- end : post response -->
