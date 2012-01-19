<html>
<body>
Thank you <b>${user.loginName}</b> for requesting to reset your PLoS password.<br/>

Please click on this link to reset your password:

<a href="${url}?loginName=${user.loginName}&resetPasswordToken=${user.resetPasswordToken}">Reset password</a>
<br/><br/>
or copy and paste this link if you have problems:<br/>
${url}?loginName=${user.loginName}&resetPasswordToken=${user.resetPasswordToken}

</body>
</html>

