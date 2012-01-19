<html>
	<head>
		<title>Images you can view</title>
	</head>

	<body>
    <fieldset>
      <legend>Available images</legend>

      <#list secondaryObjects as image>
        <@s.url id="imageThumbUrl"  action="fetchObject" uri="${image.uri}"/>
        <@s.a href="%{imageThumbUrl}&representation=${image.repLarge}">
          <img src="${imageThumbUrl}&representation=${image.repSmall}" alt="${image.title}" height="100px" width="120px"/>
        </@s.a>

        <ul>
          <li>Title: ${image.title}</li>
          <li>Description: ${image.description}</li>
          <li>Uri: ${image.uri}</li>
          <ul>
            <#list image.representations as rep>
              <@s.url id="imageRepUrl"  action="fetchObject" uri="${image.uri}"/>
              <@s.a href="%{imageRepUrl}&representation=${rep.name}">${rep.name}-${rep.contentType}</@s.a>
            </#list>
          </ul>
        </ul>

        <br/>

      </#list>

    </fieldset>

  </body>
</html>
