<html>
<body>
<@s.form name="feedbackForm" cssClass="pone-form" action="feedback" method="post" title="Feedback">
  <@s.hidden name="page"/>
  <tr>
    <td>Name:</td>
    <td>
      <@s.textfield name="name" size="50" required="true"/>
    </td>
  </tr>
  <tr>
    <td>E-mail address:</td>
    <td>
      <@s.textfield name="fromEmailAddress" size="50" required="true"/>
    </td>
  </tr>
  <tr>
    <td>Subject:</td>
    <td>
      <@s.textfield name="subject" size="50"/>
    </td>
  </tr>
  <tr>
    <td>Message:</td>
    <td>
      <@s.textarea name="note" cols="50" rows="5" value="%{'love you. love you. love you'}"/>
    </td>
  </tr>
  <@s.submit value="Submit Feedback"/>
</@s.form>
</body>
</html>