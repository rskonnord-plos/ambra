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
		<title>Images you can view</title>
	</head>

	<body>
    <fieldset>
      <legend>Available images</legend>

      <#list secondaryObjects as image>
        <@s.url id="imageThumbUrl"  action="fetchObject" uri="${image.uri}"/>
        <@s.a href="%{imageThumbUrl}&representation=${image.repLarge}">
          <img src="${imageThumbUrl}&representation=${image.repSmall}" alt="${image.title}" height="100px" width="120px"/>
        </@s.a>

        <ul>
          <li>Title: ${image.title}</li>
          <li>Description: ${image.description}</li>
          <li>Uri: ${image.uri}</li>
          <ul>
            <#list image.representations as rep>
              <@s.url id="imageRepUrl"  action="fetchObject" uri="${image.uri}"/>
              <@s.a href="%{imageRepUrl}&representation=${rep.name}">${rep.name}-${rep.contentType}</@s.a>
            </#list>
          </ul>
        </ul>

        <br/>

      </#list>

    </fieldset>

  </body>
</html>
