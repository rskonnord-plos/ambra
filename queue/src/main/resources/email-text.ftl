<#assign filterJournalsAsString>
  <#list searchParameters.filterJournals as journalKey>
  ${journalKey}<#if journalKey_has_next> OR </#if>
  </#list>
</#assign>

<#assign filterSubjectsAsString>
  <#list searchParameters.filterSubjects as subject>
  "${subject}"<#if subject_has_next> AND </#if>
  </#list>
</#assign>

Query:

<#if searchParameters.unformattedQuery?has_content>
  ${searchParameters.unformattedQuery}
<#else>
  ${searchParameters.query}
</#if>

Filters:

<#if filterJournalsAsString?has_content>
  Journals:${filterJournalsAsString}
</#if>

<#if filterSubjectsAsString?has_content>
  Subject Category:${filterSubjectsAsString}
</#if>

<#if searchParameters.filterKeyword?has_content>
  Keyword: ${searchParameters.filterKeyword}
</#if>

<#if searchParameters.filterKeyword?has_content>
  Article Type: ${searchParameters.filterKeyword}
</#if>

New Articles in PLOS Journals
Published between ${startTime?string("MMM dd yyyy")} - ${endTime?string("MMM dd yyyy")}

<#list searchHitList as searchHit>
  ${searchHit.title} - ${searchHit.creator}
</#list>