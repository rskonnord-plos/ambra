<html>
	<head>
		<title>Reply details</title>
	</head>

	<body>

    <fieldset>
        <legend>Reply details</legend>

          id          =${reply.id}            <br/>
          root        =${reply.root}          <br/>
          inReplyTo   =${reply.inReplyTo}     <br/>
          title       =${reply.commentTitle}         <br/>
          body        =${reply.comment}          <br/>
          created     =${reply.created}       <br/>
          creator     =${reply.creator}       <br/>
          mediator    =${reply.mediator}      <br/>
          type        =${reply.type}          <br/>

          <@ww.url id="createReplyURL" action="createReplySubmit" root="${reply.root}" inReplyTo="${reply.id}" namespace="/annotation/secure"/>
          <@ww.a href="%{createReplyURL}">create reply</@ww.a> <br/>

          <@ww.url id="listReplyURL" action="listAllReplies" root="${reply.root}" inReplyTo="${reply.id}"/>
          <@ww.a href="%{listReplyURL}">list all replies</@ww.a> <br/>

          <@ww.url id="listFlagURL" action="listAllFlags" target="${reply.id}" />
          <@ww.a href="%{listFlagURL}">list all flags</@ww.a> <br/>
    </fieldset>

  </body>
</html>
