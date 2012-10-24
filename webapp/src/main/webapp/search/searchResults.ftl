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
    <#if (totalPages lt 4) >
      <#list 1..totalPages as pageNumber>
        <#if pageNumber == currentPage>
        <strong>${currentPage}</strong>
        <#else>
        <a href="${url}?<@URLParameters parameters=searchParameters names="startPage" values=[pageNumber - 1] />">${pageNumber}</a>
        </#if>
      </#list>

    <#else> <#-- totalPages >= 4 -->
      <#if (currentPage gt 3) >
      ...
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
    </#if>
  </#if>
</#macro>

<#assign totalPages = ((totalNoOfResults + pageSize - 1) / pageSize)?int>

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

<div id="hdr-search-results">
  <div id="db">
    <form name="searchForm" action="/search/simpleSearch.action" method="get" id="searchForm">
      <input type="hidden" name="from" value="globalSimpleSearch"> <input type="hidden" name="filterJournals"
                                                                          value="PLoSONE">
      <fieldset>
        <legend>Search</legend>
        <label for="search">Search</label>

        <div class="wrap">
          <input id="search" type="text" name="query" placeholder="${queryAsExecuted}">
          <input type="image" alt="SEARCH" src="/images/icon.search.gif">
        </div>
      </fieldset>
    </form>
    <a id="advSearch" class="btn" href="TEST" name="advSearch">advanced</a>
  </div>
  <div class="options">
    <span class="clear-filter"><a id="clearAllFilters" href="TEST" class="btn">Clear all filters</a></span>

    <div class="resultSort">
      <select name="sort" id="sortPicklist">
        <option value="" selected>Sort by</option>
        <option value="Relevance">Relevance</option>
        <option value="Date, newest first">Date, newest first</option>
        <option value="Date, oldest first">Date, oldest first</option>
        <option value="Most views, last 30 days">Most views, last 30 days</option>
        <option value="Most views, all time">Most views, all time</option>
        <option value="Most cited, all time">Most cited, all time</option>
        <option value="Most bookmarked">Most bookmarked</option>
        <option value="Most shared in social media">Most shared in social media</option>
      </select>
    </div>
  </div>
  <div class="filter-block cf">
    <div class="filter-item">
      PLOS Biology&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
    <div class="filter-item">
      PLOS Collections&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
    <div class="filter-item">
      PLOS Medicine&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
    <div class="filter-item">
      PLOS Neglected Tropical Diseases&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
    <div class="filter-item">
      "Non-Clinical Medicine"&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
    <div class="filter-item">
      "Public Health and Epidemiology"&nbsp;
      <img src="/images/btn.close.png" class="clear-filter" title="Clear this filter" alt="Clear this filter">
    </div>
  </div>

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

  <div id="journalFacet" class="facet">
    <dl>
      <dt>Journals</dt>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSONE"> PLoS ONE (15,467)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSPathogens"> PLoS Pathogens (1,611)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSGenetics"> PLoS Genetics (905)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSBiology"> PLoS Biology (770)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSCompBiol"> PLoS Computational Biology (703)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSNTD"> PLoS Neglected Tropical Diseases (534)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSMedicine"> PLoS Medicine (412)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSClinicalTrials"> PLoS Hub for Clinical Trials (226)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterJournals" value="PLoSCollections"> PLoS Collections (175)</label>
      </dd>
    </dl>
  </div>

  <div id="topicFacet" class="facet">
    <dl>
      <dt>Topics</dt>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Cell Biology"> Cell Biology (264)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Genetics and Genomics"> Genetics and Genomics (204)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Neuroscience"> Neuroscience (194)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Developmental Biology"> Developmental Biology (153)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Molecular Biology"> Molecular Biology (127)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Biochemistry"> Biochemistry (122)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Immunology"> Immunology (118)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Computational Biology"> Computational Biology (114)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Evolutionary Biology"> Evolutionary Biology (96)</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value="Microbiology"> Microbiology (95)</label>
      </dd>
      <dd>
        <label><span class="view-more">See more...</span></label>
      </dd>
    </dl>

    <dl class="more">
      <dt>More Topics</dt>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><input type="checkbox" name="filterSubjects" value=""> Additional Topic</label>
      </dd>
      <dd>
        <label><a href="#hdr-search-results" class="view-less">See less...</a></label>
      </dd>
    </dl>
  </div>

  <div id="keywordFacet" class="facet">
    <dl>
      <dt>Where my keywords appear</dt>
      <dd>
        <a href="TEST">Body (193)</a>
      </dd>
      <dd>
        <a href="TEST">Results and Discussion (75)</a>
      </dd>
      <dd>
        <a href="TEST">References (21)</a>
      </dd>
      <dd>
        <a href="TEST">Introduction (9)</a>
      </dd>
      <dd>
        <a href="TEST">Materials and Methods (8)</a>
      </dd>
      <dd>
        <a href="TEST">Abstract (1)</a>
      </dd>
      <dd>
        <a href="TEST">Supporting Information (1)</a>
      </dd>
      <dd>
        <a href="TEST">Title (1)</a>
      </dd>
    </dl>
  </div>

  <div id="articleTypeFacet" class="facet">
    <dl>
      <dt>Article Type</dt>
      <dd>
        <a href="TEST">Research Article (629)</a>
      </dd>
      <dd>
        <a href="TEST">Synopsis (49)</a>
      </dd>
      <dd>
        <a href="TEST">Primer (39)</a>
      </dd>
      <dd>
        <a href="TEST">Essay (19)</a>
      </dd>
      <dd>
        <a href="TEST">Feature (19)</a>
      </dd>
      <dd>
        <a href="TEST">Unsolved Mystery (5)</a>
      </dd>
      <dd>
        <a href="TEST">Community Page (4)</a>
      </dd>
      <dd>
        <a href="TEST">Perspective (3)</a>
      </dd>
      <dd>
        <a href="TEST">Book Review/Science in the Media (2)</a>
      </dd>
      <dd>
        <a href="TEST">Obituary (1)</a>
      </dd>
    </dl>
  </div>

  <div id="authorFacet" class="facet">
    <dl>
      <dt>Authors</dt>
      <dd>
        <a href="TEST">Christoph Kayser</a>
      </dd>
      <dd>
        <a href="TEST">Christopher I Petkov</a>
      </dd>
      <dd>
        <a href="TEST">Mark Augath</a>
      </dd>
      <dd>
        <a href="TEST">ikos K Logothetis</a>
      </dd>
      <dd>
        <a href="TEST">Robin A A Ince</a>
      </dd>
    </dl>
  </div>
  </div>

