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
<!-- begin : main content -->
<div id="content" class="static">
  <h1>RSS Feeds</h1>
  <@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssURL"/>
  
  <p>
    The PLoS Hub for Clinical Trials provides the following <@s.a href="${rssURL}">RSS feeds</@s.a> which are updated as new articles are posted:
  </p>

  <ul>
    <li><a href="/feed/NewArticles">New Articles</a></li>
  </ul>
   
</div>
<!-- end : main contents -->