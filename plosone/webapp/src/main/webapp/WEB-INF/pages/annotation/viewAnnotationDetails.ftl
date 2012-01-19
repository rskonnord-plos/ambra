<html>
	<head>
		<title>Annotation details</title>
	</head>

	<body>

    <fieldset>
        <legend>Annotation details</legend>

          id          =${annotation.id}            <br/>
          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.commentTitle}         <br/>
          body        =${annotation.comment}          <br/>
          bodyWithUrlLinking        =${annotation.commentWithUrlLinking}          <br/>
          context     =${annotation.context}       <br/>
          created     =${annotation.created}       <br/>
          creator     =${annotation.creator}       <br/>
          mediator    =${annotation.mediator}      <br/>
          type        =${annotation.type}          <br/>

          <@s.url id="createReplyURL" action="createReplySubmit" root="${annotation.id}" inReplyTo="${annotation.id}" namespace="/annotation/secure"/>
          <@s.a href="%{createReplyURL}">create reply</@s.a> <br/>

          <@s.url id="listReplyURL" action="listAllReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@s.a href="%{listReplyURL}">list all replies</@s.a> <br/>

          <@s.url id="listFlagURL" action="listAllFlags" target="${annotation.id}" />
          <@s.a href="%{listFlagURL}">list all flags</@s.a> <br/>

    </fieldset>

  </body>
</html>
