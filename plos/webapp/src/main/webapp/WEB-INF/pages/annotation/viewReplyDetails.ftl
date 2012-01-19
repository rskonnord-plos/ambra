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

          <@s.url id="createReplyURL" action="createReplySubmit" root="${reply.root}" inReplyTo="${reply.id}" namespace="/annotation/secure"/>
          <@s.a href="%{createReplyURL}">create reply</@s.a> <br/>

          <@s.url id="listReplyURL" action="listAllReplies" root="${reply.root}" inReplyTo="${reply.id}"/>
          <@s.a href="%{listReplyURL}">list all replies</@s.a> <br/>

          <@s.url id="listFlagURL" action="listAllFlags" target="${reply.id}" />
          <@s.a href="%{listFlagURL}">list all flags</@s.a> <br/>
    </fieldset>

  </body>
</html>
