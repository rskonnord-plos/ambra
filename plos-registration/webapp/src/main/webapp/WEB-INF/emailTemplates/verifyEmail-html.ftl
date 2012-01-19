<html>
<body>
Thank you <b>${user.loginName}</b> for registering with PLoS.<br/>

Please click on this link to verify your e-mail address:

<a href="${url}?loginName=${user.loginName}&emailVerificationToken=${user.emailVerificationToken}">Verification link</a>
<br/><br/>
or cut and paste the link below if you have problems:
<br/>
${url}?loginName=${user.loginName}&emailVerificationToken=${user.emailVerificationToken}

</body>
</html>

