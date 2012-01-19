<!-- begin : main content -->
<div id="content" class="static">

  <h1>Browse Articles</h1>

  <p class="intro">The PLoS Hub for Clinical Trials posts new content as often as once 
  per week. You can browse articles two ways:</p>

  <@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
  <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>
  <ul>
    <li><@s.a href="${browseDateURL}" title="PLoS Hub | Browse by Publication Date">By Publication Date</@s.a> -
    Browse articles by choosing a specific week or month of publication.</li>
    <li><@s.a href="${browseSubURL}" title="PLoS Hub | Browse by Subject">By Subject</@s.a> -
    Browse articles published in a specific subject area.</li>
  </ul>
</div>
<!-- end : main contents -->