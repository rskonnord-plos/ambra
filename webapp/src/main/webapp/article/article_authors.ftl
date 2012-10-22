<#import "/article/article_variables.ftl" as article>

<div id="pagebdy-wrap">
  <div id="pagebdy">

    <div id="article-block" class="cf">
      <div class="article-meta cf">
        <ul>
          <li><span class="num">1,278</span> views</li>
          <li><span class="num">12</span> citations</li>
        </ul>
        <div class="article-type">
          <span class="type oa">Open Access Article</span>
          <span class="type pr">Peer-Reviewed</span>
        </div>
      </div>
      <div class="header" id="hdr-article">
        <div class="article-kicker">RESEARCH IN TRANSLATION</div>
        <h1><@articleFormat>${article.docTitle}</@articleFormat></h1>
        <p class="authors">
          <#if authorExtras?? >
            <#list authorExtras as author>
              <#if (author_has_next)>
                <span rel="dc:creator" class="author"><span class="person" property="foaf:name" typeof="foaf:Person">${author.authorName}<#if author.equalContrib == "yes">&nbsp;<span class="equal-contrib" title="These authors contributed equally to this work">equal contributor</span></#if>,</span></span>
              <#else>
                <span rel="dc:creator" class="author"><span class="person" property="foaf:name" typeof="foaf:Person">${author.authorName}<#if author.equalContrib == "yes">&nbsp;<span class="equal-contrib" title="These authors contributed equally to this work">equal contributor</span></#if></span></span>
              </#if>
            </#list>
          </#if>
        </p>
      </div>
      <div class="main cf">

        <div class="nav" id="nav-article">
          <ul>
            <li><a href="${articleTabURL}">Article</a></li>
            <li><span class="active">About the Authors</span></li>
            <li><a href="${commentsTabURL}">Comments &amp; Letters</a></li>
            <li><a href="${metricsTabURL}">Metrics</a></li>
            <li><a href="${relatedTabURL}">Related Content</a></li>
            <li><a href="${relatedTabURL}">Corrections</a></li>
          </ul>
        </div>

        <div id="about-authors">
          <h2>About the Authors</h2>

          <dl class="authors">
            <#list authorExtras as author>
              <@s.url id="searchURL" includeParams="none"
              pagesize="10" queryField="author" unformattedQuery="author:\"${author.authorName}\""
              journalOpt="all" subjectCatOpt="all" filterArticleTypeOpt="all"
              namespace="/search" action="advancedSearch"/>
              <dt>
                <a href="${searchURL}">${author.authorName}</a>
              </dt>
              <dd>
                <#list author.affiliations as affiliation>
                  ${affiliation} <#if affiliation_has_next>, </#if>
                </#list>
              </dd>
            </#list>
          </dl>

        <#if correspondingAuthor?? && correspondingAuthor?size gt 0>
          <h3>Corresponding Author</h3>
          <#if correspondingAuthor?size == 1>
            <p>Email: ${correspondingAuthor[0]}</p>
          <#else>
            <ul>
              <#list correspondingAuthor as author><li>Email: ${author}</li></#list>
            </ul>
          </#if>
        </#if>

        <#if competingInterest?? && competingInterest?size gt 0>
          <h3>Competing Interests</h3>
          <#if competingInterest?size == 1>
            <p>${competingInterest[0]}</p>
          <#else>
            <ul>
              <#list competingInterest as interest>
                <li>${interest}</li></#list>
            </ul>
          </#if>
        </#if>

        <#if authorContributions?? && authorContributions?size gt 0>
          <h3>Author Contributions</h3>
          <#if authorContributions?size == 1>
            <p>${authorContributions[0]}</p>
          <#else>
            <ul>
              <#list authorContributions as contribution><li>${contribution}</li></#list>
            </ul>
          </#if>
        </#if>

        </div>

      </div><!-- main -->

      <#include "article_sidebar.ftl">

    </div><!-- article-block -->

    <#-- Not supported yet
    <div id="banner-ftr">
      <div class="content">
        <div class="col col-1">
          <h4>Did you know?</h4>
        </div>
        <div class="col col-2">
          Some great message PLOS wants to tell all readers could go here! Lorem ipsum laoreet dolore magna aliquam erat volutpat.
        </div>
        <div class="col col-3">
          <div class="stat">
            <span class="num">23</span> <span class="txt">people reading this paper now</span>
          </div>
        </div>
      </div>
    </div>
    -->

  </div><!-- pagebdy -->

</div><!-- pagebdy-wrap -->
