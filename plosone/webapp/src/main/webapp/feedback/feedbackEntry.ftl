<div id="content">
	<p>Fields marked with an <span class="required">*</span> are required. </p>
	<@s.form name="feedbackForm" cssClass="pone-form" action="feedback" method="post" title="Feedback" enctype="multipart/form-data">
	  <@s.hidden name="page"/>
  	  <fieldset>
			<legend>Feedback</legend>
			<ol>
  	    <@s.textfield label="Name: " name="name" size="50" required="true"/>
    	  <@s.textfield label="E-mail Address: " name="fromEmailAddress" size="50" required="true"/>
      	<@s.textfield label="Subject: " name="subject" required="true" size="50"/>
	      <@s.textarea label="Message" name="note" required="true" cols="50" rows="5" />
			</ol>
	  <@s.submit value="Submit Feedback"/>
	</@s.form>
</div>