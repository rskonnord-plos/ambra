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
<#if rating.body.commentTitle?exists>
  <#assign commentTitle = rating.body.commentTitle>
<#else>
  <#assign commentTitle = '"Rating has no Title"'>
</#if>
<#if rating.body.commentValue?exists>
  <#assign commentText = rating.body.commentValue>
<#else>
  <#assign commentText = '"Rating has no Text"'>
</#if>
<html>
  <head>
    <title>Ambra: Administration: Rating Details</title>
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Rating Details</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a>
    </p>
    <hr />

    <#include "templates/messages.ftl">

    <fieldset>
      <legend><b>Rating Details</b></legend>
      <table width="100%">
        <tr><td width="100px">&nbsp;</td><td/></tr>
        <tr><td>Id</td><td>${rating.body.id}</td></tr>
        <tr><td>Title</td><td>${commentTitle}</td></tr>
        <tr><td>Created</td><td>${rating.created?datetime}</td></tr>
        <tr><td>Creator</td><td><a href="../user/showUser.action?userId=${rating.creator}">${rating.creator}</a></td></tr>
        <tr>
          <td colspan="2">
            <fieldset>
              <legend><b>Content</b></legend>
              ${commentTitle}<br />
              ${commentText}
            </fieldset>
          </td>
        </tr>
      </table>
    </fieldset>
  </body>
</html>
