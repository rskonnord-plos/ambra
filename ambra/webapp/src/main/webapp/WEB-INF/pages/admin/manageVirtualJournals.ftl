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
    <title>Ambra: Administration: Manage Virtual Journals</title>
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration: Manage Virtual Journals</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">
      <@s.a href="${adminTop}">Admin Top</@s.a></p>
    <hr/>

    <#include "templates/messages.ftl">

    <h2>${journal.key} (${journal.eissn!""})</h2>

    <@s.url id="manageVolumesIssues" namespace="/admin" action="manageVolumesIssues"
      journalKey="${journal.key}" journalEIssn="${journal.eissn}"/>
    <@s.a href="${manageVolumesIssues}">Manage Volumes and Issues</@s.a><br />
    <br />
    <!-- TODO: display rules in a meaningful way  -->
    Smart Collection Rules: ${journal.smartCollectionRulesDescriptor!""}<br />
    <br />
    <@s.form id="manageVirtualJournals_${journal.key}"
      name="manageVirtualJournals_${journal.key}" action="manageVirtualJournals" method="post"
      namespace="/admin">
      <@s.hidden name="journalToModify" label="Journal to Modify" required="true"
        value="${journal.key}"/>
      <table border="1" cellpadding="2" cellspacing="0">
        <tr>
          <td>
            <#if journal.image?exists>
              <@s.url id="journalImage" action="fetchObject" namespace="/article"
                uri="${journal.image}.g001" representation="PNG_S" includeParams="none"/>
              <#assign altText="Journal Image" />
            <#else>
              <@s.url id="journalImage" value="" />
              <#assign altText="Journal Image null" />
            </#if>
            <img src="${journalImage}" alt="${altText}" height="120" width="120"/>
          </td>
          <td>
            <@s.textfield name="journalImage" value="${journal.image!''}"
              label="Image (URI)" size="42" />
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <@s.textfield name="currentIssue" value="${journal.currentIssue!''}"
              label="Current Issue (DOI)" size="42" />
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <@s.textfield name="volumes" label="Volumes" size="96" value="${journal.volumes!''}"/>
          </td>
        </tr>
        <tr><th colspan="2">Articles</th></tr>
        <tr>
          <td colspan="2"><@s.textfield name="articlesToAdd" label="Add" size="100"/></td>
        </tr>
        <#list journal.simpleCollection as articleUri>
          <@s.url id="fetchArticle" namespace="/article" action="fetchArticle" articleURI="${articleUri}"/>
          <tr>
            <td>
              <@s.checkbox label="Delete" name="articlesToDelete" fieldValue="${articleUri}"/>
            </td>
            <td><@s.a href="${fetchArticle}">${articleUri}</@s.a></td>
          </tr>
        </#list>
      </table>
      <br />
      <@s.submit value="Modify ${journal.key}"/>
    </@s.form>
  </body>
</html>
