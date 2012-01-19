<html>
    <head>
        <title>The request failed</title>
    </head>
    <body>
        <br/>
        <h3>The request failed with the following error:</h3>

        <fieldset>
            <legend>Error message</legend>
            <p>

              <#list actionErrors as message>
                ${message} <br/>
              </#list>

            </p>
        </fieldset>

    </body>
</html>
