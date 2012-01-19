<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>User Created!</title>
	</head>
	<body>
		User was created with ID: ${internalId}.
		<a href="${freemarker_config.getContext()}">Continue</a> on to PLoS ONE

    <br/>
    <@s.url id="displayUserURL" namespace="/user" action="displayUser" userId="${internalId}"/>
    <@s.a href="%{displayUserURL}">Display user info</@s.a><br/>

    <@s.url id="displayUserURL" namespace="/user/secure" action="displayPrivateFieldNames" userId="${internalId}"/>
    <@s.a href="%{displayUserURL}">Display user info</@s.a>

  </body>
</html>