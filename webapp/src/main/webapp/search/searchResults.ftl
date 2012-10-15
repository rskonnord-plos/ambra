<#import "search_variables.ftl" as search>

<#function max x y>
  <#if  (x > y) >
    <#return x />
  <#else>
    <#return y />
  </#if>
</#function>
<#function min x y>
  <#if x < y>
    <#return x />
  <#else>
    <#return y />
  </#if>
</#function>

<#assign max_authors = 5>
<#assign max_editors = 5>
<#assign max_institutions = 5>
<#assign max_subjects = 10>
<#assign max_articletypes = 10>

<#assign filterJournalsAsString>
  <#list filterJournals as journalKey>
  ${freemarker_config.getDisplayName(journalKey)}<#if journalKey_has_next> OR </#if>
  </#list>
</#assign>

<#assign filterSubjectsAsString>
  <#list filterSubjects as subject>
  "${subject}"<#if subject_has_next> AND </#if>
  </#list>
</#assign>

<#--
  This URL is used for both the return link to the Advanced Search form AND the links to other pages of results.
-->
<#if (searchType.length() == 0)>
ERROR, searchType must be defined.
</#if>

<#--
  Allow viewing of other "pages" of results.  Form submits an advanced search. "startPage" is usually modified.
-->
<form name="rssSearchForm" action="${rssSearchURL}" method="get">

<#--  Simple Search field  -->
  <@s.hidden name="query" />
  <#--  Unformatted Search field (new Advanced Search)  -->
  <@s.hidden name="unformattedQuery" />

  <#--  Find An Article Search fields  -->
  <@s.hidden name="volume" />
  <@s.hidden name="eLocationId" />
  <@s.hidden name="id" />
  <@s.hidden name="filterJournals" />
  <@s.hidden name="filterSubjects" />
  <@s.hidden name="filterArticleType" />
  <@s.hidden name="filterKeyword" />
</form>

<#--
  Allow viewing of other "pages" of results.  Form submits an advanced search. "startPage" is usually modified.
-->
<form name="otherSearchResultPages" action="${searchURL}" method="get">
<@s.hidden name="startPage" />
  <@s.hidden name="pageSize" />
  <@s.hidden name="sort" />
  <#--  Simple Search field  -->
  <@s.hidden name="query" />

  <#--  Unformatted Search field (new Advanced Search)  -->
  <@s.hidden name="unformattedQuery" />

  <#--  Find An Article Search fields  -->
  <@s.hidden name="volume" />
  <@s.hidden name="eLocationId" />
  <@s.hidden name="id" />
  <@s.hidden name="filterJournals" />
  <@s.hidden name="filterSubjects" />
  <@s.hidden name="filterArticleType" />
  <@s.hidden name="filterKeyword" />
</form>


<form name="reviseSearch" action="${advancedSearchURL}" method="get">
<@s.hidden name="noSearchFlag" value="set" />
<@s.hidden name="pageSize" />
<@s.hidden name="sort" />
<#--  Simple Search field
-->
<@s.hidden name="query" />

<#--  Unformatted Search field for the Query Builder (new Advanced Search)
-->
<#if searchType == "findAnArticle">
  <input type="hidden" name="unformattedQuery" value="${queryAsExecuted}"/>
<#else>
  <@s.hidden name="unformattedQuery" />
</#if>

<#--  Find An Article Search fields
-->
<@s.hidden name="volume" />
<@s.hidden name="eLocationId" />
<@s.hidden name="id" />
<@s.hidden name="filterJournals" />
<@s.hidden name="filterSubjects" />
<@s.hidden name="filterArticleType" />
<@s.hidden name="filterKeyword" />
</form>

<div id="nav-main" class="nav txt-lg">
  <#include "/global/global_nav.ftl">
</div>

</div><!-- pagehdr-->
</div><!-- pagehdr-wrap -->

<div id="hdr-search-results">
  <div id="db">
    <form name="searchForm" action="/search/simpleSearch.action" method="get" id="searchForm">
      <input type="hidden" name="from" value="globalSimpleSearch"> <input type="hidden" name="filterJournals" value="PLoSONE">
      <fieldset>
        <legend>Search</legend> <label for="search">Search</label>
        <div class="wrap">
          <input id="search" type="text" name="query" placeholder="implicit learning and autism">
          <input type="image" alt="SEARCH" src="/images/icon.search.gif">
        </div>
      </fieldset>
    </form>
    <a id="advSearch" class="btn" href="TEST" name="advSearch">advanced</a>
  </div>
  <div id="search-options" class="cf">
    <div class="section">
      <b>Sort:</b>
      <span class="btn active">most recent</span>
      <a href="TEST" class="btn">most viewed</a>
      <a href="TEST" class="btn">most cited</a>
    </div>
    <div class="section view">
      <b>View as:</b>
      <span class="figs">Figures</span>
      <a href="TEST" class="list">List</a>
    </div>
  </div>
</div><!-- hdr-fig-search -->

