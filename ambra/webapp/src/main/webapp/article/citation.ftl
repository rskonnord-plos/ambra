<#--
  $HeadURL$
  $Id$

  Copyright (c) 2006-2008 by Topaz, Inc.
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
<#list citation.authors as author>
  <#if (author_index > 4) >
    <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <span class="citation_author">${author.surnames!}<#if author.givenNames??>
      <@abbreviation>${author.givenNames!}</@abbreviation></#if><#if author.suffix??>
      ${author.suffix}</#if>,
  </span>
</#list>
<#if (citation.authors?size < 4) >
<#assign max_index = 4-citation.authors?size>
<#list citation.collaborativeAuthors as collab>
  <#if (collab_index > max_index) >
    <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <span class="citation_author">${collab} </span>
</#list>
</#if>

<span class="citation_date"><#if isCorrection>(${citation.year?string('0000')})<#else>${citation.year?string('0000')}</#if></span>
<span class="citation_article_title"><#if isCorrection>Correction: </#if><@articleFormat>${citation.title}</@articleFormat>. </span>
<span class="citation_journal_title">${citation.journal!}</span><#if isCorrection>:
<#else>
<span class="citation_issue"> ${citation.volume}(${citation.issue}):</span>
<span class="citation_start_page">${citation.ELocationId!}.</span>
</#if>
<span class="citation_doi"><#if isCorrection>http://dx.doi.org/<#else>doi:</#if><#if doi??>${doi}<#else>${citation.doi}</#if></span>
