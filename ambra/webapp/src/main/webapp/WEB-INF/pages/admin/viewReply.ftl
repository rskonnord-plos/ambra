<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<html>
  <head>
    <title>Ambra: Administration: Reply Details</title>
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Reply Details</h1>

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
