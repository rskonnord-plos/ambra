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
                    <@ww.url id="registerURL" action="register" />
                    <@ww.a href="%{registerURL}">Register</@ww.a>
                </p>

                <p>
                    <@ww.url id="changePasswordURL" action="changePassword" />
                    <@ww.a href="%{changePasswordURL}">Change Password</@ww.a>
                </p>

                <p>
                    <@ww.url id="forgotPasswordURL" action="forgotPassword" />
                    <@ww.a href="%{forgotPasswordURL}">Forgot Password</@ww.a>
                </p>

                <p>
                    <@ww.action name="constants" namespace="" id="otherConstants"/>
                    Plosone app url: ${otherConstants.get("mainAppUrl")}
                </p>


            </fieldset>
        </p>
    </body>
</html>
