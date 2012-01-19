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
<!-- begin : right hand column -->
<@s.url id="articleArticleRepXML"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
  <@s.param name="representation" value="%{'XML'}"/>
</@s.url>
<@s.url id="articleArticleRepPDF"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
  <@s.param name="representation" value="%{'PDF'}"/>
</@s.url>
<@s.url id="articleCitationURL"  namespace="/article" action="citationList" includeParams="none" articleURI="${articleURI}" />
<@s.url id="emailArticleURL" namespace="/article" action="emailArticle" articleURI="${articleURI}"/>
<@s.url id="relatedArticleURL" namespace="/article" action="fetchRelatedArticle" articleURI="${articleURI}"/>

<div id="rhc" xpathLocation="noDialog">
 
  <div id="download" class="rhcBox_type1">
    <div class="wrap">
      <ul>
        <li class="download icon"><strong>Download:</strong> <a href="${articleArticleRepPDF}">PDF</a> | <a href="${articleCitationURL}">Citation</a> | <a href="${articleArticleRepXML}">XML</a></li>
        <li class="print icon"><a href="#" onclick="window.print();return false;"><strong>Print article</strong></a></li>
      </ul>
    </div>
  </div>

  <#if articleIssues?? && articleIssues?size gt 0>
    <div id="published" xpathLocation="noSelect" class="rhcBox_type2">
      <p><strong>Published in the</strong>
      <#list articleIssues as oneIssue>
        <@s.a href="${freemarker_config.getJournalUrl(oneIssue[1])}${freemarker_config.context}/article/browseIssue.action?issue=${oneIssue[4]?url}" title="Browse Open-Access Issue">${oneIssue[5]} ${oneIssue[3]} Issue of <em>${freemarker_config.getDisplayName(oneIssue[1])}</em></@s.a>
      </#list>
    </div>
  </#if>

  <div id="impact" class="rhcBox_type2">
    <div id="ratingRhc1">
      <#include "/article/article_rhc_rating.ftl">
    </div>
  </div>

  <#if articleInfoX?? && articleInfoX.relatedArticles?size gt 0>
    <div id="related" class="rhcBox_type2">
      <h6>Related Content</h6>
      <#if articleInfoX?? && articleInfoX.relatedArticles?size gt 0>
        <dl class="related">
          <dt>Related ${freemarker_config.orgName} Articles</dt>
          <#list articleInfoX.relatedArticles as ra>
          <@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${ra.uri}" includeParams="none"/>
          <dd><@s.a href="%{fetchArticleURL}" title="Read Open-Access Article"><@articleFormat>${ra.title}</@articleFormat></@s.a></dd>
          </#list>
        </dl>
      </#if>
      <div class="more clearfix"><a href="${relatedArticleURL}">More</a></div>
    </div>
  </#if>

  <div id="share" class="rhcBox_type2">
    <h6>Share this Article <a href="/static/help.action#socialBookmarkLinks" class="replaced" id="info" title="More information"><span>info</span></a></h6>
    <ul>
      <li class="bookmarklets">Bookmark:

          <#-- StumbleUpon -->
          <a href="http://www.stumbleupon.com/submit?url=${jDocURL}" target="_new"> <img border=0 src="http://cdn.stumble-upon.com/images/16x16_su_solid.gif" alt="StumbleUpon" title="Add to StumbleUpon"></a>
          <#-- for more info, see http://www.stumbleupon.com/buttons.php -->
          <#-- Facebook -->
          <script>function fbs_click() {u='${docURL}';t='${docTitle?url}';window.open('http://www.facebook.com/sharer.php?u='+encodeURIComponent(u)+'&t='+encodeURIComponent(t),'sharer','toolbar=0,status=0,width=626,height=436');return false;}</script><a href="http://www.facebook.com/share.php?u=${docURL?url}" onclick="return fbs_click()"><img src="http://static.ak.fbcdn.net/images/share/facebook_share_icon.gif" alt="Facebook" title="Add to Facebook" /></a>       <!-- for mor info, see http://www.facebook.com/share_partners.php -->
          <#-- Connotea -->
          <script type="text/javascript">
            function bookmark_in_connotea(u) {
                a=false; x=window; e=x.encodeURIComponent; d=document;
                w=open('http://www.connotea.org/addpopup?continue=confirm&uri='+e(u),
                    'add', 'width=600, height=400, scrollbars, resizable');
                void(x.setTimeout('w.focus()',200));
            }
          </script>
          <a style='cursor: pointer;' onclick='javascript:bookmark_in_connotea("${docURL}");'><img src='${freemarker_config.getContext()}/images/icon_connotea_16x16.gif' alt="Connotea" title="Add to Connotea"/></a>
          <#-- See: http://www.connotea.org/wiki/AddToConnoteaButton -->
          <#-- Citeulike -->
          <a href="http://www.citeulike.org/posturl?url=${docURL?url}&title=${docTitle?url}" target="_new"><img src='${freemarker_config.getContext()}/images/icon_citeulike_16x16.gif' alt="CiteULike" title="Add to CiteULike" /></a>
          <#-- For more info see http://www.citeulike.org/faq/all.adp -->
          <#-- Digg
            TODO:Eventually we should be passing the abstract as the bodytext to digg and sending a topic as well
            -->
          <script type="text/javascript">
          digg_url = '${docURL}';
          digg_skin = 'icon';
          digg_title = '<@articleFormat><@simpleText>${docTitle?replace("'","\\'")}</@simpleText></@articleFormat>';
          digg_bodytext = '';
          digg_topic = '';
          digg_media = 'news';
          digg_window = 'new';

          </script>
          <script src="http://digg.com/tools/diggthis.js" type="text/javascript"></script>
          <#-- for more info see http://digg.com/tools/integrate -->

      </li>
      <li class="email icon"><a href="${emailArticleURL}">Email this article</a></li>
    </ul>
  </div>
</div>
<!-- end : right hand column -->
