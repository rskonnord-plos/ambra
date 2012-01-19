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

          <@ww.url id="createReplyURL" action="createReplySubmit" root="${annotation.id}" inReplyTo="${annotation.id}" namespace="/annotation/secure"/>
          <@ww.a href="%{createReplyURL}">create reply</@ww.a> <br/>

          <@ww.url id="listReplyURL" action="listAllReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{listReplyURL}">list all replies</@ww.a> <br/>

          <@ww.url id="listFlagURL" action="listAllFlags" target="${annotation.id}" />
          <@ww.a href="%{listFlagURL}">list all flags</@ww.a> <br/>

    </fieldset>

  </body>
</html>
