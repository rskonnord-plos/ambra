<html>
	<head>
		<title>Flags list</title>
	</head>

	<body>

    <fieldset>
        <legend>Available flags</legend>

        <#list flags as flag>
          id          =
          <@s.url id="getFlagURL" action="getFlag" flagId="${flag.id}"/>
          <@s.a href="%{getFlagURL}">${flag.id}</@s.a> <br/>

          annotates   =${flag.annotates}     <br/>
          comment       =${flag.comment}         <br/>
          reasonCode       =${flag.reasonCode}         <br/>
          creator     =${flag.creator}       <br/>

          <br/>
      
          <@s.url id="deleteFlagURL" action="deleteFlag" flagId="${flag.id}" namespace="/annotation/secure" />
          <@s.a href="%{deleteFlagURL}">delete</@s.a><br/>
          <hr/>
        </#list>

    </fieldset>

  </body>
</html>
