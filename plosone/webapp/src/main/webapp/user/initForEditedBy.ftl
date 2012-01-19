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
<@ww.url id="adminTopURL" action="adminTop" namespace="/admin" includeParams="none"/>
<@ww.a href="%{adminTopURL}">back to admin console</@ww.a>  
<br/>
<br/>
<@ww.url id="editProfileByAdminURL" action="editProfileByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
<@ww.url id="editPreferencesByAdminURL" action="retrieveUserAlertsByAdmin" namespace="/admin" topazId="${topazId}" includeParams="none"/>
  Edit <@ww.a href="%{editProfileByAdminURL}">profile</@ww.a>
  or <@ww.a href="%{editPreferencesByAdminURL}">alerts/preferences</@ww.a> for <strong>${topazId}</strong>
<br/>

</#if>

