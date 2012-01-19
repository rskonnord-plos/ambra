<html>
    <head>
        <title>Reset password email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@s.property value="loginName"/> : Please check your email account for further instructions to reset your password.</h3>

        <fieldset>
            <legend>Email body</legend>
            <p>
              Please click the following link to verify your email address:

              <@s.url id="forgotPasswordEmailURL" action="forgotPasswordVerify">
                <@s.param name="loginName" value="user.loginName"/>
                <@s.param name="resetPasswordToken" value="user.resetPasswordToken"/>
              </@s.url>
              <@s.a href="%{forgotPasswordEmailURL}"  >${forgotPasswordEmailURL}</@s.a>
            </p>
        </fieldset>

    </body>
</html>
