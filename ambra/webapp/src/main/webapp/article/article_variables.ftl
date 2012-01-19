<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2009 by Topaz, Inc.
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

<#--
  The digg URL has to be different as digg appears to be picking up the redirect
  from our internal DOI resolver and messing up the formating.
 -->
<#if articleInfoX??>
  <#assign shortDOI = "${articleInfoX.id?replace('info:doi/','')}" />
  <#assign docURL = "http://dx.plos.org/" + shortDOI />
  <#assign jDocURL = freemarker_config.getJournalUrl(journalContext) + "/article/" + articleInfoX.id?url />
  <#assign docTitle = articleInfoX.title />
<#else>
  <#assign shortDOI = "" />
  <#assign docURL = "" />
  <#assign jDocURL = "" />
  <#assign docTitle = "" />
</#if>

<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>

<#assign publisher = "">
<#list journalList as jour>
  <#if jour.key != journalContext && articleInfo.eIssn == jour.eIssn>
    <#assign publisher = "Published in <em><a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">" + jour.dublinCore.title + "</a></em>">
    <#break/>
  <#else>
    <#if jour.key != journalContext>
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">"/>
      <#if (publisher?length > 0)>
        <#assign publisher = publisher + ", " + jourAnchor + jour.dublinCore.title + "</a>" />
      <#else>
        <#assign publisher = publisher + "Featured in " + jourAnchor + jour.dublinCore.title + "</a>" />
      </#if>
    </#if>
  </#if>
</#list>