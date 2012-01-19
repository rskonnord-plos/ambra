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

<#if articleInfoX??>
  <#assign docTitle = articleInfoX.title />
<#else>
  <#assign docTitle = "" />
</#if>

<@s.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion"
  includeParams="none" target="${articleURI}" />
<div id="researchArticle" class="content">
  <a id="top" name="top"></a>
  <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
  <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate"
    page="${thisPageURL?url}"/>
  <#include "article_blurb.ftl">
  <h1 xpathLocation="noSelect">${docTitle}</h1>
  <#assign tab="article" />
  <#include "article_tabs.ftl">
    <div id="retractionHtmlId" class="retractionHtmlId" style="display:none;" xpathLocation="noSelect">
      <p class="retractionHtmlId"><strong> Retraction:</strong>
        <span id="retractionlist" class="retractionHtmlId"></span>
    </p>
  </div>   
  <div id="fch" class="fch" style="display:none;" xpathLocation="noSelect">
    <p class="fch"><strong> Formal Correction:</strong> This article has been <em>formally corrected</em> to address the following errors.</p>
    <ol id="fclist" class="fclist"></ol>
  </div>
  <div id="articleMenu" xpathLocation="noSelect">
    <div class="wrap">
      <ul>
        <li class="annotation icon">To <strong>add a note</strong>, highlight some text. <a href="#" onclick="toggleAnnotation(this, 'public'); return false;" title="Click to turn notes on/off">Hide notes</a></li>
        <li class="discuss icon">
          <#if Session[freemarker_config.userAttributeKey]?exists>
            <a href="${createDiscussionURL}">Make a general comment</a>
          <#else>
            <a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}">Make a general comment</a>
          </#if>
        </li>
      </ul>
      <div id="sectionNavTopBox" style="display:none;">
        <p><strong>Jump to</strong></p>
        <div id="sectionNavTop" class="tools"></div>
      </div>
    </div>
  </div>
  <@s.property value="transformedArticle" escape="false"/>
</div>
