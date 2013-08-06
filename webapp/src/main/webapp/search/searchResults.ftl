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

<#assign max_authors_filter = 15>
<#assign max_subjects_filter = 10>
<#assign max_articletypes_filter = 10>

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

<#macro renderSearchPaginationLinks url totalPages currentPageParam>
<#--
  currentPage is zero based
  SOLR (the action class) expects a startPage parameter of 0 to N
  We change this to a nonZero value here to make things a bit more readable

  It supports the following use cases and will output the following:

  Current page is the start or end
  Current Page is 1:
  < 1 2 3  ... 10 >
  Current Page is 10:
  < 1 ...8 9 10 >

  Current page is 5:
  (Current page is greater then 2 pages away from start or end)
  < 1 ...4 5 6 ... 10 >

  Current page is less then 2 pages away from start or end:
  Current Page is 8:
  < 1 ...7 8 9 10 >
  < 1 2 3 4 ... 10 >
-->
  <#assign currentPage = currentPageParam + 1/>

  <#if (totalPages gt 1 )>
  <div class="pagination">
    <#if (totalPages lt 4) >
      <#if (currentPage gt 1) >
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage - 2] />"
           class="prev">&lt;</a>&nbsp;
      <#else>
        <span class="prev">&lt;</span>
      </#if>

      <#list 1..totalPages as pageNumber>
        <#if pageNumber == currentPage>
          <strong>${currentPage}</strong>
        <#else>
          <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[pageNumber - 1] />">${pageNumber}</a>
        </#if>
      </#list>

      <#if (currentPage lt totalPages)>
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage] />"
           class="next">
          &gt;</a>
      <#else>
        <span class="next">&gt;</span>
      </#if>
    <#else>
      <#if (currentPage gt 1) >
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage - 2] />"
           class="prev">&lt;</a>
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[0] />">1</a>
      <#else>
        <span class="prev">&lt;</span><strong>1</strong>
      </#if>
      <#if (currentPage gt 3) >
        <span>...</span>
      </#if>

    <#--
      Yes the following statements are confusing,
      but it should take care of all the use cases defined at the top
    --->
      <#list min(currentPage - 1,0)..max(3,(currentPage + 1)) as pageNumber>
        <#if ((pageNumber > 1 && pageNumber < totalPages && pageNumber > (currentPage - 2)
        || ((pageNumber == (totalPages - 2)) && (pageNumber > (currentPage - 3)))))>
          <#if (currentPage == pageNumber)>
            <strong>${pageNumber}</strong>
          <#else>
            <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[pageNumber - 1] />">${pageNumber}</a>
          </#if>
        </#if>
      </#list>
      <#if (currentPage lt (totalPages - 2))>
        ...
      </#if>
      <#if (currentPage lt totalPages)>
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[totalPages - 1] />">${totalPages}</a>
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[currentPage] />"
           class="next">&gt;</a>
      <#else>
        <strong>${totalPages}</strong>
        <span class="next">&gt;</span>
      </#if>
    </#if>
  </div>
  </#if>
</#macro>

<#assign totalPages = ((totalNoOfResults + pageSize - 1) / pageSize)?int>

<form name="searchFormOnSearchResultsPage" id="searchFormOnSearchResultsPage" action="${searchURL}" method="get">

<div id="hdr-search-results">
<div id="db">
<@s.hidden name="startPage" />
<@s.hidden name="filterArticleType" />
<@s.hidden name="filterKeyword" />

<#if searchType == "simple">
  <fieldset>
    <legend>Search</legend>
    <label for="searchOnResult">Search</label>
    <div class="wrap">
      <input id="searchOnResult" type="text" name="query" value="${query?html}">
      <input type="image" alt="SEARCH" src="/images/icon.search.gif">
    </div>
  </fieldset>
  <a id="advSearch" class="btn" href="${advancedSearchURL}?query=${query?html}&noSearchFlag=set" name="advSearch">advanced</a>
