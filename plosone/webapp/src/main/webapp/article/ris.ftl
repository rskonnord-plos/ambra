TY  - JOUR
T1  - ${citation.articleTitle}
<#list citation.authors as author>
<#if author.isPrimary>
  <#assign authorTag = "A1">
<#else>
  <#assign authorTag = "A2">
</#if>
<#if author.suffix?exists>
  <#assign authorSuffix = ", " + author.suffix>
<#else>
  <#assign authorSuffix = "">
</#if>
${authorTag}  - ${author.surname}, ${author.givenNames}${authorSuffix}
</#list>
Y1  - ${citation.publicationDate?string("yyyy/MM/dd")}
N2  - ${citation.articleAbstract}
JF  - ${citation.journalTitle}
VL  - ${citation.volume}
IS  - ${citation.issue}
UR  - ${citation.URL}
SP  - ${citation.startPage}
EP  - ${citation.endPage}
PB  - ${citation.publisherName}
ER  - 

