<html>
    <head>
        <title>Reset password</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@s.property value="loginName"/></h3>

        <fieldset>
            <legend>Please enter a new password</legend>
            <p>
              <@s.form method="post" name="forgotPasswordChangePasswordForm" action="forgotPasswordChangePasswordSubmit">
                <@s.hidden name="loginName" />
                <@s.hidden name="resetPasswordToken" />
                <@s.password name="password1" label="Enter your new password" />
                <@s.password name="password2" label="Enter your new password again" />
                <@s.submit value="change my password" />
              </@s.form>

            </p>
        </fieldset>

    </body>
</html>


