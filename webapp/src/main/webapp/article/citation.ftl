<#list authorList as author>
  <#if (author_index > 4) >
  <span class="citation_author">et al. </span>
    <#break>
  </#if>
  <#if (author_has_next) || (authorList?size < 4 && collaborativeAuthors?size > 0) > <#--Show a comma after last author if we're going to show collabs -->
  <span class="citation_author">${author.surnames!}<#if author.givenNames??>
    <@abbreviation>${author.givenNames!}</@abbreviation></#if><#if author.suffix?has_content>
  ${author.suffix?replace(".","")}</#if>,
    </span>
  <#else>
  <span class="citation_author">${author.surnames!}<#if author.givenNames??>
    <@abbreviation>${author.givenNames!}</@abbreviation></#if><#if author.suffix?has_content>
  ${author.suffix?replace(".","")}</#if>
      </span>
  </#if>
</#list>
<#if (authorList?size < 4) >
  <#assign max_index = 4-authorList?size>
  <#list collaborativeAuthors as collab>
    <#if (collab_index > max_index) >
    <span class="citation_author">et al. </span>
      <#break>
    </#if><#-- The following line is so long
   because the browser was picking up tabs and inserting
   spaces in weird spots --><#if (collab_index > 0)><span
      class="citation_author">, ${collab}</span><#else><span class="citation_author">${collab}</span></#if></#list>
</#if>
<#if year??><span class="citation_date">(${year})</span></#if>
<#if title??><#if !(title?ends_with("?"))><#assign addPunctuation = "."></#if><span
    class="citation_article_title"><@articleFormat>${title}</@articleFormat><#if addPunctuation??>${addPunctuation}</#if></span></#if>
<#if journal??><span class="citation_journal_title">${journal}</span></#if><span
    class="citation_issue"><#if volume??> ${volume}</#if><#if issue??>(${issue})</#if>:</span>
<#if eLocationId??><span class="citation_start_page">${eLocationId}.</span></#if>
<#if doi??><span class="citation_doi"><#if isCorrection>http://dx.doi.org/<#else>doi:</#if>${doi}</span></#if>
