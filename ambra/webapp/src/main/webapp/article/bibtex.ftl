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
<#assign authorList = "">
<#list citation.authors as author>
  <#assign authorList = authorList + author.surnames>
  <#assign authorList = authorList + ", ">
  <#if author.suffix?exists>
    <#assign authorList = authorList + author.suffix>
    <#assign authorList = authorList + ", ">
  </#if>
  <#assign authorList = authorList + author.givenNames>
  <#if author_has_next><#assign authorList = authorList + " AND "></#if>
</#list>
<#if citation.collaborativeAuthors?has_content>
  <#assign authorList = authorList + " AND ">
  <#list citation.collaborativeAuthors as collab>
    <#assign authorList = authorList + collab>
    <#if collab_has_next><#assign authorList = authorList + ", "></#if>
  </#list>
</#if>

@article{${citation.doi},
    author = {${authorList}},
    journal = {${citation.journal}},
    publisher = {${citation.publisherName}},
    title = {${citation.title}},
    year = {${citation.year?string('0000')}},
    month = {${citation.month!}},
    volume = {${citation.volume!}},
    url = {${citation.url!}},
    pages = {${citation.ELocationId!}},
    abstract = {${citation.summary!}},
    number = {${citation.issue!}},
    doi = {${citation.doi}}
}        




