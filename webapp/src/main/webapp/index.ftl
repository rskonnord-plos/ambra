<#--
  $HeadURL::                                                                            $
  $Id$

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
<!-- begin : main content -->

<div id="search-home">
    <p class='intro'></p>

    <div id="db">
    <@s.form name="searchForm" namespace="/search" action="simpleSearch" method="get">
      <@s.hidden name="from" value="globalSimpleSearch"/>
      <@s.hidden name="filterJournals" value="${currentJournal}"/>
        <fieldset>
            <legend>Search</legend>
            <label for="search">Search</label>

            <div class="wrap">
                <input id="search" type="text" name="query" placeholder="Search">
                <input type="image" alt="SEARCH" src="/images/icon.search.gif">
            </div>
        </fieldset>
    </@s.form>
    <@s.url id="advancedSearchURL" includeParams="none" namespace="/search" action="advancedSearch"/>
        <a id="advSearch" class="btn primary" href="${advancedSearchURL}?filterJournals=${currentJournal}">advanced</a>
    </div>
</div>

<div id="pagebdy-wrap">
    <div id="pagebdy">

        <div class="layout-625_300 cf">
            <div class="col-1">

                <div id="headlines">
                    <h1>Research Headlines</h1>

                    <div class="tab-block">

                        <div class="nav tab-nav">
                            <ul>
                                <li><a href="#tab-01">in the news</a></li>
                                <li><a href="#tab-02" data-loadurl="/recentArticles.action">recent</a></li>
                                <li><a href="#tab-03" data-loadurl="/mostViewed.action">most viewed</a></li>
                            </ul>
                        </div>

                        <div class="tab-content">
                            <div class="tab-pane" id="tab-01">
                                <h3>In the News</h3>
                            </div>
                            <div class="tab-pane" id="tab-02">
                                <h3>Recent</h3>
                            <#include "article/recentArticles.ftl">
                            </div>
                            <div class="tab-pane" id="tab-03">
                                <h3>Most Viewed</h3>
                            </div>
                        </div>

                    </div>
                </div>
                <!-- headlines -->

                <!-- begin : explore by subject block -->
                <!--
      <#if categoryInfos?size gt 0>
        <#assign colSize = (categoryInfos?size / 2)?floor>
        <div class="explore nav cf">
          <h2>Search by Subject Area</h2>
          <ul class="flt-l">
            <#list categoryInfos?keys as category>
              <#assign categoryId = category?replace("\\s|\'","","r")>
              <@s.url id="feedURL" action="getFeed" namespace="/article/feed"
                categories="${category}" includeParams="all"/>

              <li>
                <#assign anchorLabel = category.toLowerCase().replaceAll(" ", "_") />
                <a id="widget${categoryId}" href="/taxonomy.action#${anchorLabel}">${category}</a>&nbsp;
                <a href="${feedURL}"><img src="${freemarker_config.context}/images/icon.rss.16.png" alt="RSS"/></a>
              </li>
              <#if (category_index + 1) == colSize>
                </ul>
                <ul class="flt-r">
              </#if>
            </#list>
          </ul>
        </div>
      </#if> -->
                <!-- end : explore by subject block -->
            </div>
            <!-- col-1 -->

            <div class="col-2">

                <div id="issue" class="block">
                    <h3>Featured Image</h3>

                    <div class="img">
                    </div>
                    <div class="txt">
                    </div>
                </div>

                <!-- begin : stay-connected block -->
                <div id="connect" class="nav">
                    <ul class="lnk-social cf">
                        <li class="lnk-email ir"><a href="/user/secure/editProfile.action?tabId=alerts"
                                                    title="E-mail Alerts">E-mail Alerts</a></li>
                        <li class="lnk-rss ir"><a href="/taxonomy" title="RSS">RSS</a></li>
                    </ul>
                </div>
                <!-- end : stay-connected block -->

                <div class="publish">
                    <h2>Publish</h2>

                    <div class="body cf">
                        <div class="item">
                            <a href="${manuscriptURL}">Submission Instructions</a>
                        </div>
                        <div class="item">
                        </div>
                    </div>
                </div>

            <#assign Rectangle = 357>       <!-- Ad Space : Rectangle -->

                <div class="ad">
                    <div class="title">Advertisement</div>
                <@iFrameAd zone=Rectangle id="" width="300" height="250" />
                </div>

            </div>
            <!-- col-2 -->
        </div>
        <!-- layout-625_300 -->

        <!-- begin : marketing advocacy blocks -->
        <div id="adWrap" class="cf">
        <@iFrameAd zone=385 id="" width="320" height="150" />
    <@iFrameAd zone=429 id="" width="320" height="150" />
    <@iFrameAd zone=427 id="" width="320" height="150" />
        </div>
        <!-- end : marketing advocacy blocks -->

        <!-- twitter -->
    <#assign twitterSearch = "" >
    <#if twitterSearch?length gt 0 >
        <div id="homepageTwitterStream" class="block" style="display:none;">
            <script src="http://widgets.twimg.com/j/2/widget.js"></script>
            <script>
                new TWTR.Widget({
                    version: 2,
                    type: 'search',
                    search: '${twitterSearch} lang:en',
                    interval: 6000,
                    rpp: 4,
                    title: '',
                    subject: 'What the community is saying',
                    width: 960,
                    height: 600,
                    theme: {
                        shell: {
                            background: '#efefef',
                            color: '#333333'
                        },
                        tweets: {
                            background: '#efefef',
                            color: '#666666',
                            links: '#3c62af'
                        }
                    },
                    features: {
                        scrollbar: false,
                        loop: false,
                        live: false,
                        hashtags: true,
                        timestamp: true,
                        avatars: true,
                        toptweets: true,
                        behavior: 'all'
                    },
                    ready: function () {
                        //Ready isn't called when it's done, it's called right before render.
                        //Let render complete and then search for results, if results exist, display the box
                        setTimeout(function () {
                            var tweetNode = $("div.twtr-tweet");

                            if (tweetNode.length > 0) {
                                $("#homepageTwitterStream").fadeIn("slow");
                            }

                        }, 500);
                    }
                }).render().start();
            </script>
        </div>
    </#if>

    </div>
    <!-- pagebdy -->
</div><!-- pagebdy-wrap -->
<!--page-wrap closing tag is in global_footer.ftl-->
