<#import "/article/article_variables.ftl" as article>
<#import "/includes/global_body.ftl" as global>

<#include "article_common.ftl">

<div id="pagebdy-wrap">
  <div id="pagebdy">

    <div id="article-block" class="cf">
      <div class="article-meta cf">
        <ul>
          <li class="pick">Editor's Pick</li>
          <li><span class="num">1,278</span> views</li>
          <li><span class="num">12</span> citations</li>
        </ul>
        <div class="article-type">
          <span class="type oa">Open Access Article</span>
          <span class="type pr">Peer-Reviewed</span>
        </div>
      </div>
      <div class="header" id="hdr-article">
        <div class="article-kicker">RESEARCH IN TRANSLATION</div>
        <h1><@articleFormat>${article.docTitle}</@articleFormat></h1>
        <p class="authors">
          <#if authorExtras?? >
            <#list authorExtras as author>
              <#if (author_has_next)>
                <span rel="dc:creator" class="author"><span class="person" property="foaf:name" typeof="foaf:Person">${author.authorName}<#if author.equalContrib == "yes"><span class="equal-contrib" title="These authors contributed equally to this work">equal contributor</span></#if>,</span></span>
              <#else>
                <span rel="dc:creator" class="author"><span class="person" property="foaf:name" typeof="foaf:Person">${author.authorName}<#if author.equalContrib == "yes"><span class="equal-contrib" title="These authors contributed equally to this work">equal contributor</span></#if></span></span>
              </#if>
            </#list>
          </#if>
        </p>
      </div>
      <div class="main cf">

        <div class="nav" id="nav-article">
          <ul>
            <li><a href="${articleTabURL}">Article</a></li>
            <li><span class="active">About the Authors</span></li>
            <li><a href="${commentsTabURL}">Comments &amp; Letters</a></li>
            <li><a href="${metricsTabURL}">Metrics</a></li>
            <li><a href="${relatedTabURL}">Related Content</a></li>
            <li><a href="${relatedTabURL}">Corrections</a></li>
          </ul>
        </div>

        <div id="about-authors">
          <h2>About the Authors</h2>

          <dl class="authors">
            <#list authorExtras as author>
              <dt>
                ${author.authorName}
              </dt>
              <dd>
                <#list author.affiliations as affiliation>
                  ${affiliation} <#if affiliation_has_next>, </#if>
                </#list>
              </dd>
            </#list>
          </dl>

          <h3>Corresponding Author</h3>
          <p>Email: ${correspondingAuthor}</a></p>
          <h3>Competing Interests</h3>
          <p>${competingInterest}</p>
          <h3>Author Contributions</h3>
          <p>${authorContributions}</p>

        </div>

      </div><!-- main -->

      <div class="sidebar">

        <div class="article-actions cf">
          <a href="TEST" class="btn">Download</a>
          <div class="btn-reveal flt-l">
            <span class="btn">Print</span>
            <div class="content">
              <ul class="bullet">
                <li><a href="#" onclick="window.print();return false;" title="Print Article">Print article</a></li>
                <li><a href="TEST" title="Odyssey Press">EzReprint</a> <img src="images/icon.new.png" width="29" height="12" alt="New"> <span class="note">New &amp; improved!</span></li>
              </ul>
            </div>
          </div>
          <div class="btn-reveal flt-r">
            <span class="btn">Share</span>
            <div class="content">
              <ul class="social">
                <li><a href="TEST"><img src="images/icon.reddit.16.png" width="16" height="16" alt="Reddit"> Reddit</a></li>
                <li><a href="TEST"><img src="images/icon.gplus.16.png" width="16" height="16" alt="G+"> G+</a></li>
                <li><a href="TEST"><img src="images/icon.stumble.16.png" width="16" height="16" alt="StumbleUpon"> StumbleUpon</a></li>
                <li><a href="TEST"><img src="images/icon.fb.16.png" width="16" height="16" alt="Facebook"> Facebook</a></li>
                <li><a href="TEST"><img src="images/icon.connotea.16.png" width="16" height="16" alt="Connotea"> Connotea</a></li>
                <li><a href="TEST"><img src="images/icon.cul.16.png" width="16" height="16" alt="CiteULike"> CiteULike</a></li>
                <li><a href="TEST"><img src="images/icon.mendeley.16.png" width="16" height="16" alt="Mendeley"> Mendeley</a></li>
                <li><a href="TEST"><img src="images/icon.twtr.16.png" width="16" height="16" alt="Twitter"> Twitter</a></li>
                <li><a href="TEST"><img src="images/icon.email.16.png" width="16" height="16" alt="Email"> Email</a></li>
              </ul>
            </div>
          </div>
        </div>

        <div class="block">
          <div class="header">
            <h3>Editor Recommends</h3>
          </div>
          <p><a href="TEST">Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros.</a></p>
          <p><a href="TEST">Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros.</a></p>
          <p><a href="TEST">Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Donec odio. Quisque volutpat mattis eros.</a></p>
        </div>

        <div class="block">
          <div class="header">
            <h3>Commments</h3>
          </div>
          <p><a href="TEST">Please explain the relevance to humans and concern that ADs cause damage to brain cells.</a><br>
            Posted by BryanRoth</p>
          <p><a href="TEST">Kudos on the excellent use of the yeast model</a><br>
            Posted by jkrise</p>
          <p><a href="TEST">Some layman's questions about your article</a><br>
            Posted by ArniePerlstein</p>
        </div>

        <div class="ad">
          <div class="title">Advertisement</div>
          <img src="images/placeholder.gif" width="160" height="600" alt="">
        </div>

      </div><!-- sidebar -->
    </div><!-- article-block -->

    <div id="banner-ftr">
      <div class="content">
        <div class="col col-1">
          <h4>Did you know?</h4>
        </div>
        <div class="col col-2">
          Some great message PLOS wants to tell all readers could go here! Lorem ipsum laoreet dolore magna aliquam erat volutpat.
        </div>
        <div class="col col-3">
          <div class="stat">
            <span class="num">23</span> <span class="txt">people reading this paper now</span>
          </div>
        </div>
      </div>
    </div>

  </div><!-- pagebdy -->

</div><!-- pagebdy-wrap -->
