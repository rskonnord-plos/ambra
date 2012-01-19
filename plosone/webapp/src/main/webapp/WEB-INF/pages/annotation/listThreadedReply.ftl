<html>
	<head>
		<title>Available replies</title>
	</head>

	<body>

    <fieldset>
      <#macro writeReplyDetails reply>
        <fieldset>
            <legend>${reply.id}</legend>
              root        =${reply.root}          <br/>
          <span style="background:burlywood">
              inReplyTo   =${reply.inReplyTo}</span>     <br/>
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

              <@ww.url id="listThreadedRepliesURL" action="listThreadedReplies" root="${reply.root}" inReplyTo="${reply.id}"/>
              <@ww.a href="%{listThreadedRepliesURL}">list threaded replies</@ww.a> <br/>

              <@ww.url id="listFlagURL" action="listAllFlags" target="${reply.id}" />
              <@ww.a href="%{listFlagURL}">list all flags</@ww.a> <br/>
        </fieldset>
        <li>
          <ul>
            <#list reply.replies as subReply>
              <@writeReplyDetails subReply/>
            </#list>
          </ul>
        </li>
      </#macro>

      <ul>
        <#list replies as reply>
          <@writeReplyDetails reply/>
        </#list>
      </ul>
    </fieldset>

  </body>
</html>
