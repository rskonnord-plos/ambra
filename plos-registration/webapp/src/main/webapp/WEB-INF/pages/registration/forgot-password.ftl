<html>
    <head>
        <title>Plosone registration</title>
    </head>
    <body>
        <!--<h1>Forgot password</h1>-->

        <p>
            <fieldset>
                <legend>Forgot Password</legend>
                <@ww.form method="post" name="forgotPasswordForm" action="forgotPasswordSubmit">
                  <@ww.textfield name="loginName" label="Please enter your previously registered email address" />
                  <@ww.submit value="i want to reset my password" />
                </@ww.form>
            </fieldset>
        </p>
    </body>
</html>
