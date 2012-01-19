<html>
    <head>
        <title>Plosone registration</title>
    </head>
    <body>
        <!--<h1>Forgot password</h1>-->

        <p>
            <fieldset>
                <legend>Forgot Password</legend>
                <@s.form method="post" name="forgotPasswordForm" action="forgotPasswordSubmit">
                  <@s.textfield name="loginName" label="Please enter your previously registered email address" />
                  <@s.submit value="i want to reset my password" />
                </@s.form>
            </fieldset>
        </p>
    </body>
</html>
