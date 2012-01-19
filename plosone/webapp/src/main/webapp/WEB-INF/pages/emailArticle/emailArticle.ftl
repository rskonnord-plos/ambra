<html>
<title>Email this article</title>
<body>

<@s.url id="fetchArticleURL" action="fetchArticle" articleURI="${articleURI}"/>
<b>Title:</b> <@s.a href="%{fetchArticleURL}">${title}</@s.a> <br/>

<b>Description :</b> ${description}
<@s.form name="emailThisArticle" cssClass="pone-form" action="emailThisArticleSubmit" namespace="/article" method="post" title="Email this article">
  <@s.hidden name="articleURI"/>
  <tr>
    <td>Recipient's E-mail address:</td>
    <td>
      <@s.textfield name="emailTo" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your E-mail address:</td>
    <td>
      <@s.textfield name="emailFrom" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your name:</td>
    <td>
      <@s.textfield name="senderName" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your comments to add to the E-mail:</td>
    <td>
      <@s.textarea name="note" cols="40" rows="5" value="%{'I thought you would find this article interesting.'}"/>
    </td>
  </tr>
  <@s.submit value="send"/>
</@s.form>
</body>
</html>