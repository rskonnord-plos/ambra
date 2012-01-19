<html>
  <head>
    <title>The user profile fields with private visibility</title>
  </head>
  <body>
    <fieldset>
      <legend>The user profile fields with private visibility for topazId:${userId}</legend>
      <ul>
        <#list privateFields as fieldname>
          <li>${fieldname}</li>
        </#list>
      </ul>
    </fieldset>
  </body>
</html>
