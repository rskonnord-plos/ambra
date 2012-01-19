<html>
  <head>
    <title>Create an annotation</title>
  </head>
  <body>
    <legend>Messages</legend>

    <fieldset>
      <p>
        <#list actionMessages as message>
        ${message} <br/>
      </#list>
      </p>
    </fieldset>

    <p>
      <fieldset>
          <legend>Create an reply</legend>
          <@ww.form name="createReplyForm" action="createReplySubmit">
            <@ww.textfield name="root" label="What is the root of this reply" required="true"/>
            <@ww.textfield name="inReplyTo" label="What is it in reply to" required="true"/>
            <@ww.textfield name="commentTitle" label="Title"/>
            <@ww.textarea name="comment" label="Reply text" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create reply" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