<div id="pagebdy-wrap" class="bg-dk">
<div id="pagebdy">

<div id="search-results-block" class="cf">

<div class="header hdr-results">
  <h2>${totalNoOfResults} results for <span>${query?html}</span></h2>
</div>

<div class="main">

  <ul id="search-results">
    <#list searchResults as hit>
      <li doi="${hit.uri}" pdate="${hit.date.getTime()?string.computer}">
              <span class="article">
               <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="info:doi/${hit.uri}" includeParams="none"/>
               <@s.a href="${(freemarker_config.getJournalUrlFromIssn(hit.issn))!(freemarker_config.doiResolverURL)}%{fetchArticleURL}" title="Read Open-Access Article"><@articleFormat>${hit.title}</@articleFormat></@s.a>
              </span>
        <span class="authors"> <#-- hitScore: ${hit.hitScore} --> ${hit.creator!""}</span>
        <#if hit.highlight??><span class="cite">${hit.highlight}</span></#if>
        <#if filterJournals?size == 1 && filterJournals?first == freemarker_config.getIssn(journalContext)>
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
          <span class="uri">${hit.uri?replace("info:doi/", "doi:")}</span>
        </#if>
      </li>
    </#list>
  </ul>

  <div class="pagination">
    <span class="prev">&lt;</span>
    <strong>1</strong>
    <a href="TEST">2</a>
    <a href="TEST">3</a>
    <span>...</span>
    <a href="TEST">6</a>
    <a href="TEST" class="next">&gt;</a>
  </div>

</div>

<div class="sidebar">

  <div class="block blk-style-a blk-search-history">
    <div class="header">
      <h3>Search History</h3>
    </div>
    <div class="body">
      <#assign recentSearchDisplayTextMaxLength = 28>
      <#if recentSearches?? && recentSearches?size gt 0>
        <dl id="recentSearches" class="facet">
          <#list recentSearches?keys?reverse as key>
            <#if key?length gt recentSearchDisplayTextMaxLength>
              <dd><a href="${recentSearches[key]}" title="${key}">${key?substring(0,recentSearchDisplayTextMaxLength-2)}...</a></dd>
            <#else>
              <dd><a href="${recentSearches[key]}" title="${key}">${key}</a></dd>
            </#if>
          </#list>
        </dl>
      </#if>
    </div>
  </div>

  <!-- This block for Phase 2 development -->
  <div class="block blk-style-a blk-related-collections">
    <div class="header">
      <h3>Related Collections</h3>
    </div>
    <div class="body">
      <#if ((totalNoOfResults gt 0) && (fieldErrors?size == 0))>
        <#if (resultsSinglePage.authorFacet??)>
          <h4>Authors</h4>
          <ul class="actions">
            <#list resultsSinglePage.authorFacet as f>
              <#if f_index < max_authors>
                <li>
                  <a href="${advancedSearchURL}?unformattedQuery=author%3A%22${f.name?url}%22&from=authorLink&sort=${sorts[0]?url}">${f.name}</a>
                  <span class="icons">
                    <a href="TEST"><img src="/images/icon.rss.16.png" width="16" height="17" alt="RSS" title="RSS"></a>
                    <a href="TEST"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert" title="Alert"></a>
                    <a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail" title="E-mail"></a>
                  </span>
                </li>
              </#if>
            </#list>
          </ul>
        </#if>

        <#if (resultsSinglePage.editorFacet??)>
          <h4>Editors</h4>
          <ul class="actions">
            <#list resultsSinglePage.editorFacet as f>
              <#if f_index < max_editors>
                <li>
                  <a href="${advancedSearchURL}?unformattedQuery=editor%3A%22${f.name?url}%22&from=editorLink&sort=${sorts[0]?url}">${f.name}</a>
                  <span class="icons">
                    <a href="TEST"><img src="/images/icon.rss.16.png" width="16" height="17" alt="RSS" title="RSS"></a>
                    <a href="TEST"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert" title="Alert"></a>
                    <a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail" title="E-mail"></a>
                  </span>
                </li>
              </#if>
            </#list>
          </ul>
        </#if>

        <#if (resultsSinglePage.institutionFacet??)>
          <h4>Institutions:</h4>
          <ul class="actions">
            <#list resultsSinglePage.institutionFacet as f>
              <#if f_index < max_institutions>
                <li>
                  <a href="${advancedSearchURL}?unformattedQuery=affiliate%3A%22${f.name?url}%22&from=institutionLink&sort=${sorts[0]?url}">${f.name}</a>
                  <span class="icons">
                    <a href="TEST"><img src="/images/icon.rss.16.png" width="16" height="17" alt="RSS" title="RSS"></a>
                    <a href="TEST"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert" title="Alert"></a>
                    <a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail" title="E-mail"></a>
                  </span>
                </li>
              </#if>
            </#list>
          </ul>
        </#if>
      </#if>
    </div>
  </div>
</div>

</div>

</div><!-- pagebdy -->
</div><!-- pagebdy-wrap -->