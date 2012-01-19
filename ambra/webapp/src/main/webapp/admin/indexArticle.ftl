<#--
 $HeadURL$
 $Id$

 Copyright (c) 2006-2010 by Topaz, Inc.
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
<#include "includes/globals.ftl">
<html>
<head>
  <title>Manage Article Search Index</title>
<#include "includes/header.ftl">
</head>
<body>
  <h1 style="text-align: center">Manage Article Search Index</h1>
  <#include "includes/navigation.ftl">

  <@messages />

  <!-- XPub and article URI -->
  <fieldset>
    <legend>Article ID</legend>
    <@s.form method="post" namespace="/admin" action="indexArticle"
    name="indexArticle" id="indexArticle" >
      <@s.textfield name="articleId"  size="50"/>
      <@s.submit value="Re-Index"/>
    </@s.form>
  </fieldset>
</body>
</html>
