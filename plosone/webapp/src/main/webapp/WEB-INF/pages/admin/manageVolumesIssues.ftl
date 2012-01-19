<#macro trimBrackets bracketedString>
  <#if bracketedString??>
    <#assign unbracketedString=bracketedString>
    <#if unbracketedString?starts_with("[")>
      <#assign unbracketedString=unbracketedString?substring(1)>
    </#if>
    <#if unbracketedString?ends_with("]")>
      <#assign unbracketedString=unbracketedString?substring(0, unbracketedString?length - 1)>
    </#if>
  <#else>
    <#assign unbracketedString="">
  </#if>
</#macro>
<html>
  <head>
    <title>PLoS ONE: Administration: Manage Virtual Journals : Volumes / Issues</title>
  </head>
  <body>
    <h1>PLoS ONE: Administration: Manage Virtual Journals : Volumes / Issues</h1>

    <@s.url id="adminTop" namespace="/admin" action="adminTop"/>
    <@s.url id="manageVirtualJournals" namespace="/admin" action="manageVirtualJournals"/>
    <p style="text-align: right">
      Return to <@s.a href="${adminTop}">Admin Top</@s.a>,
      <@s.a href="${manageVirtualJournals}">Manage Virtual Journals</@s.a>
    </p>
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

    <h2>${journal.key} (${journal.getEIssn()!""})</h2>

    <!-- create a Volume -->
    <fieldset>
      <legend><b>Create Volume</b></legend>

      <@s.form id="manageVolumesIssues_createVolume" name="manageVolumesIssues_createVolume"
        namespace="/admin" action="manageVolumesIssues" method="post">

        <@s.hidden name="journalKey" label="Journal Key" required="true"
          value="${journal.key}"/>
        <@s.hidden name="journalEIssn" label="Journal eIssn" required="true"
          value="${journal.getEIssn()}"/>
        <@s.hidden name="manageVolumesIssuesAction" label="Action" required="true"
          value="CREATE_VOLUME"/>

        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th align="right">Id<br/>(PLoS DOI syntax)</th>
            <td><@s.textfield name="doi" size="72" required="true"/></td>
          </tr>
          <tr>
            <th align="right">Display Name</th>
            <td><@s.textfield name="displayName" size="72" required="true"/></td>
          </tr>
          <tr>
            <th align="right">Image Article (DOI)</th>
            <td><@s.textfield name="image" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Previous Volume (Id)</th>
            <td><@s.textfield name="prev" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Next Volume (Id)</th>
            <td><@s.textfield name="next" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Issues</th>
            <td><@s.textfield name="aggregation" size="100"/></td>
          </tr>
        </table>
        <br/>
        <@s.submit value="Create Volume"/>
      </@s.form>
    </fieldset>

    <!-- list Volumes -->
    <fieldset>
      <legend><b>Existing Volumes</b></legend>

        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th>Update</th>
            <th>Delete</th>
            <th>Id</th>
            <th>Display Name</th>
            <th>Image</th>
            <th>Previous</th>
            <th>Next</th>
            <th>Issues</th>
          </tr>
          <#list volumes as volume>
            <tr>
              <@s.form id="manageVolumesIssues_updateVolume"
                name="manageVolumesIssues_updateVolume"
                namespace="/admin" action="manageVolumesIssues" method="post">

                <@s.hidden name="journalKey" label="Journal Key" required="true"
                  value="${journal.key}"/>
                <@s.hidden name="journalEIssn" label="Journal eIssn" required="true"
                  value="${journal.getEIssn()}"/>
                <@s.hidden name="manageVolumesIssuesAction" label="Action" required="true"
                  value="UPDATE_VOLUME"/>

                <td align="center">
                  <#if volume.image?exists>
                    <@s.url id="volumeImage" value="${volume.image}" />
                    <#assign altText="Volume Image" />
                  <#else>
                    <@s.url id="volumeImage" value="" />
                    <#assign altText="Submit (Volume Image null)" />
                  </#if>
                  <@s.submit type="image" value="${altText}" src="${volumeImage}"/>
                </td>
                <td align="center">
                  <@s.checkbox name="aggregationToDelete" fieldValue="${volume.id}"/>
                </td>
                <td>
                  <@s.textfield name="doi" required="true" readonly="true" value="${volume.id}"/>
                </td>
                <td>
                  <@s.textfield name="displayName" size="24" required="true"
                    value="${volume.displayName}"/>
                </td>
                <td>
                  <@s.textfield name="image" size="32" value="${volume.image!''}"/>
                </td>
                <td>
                  <@s.textfield name="prev" size="32" value="${volume.prevVolume!''}"/>
                </td>
                <td>
                  <@s.textfield name="next" size="32" value="${volume.nextVolume!''}"/>
                </td>
                <td>
                  <@trimBrackets volume.simpleCollection!'' />
                  <@s.textfield name="aggregation" size="96"
                    value="${unbracketedString}"/>
                </td>
              </@s.form>
            </tr>
          </#list>
        </table>
    </fieldset>

    <!-- create a Issue -->
    <fieldset>
      <legend><b>Create Issue</b></legend>

      <@s.form id="manageVolumesIssues_createIssue" name="manageVolumesIssues_createIssue"
        namespace="/admin" action="manageVolumesIssues" method="post">

        <@s.hidden name="journalKey" label="Journal Key" required="true"
          value="${journal.key}"/>
        <@s.hidden name="journalEIssn" label="Journal eIssn" required="true"
          value="${journal.getEIssn()}"/>
        <@s.hidden name="manageVolumesIssuesAction" label="Action" required="true"
          value="CREATE_ISSUE"/>

        <table border="1" cellpadding="2" cellspacing="0">
          <tr>
            <th align="right">Id<br/>(PLoS DOI syntax)</th>
            <td><@s.textfield name="doi" size="72" required="true"/></td>
          </tr>
          <tr>
            <th align="right">Display Name</th>
            <td><@s.textfield name="displayName" size="72" required="true"/></td>
          </tr>
          <tr>
            <th align="right">Volume</th>
            <td><@s.textfield name="volume" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Image Article (DOI)</th>
            <td><@s.textfield name="image" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Previous Issue (Id)</th>
            <td><@s.textfield name="prev" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Next Issue (Id)</th>
            <td><@s.textfield name="next" size="72"/></td>
          </tr>
          <tr>
            <th align="right">Articles</th>
            <td><@s.textfield name="aggregation" size="100"/></td>
          </tr>
        </table>
        <br/>
        <@s.submit value="Create Issue"/>
      </@s.form>
    </fieldset>

    <!-- list Issues -->
    <fieldset>
      <legend><b>Existing Issues</b></legend>
      <table border="1" cellpadding="2" cellspacing="0">
        <tr>
          <th>Update</th>
          <th>Delete</th>
          <th>Id</th>
          <th>Display Name</th>
          <th>Volume</th>
          <th>Image</th>
          <th>Previous</th>
          <th>Next</th>
          <th>Articles</th>
        </tr>
        <#list issues as issue>
          <tr>
            <@s.form id="manageVolumesIssues_updateIssue"
              name="manageVolumesIssues_updateIssue"
              namespace="/admin" action="manageVolumesIssues" method="post">

              <@s.hidden name="journalKey" label="Journal Key" required="true"
                value="${journal.key}"/>
              <@s.hidden name="journalEIssn" label="Journal eIssn" required="true"
                value="${journal.getEIssn()}"/>
              <@s.hidden name="manageVolumesIssuesAction" label="Action" required="true"
                value="UPDATE_ISSUE"/>

              <td align="center">
                <#if issue.image?exists>
                  <@s.url id="issueImage" value="${issue.image}" />
                  <#assign altText="Issue Image" />
                <#else>
                  <@s.url id="issueImage" value="" />
                  <#assign altText="Submit (Issue Image null)" />
                </#if>
                <@s.submit type="image" value="${altText}" src="${issueImage}"/>
              </td>
              <td align="center">
                <@s.checkbox name="aggregationToDelete" fieldValue="${issue.id}"/>
              </td>
              <td>
                <@s.textfield name="doi" required="true" readonly="true" value="${issue.id}"/>
              </td>
              <td>
                <@s.textfield name="displayName" size="24" required="true"
                  value="${issue.displayName}"/>
              </td>
              <td>
                <@s.textfield name="volume" size="32" value="${issue.volume!''}"/>
              </td>
              <td>
                <@s.textfield name="image" size="32" value="${issue.image!''}"/>
              </td>
              <td>
                <@s.textfield name="prev" size="32" value="${issue.prevIssue!''}"/>
              </td>
              <td>
                <@s.textfield name="next" size="32" value="${issue.nextIssue!''}"/>
              </td>
              <td>
                <@trimBrackets issue.simpleCollection!'' />
                <@s.textfield name="aggregation" size="96"
                  value="${unbracketedString}"/>
              </td>
            </@s.form>
          </tr>
        </#list>
      </table>
    </fieldset>

  </body>
</html>
