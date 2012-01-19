<html>
<body>
<@ww.form name="feedbackForm" cssClass="pone-form" action="feedback" method="post" title="Feedback">
  <@ww.hidden name="page"/>
  <tr>
    <td>Name:</td>
    <td>
      <@ww.textfield name="name" size="50" required="true"/>
    </td>
  </tr>
  <tr>
    <td>E-mail address:</td>
    <td>
      <@ww.textfield name="fromEmailAddress" size="50" required="true"/>
    </td>
  </tr>
  <tr>
    <td>Subject:</td>
    <td>
      <@ww.textfield name="subject" size="50"/>
    </td>
  </tr>
  <tr>
    <td>Message:</td>
    <td>
      <@ww.textarea name="note" cols="50" rows="5" value="%{'love you. love you. love you'}"/>
    </td>
  </tr>
  <@ww.submit value="Submit Feedback"/>
</@ww.form>
</body>
</html>