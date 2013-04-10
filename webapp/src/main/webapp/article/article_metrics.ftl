<#import "/article/article_variables.ftl" as article>

<@s.url id="twitterPageURL"  namespace="/article"
  action="fetchArticleTwitter" includeParams="none" articleURI="${articleURI}" />
<@s.url id="almInfoURL" namespace="/static" action="almInfo" includeParams="none" />

<div id="pagebdy-wrap">
  <div id="pagebdy">
    <div id="article-block" class="cf">
      <#include "../includes/article_header.ftl"/>
      <div class="main cf">

        <#assign tab="metrics" />
        <#include "../includes/article_tabs.ftl"/>

        <div id="article-metrics">
          <h2>Article Usage <a href="${almInfoURL}#usageInfo" class="ir" title="More information">info</a>
          <span id="chartSpinner"><#include "/includes/loadingcycle.ftl"></span></h2>

          <div id="usage"></div>

          <@s.url id="journalStatisticsURL" includeParams="none" namespace="/static" action="journalStatistics" />
          <@s.url id="almInfoURL" includeParams="none" namespace="/static" action="almInfo" />

          <a id="citations" name="citations"></a>
          <h2 class="topstroke">Citations <a href="${almInfoURL}#citationInfo" class="ir" title="More information">info</a>
          <span id="relatedCitesSpinner"><#include "/includes/loadingcycle.ftl"></span></h2>
          <div id="relatedCites"></div>

          <div id="socialNetworksOnArticleMetricsPage">
            <a id="other" name="other"></a>
            <h2 class="topstroke">Social Networks <a href="${almInfoURL}#socialBookmarks" class="ir" title="More information">info</a>
            <span id="relatedBookmarksSpinner"><#include "/includes/loadingcycle.ftl"></span></h2>

            <div id="relatedBookmarks"></div>
          </div>

          <h2 class="topstroke">Blogs and Media Coverage <a href="${almInfoURL}#blogCoverage" class="ir" title="More information">info</a>
          <span id="relatedBlogPostsSpinner"><#include "/includes/loadingcycle.ftl"></span></h2>
          <div id="relatedBlogPosts" style="display:none;">
            <#if trackbackCount gt 0>
              <div id="trackbackOnArticleMetricsTab" class="metrics_tile">
                <@s.a href="${relatedTabURL}#trackbackLinkAnchor"><img id="trackbackImageOnArticleMetricsTab"
                  src="${freemarker_config.context}/images/logo-trackbacks.png" alt="${trackbackCount} Trackbacks"
                  class="metrics_tile_image"/></@s.a>
                <div class="metrics_tile_footer" onclick="location.href='${relatedTabURL}#trackbackLinkAnchor';">
                  <@s.a href="${relatedTabURL}#trackbackLinkAnchor">${trackbackCount}</@s.a>
                </div>
              </div>
            </#if>
          </div>
          <div id="relatedBlogPostsError" style="display:none;"></div>

          <h2 class="topstroke">Readers <a href="${almInfoURL}#commentsNotes" class="ir" title="More information">info</a></h2>

          <div id="notesAndCommentsOnArticleMetricsTab" class="metrics_tile">
          <@s.a href="${commentsTabURL}"><img id="notesAndCommentsImageOnArticleMetricsTab"
              src="${freemarker_config.context}/images/logo-comments.png" alt="${totalNumAnnotations} Comments and Notes" class="metrics_tile_image"/></@s.a>
            <div class="metrics_tile_footer" onclick="location.href='${commentsTabURL}';">
            <@s.a href="${commentsTabURL}">${totalNumAnnotations}</@s.a>
            </div>
          </div>

          <div id="pgXtras" class="pgXtras"><a href="${freemarker_config.get("almHost")}/articles/${articleURI?url}.xml?citations=1">Download raw metrics data as XML</a></href></div>

          <div><a href="${journalStatisticsURL}#${journalContext}">Metrics information and summary data for <em>${freemarker_config.getDisplayName(journalContext)}</em></a></div>
          <div>Questions or concerns about usage data? <a href="${feedbackURL}">Please let us know.</a></div>

        </div>
      </div>
      <!-- main -->

    <#include "article_sidebar.ftl">

    </div><!-- article-block -->

  </div><!-- pagebdy -->

</div><!-- pagebdy-wrap -->

<script language="javascript">
  window.onload = function () {

    var almService = new $.fn.alm();

    var doi = $('meta[name=citation_doi]').attr("content");

    almService.setBookmarksText(doi, "relatedBookmarks", "relatedBookmarksSpinner");
    almService.setRelatedBlogsText(doi, "relatedBlogPosts", "relatedBlogPostsError", "relatedBlogPostsSpinner");
    almService.setCitesText(doi, "relatedCites", "relatedCitesSpinner");
    almService.setChartData(doi, "usage", "chartSpinner");
  };

</script>