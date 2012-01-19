<html>
	<head>
		<title>Annotation details</title>
	</head>

	<body>

    <fieldset>
        <legend><b>Reply details</b></legend>
			<table width="100%">
				<tr><td width="100px">&nbsp;</td><td/></tr>
				<tr><td>Id</td><td>${replyInfo.id}</td></tr>
	          	<tr><td>Title</td><td>${replyInfo.title}</td></tr>
	          	<tr><td>Created</td><td>${replyInfo.created}</td></tr>
	          	<tr><td>Creator</td><td><a href="../user/showUser.action?userId=${replyInfo.creator}">${replyInfo.creator}</a></td></tr>
			  	<tr/>
			  	<tr/>
			  	<tr><td colspan="2">
			  		<hr/><iframe src="${replyInfo.body}"/>
			  	</td></tr>
		  	</table>
    </fieldset>
  </body>
</html>
