<html>
  <head>
    <title>Create an flag</title>
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
        TODO: remove as it is merged with listAnnotation.ftl
          <legend>Create a flag</legend>
          <@ww.form name="createFlagForm" action="createFlagSubmit" method="get">
            <@ww.textfield name="target" label="What does it flag" required="true"/>
            <@ww.select name="reasonCode" label="Reason"
                        list="{'spam', 'Offensive', 'Inappropriate'}"/> 
            <@ww.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again. Who wants more viagra...'}" rows="'3'" cols="'30'" required="true"/>
            <@ww.submit value="create flag" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
