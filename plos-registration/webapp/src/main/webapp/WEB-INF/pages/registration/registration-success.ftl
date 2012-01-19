<html>
    <head>
        <title>Verification email sent</title>
    </head>
    <body>
        <br/>
        <h3>Hello <@ww.property value="loginName1"/> : Please check your email account for further instructions to setup your account</h3>

<!--        <p>
          Your password: <@ww.property value="password1"/>
        </p>
-->
        <fieldset>
            <legend>Email body</legend>
            <p>

                Please click the following link to verify your email address:

                <@ww.url includeParams="none" id="emailVerificationURL" action="emailVerification">
                  <@ww.param name="loginName" value="user.loginName"/>
                  <@ww.param name="emailVerificationToken" value="user.emailVerificationToken"/>
                </@ww.url>
                <@ww.a href="%{emailVerificationURL}"  >${emailVerificationURL}</@ww.a>
            </p>
        </fieldset>

    </body>
</html>
