<html>
	<head>
		<title>Articles you can view</title>
	</head>

	<body>
    <fieldset>
      <legend>Available articles</legend>

      <#list articles as article>

        <@ww.url id="fetchArticleURL" action="fetchArticle" articleURI="${article}"/>
        <@ww.a href="%{fetchArticleURL}">${article}</@ww.a>

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <@ww.url id="fetchSecondaryObjectsURL" action="fetchSecondaryObjects" uri="${article}"/>
        <@ww.a href="%{fetchSecondaryObjectsURL}">Images and more</@ww.a>

        <ul>
          <li>
            <@ww.url id="firstRepresentationUrl"  action="fetchFirstRepresentation" uri="${article}"/>
            <@ww.a href="%{firstRepresentationUrl}">View first representation</@ww.a>
          </li>
          <li>
            <@ww.url id="articleArticleRepXML"  action="fetchObject" uri="${article}">
              <@ww.param name="representation" value="%{'XML'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepXML}">View XML representation</@ww.a>
          </li>
          <li>
            <@ww.url id="articleArticleRepPDF"  action="fetchObject" uri="${article}">
              <@ww.param name="representation" value="%{'PDF'}"/>
            </@ww.url>
            <@ww.a href="%{articleArticleRepPDF}">View PDF representation</@ww.a>
          </li>
          <li>
            <@ww.url id="annotationURL" includeContext="false" namespace="../annotation" action="listAnnotation" target="${article}"/>
            <@ww.a href="%{annotationURL}">View Annotations for Article</@ww.a>
          </li>
          <li>
            <@ww.url id="annotatedArticleURL" action="fetchAnnotatedArticle" articleURI="${article}"/>
            <@ww.a href="%{annotatedArticleURL}">Get Annotated Article XML</@ww.a>
          </li>
          <li>
            <@ww.url id="emailArticleURL" namespace="/article" action="emailThisArticleCreate" articleURI="${article}"/>
            <@ww.a href="%{emailArticleURL}">Email this article</@ww.a>
          </li>
          <li>
            <@ww.url id="feedbackURL" action="feedbackCreate" page="${article}"/>
            <@ww.a href="%{feedbackURL}">Send Feedback</@ww.a>
          </li>
          <li>
            <fieldset>
              <legend>Create an annotation</legend>
            <@ww.form name="createAnnotationForm" action="createAnnotationSubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
              <!--enctype="multipart/form-data"-->
              <@ww.textfield name="target" label="What does it annotate" value="${article}" required="true" size="50"/>
              <@ww.textfield name="startPath" label="Start path" value="%{'id(\"x20060728a\")/p[1]'}" required="true"/>
              <@ww.textfield name="startOffset" label="Start offset" value="%{'288'}" required="true"/>
              <@ww.textfield name="endPath" label="End path" value="%{'id(\"x20060801a\")/h3[1]'}" required="true"/>
              <@ww.textfield name="endOffset" label="End offset" value="%{'39'}" required="true"/>
              <@ww.textfield name="commentTitle" label="Title" value="%{'title1'}"/>
              <@ww.textfield name="supercedes" label="Older Annotation to supersede" value="%{'doi:anOlderAnnotation'}" size="50"/>
              <@ww.checkbox name="isPublic" label="Is it Public?" fieldValue="true"/>
              <@ww.textarea name="comment" label="Annotation text" value="%{'This article seems to cover the same grounds as this ...'}" rows="'3'" cols="'30'" required="true"/>
              <@ww.submit value="create annotation" />
            </@ww.form>
            </fieldset>
          </li>

        </ul>

      </#list>

    </fieldset>

  </body>
</html>
