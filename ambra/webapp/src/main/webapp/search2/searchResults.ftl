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
<#assign limitToJournalAsString><#list limitToJournal as journalKey>${journalKey}<#if journalKey_has_next>, </#if></#list></#assign>

      <!--
        This URL is used for both the return link to the Advanced Search form AND the links to other pages of results.
      -->
      <@s.url id="advancedSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />

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
        <@s.hidden name="startDateAsString" />
        <@s.hidden name="endDateAsString" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
        <@s.hidden name="journalOpt" />
        <@s.hidden name="limitToJournal" />
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
        of <strong>${totalNoOfResults}</strong> results, sorted by relevance,
      </#if>

      <#if isSimpleSearch>
        <#if limitToJournal?size lt 1>
          for <strong>${query?html}</strong>.</p>
        <#else>
          for <strong>${query?html}</strong> in the journal
          <#list journals as journal><#if limitToJournal?seq_contains(journal.eIssn)><strong>${journal.dublinCore.title}</strong></#if></#list>.</p>
        </#if>
      <#else>
        for your search

        <#assign isExistsPreviousQueryTerm = false>

        <!--  Authors
        -->
        <#if creator?size == 1 && creator?first?trim?length gt 0>
          on the author
          <#list creator as author>
            <strong>${author?html}</strong>
          </#list>
          <#assign isExistsPreviousQueryTerm = true>
        <#elseif creator?size gt 0 && creator?first?trim?length gt 0>
          <#if authorNameOp == "all">
            on <strong>all</strong> of
          </#if>
          <#if authorNameOp == "any">
            on <strong>any</strong> of
          </#if>
          the authors
          <#list creator as author>
            <#if authorNameOp == "all" && ! author_has_next>and</#if>
            <#if authorNameOp == "any" && ! author_has_next>or</#if>
            <strong>${author?html}</strong><#if author_has_next && creator?size gt 2>,</#if>
          </#list>
          <#assign isExistsPreviousQueryTerm = true>
        </#if>

        <#assign isExistsPreviousWhereTheWordsQueryTerm = false>
        
        <!--  At Least One of The Words
        -->
        <#if textSearchAtLeastOne?length gt 0>
          <#if isExistsPreviousQueryTerm>
            and
          <#else>
            on
          </#if>
          at least one of the words <strong>${textSearchAtLeastOne?html}</strong>
          <#assign isExistsPreviousQueryTerm = true>
          <#assign isExistsPreviousWhereTheWordsQueryTerm = true>
        </#if>

        <!--  All The Words
        -->
        <#if textSearchAll?length gt 0>
          <#if isExistsPreviousQueryTerm>
            and
          <#else>
            on
          </#if>
          all of the words <strong>${textSearchAll?html}</strong>
          <#assign isExistsPreviousQueryTerm = true>
          <#assign isExistsPreviousWhereTheWordsQueryTerm = true>
        </#if>

        <!--  Exact Phrase
        -->
        <#if textSearchExactPhrase?length gt 0>
          <#if isExistsPreviousQueryTerm>
            and
          <#else>
            on
          </#if>
          the exact phrase "<strong>${textSearchExactPhrase?html}</strong>"
          <#assign isExistsPreviousQueryTerm = true>
          <#assign isExistsPreviousWhereTheWordsQueryTerm = true>
        </#if>

        <!--  Without The Words
        -->
        <#if textSearchWithout?length gt 0>
          <#if isExistsPreviousQueryTerm>
            but
          </#if>
          without any of the words <strong>${textSearchWithout?html}</strong>
          <#assign isExistsPreviousQueryTerm = true>
          <#assign isExistsPreviousWhereTheWordsQueryTerm = true>
        </#if>

        <!--  Where my Words Occur
        -->
        <#if isExistsPreviousWhereTheWordsQueryTerm && textSearchOption?length gt 0>
          where your words occur
          <#if textSearchOption == "abstract">
            in article <strong>abstracts</strong>
          <#elseif textSearchOption == "refs">
            in article <strong>references</strong>
          <#elseif textSearchOption == "title">
            in article <strong>titles</strong>
          <#else>
            anywhere in the articles
          </#if>
        </#if>

        <!--  Dates
        -->
        <#if startDateAsString?length gt 0 && endDateAsString?length gt 0 && startDateAsString != endDateAsString>
          from <strong>${startDateAsString}</strong> to <strong>${endDateAsString}</strong>
        </#if>

        <!--  Subject Categories
        -->
        <#if subjectCatOpt == "all">
