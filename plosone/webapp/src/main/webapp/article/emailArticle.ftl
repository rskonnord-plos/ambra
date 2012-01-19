<div id="content">
	<h1>E-mail this Article</h1>
	<@s.url id="fetchArticleURL" action="fetchArticle" articleURI="${articleURI}"/>
	<div class="source">
		<@s.a href="%{fetchArticleURL}" title="Back to original article" class="article icon">${title}</@s.a>
	</div>						
	<p>Fields marked with an <span class="required">*</span> are required. </p>
	<@s.form name="emailThisArticle" cssClass="pone-form" action="emailThisArticleSubmit" namespace="/article" method="post" title="E-mail this article" enctype="multipart/form-data">
	  <@s.hidden name="articleURI"/>
 	  <@s.hidden name="title"/>
	  <@s.hidden name="journalName" value="${freemarker_config.getDisplayName(journalContext)}"/>
		<fieldset>
			<legend>Complete this form</legend>
			<ol>
          <@s.textarea rows="${maxEmails}" label="Recipients' E-mail addresses (one per line, max ${maxEmails})" required="true" name="emailTo" size="40" />
 		      <@s.textfield label="Your E-mail address" required="true" name="emailFrom" size="40" />
 		      <@s.textfield label="Your name" required="true" name="senderName" size="40" />					
 		      <@s.textarea label="Your comments to add to the E-mail" value="%{'I thought you would find this article interesting.'}" name="note" rows="5" cols="40"/>					
			</ol>
		  <@s.submit value="Send"/>
		</fieldset>
	</@s.form>	
	<@s.url id="privacyURL" includeParams="none" namespace="/static" action="privacy"/>
	<p class="citation"><a href="${privacyURL}">Privacy Statement</a></p>
</div>
