<html>
    <head>
        <title>Verification email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@s.property value="loginName1"/> : Please check your email account for further instructions to setup your account</h3>

<!--        <p>
          Your password: <@s.property value="password1"/>
        </p>
-->
        <fieldset>
            <legend>Email body</legend>
            <p>

                Please click the following link to verify your email address:

                <@s.url includeParams="none" id="emailVerificationURL" action="emailVerification">
                  <@s.param name="loginName" value="user.loginName"/>
                  <@s.param name="emailVerificationToken" value="user.emailVerificationToken"/>
                </@s.url>
                <@s.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@s.a>
            </p>
        </fieldset>

    </body>
</html>
