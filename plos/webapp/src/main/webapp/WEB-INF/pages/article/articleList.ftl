<html>
  <head>
    <title>Articles you can view</title>
  </head>

  <body>
    <#list articles as article>
    <fieldset>
      <legend>Available articles</legend>
        <@s.url id="fetchArticleURL" action="fetchArticle" articleURI="${article}"/>
        <@s.a href="%{fetchArticleURL}">${article}</@s.a>

        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <@s.url id="fetchSecondaryObjectsURL" action="fetchSecondaryObjects" uri="${article}"/>
        <@s.a href="%{fetchSecondaryObjectsURL}">Images and more</@s.a>

        <ul>
          <li>
            <@s.url id="firstRepresentationUrl"  action="fetchFirstRepresentation" uri="${article}"/>
            <@s.a href="%{firstRepresentationUrl}">View first representation</@s.a>
          </li>
          <li>
            <@s.url id="articleArticleRepXML"  action="fetchObject" uri="${article}">
              <@s.param name="representation" value="%{'XML'}"/>
            </@s.url>
            <@s.a href="%{articleArticleRepXML}">View XML representation</@s.a>
          </li>
          <li>
            <@s.url id="articleArticleRepPDF"  action="fetchObject" uri="${article}">
              <@s.param name="representation" value="%{'PDF'}"/>
            </@s.url>
            <@s.a href="%{articleArticleRepPDF}">View PDF representation</@s.a>
          </li>
          <li>
            <@s.url id="annotationURL" includeContext="false" namespace="../annotation" action="listAnnotation" target="${article}"/>
            <@s.a href="%{annotationURL}">View Annotations for Article</@s.a>
          </li>
          <li>
            <@s.url id="annotatedArticleURL" action="fetchAnnotatedArticle" articleURI="${article}"/>
            <@s.a href="%{annotatedArticleURL}">Get Annotated Article XML</@s.a>
          </li>
          <li>
            <@s.url id="emailArticleURL" namespace="/article" action="emailThisArticleCreate" articleURI="${article}"/>
            <@s.a href="%{emailArticleURL}">Email this article</@s.a>
          </li>
          <li>
            <@s.url id="feedbackURL" action="feedbackCreate" page="${article}"/>
            <@s.a href="%{feedbackURL}">Send Feedback</@s.a>
          </li>
          <li>
            <@s.url id="getRatingsURL" action="getRatingsForUser" namespace="/rate/secure" articleURI="${article}"/>
            <@s.a href="%{getRatingsURL}">GetRatings</@s.a>
          </li>
          <li>
            <@s.url id="getAvgRatingsURL" action="getAverageRatings" namespace="/rate" articleURI="${article}"/>
            <@s.a href="%{getAvgRatingsURL}">Get Average Ratings</@s.a>
          </li>
          <li>
            <@s.url id="getTrackbacksURL" action="getTrackbacks" namespace="/" trackbackId="${article}"/>
            <@s.a href="%{getTrackbacksURL}">Get Trackbacks</@s.a>
          </li>
          </ul>

            <fieldset>
              <legend>Create an annotation</legend>
            <@s.form name="createAnnotationForm" action="createAnnotationSubmit" method="post" namespace="/annotation/secure" enctype="multipart/form-data">
              <!--enctype="multipart/form-data"-->
              <@s.textfield name="target" label="What does it annotate" value="${article}" required="true" size="50"/>
              <@s.textfield name="startPath" label="Start path" value="%{'id(\"x20060728a\")/p[1]'}" required="true"/>
              <@s.textfield name="startOffset" label="Start offset" value="%{'288'}" required="true"/>
              <@s.textfield name="endPath" label="End path" value="%{'id(\"x20060801a\")/h3[1]'}" required="true"/>
              <@s.textfield name="endOffset" label="End offset" value="%{'39'}" required="true"/>
              <@s.textfield name="commentTitle" label="Title" value="%{'title1'}"/>
              <@s.textfield name="supercedes" label="Older Annotation to supersede" value="%{'doi:anOlderAnnotation'}" size="50"/>
              <@s.checkbox name="isPublic" label="Is it Public?" fieldValue="true"/>
              <@s.textarea name="comment" label="Annotation text" value="%{'This article seems to cover the same grounds as this ...'}" rows="'3'" cols="'30'" required="true"/>
              <@s.submit value="create annotation" />
            </@s.form>
            </fieldset>
            <fieldset>
              <legend>Rate the Article</legend>
            <@s.form name="rateArticle Form" action="rateArticle" method="post" namespace="/rate/secure" enctype="multipart/form-data">
              <!--enctype="multipart/form-data"-->
              <@s.textfield name="insight" label="Insight Rating" size="1"/>
              <@s.textfield name="reliability" label="Reliability Rating" size="1"/>
              <@s.textfield name="style" label="Style Rating" size="1"/>
              <@s.textfield name="articleURI" label="Article URI" value="${article}"/>
              <@s.textfield name="commentTitle" label="Title" value="%{'title'}"/>
              <@s.textarea name="comment" label="Annotation text" value="%{'This article rocks'}" rows="'3'" cols="'30'" required="true"/>
              <@s.submit value="Submit Rating" />
            </@s.form>
            </fieldset>

            <fieldset>
              <legend>TrackbackPing the article</legend>
            <@s.form name="trackbackArticle Form" action="trackback" method="post" namespace="/" enctype="application/x-www-form-urlencoded;charset=utf-8">
              <!--enctype="multipart/form-data"-->
              <@s.textfield name="title" label="Title" value="Test Title"/>
              <@s.textfield name="excerpt" label="Excerpt" value="Excerpt here"/>
              <@s.textfield name="blog_name" label="Blog Name" value="Test Blog"/>
              <@s.textfield name="url" label="blog url" value="http://www.topazproject.org"/>
              <@s.textfield name="trackbackId" label="Trackback Id" value="${article}"/>
              <@s.submit value="Submit Ping" />
            </@s.form>
            </fieldset>
        </fieldset>
      </#list>
  </body>
</html>
