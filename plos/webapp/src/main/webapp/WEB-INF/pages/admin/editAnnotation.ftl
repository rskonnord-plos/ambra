<html>
  <head>
    <title>PLoS ONE: Administration: Edit Annotation</title>
  </head>
  <body>
    <h1 style="text-align: center">PLoS ONE: Administration: Edit Annotation</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr/>

    <#include "templates/messages.ftl">

    <fieldset>
      <legend>Load Annotation</legend>
      <@s.form name="loadAnnotationForm" action="editAnnotationLoad" namespace="/admin" method="post">
        <@s.textfield name="loadAnnotationId" label="Annotation Id" required="true" size="60"/>
        <@s.submit value="Load Annotation" />
      </@s.form>
    </fieldset>
    <br />

    <hr />

    <fieldset>
      <legend><b>Annotation Details</b></legend>
      <@s.form name="saveAnnotationForm" action="editAnnotationSave" namespace="/admin" method="post">
        <@s.textfield name="saveAnnotationContext" label="Context" required="true" value="${annotationContext}" size="100"/>
        <table>
          <tr>
            <td>Id</td>
            <td>
              ${annotationId}
              <@s.hidden name="saveAnnotationId" label="Id" required="true" value="${annotationId}"/>
            </td>
          </tr>
          <tr><td>Type</td><td>${annotationType}</td></tr>
          <tr><td>Created</td><td>${annotationCreated}</td></tr>
          <@s.url id="showUser" namespace="/user" action="showUser" userId="${annotationCreator}"/>
          <tr><td>Creator</td><td><@s.a href="${showUser}">${annotationCreator}</@s.a></td></tr>
          <tr><td>Annotates</td><td>${annotationAnnotates}</td></tr>
          <tr><td>Superseded By</td><td>${annotationSupersededBy}</td></tr>
          <tr><td>Supersedes</td><td>${annotationSupersedes}</td></tr>
          <tr><td>Title</td><td>${annotationTitle}</td></tr>
        </table>
        <@s.submit value="Save Annotation" />
      </@s.form>
    </fieldset>
  </body>
</html>
