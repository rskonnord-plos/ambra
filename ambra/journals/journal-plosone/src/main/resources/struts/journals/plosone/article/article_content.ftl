<#--
  $HeadURL::                                                                            $
  $Id$

  Copyright (c) 2007-2008 by Topaz, Inc.
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
<#--
  The digg URL has to be different as digg appears to be picking up the redirect
  from our internal DOI resolver and messing up the formating.
 -->
<#assign docURL = "http://dx.plos.org/${articleInfoX.id?replace('info:doi/','')}" />
<#assign jDocURL = freemarker_config.getJournalUrl(journalContext) + "/article/" +
  articleInfoX.id?url />

<#assign publisher=""/>
<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>
<#list journalList as jour>
  <#if (articleInfo.eIssn = jour.eIssn) && (jour.key != journalContext) >
    <#assign publisher = "Published in <em><a href=\"" + freemarker_config.getJournalUrl(jour.key)
                         + "\">"+ jour.dublinCore.title + "</a></em>" />
    <#break/>
  <#else>
    <#if jour.key != journalContext>
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">"/>
      <#if publisher?length gt 0>
        <#assign publisher = publisher + ", " + jourAnchor + jour.dublinCore.title + "</a>" />
      <#else>
        <#assign publisher = publisher + "Featured in " + jourAnchor +
          jour.dublinCore.title + "</a>" />
      </#if>
    </#if>
  </#if>
</#list>
  <div id="researchArticle" class="content">
    <a id="top" name="top" toc="top" title="Top"></a>
    <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
    <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate"
    page="${thisPageURL?url}"/>
    <div id="contentHeader">
      <p>Open Access</p>
      <p id="articleType">${articleType.heading}</p>
    </div>
    <#if publisher != "">
      <div id="publisher"><p>${publisher}</p></div>
    </#if>
    <div id="fch" class="fch" style="display:none;">
    <p class="fch"><strong> Formal Correction:</strong>
      This article has been <em>formally corrected</em> to address the following errors.</p>
      <ol id="fclist" class="fclist"></ol>
    </div>
  <div id="articleMenu">
    <ul>
      <@s.url id="articleArticleRepXML"  namespace="/article" action="fetchObjectAttachment"
        includeParams="none" uri="${articleURI}">
        <@s.param name="representation" value="%{'XML'}"/>
      </@s.url>
      <li><a href="${articleArticleRepXML}" class="xml"
             title="Download XML">Download Article XML</a></li>
      <#if articleType.heading != 'Issue Image'>
        <@s.url id="articleArticleRepPDF"  namespace="/article" action="fetchObjectAttachment"
          includeParams="none" uri="${articleURI}">
          <@s.param name="representation" value="%{'PDF'}"/>
        </@s.url>
        <li><a href="${articleArticleRepPDF}" class="pdf"
               title="Download PDF">Download Article PDF</a></li>
      </#if>
      <@s.url id="articleCitationURL"  namespace="/article" action="citationList"
        includeParams="none" articleURI="${articleURI}" />
      <li><@s.a href="%{articleCitationURL}"  cssClass="citation"
       title="Download Citation">Download Citation</@s.a></li>
      <@s.url id="emailArticleURL" namespace="/article" action="emailArticle"
      articleURI="${articleURI}"/>
      <li><@s.a href="%{emailArticleURL}"  cssClass="email"
       title="E-mail This Article to a Friend or Colleague">E-mail this Article</@s.a></li>
      <li>
        Bookmark this page:
        <div id="socialBookmarks">
        <#-- StumbleUpon -->
        <a href="http://www.stumbleupon.com/submit?url=${docURL?url}" target="_new"><img border=0
         src="http://cdn.stumble-upon.com/images/16x16_su_solid.gif" alt="StumbleUpon"
         title="Add to StumbleUpon"></a>
        <#-- for more info, see http://www.stumbleupon.com/buttons.php -->
        <#-- Facebook -->
        <script>function fbs_click() {u='${docURL}';t='${articleInfoX.title}';
          window.open('http://www.facebook.com/sharer.php?u='+encodeURIComponent(u)+'&t=' +
                      encodeURIComponent(t),'sharer','toolbar=0,status=0,width=626,height=436');
          return false;}</script><a href="http://www.facebook.com/share.php?u=${docURL?url}"
            onclick="return fbs_click()">
          <img src="http://static.ak.fbcdn.net/images/share/facebook_share_icon.gif"
               alt="Facebook"
               title="Add to Facebook" /></a>
        <!-- for mor info, see http://www.facebook.com/share_partners.php -->
        <#-- Connotea -->
        <script type="text/javascript">
          function bookmark_in_connotea(u) {
              a=false; x=window; e=x.encodeURIComponent; d=document;
              w=open('http://www.connotea.org/addpopup?continue=confirm&uri='+e(u),
                  'add', 'width=600, height=400, scrollbars, resizable');
              void(x.setTimeout('w.focus()',200));
          }
        </script>
        <a style='cursor: pointer;' onclick='javascript:bookmark_in_connotea("${docURL}");'><img
                src='${freemarker_config.getContext()}/images/icon_connotea_16x16.gif'
                alt="Connotea" title="Add to Connotea"/></a>
        <#-- See: http://www.connotea.org/wiki/AddToConnoteaButton -->
        <#-- Citeulike -->
        <a href="http://www.citeulike.org/posturl?url=${docURL?url}&title=${articleInfoX.title?url}"
           target="_new"><img
           src='${freemarker_config.getContext()}/images/icon_citeulike_16x16.gif' alt="CiteULike"
           title="Add to CiteULike" /></a>
        <#-- For more info see http://www.citeulike.org/faq/all.adp -->
        <#-- Digg  -->
        <script type="text/javascript">
        digg_url = '${jDocURL}';
        digg_skin = 'icon';
        digg_title = '${articleInfoX.title?replace("'","\\'")?replace("\n","")?replace("  "," ")}';
        digg_bodytext = '';
        digg_topic = '';
        digg_media = 'news';
        digg_window = 'new';

        </script>
        <script src="http://digg.com/tools/diggthis.js" type="text/javascript"></script>
        <#-- for more info see http://digg.com/tools/integrate -->
        </div>
      </li>
    <li><a href="#" onclick="window.print();return false;" class="print last"
           title="Print this article">Print this Article</a></li>
    </ul>
  </div>
    <@s.property value="transformedArticle" escape="false"/>
  </div>
