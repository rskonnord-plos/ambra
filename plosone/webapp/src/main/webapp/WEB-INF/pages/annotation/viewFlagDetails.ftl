<html>
	<head>
		<title>Flag details</title>
	</head>

	<body>

    <fieldset>
        <legend>Flag details</legend>

          id          =${flag.id}            <br/>
          annotates   =${flag.annotates}     <br/>
          body        =${flag.comment}       <br/>
          bodyWithUrlLinking        =${flag.commentWithUrlLinking}          <br/>
          created     =${flag.created}       <br/>
          creator     =${flag.creator}       <br/>
          mediator    =${flag.mediator}      <br/>
          type        =${flag.type}          <br/>

          <@s.url id="createReplyURL" action="createReplySubmit" root="${flag.id}" inReplyTo="${flag.id}" namespace="/annotation/secure"/>
          <@s.a href="%{createReplyURL}">create reply</@s.a> <br/>

          <@s.url id="listReplyURL" action="listAllReplies" root="${flag.id}" inReplyTo="${flag.id}"/>
          <@s.a href="%{listReplyURL}">list all replies</@s.a> <br/>

    </fieldset>

  </body>
</html>
