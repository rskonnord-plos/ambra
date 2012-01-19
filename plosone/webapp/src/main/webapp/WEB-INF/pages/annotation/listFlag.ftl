<html>
	<head>
		<title>Flags list</title>
	</head>

	<body>

    <fieldset>
        <legend>Available flags</legend>

        <#list flags as flag>
          id          =
          <@ww.url id="getFlagURL" action="getFlag" flagId="${flag.id}"/>
          <@ww.a href="%{getFlagURL}">${flag.id}</@ww.a> <br/>

          annotates   =${flag.annotates}     <br/>
          comment       =${flag.comment}         <br/>
          reasonCode       =${flag.reasonCode}         <br/>
          creator     =${flag.creator}       <br/>

          <br/>
      
          <@ww.url id="deleteFlagURL" action="deleteFlag" flagId="${flag.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deleteFlagURL}">delete</@ww.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
