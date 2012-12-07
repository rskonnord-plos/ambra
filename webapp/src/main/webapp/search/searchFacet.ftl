<#import "search_variables.ftl" as search>

<div id="content" class="more">
  <a href="${searchURL}?<@URLParameters parameters=searchParameters />&searchType=${searchType}&from=moreFacetGoBackLink"><strong><
    Back to Search Results</strong></a><br/>
<#if facetName == "subjects">
  <h1>More Top Subject Categories</h1>

  <div id="fsSubjectOpt" class="clearfix">
    <#assign colSize = (resultsSinglePage.subjectFacet?size / 2) + 0.5>
    <ul class="clm">
      <#list resultsSinglePage.subjectFacet as subject>
        <#if (subject_index + 1) lte colSize>
          <li><a
              href="${searchURL}?<@URLParameters parameters=searchParameters names="filterSubjects,startPage" values=[subject.name,0] method="add" />&searchType=${searchType}&from=moreFacetSubjectLink">${subject.name}</a>
            (${subject.count})
          </li>
        </#if>
      </#list>
    </ul>
    <ul class="clm">
      <#list resultsSinglePage.subjectFacet as subject>
        <#if (subject_index + 1) gt colSize>
          <li><a
              href="${searchURL}?<@URLParameters parameters=searchParameters names="filterSubjects,startPage" values=[subject.name,0] method="add" />&searchType=${searchType}&from=moreFacetSubjectLink">${subject.name}</a>
            (${subject.count})
          </li>
        </#if>
      </#list>
    </ul>
  </div>
</#if>

<#if facetName == "articleTypes">
  <h1>More Top Article Types</h1>

  <div id="fsSubjectOpt" class="clearfix">
    <#assign colSize = (resultsSinglePage.articleTypeFacet?size / 2) + 0.5>
    <ul class="clm">
      <#list resultsSinglePage.articleTypeFacet as articleType>
        <#if (articleType_index + 1) lte colSize>
          <li><a
              href="${searchURL}?<@URLParameters parameters=searchParameters names="filterArticleType,startPage" values=[articleType.name,0] />&searchType=${searchType}&from=moreFacetArticleTypeLink">${articleType.name}</a>
            (${articleType.count})
          </li>
        </#if>
      </#list>
    </ul>
    <ul class="clm">
      <#list resultsSinglePage.articleTypeFacet as articleType>
        <#if (articleType_index + 1) gt colSize>
          <li><a
              href="${searchURL}?<@URLParameters parameters=searchParameters names="filterArticleType,startPage" values=[articleType.name,0] />&searchType=${searchType}&from=moreFacetArticleTypeLink">${articleType.name}</a>
            (${articleType.count})
          </li>
        </#if>
      </#list>
    </ul>
  </div>
</#if>
  <a href="${searchURL}?<@URLParameters parameters=searchParameters />&searchType=${searchType}&from=moreFacetGoBackLink"><strong><
    Back to Search Results</strong></a>
</div><#-- content -->
<#-- end : main content wrapper -->