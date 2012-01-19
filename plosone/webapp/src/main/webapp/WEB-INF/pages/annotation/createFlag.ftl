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
          <@s.form name="createFlagForm" action="createFlagSubmit" method="get">
            <@s.textfield name="target" label="What does it flag" required="true"/>
            <@s.select name="reasonCode" label="Reason"
                        list="{'spam', 'Offensive', 'Inappropriate'}"/> 
            <@s.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again. Who wants more viagra...'}" rows="'3'" cols="'30'" required="true"/>
            <@s.submit value="create flag" />
          </@s.form>
      </fieldset>
    </p>
  </body>
</html>
