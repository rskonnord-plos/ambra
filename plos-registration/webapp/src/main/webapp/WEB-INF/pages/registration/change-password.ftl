<html>
    <head>
        <title>Change password</title>
    </head>
    <body>
        <br/>
        <fieldset>
            <legend>Change password</legend>
            <p>

              <@s.form method="post" name="changePasswordForm" action="changePasswordSubmit">
                <@s.textfield name="loginName" label="Enter your registered email address" />
                <@s.password name="oldPassword" label="Enter your old password" />
                <@s.password name="newPassword1" label="Enter your new password" />
                <@s.password name="newPassword2" label="Enter your new password again" />
                <@s.submit value="change my password" />
              </@s.form>

            </p>
        </fieldset>

    </body>
</html>
