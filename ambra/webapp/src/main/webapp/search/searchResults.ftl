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
<#function max x y><#return stack.findValue("@@max(${x}, ${y})")/></#function>
<#function min x y><#return stack.findValue("@@min(${x}, ${y})")/></#function>

<#macro renderSearchPaginationLinks totalPages, hasMore>
  <#if (totalPages gt 1) >
    <#if (startPage gt 0) >
      <@s.url id="prevPageURL" action="simpleSearch" namespace="/search" startPage="${startPage - 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
      <@s.a href="%{prevPageURL}">&lt; Prev</@s.a> |
    </#if>
    <#list max(1, startPage - 9)..min(totalPages, startPage + 11) as pageNumber>
      <#if (startPage == (pageNumber-1))>
        ${pageNumber}
      <#else>
        <@s.url id="searchPageURL" action="simpleSearch" namespace="/search" startPage="${pageNumber - 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
        <@s.a href="%{searchPageURL}">${pageNumber}</@s.a>
      </#if>
      <#if pageNumber != min(totalPages, startPage + 11)>|</#if>
    </#list>
    <#if hasMore == 1>
      <@s.url id="nextPageURL" action="simpleSearch" namespace="/search" startPage="${startPage + 1}" pageSize="${pageSize}" query="${query}" includeParams="none"/>
      | <@s.a href="%{nextPageURL}"> Next &gt;</@s.a>
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
        <p>There are no results for <strong>${query?html}</strong>.</p>
      <#else>
        <#assign startIndex = startPage * pageSize>
        <p>
        Viewing results <strong>${startIndex + 1} - ${noOfResults}</strong>
        of <strong>${totalNoOfResults}</strong> results, sorted by relevance,
        for <strong>${query?html}</strong>.
        </p>
      </#if>
    </#if>
    
    <@s.url id="searchHelpURL" includeParams="none" namespace="/static" action="searchHelp" />
    <@s.url id="advancedSearchURL" includeParams="none" namespace="/search" action="advancedSearch" />
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
      </form>
      <form name="reviseQuery" action="${searchURL}" method="get">
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
        <a href="#" onclick="document.reviseSearch.submit();return false;">Go to Advanced Search</a> to revise <em>or</em>
        <label for="searchEdit" style="display:inline;">edit your query here</label>
        (<a href="${searchHelpURL}">help</a>):&nbsp;
        <input type="text" size="50" value="${query?html}" id="searchEdit" name="query"/>
        <input type="submit"  value="Go" class="button"/>
      </form>
    </div>

    <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>

    <#if totalNoOfResults gt 0>
      <ul>
        <#list searchResults as hit>
          <li>
            <span class="date">Published ${hit.date?string("dd MMM yyyy")}</span>
            <span class="article">
             <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${hit.uri}" includeParams="none"/>
             <@s.a href="%{fetchArticleURL}" title="Read Open Access Article"><@articleFormat>${hit.title}</@articleFormat></@s.a>
            </span>       
            <span class="authors"> <!-- hitScore: ${hit.hitScore} --> ${hit.creator!""}</span>
            <span class="cite">${hit.highlight}</span>
          </li>
        </#list>
      </ul>
      <div class="resultsTab"><@renderSearchPaginationLinks totalPages, hasMore/></div>
    </#if>

  </div> <!-- search-results -->
</div> <!-- content -->
<!-- end : main content wrapper -->
