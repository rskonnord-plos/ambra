<html>
	<head>
		<title>Annotation details</title>
	</head>

	<body>

    <fieldset>
        <legend><b>Reply details</b></legend>
			<table width="100%">
				<tr><td width="100px">&nbsp;</td><td/></tr>
				<tr><td>Id</td><td>${reply.id}</td></tr>
	          	<tr><td>Title</td><td>${reply.commentTitle}</td></tr>
	          	<tr><td>Created</td><td>${reply.createdAsDate?datetime}</td></tr>
	          	<tr><td>Creator</td><td><a href="../user/showUser.action?userId=${reply.creator}">${reply.creator}</a></td></tr>
			  	<tr/>
			  	<tr/>
			  	<tr><td colspan="2">
			  		<hr/>${reply.commentWithUrlLinking}
			  	</td></tr>
		  	</table>
    </fieldset>
  </body>
</html>
