<!DOCTYPE html>
<html lang="en">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>

<body>

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

<p>&nbsp;</p>

<table width="728" cellspacing="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 12px;" align="center">
  <tbody>
  <tr>
    <td valign="top"><a name="query"></a>
      <h3>Query:</h3>
      <p>
        <#if searchParameters.unformattedQuery?has_content>
          <span>${searchParameters.unformattedQuery}</span>
        <#else>
          <span>${searchParameters.query}</span>
        </#if>
      </p>

      <h3>Filters:</h3>

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
    </td>
  </tr>
  </tbody>
</table>


<table width="728" cellspacing="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 12px;" align="center">
  <tbody>
    <tr>
      <td valign="top"><a name="top"></a>
        <h3>New Articles in <em>Journals</em></h3>
        <h4>Published between ${startTime?string("MMM dd yyyy")} - ${endTime?string("MMM dd yyyy")}</h4>
      </td>
    </tr>
    <tr>
      <td valign="top">

        <#list searchHitList as searchHit>
          <p><a href="http://dx.doi.org/info:doi/${searchHit.uri}"><strong>${searchHit.title}</strong></a><br />
          ${searchHit.creator}
          </p>
        </#list>

        <br />
      </td>
    </tr>
  </tbody>
</table>