<ul class="articles">
  <@s.url id="art1URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="#"/>
  <li>
    <a href="${art1URL}" title="Read Open Access Article">Lorem Ipsum Dolor sit Amet, Consectetuer Adipiscing Elit</a>
  </li>

  <@s.url id="art2URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="#"/>
  <li>
    <a href="${art2URL}" title="Read Open Access Article">Ut Wisi Enim ad Minim Veniam, Quis Nostrud Exerci Tation Ullamcorper Suscipit Lobortis Nisl ut Aliquip ex ea Commodo</a>
  </li>

  <@s.url id="art3URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="#"/>
  <li>
    <a href="${art3URL}" title="Read Open Access Article">Duis Autem vel eum Iriure Dolor in Hendrerit in Vulputate Velit Esse Molestie Consequat, vel Illum Dolore eu Feugiat Nulla Facilisis</a>
  </li>

  <!-- Do not edit below this comment -->
  <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="comment"/>
  <li class="more">Learn how to <a href="${comment}">add comments and start discussions</a> on articles.</li>
</ul>
