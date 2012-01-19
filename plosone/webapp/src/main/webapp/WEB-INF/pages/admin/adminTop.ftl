<html>
  <head>
    <title>PLoS ONE: Administration</title>
  </head>
  <body>

    <h1>PLoS ONE: Administration</h1>
    <fieldset>
      <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>
    <br/>

    <fieldset>
      <legend><b>Actions</b></legend>
      <table border="1" cellpadding="2" cellspacing="0">
        <tr>
          <td>
            <@s.url id="editAnnotation" namespace="/admin" action="editAnnotation"/>
            <@s.a href="${editAnnotation}">Edit Annotation</@s.a>
          </td>
          <td>
            <@s.url id="findUserURL" namespace="/admin" action="findUser" />
            <@s.a href="%{findUserURL}">Find User</@s.a>
          </td>
          <td>
            <@s.url id="manageVirtualJournalsURL" namespace="/admin" action="manageVirtualJournals" />
            <@s.a href="%{manageVirtualJournalsURL}">Manage Virtual Journals</@s.a>
          </td>
        </tr>
      </table>
    </fieldset>

    <fieldset>
      <legend><b>Ingestable Archives</b></legend>
      <@s.form name="ingestArchives" action="ingestArchives" method="post" namespace="/admin">
        <#list uploadableFiles as file>
          <@s.checkbox name="filesToIngest" label="${file}" fieldValue="${file}"/><br/>
        </#list>
        <br/>
        <@s.submit value="Ingest Selected Archives" />
      </@s.form>
    </fieldset>
    <br/>

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

    <fieldset>
      <legend><b>Flagged Comments</b></legend>
      <@s.form name="processFlags" action="processFlags" method="post" namespace="/admin">
        <table width="100%">
          <tr><td><b>Time</b></td><td><b>Comment</b></td><td><b>By</b></td><td><b>Refers To</b></td><td><b>Reason</b></td><td><b>Action</b></td></tr>
          <tr><td colspan="6"><hr/></td></tr>
          <#list flaggedComments as flaggedComment>
            <#if flaggedComment.isAnnotation>
              <@s.url id="flagURL" namespace="/admin" action="viewAnnotation" annotationId="${flaggedComment.target}"/>
              <#assign deleteLabel = "Delete Comment">
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
                <@s.checkbox name="commentsToUnflag" label="Remove Flag" fieldValue="${flaggedComment.target}_${flaggedComment.flagId}_${flaggedComment.targetType}"/><br/>
                <@s.checkbox name="commentsToDelete" label="${deleteLabel}" fieldValue="${flaggedComment.root}_${flaggedComment.target}_${flaggedComment.targetType}"/>
              </td>
            </tr>
            <tr><td colspan="6"><hr/></td></tr>
          </#list>
        </table>
        <@s.submit value="Process Selected Flags" />
      </@s.form>
      <br/>
    </fieldset>
  </body>
</html>
