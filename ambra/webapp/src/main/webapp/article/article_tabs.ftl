<@s.url id="articleTabURL" namespace="/article" action="fetchArticle" includeParams="all" />
<@s.url id="metricsTabURL" namespace="/article" action="fetchArticleMetrics" includeParams="all" />
<@s.url id="relatedTabURL" namespace="/article" action="fetchRelatedArticle" includeParams="all" />
<@s.url id="commentsTabURL" namespace="/article" action="fetchArticleComments" includeParams="all" />
<@s.url id="crossRefPageURL"  namespace="/article" action="fetchArticleCrossRef" includeParams="none" articleURI="${articleURI}" />
<form action="">
  <input type="hidden" name="journalDisplayName" id="journalDisplayName" value="${freemarker_config.getDisplayName(journalContext)}" />
  <input type="hidden" name="crossRefPageURL" id="crossRefPageURL" value="${crossRefPageURL}" />
  <input type="hidden" name="metricsTabURL" id="metricsTabURL" value="${metricsTabURL}" />
  <input type="hidden" name="doi" id="doi" value="${articleURI}" />
  <input type="hidden" name="articleTitleUnformatted" id="articleTitleUnformatted" value="${articleInfoX.unformattedTitle?url}" />
  <input type="hidden" name="articlePubDate" id="articlePubDate" value="${articleInfoX.getDate().getTime()?string.computer}" />
</form>
<div class="horizontalTabs" xpathLocation="noSelect">
  <ul id="tabsContainer">
    <#switch tab>
      <#case "article">
        <li id="article" class="active"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="metrics"><@s.a href="${metricsTabURL}" cssClass="tab" title="Metrics">Metrics</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#case "related">
        <li id="article"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="metrics"><@s.a href="${metricsTabURL}" cssClass="tab" title="Metrics">Metrics</@s.a></li>
        <li id="related" class="active"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#case "metrics">
        <li id="article"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="metrics" class="active"><@s.a href="${metricsTabURL}" cssClass="tab" title="Metrics">Metrics</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#case "comments">
        <li id="article"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="metrics"><@s.a href="${metricsTabURL}" cssClass="tab" title="Metrics">Metrics</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments" class="active"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#default>
        <li id="article" class="active"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="metrics"><@s.a href="${metricsTabURL}" cssClass="tab" title="Metrics">Metrics</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
    </#switch>
  </ul>
</div>