<#else>
  <fieldset>
    <legend>Search</legend>
    <label for="searchOnResult">Search</label>
    <div class="wrap">
      <input id="searchOnResult" type="text" name="unformattedQuery" value="${queryAsExecuted?html}">
      <input type="image" alt="SEARCH" src="/images/icon.search.gif">
    </div>
  </fieldset>
  <a id="advSearch" class="btn" href="${advancedSearchURL}?<@URLParameters parameters=searchParameters />&noSearchFlag=set" name="advSearch">advanced</a>
</#if>
</div>

<#if (totalNoOfResults > 0)>
<div class="options">
    <span class="clear-filter">
      <a id="clearAllFilters" href="${searchURL}?<@URLParameters parameters=searchParameters names="filterKeyword,filterArticleType,filterJournals,filterSubjects,startPage" values="" />" class="btn">Clear all filters</a>
    </span>
  <div class="resultSort">
    <select name="sort" id="sortPicklist">
      <#list sorts as sortItem>
        <#if ((!sort?? || (sort?? && sort == "")) && (sortItem_index == 0))>
          <option selected value="${sortItem}">${sortItem}</option>
        <#else>
          <#if (sort?? && (sort == sortItem))>
            <option selected value="${sortItem}">${sortItem}</option>
          <#else>
            <option value="${sortItem}">${sortItem}</option>
          </#if>
        </#if>
      </#list>
    </select>
  </div>
</div>


  <#if ((filterSubjects?size > 0) || (filterJournals?size > 0) || filterArticleType != "" ||
  (filterArticleType?length > 1) || (filterAuthors?size > 0) || filterKeyword != "")>
  <div class="filter-block cf">
    <#if (filterJournals?size > 0)>
      <div class="filter-item">
      ${filterJournalsAsString}&nbsp;
        <img id="clearJournalFilter" src="/images/btn.close.png" class="clear-filter" title="Clear journals filter" alt="Clear journals filter">
      </div>
    </#if>
    <#if (filterSubjects?size > 0)>
      <div class="filter-item">
        Subject categories:
        <#list filterSubjects as subject>"${subject}" <#if (subject_index) gt filterSubjects?size - 3><#if subject_has_next> and </#if><#else><#if subject_has_next>, </#if></#if></#list>
        &nbsp;<img id="clearSubjectFilter" src="/images/btn.close.png" class="clear-filter" title="Clear topics filter" alt="Clear topics filter">
      </div>
    </#if>
    <#if (filterAuthors?size > 0)>
      <div class="filter-item">
        Authors:
        <#list filterAuthors as author>"${author}" <#if (author_index) gt filterAuthors?size - 3><#if author_has_next> and </#if><#else><#if author_has_next>, </#if></#if></#list>
        &nbsp;<img id="clearAuthorFilter" src="/images/btn.close.png" class="clear-filter" title="Clear authors filter" alt="Clear authors filter">
      </div>
    </#if>
    <#if (filterArticleType != "")>
      <div class="filter-item">
        Article Type: ${filterArticleType}&nbsp;
        <a href="${searchURL}?<@URLParameters parameters=searchParameters names="filterArticleType,startPage" values="" />&from=articleTypeClearFilterLink">
          <img src="/images/btn.close.png" class="clear-filter" title="Clear article type filter" alt="Clear article type filter"></a>
      </div>
    </#if>
    <#if (filterKeyword != "")>
      <div class="filter-item">
        Searching in: ${filterKeyword}&nbsp;
        <a href="${searchURL}?<@URLParameters parameters=searchParameters names="filterKeyword,startPage" values="" />&from=keywordFilterClearLink">
          <img src="/images/btn.close.png" class="clear-filter" title="Clear searching in filter" alt="Clear searching in filter"></a>
      </div>
    </#if>
  </div>
  </#if>


<div id="search-facets">
  <div class="menu">
    <!--<div class="item" data-facet="dateFacet">Date</div> -->
    <div class="item" data-facet="journalFacet">Journals</div>
    <div class="item" data-facet="topicFacet">Topics</div>
    <div class="item" data-facet="authorFacet">Authors</div>
    <div class="item" data-facet="keywordFacet">Where my keywords appear</div>
    <div class="item" data-facet="articleTypeFacet">Article Type</div>
  </div>

  <!--
