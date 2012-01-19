<html>
	<head>
		<title>Annotations list</title>
	</head>

	<body>

    <fieldset>
      <legend>Available annotations</legend>

      <#list annotations as annotation>
          id          =
          <@s.url id="getAnnotationURL" includeParams="none" action="getAnnotation" annotationId="${annotation.id}"/>
          <@s.a href="%{getAnnotationURL}">${annotation.id}</@s.a> <br/>
          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.commentTitle}         <br/>
          creator     =${annotation.creator}       <br/>
          context     =${annotation.context!""}       <br/>

      <ul>
        <li>
          <@s.url id="listReplyURL" action="listAllReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@s.a href="%{listReplyURL}">list all replies</@s.a> <br/>
        </li>

        <li>
          <@s.url id="listThreadedRepliesURL" action="listThreadedReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@s.a href="%{listThreadedRepliesURL}">list threaded replies</@s.a> <br/>
        </li>

        <li>
          <@s.url includeParams="none" id="listThreadURL" action="listThread" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@s.a href="%{listThreadURL}">list threaded replies (new - plosone)</@s.a> <br/>
        </li>

        <li>
          <fieldset>
              <legend>Create an reply</legend>
              <@s.form name="createReplyForm" action="createReplySubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
                <@s.textfield name="root" label="What is the root of this reply" value="${annotation.id}" required="true" size="50"/>
                <@s.textfield name="inReplyTo" label="What is it in reply to" value="${annotation.id}" required="true" size="50"/>
                <@s.textfield name="commentTitle" label="Title"/>
                <@s.textarea name="comment" label="Reply text" rows="'3'" cols="'30'" required="true" value="%{'a reply to an annotation'}"/>
                <@s.submit value="create reply" />
              </@s.form>
          </fieldset>
        </li>
        <li>
          <@s.url id="listFlagURL" action="listFlags" target="${annotation.id}" />
          <@s.a href="%{listFlagURL}">list flags</@s.a> <br/>
        </li>
        <li>
          <fieldset>
              <legend>Create a flag</legend>
              <@s.form name="createFlagForm" action="createAnnotationFlagSubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
                <@s.textfield name="target" label="What does it flag" value="${annotation.id}" required="true" size="50"/>
                <@s.select name="reasonCode" label="Reason"
                            list="{'spam', 'Offensive', 'Inappropriate'}"/>
                <@s.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again....'}" rows="'3'" cols="'30'" required="true"/>
                <@s.submit value="create flag" />
              </@s.form>
          </fieldset>
        </li>
        <li>
          <@s.url id="makePublicAnnotationURL" action="setAnnotationPublic" targetId="${annotation.id}" namespace="/annotation/secure"/>
          <@s.a href="%{makePublicAnnotationURL}">Set Annotation as public</@s.a> <br/>
        </li>
        <li>
          <@s.url id="unflagAnnotationURL" action="unflagAnnotation" targetId="${annotation.id}" />
          <@s.a href="%{unflagAnnotationURL}">unflag Annotation</@s.a> <br/>
        </li>
        <li>
          <@s.url id="deletePrivateAnnotationURL" action="deletePrivateAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@s.a href="%{deletePrivateAnnotationURL}">delete private annotation</@s.a><br/>
        </li>
        <li>
          <@s.url id="deletePublicAnnotationURL" action="deletePublicAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@s.a href="%{deletePublicAnnotationURL}">delete public annotation</@s.a><br/>
        </li>
      </ul>
       <hr/>
      </#list>

    </fieldset>

  </body>
</html>
