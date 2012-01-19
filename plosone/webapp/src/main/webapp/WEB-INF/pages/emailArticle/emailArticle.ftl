<html>
<title>Email this article</title>
<body>

<@ww.url id="fetchArticleURL" action="fetchArticle" articleURI="${articleURI}"/>
<b>Title:</b> <@ww.a href="%{fetchArticleURL}">${title}</@ww.a> <br/>

<b>Description :</b> ${description}
<@ww.form name="emailThisArticle" cssClass="pone-form" action="emailThisArticleSubmit" namespace="/article" method="post" title="Email this article">
  <@ww.hidden name="articleURI"/>
  <tr>
    <td>Recipient's E-mail address:</td>
    <td>
      <@ww.textfield name="emailTo" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your E-mail address:</td>
    <td>
      <@ww.textfield name="emailFrom" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your name:</td>
    <td>
      <@ww.textfield name="senderName" size="40"/>
    </td>
  </tr>
  <tr>
    <td>Your comments to add to the E-mail:</td>
    <td>
      <@ww.textarea name="note" cols="40" rows="5" value="%{'I thought you would find this article interesting.'}"/>
    </td>
  </tr>
  <@ww.submit value="send"/>
</@ww.form>
</body>
</html>