<div id="dateFacet" class="facet">
<label for="startDateAsStringId">Content posted between:</label>
<input type="text" name="startDateAsString" maxlength="10" placeholder="YYYY-MM-DD" id="startDateAsStringId" />
<label for="endDateAsStringId">and</label>
<input type="text" name="endDateAsString" maxlength="10" placeholder="YYYY-MM-DD" id="endDateAsStringId" />
<input type="button" class="btn" value="apply" title="apply" />
</div>    -->

  <#if (resultsSinglePage.journalFacet??)>
    <div id="journalFacet" class="facet">
      <dl>
        <dt>Journals</dt>
        <#list resultsSinglePage.journalFacet as f>
          <dd>
            <label><input type="checkbox" name="filterJournals" value="${f.name}"
              <#if (filterJournals?seq_contains(f.name)) > checked="true"</#if>> ${freemarker_config.getDisplayName(f.name)}
              (${f.count})</label>
          </dd>
        </#list>
      </dl>
    </div>
  </#if>

  <#if (resultsSinglePage.subjectFacet??)>
    <div id="topicFacet" class="facet">
      <dl>
        <dt>Topics</dt>
        <#list resultsSinglePage.subjectFacet as f>
          <#if f_index lt max_subjects_filter>
            <dd>
              <label><input type="checkbox" name="filterSubjects" value="${f.name}"
                <#if (filterSubjects?seq_contains(f.name)) > checked="true"</#if>> ${f.name} (${f.count})</label>
            </dd>
          </#if>
        </#list>
        <#if resultsSinglePage.subjectFacet?size gt max_subjects_filter>
          <dd>
            <label><span class="view-more">See more...</span></label>
          </dd>
        </#if>
      </dl>

      <dl class="more">
        <dt>More Topics</dt>
        <#list resultsSinglePage.subjectFacet as f>
        <#-- TODO: Confirm this logic works -->
          <#if f_index gte max_subjects_filter>
            <dd>
              <label><input type="checkbox" name="filterSubjects" value="${f.name}"
                <#if (filterSubjects?seq_contains(f.name)) > checked="true"</#if>> ${f.name} (${f.count})</label>
            </dd>
          </#if>
        </#list>
        <dd>
          <label><a href="#hdr-search-results" class="view-less">See less...</a></label>
        </dd>
      </dl>
    </div>
  </#if>

  <#if (resultsSinglePage.keywordFacet??)>
    <div id="keywordFacet" class="facet">
      <dl>
        <dt>Where my keywords appear</dt>
        <#list resultsSinglePage.keywordFacet as f>
          <dd>
            <a href="${searchURL}?<@URLParameters parameters=searchParameters names="filterKeyword,startPage" values=[f.name, 0] />&from=keywordFilterLink">${f.name}
              (${f.count})</a>
          </dd>
        </#list>
      </dl>
    </div>
  </#if>

  <#if filterArticleType == "">
    <#if (resultsSinglePage.articleTypeFacet??)>
      <div id="articleTypeFacet" class="facet">
        <dl>
          <dt>Article Type</dt>
          <#list resultsSinglePage.articleTypeFacet as f>
            <#if f_index lt max_articletypes_filter>
              <dd>
                <a href="${searchURL}?<@URLParameters parameters=searchParameters names="filterArticleType,startPage" values=[f.name, 0] />&from=articleTypeFilterLink">${f.name} (${f.count})</a>
              </dd>
            </#if>
          </#list>
        <#-- TODO: Confirm this logic works -->
          <#if resultsSinglePage.articleTypeFacet?size gt max_articletypes_filter>
            <dd>
              <label><span class="view-more">See more...</span></label>
            </dd>
          </#if>
        </dl>

        <dl class="more">
          <dt>More Article Types</dt>
          <#list resultsSinglePage.articleTypeFacet as f>
          <#-- TODO: Confirm this logic works -->
            <#if f_index gte max_articletypes_filter>
              <dd>
                <a href="${searchURL}?<@URLParameters parameters=searchParameters names="filterArticleType,startPage" values=[f.name, 0] />&from=articleTypeFilterLink">${f.name} (${f.count})</a>
              </dd>
            </#if>
          </#list>
          <dd>
            <label><a href="#hdr-search-results" class="view-less">See less...</a></label>
          </dd>
        </dl>

      </div>
    </#if>
  </#if>

  <#if (resultsSinglePage.authorFacet??)>
    <div id="authorFacet" class="facet">
      <dl>
        <dt>Authors</dt>
        <#list resultsSinglePage.authorFacet as f>
          <#if f_index lt max_authors_filter>
            <dd>
              <label><input type="checkbox" name="filterAuthors" value="${f.name}"
                <#if (filterAuthors?seq_contains(f.name)) > checked="true"</#if>> ${f.name}
                (${f.count})</label>
            </dd>
          </#if>
        </#list>
      <#-- TODO: Confirm this logic works -->
        <#if resultsSinglePage.authorFacet?size gt max_authors_filter>
          <dd>
            <label><span class="view-more">See more...</span></label>
          </dd>
        </#if>
      </dl>

      <dl class="more">
        <dt>More Authors</dt>
        <#list resultsSinglePage.authorFacet as f>
        <#-- TODO: Confirm this logic works -->
          <#if f_index gte max_authors_filter>
            <dd>
              <label><input type="checkbox" name="filterAuthors" value="${f.name}"
                <#if (filterAuthors?seq_contains(f.name)) > checked="true"</#if>> ${f.name} (${f.count})</label>
            </dd>
          </#if>
        </#list>
        <dd>
          <label><a href="#hdr-search-results" class="view-less">See less...</a></label>
        </dd>
      </dl>

    </div>
  </#if>
