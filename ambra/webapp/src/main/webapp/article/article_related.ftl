<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2009 by Topaz, Inc.
  http://topazproject.org

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
<#include "article_variables.ftl">
<@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
<div id="content" class="article" style="visibility:visible;">
  <#include "article_rhc.ftl">

  <div id="articleContainer">
    <form action="">
      <input type="hidden" name="doi" id="doi" value="${articleURI}" />
    </form>

    <div id="researchArticle" class="content related">
      <a id="top" name="top" toc="top" title="Top"></a>
      <#include "article_blurb.ftl">
      <h1 xpathLocation="noSelect"><@articleFormat>${docTitle}</@articleFormat></h1>
      <#assign tab="related" />
      <#include "article_tabs.ftl">

      <h2>Related Articles <a href="/static/help.action#relatedArticles" class="replaced" id="info" title="More information"><span>info</span></a></h2>

      <h3>Related Articles on the Web <img id="relatedArticlesSpinner" src="../../images/loading_small.gif" class="loading" /></h3>
      <div id="pubMedRelatedErr" style="display:none;"></div>
      <ul>
        <li><a href="http://scholar.google.com/scholar?hl=en&lr=&q=related:${docURL?url}&btnG=Search">Google Scholar</a></li>
        <li id="pubMedRelatedLI" style="display:none;"><a id="pubMedRelatedURL">PubMed</a></li>
      </ul>

      <h3>Cited in <img id="relatedCitesSpinner" src="../../images/loading_small.gif" class="loading" /></h3>
      <div id="relatedCites"></div>
      <div>Search for citations on <a href="http://scholar.google.com/scholar?hl=en&lr=&cites=${docURL?url}">Google Scholar</a>.</div>

      <h3>Bookmarked in <img id="relatedBookmarksSpinner" src="../../images/loading_small.gif" class="loading" /></h3>
      <div id="relatedBookmarks"></div>

      <h2>Related Blog Posts <a href="/static/help.action#relatedBlogPosts" class="replaced" id="info" title="More information"><span>info</span></a> <img id="relatedBlogSpinner" src="../../images/loading_small.gif" class="loading" /></h2>
      <div id="relatedBlogPosts"></div>

      Search for related blog posts on <a href="http://blogsearch.google.com/blogsearch?hl=en&ie=UTF-8&q=${shortDOI?url}&btnG=Search+Blogs">Google Blogs</a>

      <h3>Trackbacks</h3>
      <div>To trackback this article use the following trackback URL:<br/>
        <@s.url namespace="/" includeParams="none" id="trackbackURL" action="trackback" trackbackId="${articleURI}"/>
        <#assign trackbackLink = Request[freemarker_config.journalContextAttributeKey].baseHostUrl + trackbackURL>
        <a href="${trackbackLink}" title="Trackback URL">${trackbackLink}</a>
      </div>

      <#list trackbackList as t>
        <div class="trackback">
          <#if t.title?exists && !(t.title = "")>
            <#assign title = t.title>
          <#else>
            <#assign title = t.url>
          </#if>
          <p class="header">
          <#if t.blog_name?exists>
          <span class="blog">${t.blog_name}</span>
          <#else>
          An unknown source
          </#if>
           referenced this article in "<a href="${t.url}" title="${t.title?replace('"',"")!""}" class="post">${title}</a>" <span class="timestamp">on <strong>${t.created?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong></span></p>
          <#if t.excerpt?exists>
          <p class="excerpt">"${t.excerpt}"</p>
          </#if>
        </div>
      </#list>
    </div>
  </div>
  <div style="visibility:hidden">
    <#include "/widget/ratingDialog.ftl">
    <#include "/widget/loadingCycle.ftl">
  </div>
</div>
