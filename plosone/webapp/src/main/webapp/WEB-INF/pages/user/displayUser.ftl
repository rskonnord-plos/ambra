<html>
  <head>
    <title>User details</title>
  </head>
  <body>
    <table>
      <tr><td>User Name:</td><td>${pou.displayName}</td></tr>
      <tr><td>GivenNames:</td><td> ${pou.givenNames}</td></tr>
      <tr><td>Surnames:</td><td> ${pou.surnames}</td></tr>
      <tr><td>PositionType:</td><td> ${pou.positionType}</td></tr>
      <tr><td>OrganizationName:</td><td> ${pou.organizationName}</td></tr>
      <tr><td>OrganizationType:</td><td> ${pou.organizationType}</td></tr>
      <tr><td>PostalAddress:</td><td> ${pou.postalAddress}</td></tr>
      <tr><td>BiographyText:</td><td> ${pou.biographyText}</td></tr>
      <tr><td>InterestsText:</td><td> ${pou.interestsText}</td></tr>
      <tr><td>ResearchAreasText:</td><td> ${pou.researchAreasText}</td></tr>
      <tr><td>Email:</td><td> <a href="mailto:${pou.email}">${pou.email}</a></td></tr>
      <tr><td>City:</td><td> ${pou.city}</td></tr>
      <tr><td>Country:</td><td> ${pou.country}</td></tr>
      <tr><td>Title:</td><td> ${pou.title}</td></tr>
      <tr><td>HomePage:</td><td> ${pou.homePage}</td></tr>
      <tr><td>Weblog:</td><td> ${pou.weblog}</td></tr>
    </table>
  </body>
</html>
