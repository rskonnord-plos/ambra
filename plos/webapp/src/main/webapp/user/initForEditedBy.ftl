<#if isEditedByAdmin?exists && isEditedByAdmin == true>
  <#assign editedByAdmin = true>
<#else>
  <#assign editedByAdmin = false>
</#if>

<#assign addressingUser = "Your">
<#if editedByAdmin>
  <#if displayName?exists>
     <#assign addressingUser = displayName +"'s" >
  </#if>
<br/>
<@s.url id="adminTopURL" action="adminTop" namespace="/admin" includeParams="none"/>
<@s.a href="%{adminTopURL}">back to admin console</@s.a>  
<br/>
<br/>
<@s.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
<@s.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
  Edit <@s.a href="%{editProfileByAdminURL}">profile</@s.a>
  or <@s.a href="%{editPreferencesByAdminURL}">alerts/preferences</@s.a> for <strong>${topazId}</strong>
<br/>

</#if>

