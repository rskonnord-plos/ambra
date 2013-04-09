<#--
  $HeadURL: http://svn.ambraproject.org/svn/plos/templates/head/journals/PLoSDefault/webapp/article/article_related.ftl $
  $Id: article_related.ftl 13852 2013-03-01 03:11:34Z mbaehr $

  Copyright (c) 2007-2010 by Public Library of Science
  http://plos.org
  http://ambraproject.org

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

<#import "article_variables.ftl" as article>
<#import "../includes/global_body.ftl" as global>

<#assign max_categories = 5>

<div id="pagebdy-wrap">
  <div id="pagebdy">

    <div id="article-block" class="cf">
    <#include "../includes/article_header.ftl"/>
      <div class="main cf">

        <#assign tab="related" />
        <#include "../includes/article_tabs.ftl"/>

        <#if articleInfoX?? && articleInfoX.relatedArticles?size gt 0>
        <div id="suggestions" class="cf">
          <h2>Linked Articles</h2>

          <#list articleInfoX.relatedArticles as ra>
          <div class="module">
            <#assign relatedArticleURL = "${freemarker_config.doiResolverURL}${ra.uri?replace('info:doi/','')}" />
            <h3><a href="${relatedArticleURL}"><@articleFormat>${ra.title}</@articleFormat></a></h3>
            <#list ra.authors as author>
              <#if (author_index > 10) >...<#break></#if>
              ${author}<#if author_has_next>, </#if>
            </#list>
            <div class="meta">
              ${ra.articleTypeForDisplay} | published ${ra.date?string("d MMM yyyy")} | ${ra.publishedJournal} <br/>
              doi:${ra.uri?replace('info:doi/','')}
            </div>
          </div>
          </#list>

        </div>
        </#if>

        <input type="hidden" name="related_author_query" id="related_author_query" value='${relatedAuthorSearchQuery}' />
        <div id="more_by_authors" style="display: none;">
          <h2>More by these Authors</h2>
          <ul></ul>
        </div>

        <div id="related_collections" class="cf">

          <div>
            <h3>Related Authors</h3>
            <ul>
              <#list articleInfoX.authorsForRelatedSearch as author >
                <@s.url id="advancedSearchURL" unformattedQuery="author:\"${author}\"" namespace="/search" action="advancedSearch" />
                <li><a href="${advancedSearchURL}">${author}</a></li>
              </#list>
            </ul>
          </div>
        </div>

      </div>

      <#include "article_sidebar.ftl">

    </div>  <!-- article-block -->
  </div>  <!-- pagebdy -->
</div>  <!-- pagebdy-wrap -->
