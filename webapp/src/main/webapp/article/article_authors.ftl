Abouth the authors:

<ul>
  <#list authorExtras as author>
    <@s.url id="searchURL" includeParams="none"
    pagesize="10" queryField="author" unformattedQuery="author:\"${author.authorName}\""
    journalOpt="all" subjectCatOpt="all" filterArticleTypeOpt="all"
    namespace="/search" action="advancedSearch"/>

    <li><a href="${searchURL}">${author.authorName}</a><br/>
    <#list author.affiliations as affiliation>
      ${affiliation} <#if affiliation_has_next>, </#if>
    </#list>
    </li>
  </#list>
</ul>


Corresponding author:<br/>
${correspondingAuthor}<br/>
<br/>
Competing Interests:<br/>
TODO: Where does this come from?<br/>
<br/>
Author Contributions:<br/>
${authorContributions}<br/>
<br/>
<br/>