<!--
          in <strong>all subject categories</strong><#if limitToJournal?size lt 1 && ! journalOpt == "all">.</p></#if>
-->          
        <#else>
          <#if limitToCategory?size == 1>
            in the subject category
            <#list limitToCategory as category>
              <strong>${category?html}</strong><#if limitToJournal?size lt 1 && ! journalOpt == "all">.</p></#if>
            </#list>
          <#elseif limitToCategory?size gt 1>
            in the subject categories
            <#list limitToCategory as category>
              <#if ! category_has_next>and</#if>
              <strong>${category?html}</strong><#if category_has_next && limitToCategory?size gt 2>,<#else><#if limitToJournal?size lt 1 && ! journalOpt == "all">.</p></#if></#if>
            </#list>
          </#if>
        </#if>

        <!--  Journals
        -->
        <#if journalOpt == "all">
          in <strong>all</strong> of the journals.
<!--
          <#list journals as journal>
            <#if ! journal_has_next>and</#if>
            <strong>${journal.dublinCore.title?html}</strong><#if journal_has_next>,<#else>.</#if>
          </#list>
-->
        <#elseif (limitToJournal?size == 1)>
          in the journal
          <#list journals as journal><#if limitToJournal?seq_contains(journal.eIssn)><strong>${journal.dublinCore.title?html}</strong></#if></#list>.
        <#elseif (limitToJournal?size > 1)>
          <#assign journalNamesShownCounter = 1>
          in the journals
          <#list journals as journal>
            <#if limitToJournal?seq_contains(journal.eIssn)>
              <#if journalNamesShownCounter == limitToJournal?size>and</#if>
              <strong>${journal.dublinCore.title?html}</strong><#if journalNamesShownCounter lt limitToJournal?size && limitToJournal?size gt 2>,<#elseif journalNamesShownCounter == limitToJournal?size>.</#if>
              <#assign journalNamesShownCounter = journalNamesShownCounter + 1>
            </#if>
          </#list>
        </#if>

        </p>
      </#if>
    </#if>

    <!--  TODO: remove this form.  Always submit the form composed at the top of this FTL.  Differences are "startPage" and "noSearchFlag" parameters.
    -->

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
        <@s.hidden name="startDateAsString" />
        <@s.hidden name="endDateAsString" />
        <@s.hidden name="subjectCatOpt" />
        <@s.hidden name="limitToCategory" />
        <@s.hidden name="journalOpt" />
        <@s.hidden name="limitToJournal" />
      </form>

      <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />

      <#if isSimpleSearch>
        <a href="#" onclick="document.reviseSearch.submit();return false;">Go to Advanced Search</a> to revise <em>or</em>
        <label for="searchEdit" style="display:inline;">edit your query here</label> right
        (<a href="${searchHelpURL}">help</a>):&nbsp;
        <form name="searchFormOnSearchResultsPage" action="${searchURL}" method="get">
          <input type="text" size="50" value="${query?html}" id="searchEdit" name="query"/>
          <input type="submit"  value="Go" class="button"/>
        </form>
      <#else>
        <a href="#" onclick="document.reviseSearch.submit();return false;">Go to Advanced Search</a>
        (<a href="${searchHelpURL}">help</a>)
      </#if>
    </div>

    <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>

    <#if totalNoOfResults gt 0>
      <ul>
        <#list searchResults as hit>
          <li>
            <span class="article">
             <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="info:doi/${hit.uri}" includeParams="none"/>
             <@s.a href="${(freemarker_config.getJournalUrlFromIssn(hit.issn))!(freemarker_config.doiResolverURL)}%{fetchArticleURL}" title="Read Open Access Article"><@articleFormat>${hit.title}</@articleFormat></@s.a>
            </span>
            <span class="authors"> <!-- hitScore: ${hit.hitScore} --> ${hit.creator!""}</span>
            <#if  hit.highlight??><span class="cite">${hit.highlight}</span></#if>
            <#if journalOpt == "some" && limitToJournal?size == 1 && limitToJournal?first == freemarker_config.getIssn(journalContext)>
              <#if hit.journalTitle?? && hit.getIssn() != freemarker_config.getIssn(journalContext)>
                <strong><em>${hit.journalTitle}</em></strong><em>:</em>
              </#if>
            <#else>
              <#if hit.journalTitle??>
                <strong><em>${hit.journalTitle}</em></strong><em>:</em>
              </#if>
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
