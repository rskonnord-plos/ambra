<#include "journal_include.ftl">
<#include "/static/cj_shared_blocks.ftl">

<!-- begin : main content -->
<div id="content" class="static">
  <h1>RSS Feeds</h1>
  <@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssURL"/>
  
  <p>
    <em>${journal_name}</em> provides the following <@s.a href="${rssURL}">RSS feeds</@s.a> which are updated as new articles are posted:
  </p>

  <ul>
    <li><a href="/article/feed">New Articles</a></li>
  </ul>
   
</div>
<!-- end : main contents -->