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

          <@ww.url id="createReplyURL" action="createReplySubmit" root="${flag.id}" inReplyTo="${flag.id}" namespace="/annotation/secure"/>
          <@ww.a href="%{createReplyURL}">create reply</@ww.a> <br/>

          <@ww.url id="listReplyURL" action="listAllReplies" root="${flag.id}" inReplyTo="${flag.id}"/>
          <@ww.a href="%{listReplyURL}">list all replies</@ww.a> <br/>

    </fieldset>

  </body>
</html>
