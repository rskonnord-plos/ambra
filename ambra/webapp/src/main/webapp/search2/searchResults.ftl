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
<!-- begin : main content wrapper -->
<#function max x y><#return stack.findValue("@@max(${x?c}, ${y?c})")/></#function>
<#function min x y><#return stack.findValue("@@min(${x?c}, ${y?c})")/></#function>
<#if limitToJournal??>
  <#assign limitToJournalAsString><#list limitToJournal as journalKey>${journalKey}<#if journalKey_has_next>, </#if></#list></#assign>
</#if>

      <!--
        This URL is used for both the return link to the Advanced Search form AND the links to other pages of results.
      -->
      <@s.url id="advancedSearchURL" includeParams="none" namespace="/search2" action="advancedSearch" />

      <!--
        Allow viewing of other "pages" of results.  Form submits an advanced search. "startPage" is usually modified.
      -->
      <form name="otherSearchResultPages" action="${advancedSearchURL}" method="get">
        <@s.hidden name="startPage" />
        <@s.hidden name="creator" />
        <@s.hidden name="authorNameOp" />
        <@s.hidden name="textSearchAll" />
        <@s.hidden name="textSearchExactPhrase" />
        <@s.hidden name="textSearchAtLeastOne" />
        <@s.hidden name="textSearchWithout" />
        <@s.hidden name="textSearchOption" />
        <@s.hidden name="dateTypeSelect" />
        <@s.hidden name="startDate" />
        <@s.hidden name="endDate" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
        <@s.hidden name="journalOpt" />
        <#if limitToJournal??>
          <@s.hidden name="limitToJournal" />
        </#if>
      </form>

<#macro renderSearchPaginationLinks totalPages, hasMore>
  <#if (totalPages gt 1) >
    <#if (startPage gt 0) >
      <@s.a href="#" onclick="document.otherSearchResultPages.startPage.value=${startPage - 1};document.otherSearchResultPages.submit();return false;">&lt; Prev</@s.a> |
    </#if>

    <#list max(1, startPage - 9)..min(totalPages, startPage + 11) as pageNumber>
      <#if (startPage == (pageNumber - 1))>
        ${pageNumber}
      <#else>
        <@s.a href="#" onclick="document.otherSearchResultPages.startPage.value=${pageNumber - 1};document.otherSearchResultPages.submit();return false;">${pageNumber}</@s.a>
      </#if>
      <#if pageNumber != min(totalPages, startPage + 11)>|</#if>
    </#list>

    <#if hasMore == 1>
        <@s.a href="#" onclick="document.otherSearchResultPages.startPage.value=${startPage + 1};document.otherSearchResultPages.submit();return false;">Next &gt;</@s.a>
    </#if>
  </#if>
</#macro>

<div id="content" class="static">

  <#assign noOfResults = (startPage + 1) * pageSize>
  <#if (noOfResults >= totalNoOfResults)>
    <#assign noOfResults = totalNoOfResults>
    <#assign hasMore = 0>
  <#else>
    <#assign hasMore = 1>
  </#if>

  <#-- Compute number of pages -->
  <#assign totalPages = ((totalNoOfResults + pageSize - 1) / pageSize)?int>


  <h1>Search Results</h1>


