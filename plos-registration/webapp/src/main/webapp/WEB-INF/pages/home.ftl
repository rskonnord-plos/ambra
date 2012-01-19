<html>
    <head>
        <title>Welcome</title>
    </head>
    <body>
        <h1>Welcome to the Plosone webapp</h1>

        <p>
            <fieldset>
                <legend>A few things for you to do</legend>
                <p>
                    <@s.url id="registerURL" action="register" />
                    <@s.a href="%{registerURL}">Register</@s.a>
                </p>

                <p>
                    <@s.url id="changePasswordURL" action="changePassword" />
                    <@s.a href="%{changePasswordURL}">Change Password</@s.a>
                </p>

                <p>
                    <@s.url id="forgotPasswordURL" action="forgotPassword" />
                    <@s.a href="%{forgotPasswordURL}">Forgot Password</@s.a>
                </p>
            </fieldset>
        </p>
    </body>
</html>
