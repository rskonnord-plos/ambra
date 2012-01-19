<html>
  <head>
    <title>PLoS ONE: Administration: Reply Details</title>
  </head>
  <body>
    <h1 style="text-align: center">PLoS ONE: Administration: Reply Details</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr/>

    <#include "templates/messages.ftl">

    <fieldset>
      <legend><b>Reply Details</b></legend>
      <table width="100%">
        <tr><td width="100px">&nbsp;</td><td/></tr>
        <tr><td>Id</td><td>${reply.id}</td></tr>
        <tr><td>Title</td><td>${reply.commentTitle}</td></tr>
        <tr><td>Created</td><td>${reply.createdAsDate?datetime}</td></tr>
        <tr><td>Creator</td><td><a href="../user/showUser.action?userId=${reply.creator}">${reply.creator}</a></td></tr>
        <tr><td colspan="2"><hr/>${reply.commentWithUrlLinking}</td></tr>
      </table>
    </fieldset>
  </body>
</html>