<h1>This FTL is in the search2 directory</h1>
<!-- TODO: take this out, and the line above it, too.
<#list limitToCategory as cat>
<br/>limitToCategory: ${cat}
</#list>
<#list limitToJournal as issn>
<br/>limitToJournal: ${issn}
</#list>
<br/>
<#list journals as journal>
<br/>${journal.dublinCore.title}: ${journal.eIssn}
</#list>
-->


  <div id="search-results">

    <#if (fieldErrors?? && numFieldErrors > 0)>
      <div class="error">
        <#list fieldErrors?keys as key>
          <#list fieldErrors[key] as errorMessage>
            ${errorMessage}
          </#list>
        </#list>
      </div>
    <#else>
  <#--  <p>Debug: noOfResults: ${noOfResults}, totalNoOfResults: ${totalNoOfResults}, startPage: ${startPage}, pageSize: ${pageSize}, hasMore: ${hasMore}</p> -->
      <#if totalNoOfResults == 0>
        <p>There are no results
      <#else>
        <#assign startIndex = startPage * pageSize>
        <p>
        Viewing results <strong>${startIndex + 1} - ${noOfResults}</strong>
        of <strong>${totalNoOfResults}</strong> results,
      </#if>

      <#if (! limitToJournal??) || (limitToJournal?size < 1)>
        sorted by relevance.</p>
      <#elseif journalOpt?? && journalOpt = "all">
        sorted by relevance, in the journals
        <#list journals as journal>
          <#if ! journal_has_next>and</#if>
          <strong>${journal.dublinCore.title}</strong><#if journal_has_next>,<#else>.</#if>
        </#list>
      <#elseif (limitToJournal?size > 1)>
        <#assign journalNamesShownCounter = 1>
        sorted by relevance, in the journals
        <#list journals as journal>
          <#if limitToJournal?seq_contains(journal.eIssn)>
            <#if journalNamesShownCounter == limitToJournal?size>and</#if>
            <strong>${journal.dublinCore.title}</strong><#if (journalNamesShownCounter < limitToJournal?size) && (limitToJournal?size > 2)>,</#if><#if journalNamesShownCounter == limitToJournal?size>.</#if>
            <#assign journalNamesShownCounter = journalNamesShownCounter + 1>
          </#if>
        </#list>
      <#else>
        sorted by relevance, in the journal
        <#list journals as journal><#if limitToJournal?seq_contains(journal.eIssn)><strong>${journal.dublinCore.title}</strong></#if></#list>.
      </#if>
      </p>
    </#if>
    
    <div id="searchMore">
      <form name="reviseSearch" action="${advancedSearchURL}" method="get">
        <@s.hidden name="noSearchFlag" value="set" />
        <@s.hidden name="creator" />
        <@s.hidden name="authorNameOp" />
        <@s.hidden name="textSearchAll" />
        <@s.hidden name="textSearchExactPhrase" />
        <@s.hidden name="textSearchAtLeastOne" />
        <@s.hidden name="textSearchWithout" />
        <@s.hidden name="textSearchOption" />
        <@s.hidden name="dateTypeSelect" />
        <@s.hidden name="startDate" />
        <@s.hidden name="endDate" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
        <@s.hidden name="journalOpt" />
        <#if limitToJournal??>
          <@s.hidden name="limitToJournal" />
        </#if>
      </form>

        <a href="#" onclick="document.reviseSearch.submit();return false;">Revise your Advanced Search</a>
        <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />
        (<a href="${searchHelpURL}">old-style search help</a>)
    </div>

    <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>

    <#if totalNoOfResults gt 0>
      <ul>
        <#list searchResults as hit>
          <li>
            <span class="article">
             <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="info:doi/${hit.uri}" includeParams="none"/>
             <@s.a href="${(freemarker_config.getJournalUrl(hit.journalKey))!(freemarker_config.doiResolverURL)}%{fetchArticleURL}" title="Read Open Access Article"><@articleFormat>${hit.title}</@articleFormat></@s.a>
            </span>
            <span class="authors"> <!-- hitScore: ${hit.hitScore} --> ${hit.creator!""}</span>
            <#if  hit.highlight??><span class="cite">${hit.highlight}</span></#if>
            <#if journalOpt?? && journalOpt == "some" && limitToJournal?? && limitToJournal?size == 1 && limitToJournal?first == freemarker_config.getIssn(journalContext)>
              <#if hit.getIssn() != freemarker_config.getIssn(journalContext)>
                <strong><em>${hit.journalTitle}</em></strong><em>:</em>
              </#if>
            <#else>
              <strong><em>${hit.journalTitle}</em></strong><em>:</em>
            </#if>
            <#if hit.articleTypeForDisplay??>
              ${hit.articleTypeForDisplay},
            </#if>
            <#if hit.date??>
              published ${hit.date?string("dd MMM yyyy")}
            </#if>
            <#if hit.uri??>
             | ${hit.uri?replace("info:doi/", "doi:")}
            </#if>
          </li>
        </#list>
      </ul>
      <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>
    </#if>

  </div> <!-- search-results -->
</div> <!-- content -->
<!-- end : main content wrapper -->
