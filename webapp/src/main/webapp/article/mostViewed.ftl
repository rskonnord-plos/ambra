<#assign numArticles = mostViewedArticles?size>
<#if (numArticles > 0)>
  <ul class="articles">
    <#list mostViewedArticles as article>
      <@s.url id="artURL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/${article.first}"/>
      <li><a href="${artURL}" title="Read Open Access Article">${article.second}</a></li>
    </#list>
    <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="comment"/>
    <#if mostViewedComment??>
      <li class="more">${mostViewedComment}</li>
    </#if>
  </ul>
<#else>
  <p>Most viewed article information is currently not available. Please check back later.</p>
</#if>