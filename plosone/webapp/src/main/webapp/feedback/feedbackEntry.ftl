<div id="content">
	<p>Fields marked with an <span class="required">*</span> are required. </p>
	<@ww.form name="feedbackForm" cssClass="pone-form" action="feedback" method="post" title="Feedback" enctype="multipart/form-data">
	  <@ww.hidden name="page"/>
  	  <fieldset>
			<legend>Feedback</legend>
			<ol>
  	    <@ww.textfield label="Name: " name="name" size="50" required="true"/>
    	  <@ww.textfield label="E-mail Address: " name="fromEmailAddress" size="50" required="true"/>
      	<@ww.textfield label="Subject: " name="subject" required="true" size="50"/>
	      <@ww.textarea label="Message" name="note" required="true" cols="50" rows="5" />
			</ol>
	  <@ww.submit value="Submit Feedback"/>
	</@ww.form>
</div>