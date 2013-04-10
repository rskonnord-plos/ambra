<#--
  $HeadURL:: http://svn.ambraproject.org/svn/plos/templates/head/journals/PLoSDefault/w#$
  $Id: article_comments.ftl 13852 2013-03-01 03:11:34Z mbaehr $

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
<@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
<@s.url namespace="/article" includeParams="none" id="articleURL" action="fetchArticle" articleURI="${articleInfoX.doi}"/>
<@s.url namespace="/annotation/secure" includeParams="none" id="startDiscussionUrl" action="startDiscussion" articleURI="${articleInfoX.doi}"/>
<@s.url namespace="/article" includeParams="none" id="correctionsURL" action="fetchArticleCorrections" articleURI="${articleInfoX.doi}"/>
<@s.url namespace="/article" includeParams="none" id="commentsURL" action="fetchArticleComments" articleURI="${articleInfoX.doi}"/>

<div id="pagebdy-wrap">
  <div id="pagebdy">

    <div id="article-block" class="cf">
    <#include "../includes/article_header.ftl"/>
      <div class="main cf">

        <#assign tab="comments" />
        <#include "../includes/article_tabs.ftl"/>

        <div id="thread">
          <h2>Reader Comments (${commentary?size})</h2>

          <p class="post_comment"><a href="${startDiscussionUrl}">Post a new comment</a> on this article</p>

          <ul id="threads">
          <#list commentary as comment>
            <@s.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${comment.ID?c}"/>
            <@s.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${comment.creatorID?c}"/>

            <li class="cf">
              <div class="responses">
                <span>${comment.totalNumReplies}</span> <#if comment.totalNumReplies == 1>Response<#else>Responses</#if>
              </div>
              <div class="recent">
              ${comment.lastReplyDate?string("dd MMM yyyy '<br/>' HH:mm zzz")}<br/>
                <span>Most Recent</span>
              </div>
              <div class="title">
                <a href="${listThreadURL}">${comment.title}</a>
                <span>Posted by <a href="${showUserURL}">${comment.creatorDisplayName}</a> on
                ${comment.created?string("dd MMM yyyy 'at' HH:mm zzz")}</span>
              </div>
            </li>
          </#list>
          </ul>

        </div><!-- /#thread -->
      </div><!-- /.main -->

      <#include "article_sidebar.ftl">

    </div><!-- /article-block -->
  </div><!-- /pagebdy -->
</div><!-- /pagebdy-wrap -->
