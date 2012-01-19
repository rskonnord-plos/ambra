<html>
  <head>
    <title>Search for a user</title>
  </head>
  <body>
    <p>
      <fieldset>
          <legend>Find user with auth id</legend>
          <@ww.form name="findUserByUserIdForm" action="findUserByUserId" namespace="/admin" method="post">
            <@ww.textfield name="authId" label="User Id" required="true"/>
            <@ww.submit value="find user with user id" />
          </@ww.form>
      </fieldset>

      <fieldset>
          <legend>Find user with email address</legend>
          <@ww.form name="findUserByEmailAddressForm" action="findUserByEmailAddress" namespace="/admin" method="post">
            <@ww.textfield name="emailAddress" label="Email Address" required="true"/>
            <@ww.submit value="find user with email address" />
          </@ww.form>
      </fieldset>
    </p>

<#if topazUserIdList?exists>
  <ul>
    <#list topazUserIdList as topazId>
      <@ww.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
      <@ww.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
      <li>
        Edit <@ww.a href="%{editProfileByAdminURL}">profile</@ww.a> for ${topazId}
      </li>
    </#list>
  </ul>
</#if>

  </body>
</html>
