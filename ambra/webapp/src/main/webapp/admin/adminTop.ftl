<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2009 by Topaz, Inc.
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
    <title>Ambra: Administration</title>
    <script type="text/javascript">
      /**
       * js in support of admin UI.
       *
       * @author jsuttor
       */
      function SetAllCheckBoxes(FormName, FieldName, CheckValue) {

        // form must exist
        if (!document.forms[FormName]) { return; }

        // get all named fields
        var objCheckBoxes = document.forms[FormName].elements[FieldName];
        if (!objCheckBoxes) { return; }

        // set the check value for all check boxes
        var countCheckBoxes = objCheckBoxes.length;
        if (!countCheckBoxes) {
          objCheckBoxes.checked = CheckValue;
        } else {
          for (var i = 0; i < countCheckBoxes; i++) {
            objCheckBoxes[i].checked = CheckValue;
          }
        }
      }
    </script>
  </head>
  <body>
    <h1 style="text-align: center">Ambra: Administration</h1>

    <#include "templates/standardHeader.ftl">
    <#include "templates/messages.ftl">
    <#if uploadableFiles?has_content>
      <fieldset>
        <legend><b>Ingestable Archives</b></legend>
        <@s.form name="ingestArchives" action="ingestArchives" method="post" namespace="/admin">
          <#if (uploadableFiles.size() > 1)>
            <input type="button" value="Ingest All"
              onclick="SetAllCheckBoxes('ingestArchives', 'filesToIngest', true);"/>
            <br/>
          </#if>
          <#list uploadableFiles as file>
            <@s.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/><br/>
          </#list>
          <br/>
            <@s.checkbox name="force" label="Force ingestion even if article(s) already exist" fieldValue="true"/><br/>
          <br/>
          <@s.submit value="Ingest Selected Archives" />
        </@s.form>
      </fieldset>
      <br/>
    </#if>

    <#if publishableFiles?has_content>
      <fieldset>
        <legend><b>Publishable Documents</b></legend>
        <@s.form id="publishArchives" name="publishArchives" action="publishArchives" method="post" namespace="/admin">
          <table border="1" cellpadding="2" cellspacing="0">
            <tr>
              <th>Publish</th>
              <#list Request[freemarker_config.journalContextAttributeKey].virtualJournals as virtualJournal>
                <#if journal.key != virtualJournal>
                <th>${virtualJournal}</th>
                </#if>
              </#list>
              <th>Delete</th>
              <th>&nbsp;</th>
            </tr>
            <#if (publishableFiles.size() > 1)>
              <tr>
                <th>
                  <input type="button" value="Publish All"
                    onclick="SetAllCheckBoxes('publishArchives', 'articlesToPublish', true);"/>
                </th>
                <#list Request[freemarker_config.journalContextAttributeKey].virtualJournals as virtualJournal>
                  <#if journal.key != virtualJournal>
                  <th>&nbsp;</th>
                  </#if>
                </#list>
                <th>
                  <input type="button" value="Delete All"
                    onclick="SetAllCheckBoxes('publishArchives', 'articlesToDelete', true);"/>
                </th>
                <th>&nbsp;</th>
              </tr>
            </#if>
            <#list publishableFiles as article>
              <tr>
                <@s.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${article}"/>
                <td><@s.checkbox name="articlesToPublish" fieldValue="${article}"/></td>
                <#list Request[freemarker_config.journalContextAttributeKey].virtualJournals as virtualJournal>
                <#if journal.key != virtualJournal>
                  <td><@s.checkbox name="articlesInVirtualJournals" fieldValue="${article}::${virtualJournal}"/></td>
                </#if>
                </#list>
                <td><@s.checkbox name="articlesToDelete" fieldValue="${article}"/></td>
                <td><a target="_article" href="${articleURL}">${article}</a></td>
              </tr>
            </#list>
          </table>
          <@s.submit value="Publish/Delete Articles"/>
        </@s.form>
      </fieldset>
      <br/>
    </#if>

    <fieldset>
      <legend><b>Delete Article</b></legend>
      <@s.form name="deleteArticle" action="deleteArticle" method="get" namespace="/admin">
        <@s.textfield label="Article Uri" name="article" size="80"/>&nbsp;<@s.submit value="Delete"/>
      </@s.form>
    </fieldset>
  </body>
</html>
