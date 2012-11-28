<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2010 by Public Library of Science
  http://ambraproject.org
  http://plos.org

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- begin : main contents wrapper -->

<@s.url id="articleURL" action="fetchArticle" namespace="/article" articleURI="${originalDOI}"/>

<div id="pagebdy-wrap">
  <div id="pagebdy">
    <div id="static-wrap">
      <h1>Find this article online</h1>
      <br/>
      <h4>${title?html}</h4>
      <p>Use the following links to find the article:</p>
      <ul>
      <li><a href="${crossRefUrl}"
            onclick="window.open(this.href, 'ambraFindArticle','');return false;" title="Go to article in CrossRef" class="crossref icon">CrossRef</a>
        <#if pubGetUrl??>
          <a href="${pubGetUrl}"
              onclick="window.open(this.href, 'ambraFindArticle','');return false;" title="Get the full text PDF from PubGet">
            <img title="Get the full text PDF from PubGet" src="${freemarker_config.context}/images/icon_pubgetpdf.gif"/></a>
        </#if>
      </li>
      <#if (author?has_content)>
        <#assign pubMedAuthorQuery = author + "[author] AND ">
        <#assign googleAuthorQuery = "author:" + author + " ">
      <#else>
        <#assign pubMedAuthorQuery = "">
        <#assign googleAuthorQuery = "">
      </#if>
      <li><a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=PubMed&cmd=Search&doptcmdl=Citation&defaultField=Title+Word&term=${pubMedAuthorQuery?url}${title?url}"
       onclick="window.open(this.href, 'ambraFindArticle','');return false;" title="Go to article in PubMed" class="ncbi icon">PubMed/NCBI</a></li>
      <li><a href="http://scholar.google.com/scholar?hl=en&safe=off&q=${googleAuthorQuery?url}%22${title?url}%22"
           onclick="window.open(this.href, 'ambraFindArticle','');return false;" title="Go to article in Google Scholar" class="google icon">Google Scholar</a></li>
      </ul>
      <a href="${articleURL}" class="article icon">Back to article</a>
    </div>
  </div>
</div>