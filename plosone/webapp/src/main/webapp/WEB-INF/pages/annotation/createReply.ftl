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
          <@s.form name="createReplyForm" action="createReplySubmit">
            <@s.textfield name="root" label="What is the root of this reply" required="true"/>
            <@s.textfield name="inReplyTo" label="What is it in reply to" required="true"/>
            <@s.textfield name="commentTitle" label="Title"/>
            <@s.textarea name="comment" label="Reply text" rows="'3'" cols="'30'" required="true"/>
            <@s.submit value="create reply" />
          </@s.form>
      </fieldset>
    </p>
  </body>
</html>
