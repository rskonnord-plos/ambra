<!-- begin : main content -->
<div id="content" class="static">

  <h1>For Users</h1>

  <p class="intro">
    If you need help using or finding something on the site, select one of the following links:
  </p>

  <ul>
    <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="commentURL"/>
    <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="ratingURL"/>
    <@s.url action="help" namespace="/static" includeParams="none" id="helpURL"/>
    <@s.url action="sitemap" namespace="/static" includeParams="none" id="siteURL"/>
    <li><@s.a href="${commentURL}" title="PLoS Hub | Commenting Guidelines">Commenting Guidelines</@s.a> - Guidelines for commenting on articles via annotations and discussion</li>
    <li><@s.a href="${ratingURL}" title="PLoS Hub | Rating Guidelines">Rating Guidelines</@s.a> - Guidelines for using the article rating system</li>
    <li><@s.a href="${helpURL}" title="PLoS Hub | Help Using this Site">Help Using this Site</@s.a> - Answers to common questions</li>
    <li><@s.a href="${siteURL}" title="PLoS Hub | Site Map">Site Map</@s.a> - Directory of main pages</li>
  </ul>
</div>
<!-- end : main contents -->

