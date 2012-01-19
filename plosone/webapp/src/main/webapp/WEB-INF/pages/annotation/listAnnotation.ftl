<html>
	<head>
		<title>Annotations list</title>
	</head>

	<body>

    <fieldset>
      <legend>Available annotations</legend>

      <#list annotations as annotation>
          id          =
          <@ww.url id="getAnnotationURL" includeParams="none" action="getAnnotation" annotationId="${annotation.id}"/>
          <@ww.a href="%{getAnnotationURL}">${annotation.id}</@ww.a> <br/>
          annotates   =${annotation.annotates}     <br/>
          title       =${annotation.commentTitle}         <br/>
          creator     =${annotation.creator}       <br/>
          context     =${annotation.context!""}       <br/>

      <ul>
        <li>
          <@ww.url id="listReplyURL" action="listAllReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{listReplyURL}">list all replies</@ww.a> <br/>
        </li>

        <li>
          <@ww.url id="listThreadedRepliesURL" action="listThreadedReplies" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{listThreadedRepliesURL}">list threaded replies</@ww.a> <br/>
        </li>

        <li>
          <@ww.url includeParams="none" id="listThreadURL" action="listThread" root="${annotation.id}" inReplyTo="${annotation.id}"/>
          <@ww.a href="%{listThreadURL}">list threaded replies (new - plosone)</@ww.a> <br/>
        </li>

        <li>
          <fieldset>
              <legend>Create an reply</legend>
              <@ww.form name="createReplyForm" action="createReplySubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
                <@ww.textfield name="root" label="What is the root of this reply" value="${annotation.id}" required="true" size="50"/>
                <@ww.textfield name="inReplyTo" label="What is it in reply to" value="${annotation.id}" required="true" size="50"/>
                <@ww.textfield name="commentTitle" label="Title"/>
                <@ww.textarea name="comment" label="Reply text" rows="'3'" cols="'30'" required="true" value="%{'a reply to an annotation'}"/>
                <@ww.submit value="create reply" />
              </@ww.form>
          </fieldset>
        </li>
        <li>
          <@ww.url id="listFlagURL" action="listFlags" target="${annotation.id}" />
          <@ww.a href="%{listFlagURL}">list flags</@ww.a> <br/>
        </li>
        <li>
          <fieldset>
              <legend>Create a flag</legend>
              <@ww.form name="createFlagForm" action="createAnnotationFlagSubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
                <@ww.textfield name="target" label="What does it flag" value="${annotation.id}" required="true" size="50"/>
                <@ww.select name="reasonCode" label="Reason"
                            list="{'spam', 'Offensive', 'Inappropriate'}"/>
                <@ww.textarea name="comment" label="Flag text" value="%{'Spammer guy attacks again....'}" rows="'3'" cols="'30'" required="true"/>
                <@ww.submit value="create flag" />
              </@ww.form>
          </fieldset>
        </li>
        <li>
          <@ww.url id="makePublicAnnotationURL" action="setAnnotationPublic" targetId="${annotation.id}" namespace="/annotation/secure"/>
          <@ww.a href="%{makePublicAnnotationURL}">Set Annotation as public</@ww.a> <br/>
        </li>
        <li>
          <@ww.url id="unflagAnnotationURL" action="unflagAnnotation" targetId="${annotation.id}" />
          <@ww.a href="%{unflagAnnotationURL}">unflag Annotation</@ww.a> <br/>
        </li>
        <li>
          <@ww.url id="deletePrivateAnnotationURL" action="deletePrivateAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deletePrivateAnnotationURL}">delete private annotation</@ww.a><br/>
        </li>
        <li>
          <@ww.url id="deletePublicAnnotationURL" action="deletePublicAnnotation" annotationId="${annotation.id}" namespace="/annotation/secure" />
          <@ww.a href="%{deletePublicAnnotationURL}">delete public annotation</@ww.a><br/>
        </li>
      </ul>
       <hr/>
      </#list>

    </fieldset>

  </body>
</html>
