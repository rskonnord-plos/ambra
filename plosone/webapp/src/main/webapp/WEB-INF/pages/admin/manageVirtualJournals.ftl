<html>
  <head>
    <title>PLoS ONE: Administration: Manage Virtual Journals</title>
  </head>
  <body>
    <h1>PLoS ONE: Administration: Manage Virtual Journals</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <p style="text-align: right">Return to <@s.a href="${adminTop}">Admin Top</@s.a></p>
    <br />

    <hr />

    <fieldset>
      <legend><b>Messages</b></legend>
      <p>
        <#list actionMessages as message>
          ${message} <br/>
        </#list>
      </p>
    </fieldset>
    <br />

    <hr />

    <#list journals as journal>
      <fieldset>
        <legend><b>${journal.key}</b></legend>
        eIssn: ${journal.getEIssn()!"null"}<br />

        <!-- TODO: display rules in a meaningful way  -->
        Smart Collection Rules: ${journal.smartCollectionRules!"null"}

        <@s.form id="manageVirtualJournals_${journal.key}"
          name="manageVirtualJournals_${journal.key}" action="manageVirtualJournals" method="post"
          namespace="/admin">
          <@s.hidden name="journalToModify" label="Journal to Modify" required="true"
            value="${journal.key}"/>
          <@s.submit value="Modify Articles In ${journal.key}"/>
          <table border="1" cellpadding="2" cellspacing="0">
            <tr><th colspan="2">Simple Collection</th></tr>
            <tr>
              <td colspan="2"><@s.textfield name="articlesToAdd" label="Add" size="100"/></td>
            </tr>
            <#list journal.simpleCollection as articleUri>
              <@s.url id="fetchArticle" namespace="/article" action="fetchArticle"
                articleURI="${articleUri}"/>
              <tr>
                <td>
                  <@s.checkbox label="Delete" name="articlesToDelete" fieldValue="${articleUri}"/>
                </td>
                <td><@s.a href="${fetchArticle}">${articleUri}</@s.a></td>
              </tr>
            </#list>
          </table>
        </@s.form>
      </fieldset>
    </#list>
  </body>
</html>
