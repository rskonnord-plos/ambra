<#--
  $HeadURL$
  $Id$

  Copyright (c) 2006-2010 by Public Library of Science
  http://plos.org
  http://ambraproject.org

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
<#list authors as author>
  <#if (author_index > 4) >
    <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <span class="citation_author">${author.surnames!}<#if author.givenNames??>
      <@abbreviation>${author.givenNames!}</@abbreviation></#if><#if author.suffix??>
      ${author.suffix}</#if>,
  </span>
</#list>
<#if (authors?size < 4) >
<#assign max_index = 4-authors?size>
<#list citation.collaborativeAuthors as collab>
  <#if (collab_index > max_index) >
    <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <span class="citation_author">${collab} </span>
</#list>
</#if>
<#if citation.displayYear??><span class="citation_date">${citation.displayYear}</span></#if>
<#if citation.title??><#if !(citation.title?ends_with("?"))><#assign addPunctuation = "."></#if><span class="citation_article_title"><@articleFormat>${citation.title}</@articleFormat><#if addPunctuation??>${addPunctuation}</#if></span></#if>
<#if citation.journal??><span class="citation_journal_title">${citation.journal}</span></#if><span class="citation_issue"><#if citation.volume??> ${citation.volume}</#if><#if citation.issue??>(${citation.issue})</#if>:</span>
<#if citation.ELocationId??><span class="citation_start_page">${citation.ELocationId}.</span></#if>
<#if citation.doi??><span class="citation_doi"><#if isCorrection>http://dx.doi.org/<#else>doi:</#if>${citation.doi}</span></#if>
