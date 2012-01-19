<html>
  <head>
    <title>Assign a admin role</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Assign a admin role to a topaz user</legend>
          <@s.form name="assignAdminRoleToSubmitForm" action="assignAdminRoleToSubmit" namespace="/user/secure" method="post">
            <@s.textfield name="topazId" label="Topaz Id" required="true"/>
            <@s.submit value="assign admin role to this topaz user id" />
          </@s.form>
      </fieldset>
    </p>
  </body>
</html>
