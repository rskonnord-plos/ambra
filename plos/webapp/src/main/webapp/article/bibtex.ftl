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
<#if citation.collaborativeAuthors?has_content>
  <#assign authorList = authorList + " AND ">
  <#list citation.collaborativeAuthors as collab>
    <#assign authorList = authorList + collab.nameRef + ", ">
  </#list>
</#if>

@article{${citation.DOI},
    author = {${authorList}},
    journal = {${citation.journalName}},
    publisher = {${citation.publisherName}},
    title = {${citation.articleTitle}},
    year = {${citation.publicationDate?string("yyyy")}},
    month = {${citation.publicationDate?string("MMM")}},
    volume = {${citation.volume}},
    url = {${citation.URL}},
    pages = {${citation.startPage}<#if citation.endPage?has_content>--${citation.endPage}</#if>},
    abstract = {${citation.articleAbstract!''}},
    number = {${citation.issue}},
    doi = {${citation.DOI}}
}        




