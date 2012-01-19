<@s.url id="articleTabURL" namespace="/article" action="fetchArticle" includeParams="all" />
<@s.url id="relatedTabURL" namespace="/article" action="fetchRelatedArticle" includeParams="all" />
<@s.url id="commentsTabURL" namespace="/article" action="fetchArticleComments" includeParams="all" />
<div class="horizontalTabs" xpathLocation="noSelect">
  <ul id="tabsContainer">
    <#switch tab>
      <#case "article">
        <li id="article" class="active"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#case "related">
        <li id="article"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="related" class="active"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#case "comments">
        <li id="article"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments" class="active"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
        <#break>
      <#default>
        <li id="article" class="active"><@s.a href="${articleTabURL}" cssClass="tab" title="Article">Article</@s.a></li>
        <li id="related"><@s.a href="${relatedTabURL}" cssClass="tab" title="Related Content">Related Content</@s.a></li>
        <li id="comments"><@s.a href="${commentsTabURL}" cssClass="tab" title="Comments">Comments: ${totalComments}</@s.a></li>
    </#switch>
  </ul>
</div>
