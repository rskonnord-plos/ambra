<#import "../article/article_variables.ftl" as article>
<#import "../includes/global_body.ftl" as global>
<#include "../global/banner_macro.ftl">

<div id="pagebdy-wrap">
  <div id="pagebdy">

    <div id="article-block" class="cf">
      <#include "../includes/article_header.ftl"/>
      <div class="main cf">

        <#assign tab="article" />
        <#include "../includes/article_tabs.ftl"/>

        <div id="figure-thmbs" class="carousel cf">
          <div class="wrapper">
            <div class="slider">
              <#if articleAssetWrapper?size gt 0>
                <#list articleAssetWrapper as assets>
                  <#assign name = "${assets.doi?replace('info:doi/10.1371/journal.','')}"/>
                  <#assign formattedName = "${name?replace('.','-')}"/>
                  <div class="item">
                    <a href="#${formattedName}" data-doi="${articleInfoX.doi}" data-uri="${assets.doi}" title="${assets.title}">
                      <span class="thmb-wrap">
                        <img src="/article/fetchObject.action?uri=${assets.doi}&representation=PNG_I" alt="">
                      </span>
                    </a>
                  </div>
                </#list>
              </#if>
            </div>
          </div>
        </div>

        <div class="nav-col">
          <div class="nav" id="nav-article-page">
            <ul>
              <li><a href="${commentsTabURL}">Reader Comments (${commentary?size})</a></li>
              <#if (numCorrections > 0) >
                <li class="corrections"><a href="#corrections">Corrections (${numCorrections})</a></li>
              </#if>
              <#if (articleAssetWrapper?size > 0)>
                <li id="nav-figures"><a data-doi="${articleInfoX.doi}" >Figures</a></li>
              </#if>
            </ul>

          </div>
        </div>

        <div class="article">
          <#if (retractions?size > 0)>
            <div id="fch" class="fch">
              <#list retractions as retraction>
                <@s.url id="retractionFeedbackURL" includeParams="get" namespace="/annotation" action="listThread" inReplyTo="${retraction.ID?c}" root="${retraction.ID?c}" />
                <div>
                  <p class="fch"><strong>${retraction.title}</strong></p>
                  <span>${retraction.bodyWithUrlLinkingNoPTags} (<a class="formalCorrectionHref" href="${retractionFeedbackURL}">comment on this retraction</a>)</span>
                </div>
              </#list>
            </div>
          </#if>

          <#if expressionOfConcern?has_content>
            <div id="fch" class="fch">
              <span>${expressionOfConcern} </span>
            </div>
          </#if>

          <#if (formalCorrections?size > 0)>
            <div id="fch" class="fch">
              <p class="fch"><strong> Formal Correction:</strong> This article has been <em>formally corrected</em> to address the following errors.</p>
              <ol id="fclist" class="fclist">
                <#list formalCorrections as correction>
                  <@s.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${correction.ID?c}"/>
                  <li><span>${correction.truncatedBodyWithUrlLinkingNoPTags} (<a class="formalCorrectionHref" href="${listThreadURL}" annid="${correction.ID?c}">read formal correction)</a></span></li>
                </#list>
              </ol>
            </div>
          </#if>

          <@s.property value="transformedArticle" escape="false"/>
        </div>

        <#if (numCorrections > 0)>
          <div class="article section" id="corrections">
            <a id="corr" name="corrections" toc="corrections" title="Corrections"></a>
            <h3><span class="icon_corrections"></span> Corrections</h3>
            <ul id="correction_threads">

              <#list retractions + formalCorrections + minorCorrections as correction>
                <@s.url namespace="/annotation" includeParams="none" id="listThreadURL" action="listThread" root="${correction.ID?c}"/>
                <@s.url namespace="/user" includeParams="none" id="showUserURL" action="showUser" userId="${correction.creatorID?c}"/>

                <#if (correction.type == "FormalCorrection") >
                <li class="formal">
                <#else>
                <li>
                </#if>

                  <div class="title">
                    <a href="${listThreadURL}" class="correction_title">${correction.title}</a>
                  </div>

                  <p>
                    Posted by <a href="${showUserURL}" >${correction.creatorDisplayName}</a> on
                  ${correction.created?string("dd MMM yyyy 'at' HH:mm zzz")}
                  </p>

                  <#if correction.totalNumReplies != 1>
                    <#assign label = "Responses">
                  <#else>
                    <#assign label = "Response">
                  </#if>

                  <div class="meta">
                  ${correction.totalNumReplies} <strong>${label}</strong> •
                    <strong>Most Recent</strong> ${correction.lastReplyDate?string("dd MMM yyyy 'at' HH:mm zzz")}
                  </div>
                </li>
              </#list>

            </ul>
          </div>
        </#if>

      </div><#-- main -->

      <#include "article_sidebar.ftl">

    </div><#-- article-block -->

  </div><#-- pagebdy -->

</div><#-- pagebdy-wrap -->
<#list articleInfoX.articleAssets as asset>
  <input type="hidden" class="assetSize" name="${asset.doi}.${asset.extension}" value="${asset.size?c}"/>
</#list>

<script src="http://wl.figshare.com/static/p_widget.js" type="text/javascript"></script>
