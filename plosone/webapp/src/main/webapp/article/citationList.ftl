<!-- begin : main content -->
<div id="content" class="static">
  <h1>Download Citation</h1>
  <h2>Article:</h2>
  <p class="intro">
    <#assign gt5 = false />
    <#list citation.authors as author>
      <#if author_index gt 4>
        <#assign gt5 = true>
        <#break>
      </#if>
      <#assign gn = author.givenNames?word_list />
      <#assign allNames = []>
      <#list gn as n>
        <#if n?matches(".*\\p{Pd}\\p{Lu}.*")>
          <#assign names = n?split("\\p{Pd}",'r') />
          <#assign allNames = allNames + names />
        <#else>
          <#assign temp = [n]>
          <#assign allNames = allNames + temp>
        </#if>
      </#list>
      ${author.surname} <#if author.suffix?exists>${author.suffix}</#if> <#list allNames as n>${n[0]}</#list><#if author_has_next>,</#if>
    </#list>
    <#if gt5>et al.</#if>
    (${citation.publicationDate?string("yyyy")}) ${citation.articleTitle}. ${citation.journalTitle} 
    ${citation.volume}(${citation.issue}): ${citation.startPage} doi:${citation.DOI}
  </p>

  <h2>Download the article citation in the following formats:</h2>
  <ul>
    <@s.url id="risURL" namespace="/article" action="getRisCitation" includeParams="none" articleURI="${articleURI}" />
    <li><a href="${risURL}" title="RIS Citation">RIS</a> (compatible with EndNote, Reference Manager, ProCite, RefWorks)</li>
    <@s.url id="bibtexURL" namespace="/article" action="getBibTexCitation" includeParams="none" articleURI="${articleURI}" />
    <li><a href="${bibtexURL}" title="PLoS ONE | Editorial Board">BibTex</a> (compatible with BibDesk, LaTeX)</li>
  </ul>
</div>
<!-- end : main contents -->
