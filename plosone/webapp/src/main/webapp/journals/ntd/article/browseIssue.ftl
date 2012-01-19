<#macro related articleInfo>
  <#if articleInfo.relatedArticles?size gt 0>
    <dl class="related">
      <dt>Related <em>PLoS</em> Articles</dt>
      <#list articleInfo.relatedArticles as ra>
      <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${ra.uri}" includeParams="none"/>
      <dd><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${ra.title}</@s.a></dd>
      </#list>
     </dl>
  </#if>
</#macro>

<!-- begin : toc content -->
<div id="content" class="toc">
<a id="top" title="top" class="noshow">&nbsp;</a>
  <!-- begin : right-hand column -->
  <div id="rhc">
    <div id="sideNav">
    <@s.url id="archiveURL" action="browseVolume" namespace="/journals/ntd/article" field="volume" includeParams="none"/>
      <p id="issueNav">
        <#assign needSpacer=false/>
        <#if issueInfo.prevIssue?exists>
          <@s.url id="prevIssueURL" action="browseIssue" namespace="/journals/ntd/article" field="issue"  issue="${issueInfo.prevIssue}" includeParams="none"/>
          <a href="${prevIssueURL}">&lt;Previous Issue</a>
          <#assign needSpacer=true/>
        </#if>
        <!-- <#if needSpacer> | </#if> -->
        <!-- <a href="${archiveURL}">Archive</a> Commented out until we have the Archive page in place. Remember to uncomment spacers and the link in the navbar too! -->
        <#assign needSpacer=true/>
        <#if issueInfo.nextIssue?exists>
          <@s.url id="nextIssueURL" action="browseIssue" namespace="/journals/ntd/article" field="issue" issue="${issueInfo.nextIssue}" includeParams="none"/>
          <!-- <#if needSpacer> | </#if> -->
          <a href="${nextIssueURL}">Next Issue&gt;</a>
          <#assign needSpacer=true/>
        </#if>
      </p>
    <div id="floatMarker">&nbsp;</div>
    <!-- <div id="postcomment" class="fixed"> class of 'fixed' is what floats the menu. "postcomment" wrapper is probably not needed here. -->
    <div id="sectionNavTop" class="tools fixed">
      <ul>
        <li><a class="first" href="#top">Top</a></li>
        <#if issueInfo.editorials?has_content><li><a href="#editorial">Editorial</a></li></#if>
        <#if issueInfo.researchArticles?has_content><li><a href="#research">Research Articles</a></li></#if>
        <#if issueInfo.corrections?has_content><li><a href="#corrections">Corrections</a></li></#if>
      </ul>
    </div><!-- end : sectionNav -->
    <!-- </div>end : postcomment -->
    </div><!-- end : sideNav -->
  </div><!-- end : right-hand column -->
  <!-- begin : primary content area -->
  <div class="content">
  <h1>Table of Contents | ${issueInfo.displayName}</h1>
    <#if issueInfo.imageArticle?has_content>
      <@s.url id="imageSmURL" action="fetchObject" namespace="/article" uri="${issueInfo.imageArticle}.g001" representation="PNG_S" includeParams="none"/>
      <@s.url id="imageLgURL" action="slideshow" namespace="/article" uri="${issueInfo.imageArticle}" imageURI="${issueInfo.imageArticle}.g001" includeParams="none"/>
      <div id="issueImage">
        <div id="thumbnail">
          <img alt="Issue Image" src="${imageSmURL}"/>
          <a href="${imageLgURL}">View large image</a>
        </div>
        <h3>About This Image</h3>
        ${issueInfo.description}
        <p id="credit"><em>Image Credit:</em> Credit information goes here.</p>
      </div>
    </#if>
    <!-- begin : search results -->
    <div id="search-results">
      <#if issueInfo.editorials?has_content>
        <a id="editorial" class="noshow" title="Editorial">&nbsp;</a>
        <h2>Editorial</h2>
        <#list issueInfo.editorials as articleInfo>
          <div class="article">
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
            <p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
            <@related articleInfo=articleInfo/>
          </div>
        </#list>
      </#if>
	
      <#if issueInfo.researchArticles?has_content>
        <a id="research" class="noshow" title="Research Articles">&nbsp;</a>
        <h2>Research Articles</h2>
        <#list issueInfo.researchArticles as articleInfo>
          <div class="article">
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
            <p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
            <p><@s.a href="%{fetchArticleURL}#abstract1">Author Summary</@s.a></p>
            <@related articleInfo=articleInfo/>
          </div>
        </#list>
      </#if>
	
      <#if issueInfo.corrections?has_content>
        <a id="corrections" class="noshow" title="Corrections">&nbsp;</a>
        <h2>Corrections</h2>
        <#list issueInfo.corrections as articleInfo>
          <div class="article">
            <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${articleInfo.id}" includeParams="none"/>
            <h3><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${articleInfo.title}</@s.a></h3>
            <p class="authors"><#list articleInfo.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list></p>
            <@related articleInfo=articleInfo/>
          </div>
        </#list>
      </#if>
    </div> <!-- end : search results -->
  </div>
</div> <!-- end : toc content-->
