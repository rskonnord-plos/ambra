<html>
  <head>
    <title>Search for a user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Find user with auth id</legend>
          <@s.form name="findUserByUserIdForm" action="findUserByUserId" namespace="/admin" method="post">
            <@s.textfield name="authId" label="User Id" required="true"/>
            <@s.submit value="find user with user id" />
          </@s.form>
      </fieldset>

      <fieldset>
          <legend>Find user with email address</legend>
          <@s.form name="findUserByEmailAddressForm" action="findUserByEmailAddress" namespace="/admin" method="post">
            <@s.textfield name="emailAddress" label="Email Address" required="true"/>
            <@s.submit value="find user with email address" />
          </@s.form>
      </fieldset>
    </p>

<#if topazUserIdList?exists>
  <ul>
    <#list topazUserIdList as topazId>
      <@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
      <@s.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
      <li>
        Edit <@s.a href="%{editProfileByAdminURL}">profile</@s.a> for ${topazId}
      </li>
    </#list>
  </ul>
</#if>

  </body>
</html>
