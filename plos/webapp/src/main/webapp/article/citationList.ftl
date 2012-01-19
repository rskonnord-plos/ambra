<!-- begin : main content -->
<div id="content" class="static">
  <h1>Download Citation</h1>
  <h2>Article:</h2>
  <p class="intro">${citationString}</p>

  <h2>Download the article citation in the following formats:</h2>
  <ul>
    <@s.url id="risURL" namespace="/article" action="getRisCitation" includeParams="none" articleURI="${articleURI}" />
    <li><a href="${risURL}" title="RIS Citation">RIS</a> (compatible with EndNote, Reference Manager, ProCite, RefWorks)</li>
    <@s.url id="bibtexURL" namespace="/article" action="getBibTexCitation" includeParams="none" articleURI="${articleURI}" />
    <li><a href="${bibtexURL}" title="PLoS ONE | Editorial Board">BibTex</a> (compatible with BibDesk, LaTeX)</li>
  </ul>
</div>
<!-- end : main contents -->
