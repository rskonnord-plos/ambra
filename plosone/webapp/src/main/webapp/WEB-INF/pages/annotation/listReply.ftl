<html>
	<head>
		<title>Available replies</title>
	</head>

	<body>

    <fieldset>
      <legend>Available replies</legend>

      <#list replies as reply>
          id          =
          <@ww.url id="getReplyURL" action="getReply" replyId="${reply.id}"/>
          <@ww.a href="%{getReplyURL}">${reply.id}</@ww.a> <br/>

          inReplyTo   =${reply.inReplyTo}     <br/>
          root        =${reply.root}     <br/>
          title       =${reply.commentTitle}         <br/>
          creator     =${reply.creator}       <br/>

      <ul>
        <li>
          <@ww.url id="deleteReplyURL" action="deleteReply" id="${reply.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deleteReplyURL}">delete</@ww.a>
        </li>

        <li>
          <@ww.url id="listReplyURL" action="listAllReplies" root="${reply.root}" inReplyTo="${reply.id}"/>
          <@ww.a href="%{listReplyURL}">list all replies</@ww.a>
        </li>

        <li>
          <fieldset>
              <legend>Create an reply</legend>
              <@ww.form name="createReplyForm" action="createReplySubmit" namespace="/annotation/secure">
                <@ww.textfield name="root" label="What is the root of this reply" value="${reply.root}" required="true" size="50"/>
                <@ww.textfield name="inReplyTo" label="What is it in reply to" value="${reply.id}" required="true" size="50"/>
                <@ww.textfield name="commentTitle" label="Title"/>
                <@ww.textarea name="comment" label="Reply text" rows="'3'" cols="'30'" required="true"/>
                <@ww.submit value="create reply" />
              </@ww.form>
          </fieldset>
        </li>

        <li>
          <@ww.url id="listFlagURL" action="listFlags" target="${reply.id}" />
          <@ww.a href="%{listFlagURL}">list flags</@ww.a> <br/>
        </li>

        <li>
          <fieldset>
              <legend>Create a flag</legend>
              <@ww.form name="createFlagForm" action="createReplyFlagSubmit" method="get" namespace="/annotation/secure">
                <@ww.textfield name="target" label="What does it flag" value="${reply.id}" required="true" size="50"/>
                <@ww.select name="reasonCode" label="Reason"
                            list="{'spam', 'Offensive', 'Inappropriate'}"/>
                <@ww.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again....'}" rows="'3'" cols="'30'" required="true"/>
                <@ww.submit value="create flag" />
              </@ww.form>
          </fieldset>
        </li>
        <li>
          <@ww.url id="unflagAnnotationURL" action="unflagAnnotation" targetId="${reply.id}" />
          <@ww.a href="%{unflagAnnotationURL}">unflag Reply</@ww.a> <br/>
        </li>
      </ul>
      <hr/>
      </#list>

    </fieldset>

  </body>
</html>
