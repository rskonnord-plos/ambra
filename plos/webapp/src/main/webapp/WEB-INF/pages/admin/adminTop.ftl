<html>
  <head>
    <title>PLoS ONE: Administration</title>
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
    <h1 style="text-align: center">PLoS ONE: Administration</h1>

    <@s.url id="editAnnotation" namespace="/admin" action="editAnnotation"/>
    <@s.url id="manageUsersURL" namespace="/admin" action="findUser" />
    <@s.url id="manageVirtualJournalsURL" namespace="/admin" action="manageVirtualJournals" />
    <@s.url id="manageCaches" namespace="/admin" action="manageCaches" />
    <p style="text-align: right">
      <@s.a href="${editAnnotation}">Edit Annotation</@s.a>&nbsp;|&nbsp;
      <@s.a href="%{manageUsersURL}">Manage Users</@s.a>&nbsp;|&nbsp;
      <@s.a href="%{manageVirtualJournalsURL}">Manage Virtual Journals</@s.a>&nbsp;|&nbsp;
      <@s.a href="%{manageCaches}">Manage Caches</@s.a>
    </p>
    <hr/>

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
                <th>${virtualJournal}</th>
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
                  <th>&nbsp;</th>
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
                  <td><@s.checkbox name="articlesInVirtualJournals" fieldValue="${article}::${virtualJournal}"/></td>
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

    <#if flaggedComments?has_content>
      <fieldset>
        <legend><b>Flagged Comments</b></legend>
        <@s.form name="processFlags" action="processFlags" method="post" namespace="/admin">
          <table width="100%">
            <tr><td><b>Time</b></td><td><b>Comment</b></td><td><b>By</b></td><td><b>Refers To</b></td><td><b>Reason</b></td><td><b>Action</b></td></tr>
            <tr><td colspan="6"><hr/></td></tr>
            <#list flaggedComments as flaggedComment>
              <#if flaggedComment.isAnnotation>
                <@s.url id="flagURL" namespace="/admin" action="viewAnnotation" annotationId="${flaggedComment.target}"/>
                <#if flaggedComment.correction>
                  <#assign deleteLabel = "Delete Correction">
                <#else>
                  <#assign deleteLabel = "Delete Comment">
                </#if>
              <#elseif flaggedComment.isRating>
                <@s.url id="flagURL" namespace="/admin" action="viewRating" ratingId="${flaggedComment.target}"/>
                <#assign deleteLabel = "Delete Rating">
              <#elseif flaggedComment.isReply>
                <@s.url id="flagURL" namespace="/admin" action="viewReply" replyId="${flaggedComment.target}"/>
                <#assign deleteLabel = "Delete Reply (Sub-thread)">
              </#if>
              <#if flaggedComment.targetTitle?exists>
                <#assign targetTitle = flaggedComment.targetTitle>
              <#else>
                <#assign targetTitle = '"Flagged Annotation has no Title"'>
              </#if>
              <tr>
                <td>${flaggedComment.created}</td>
                <td width="20%">${flaggedComment.flagComment}</td>
                <td><a href="../user/displayUser.action?userId=${flaggedComment.creatorid}"/>${flaggedComment.creator}</a></td>
                <td width="20%"><a href="${flagURL}">${targetTitle}</a></td>
                <td>${flaggedComment.reasonCode}</td>
                <td>
                  <@s.checkbox name="commentsToUnflag" label="Remove Flag" fieldValue="${flaggedComment.target}_${flaggedComment.flagId}_${flaggedComment.targetType}"/>
                  <br/>
                  <@s.checkbox name="commentsToDelete" label="${deleteLabel}" fieldValue="${flaggedComment.root}_${flaggedComment.target}_${flaggedComment.targetType}"/>
                  <#if flaggedComment.isAnnotation >
                    <br/>
                        Convert to:
                        <br/>
                        <#if !flaggedComment.isMinorCorrection() > 
                              <@s.checkbox name="convertToMinorCorrection" label="Minor Correction" 
                               fieldValue="${flaggedComment.flagId}_${flaggedComment.target}"/>
                      <br/>
                            </#if>
                            <#if !flaggedComment.isFormalCorrection() > 
                              <@s.checkbox name="convertToFormalCorrection" label="Formal Correction" 
                               fieldValue="${flaggedComment.flagId}_${flaggedComment.target}"/>
                      <br/>
                    </#if>
                    <#if flaggedComment.isCorrection() >
                      <@s.checkbox name="convertToNote" label="Note" 
                       fieldValue="${flaggedComment.target}_${flaggedComment.target}"/>
                      <br/>
                    </#if>
                  </#if>
                </td>
              </tr>
              <tr><td colspan="6"><hr/></td></tr>
            </#list>
          </table>
          <@s.submit value="Process Selected Flags" />
        </@s.form>
        <br/>
      </fieldset>
      <br/>
    </#if>

    <fieldset>
      <legend><b>Delete Article</b></legend>
      <@s.form name="deleteArticle" action="deleteArticle" method="get" namespace="/admin">
        <@s.textfield label="ArticleDoi" name="article" size="80"/>&nbsp;<@s.submit value="Delete"/>
      </@s.form>
    </fieldset>
  </body>
</html>