</div>
</#if>
</div><!-- hdr-fig-search -->
</form>

<div id="pagebdy-wrap" class="bg-dk">
  <div id="pagebdy">

  <#if (fieldErrors?? && numFieldErrors > 0)>
    <div class="error">
      <br/>
      <h1>There was a problem with the terms you entered.</h1>
      <p>Please enter different terms
        or try our <a href="${advancedSearchURL}">advanced search</a>.</p>

      <#list fieldErrors?keys as key>
        <#list fieldErrors[key] as errorMessage>
        ${errorMessage}
        </#list>
      </#list>
    </div>
  <#else>
    <#if ((totalNoOfResults == 0))>
      <div id="search-results-block" class="cf">
        <#if ((filterSubjects?size > 0) || (filterJournals?size > 0) || (filterArticleType?length > 1))>
          <br/>
          <h1>You searched for articles that have all of the following:</h1>

          <p>Search Term(s): <strong>"${queryAsExecuted?html}"</strong></p>
        </#if>
        <#if (filterAuthors?size > 0)>
          Author(s):
          <b><#list filterAuthors as author>"${author}
            " <#if (author_index) gt filterAuthors?size - 3><#if author_has_next>
              and </#if><#else><#if author_has_next>, </#if></#if></#list></b>
          <br/>
        </#if>
        <#if (filterSubjects?size > 0)>
          Subject categories:
          <b><#list filterSubjects as subject>"${subject}
            " <#if (subject_index) gt filterSubjects?size - 3><#if subject_has_next>
              and </#if><#else><#if subject_has_next>, </#if></#if></#list></b>
          <br/>
        </#if>
        <#if (filterJournals?size > 0)>
          Journal(s):
          <b><#list filterJournals as journal>"${freemarker_config.getDisplayName(journal)}
            "<#if (journal_index) gt filterJournals?size - 3><#if journal_has_next>
              and </#if><#else><#if journal_has_next>, </#if></#if></#list></b>
          <br/>
        </#if>
        <#if (filterArticleType?length > 1)>
          Article Type: ${filterArticleType}
          <br/>
        </#if>
        <br/>
        There were no results, please <a href="${advancedSearchURL}?<@URLParameters parameters=searchParameters />&noSearchFlag=set">refine your search</a> and try again. <br/>
        <br/>
        <br/>
      </div>
    <#else>
      <div id="search-results-block" class="cf">

        <div class="header hdr-results">
          <h2>${totalNoOfResults} results for <span>${query?html}</span></h2>

          <div id="search-view">
            View as:
            <a href="TEST" class="figs">Figures</a>
            <span class="list">List</span>
          </div>
          <div id="connect" class="nav">
            <ul class="lnk-social cf">
              <li class="lnk-alert ir"><a href="#save-search-box" class="save-search" title="Alert">Alert</a></li>
            <#-- (For the "fuuuuture") li class="lnk-email ir"><a href="TEST" title="E-mail">E-mail</a></li-->
              <li class="lnk-rss ir"><a href="${rssSearchURL}?<@URLParameters parameters=searchParameters />" title="RSS">RSS</a></li>
            </ul>
          </div>
        </div>

        <div class="main">

          <ul id="search-results">
            <#list searchResults as hit>
              <li data-doi="${hit.uri}" data-pdate="${hit.date.getTime()?string.computer}">
                  <span class="article">
                   <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="info:doi/${hit.uri}" includeParams="none"/>
                   <@s.a href="${(freemarker_config.getJournalUrlFromIssn(hit.issn))!(freemarker_config.doiResolverURL)}%{fetchArticleURL}" title="Read Open-Access Article"><@articleFormat>${hit.title}</@articleFormat></@s.a>
                  </span>
                <span class="authors">${hit.creator!""}</span>

                <#if hit.articleTypeForDisplay??>
                ${hit.articleTypeForDisplay} |
                </#if>
                <#if hit.date??>
                  published ${hit.date?string("dd MMM yyyy")} |
                </#if>
                <#if hit.issn??>
                  ${freemarker_config.getDisplayNameByEissn(hit.issn)}
                </#if>
              </li>
            </#list>
          </ul>

          <@renderSearchPaginationLinks searchURL totalPages startPage />

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
                      <dd><a href="${recentSearches[key]}"
                             title="${key}">${key?substring(0,recentSearchDisplayTextMaxLength-2)}...</a></dd>
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
              <h3>Related</h3>
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
                            <a href="${rssSearchURL}?unformattedQuery=author%3A%22${f.name?url}%22&from=authorLink&sort=${sorts[0]?url}"><img src="/images/icon.rss.16.png" width="16" height="17" alt="RSS" title="RSS"></a>
                            <a href="#save-search-box" class="save-search"><img src="/images/icon.alert.16.png"
                                                                             width="16" height="17"
                                                      alt="Alert"
                                                title="Alert"></a>
                          <#-- (For the "fuuuuture") a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail"
                          title="E-mail"></a-->
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
                            <a href="${rssSearchURL}?unformattedQuery=editor%3A%22${f.name?url}%22&from=editorLink&sort=${sorts[0]?url}"><img src="/images/icon.rss.16.png" width="16" height="17" alt="RSS" title="RSS"></a>
                            <a href="#save-search-box" class="save-search"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert"
                                                title="Alert"></a>
                          <#-- (For the "fuuuuture") a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail"
                          title="E-mail"></a-->
                          </span>
                        </li>
                      </#if>
                    </#list>
                  </ul>
                </#if>

                <#if (resultsSinglePage.institutionFacet??)>
                  <h4>Institutions:</h4>
                  <#list resultsSinglePage.institutionFacet as f>
                    <#if f_index < max_institutions>
                      <p>
                        <a href="${advancedSearchURL}?unformattedQuery=affiliate%3A%22${f.name?url}%22&from=institutionLink&sort=${sorts[0]?url}">${f.name}</a>
                      </p>
                    </#if>
                  </#list>
                </#if>
              </#if>
            </div>
          </div>
        </div>
      </div>
    </#if>
  </#if>
  </div>
  <!-- pagebdy -->
</div><!-- pagebdy-wrap -->