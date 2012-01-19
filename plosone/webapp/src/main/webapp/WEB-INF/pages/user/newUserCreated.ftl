<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>User Created!</title>
	</head>
	<body>
		User was created with ID: ${internalId}.
		<a href="${freemarker_config.getContext()}">Continue</a> on to PLoS ONE

    <br/>
    <@ww.url id="displayUserURL" namespace="/user" action="displayUser" userId="${internalId}"/>
    <@ww.a href="%{displayUserURL}">Display user info</@ww.a><br/>

    <@ww.url id="displayUserURL" namespace="/user/secure" action="displayPrivateFieldNames" userId="${internalId}"/>
    <@ww.a href="%{displayUserURL}">Display user info</@ww.a>

  </body>
</html>