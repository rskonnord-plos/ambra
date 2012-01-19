<html>
  <head>
    <title>Assign a admin role</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Assign a admin role to a topaz user</legend>
          <@ww.form name="assignAdminRoleToSubmitForm" action="assignAdminRoleToSubmit" namespace="/user/secure" method="post">
            <@ww.textfield name="topazId" label="Topaz Id" required="true"/>
            <@ww.submit value="assign admin role to this topaz user id" />
          </@ww.form>
      </fieldset>
    </p>
  </body>
</html>
