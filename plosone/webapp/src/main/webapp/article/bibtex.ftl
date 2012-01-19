<#assign authorList = "">
<#list citation.authors as author>
  <#assign authorList = authorList + author.surname>
  <#assign authorList = authorList + ", ">
  <#if author.suffix?exists>
    <#assign authorList = authorList + author.suffix>
    <#assign authorList = authorList + ", ">
  </#if>
  <#assign authorList = authorList + author.givenNames>
  <#if author_has_next><#assign authorList = authorList + " AND "></#if>
</#list>

@article{${citation.DOI},
    author = {${authorList}},
    journal = {${citation.journalTitle}},
    publisher = {${citation.publisherName}},
    title = {${citation.articleTitle}},
    year = {${citation.publicationDate?string("yyyy")}},
    month = {${citation.publicationDate?string("MMM")}},
    volume = {${citation.volume}},
    url = {${citation.URL}},
    pages = {${citation.startPage}},
    abstract = {${citation.articleAbstract}},
    number = {${citation.issue}}
}        