</div><!-- hdr-fig-search -->

<div id="pagebdy-wrap" class="bg-dk">
  <div id="pagebdy">

    <div id="search-results-block" class="cf">

      <div class="header hdr-results">
        <h2>${totalNoOfResults} results for <span>${query?html}</span></h2>

        <div id="search-view">
          View as:
          <a href="TEST" class="figs">Figures</a>
          <span class="list">List</span>
        </div>
        <div id="connect" class="nav">
          <span class="txt">Like this collection?</span>
          <ul class="lnk-social cf">
            <li class="lnk-alert ir"><a href="TEST" title="Alert">Alert</a></li>
            <li class="lnk-email ir"><a href="TEST" title="E-mail">E-mail</a></li>
            <li class="lnk-rss ir"><a href="http://www.plosone.org/article/feed" title="RSS">RSS</a></li>
          </ul>
        </div>
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
        <#-- &lt; and &gt; are fallbacks for browsers without CSS. Even with CSS, they must not be blank. -->
        <#if startPage == 0>
          <span class="prev">&lt;</span>
        <#else>
          <a href="${searchURL}?<@URLParameters parameters=searchParameters names="startPage" values=[startPage - 1] />"
             class="prev">&lt;</a>
        </#if>

        <@renderSearchPaginationLinks searchURL totalPages startPage />

        <#if startPage == totalPages - 1>
          <span class="next">&lt;</span>
        <#else>
          <a href="${searchURL}?<@URLParameters parameters=searchParameters names="startPage" values=[startPage + 1] />"
             class="next">&gt;</a>
        </#if>
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
                    <a href="TEST"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert"
                                        title="Alert"></a>
                    <a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail"
                                        title="E-mail"></a>
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
                    <a href="TEST"><img src="/images/icon.alert.16.png" width="16" height="17" alt="Alert"
                                        title="Alert"></a>
                    <a href="TEST"><img src="/images/icon.email.16.b.png" width="16" height="17" alt="E-mail"
                                        title="E-mail"></a>
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

  </div>
  <!-- pagebdy -->
</div><!-- pagebdy-wrap -->