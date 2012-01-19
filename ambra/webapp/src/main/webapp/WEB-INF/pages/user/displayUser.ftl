<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
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
