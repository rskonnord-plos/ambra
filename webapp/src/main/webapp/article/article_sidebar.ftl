<div class="sidebar">

  <div class="article-actions cf">
    <div class="btn-reveal">
      <span class="btn">Download</span>
      <div class="content">
        <ul class="bullet">
          <li><a href="${articlePDFURL}" title="Download article PDF">PDF</a></li>
          <li><a href="${articleCitationURL}" title="Download citations">Citation</a></li>
          <li><a href="${articleXMLURL}" title="Download article XML">XML</a></li>
        </ul>
      </div>
    </div>
    <div class="btn-reveal flt-l">
      <span class="btn">Print</span>
      <div class="content">
        <ul class="bullet">
        <#if tab?? && tab == "article">
          <li><a href="#" onclick="window.print();return false;" title="Print Article">Print article</a></li>
        </#if>
        <#if article.ezReprintLink??>
          <li>
            <a href="${article.ezReprintLink}" title="Odyssey Press">EzReprint</a>
          </li>
        <#else>
          <li>
            <a href="http://www.authorbilling.com/client.php?ID=1806" title="Order Reprints">Order Reprints</a>
            <img src="/images/icon.reprint.gif" width="29" height="12" alt="New">
          </li>
        </#if>
        </ul>
      </div>
    </div>
    <div class="btn-reveal flt-r">
      <span class="btn">Share</span>
      <div class="content">
        <ul class="social">
          <#--
          some notes about social media buttons:
          
          1) the current document's URL passed to the 'url' query parameter 
          of the service needs to be escaped by freemarker via the '?url' 
          string method. (the same goes for the title, though titles are 
          usually determined by the service, so we (usually) avoid passing 
          them.)
          
          2) we're avoiding using external scripts where possible to reduce 
          external dependencies, which directly and negatively affect page 
          load time.

          -JSB/DP
          
          -->

          <#-- reddit, as per <http://www.reddit.com/buttons/> but modified to not use JS for encoding -->
          <li><a href="http://www.reddit.com/submit?url=${article.docURL?url}" target="_blank" title="Submit to Reddit"><img src="/images/icon.reddit.16.png" width="16" height="16" alt="Reddit">Reddit</a></li>

          <#-- google plus, as per <https://developers.google.com/+/plugins/share/#sharelink>  -->
          <li><a href="https://plus.google.com/share?url=${article.docURL?url}" target="_blank" title="Share on Google+"><img src="/images/icon.gplus.16.png" width="16" height="16" alt="Google+">Google+</a></li>

          <#-- stumbleupon, as per previous implementation. no current public 
          documentation can be found on their site or elsewhere. -->
          <li><a href="http://www.stumbleupon.com/submit?url=${article.jDocURL?url}" target="_blank" title="Add to StumbleUpon"><img src="/images/icon.stumble.16.png" width="16" height="16" alt="StumbleUpon">StumbleUpon</a></li>

          <#-- facebook, as per previous implementation which uses the now 
          deprecated share.php (which redirects to /sharer/sharer.php) -->
          <li><a href="http://www.facebook.com/share.php?u=${article.docURL?url}&amp;t=${article.noHTMLDocTitle?url}" target="_blank" title="Share on Facebook"><img src="/images/icon.fb.16.png" width="16" height="16" alt="Facebook">Facebook</a></li>

          <#-- citeulike, as per <http://www.citeulike.org/bookmarklets.adp>
          and <http://wiki.citeulike.org/index.php/Organizing_your_library#Any_other_posting_tricks.3F> -->
          <li><a href="http://www.citeulike.org/posturl?url=${article.docURL?url}&amp;title=${article.noHTMLDocTitle?url}" target="_blank" title="Add to CiteULike"><img src="/images/icon.cul.16.png" width="16" height="16" alt="CiteULike">CiteULike</a></li>

          <#-- mendeley, as per previous implementation. no current public 
          documentation can be found on their site or elsewhere. -->
          <li><a href="http://www.mendeley.com/import/?url=${article.docURL?url}" target="_blank" title="Add to Mendeley"><img src="/images/icon.mendeley.16.png" width="16" height="16" alt="Mendeley">Mendeley</a></li>

          <#-- twitter, as per previous implementation <http://www.saschakimmel.com/2009/05/how-to-create-a-dynamic-tweet-this-button-with-javascript/>,
          but slightly modified to work without an (evil) document.write call 
          and updated to account for new twitter URL auto-shortening. in 
          theory, this could/should be done in freemarker instead of via JS 
          but, alas, my freemarker skills are not mad enuff. -JSB/DP -->
          <script type="text/javascript">
            // replace tweet with one that's pre-shortened to 140 chars
            function truncateTweetText() {
              var twtTitle = '${article.noHTMLDocTitle?replace("\'", "\\\'")}';
              var twtUrl = '${article.docURL?replace("\'", "\\\'")}';
              // all URLs posted to twitter get auto-shortened to 20 chars.
              var maxLength = 140 - (20 + 1);
              // truncate the title to include space for twtTag and ellipsis (here, 10 = tag length + space + ellipsis)
              if (twtTitle.length > maxLength) { twtTitle = twtTitle.substr(0, (maxLength - 10)) + '...'; }
              // set the href to use the shortened tweet
              $('#twitter-share-link').prop('href', 'http://twitter.com/intent/tweet?text=' + encodeURIComponent('${freemarker_config.getHashTag(journalContext)}: ' + twtTitle + ' ' + twtUrl));
            }
          </script>
          <li><a href="http://twitter.com/intent/tweet?text=${freemarker_config.getHashTag(journalContext) + ': '?url + article.noHTMLDocTitle?url + ' ' + article.docURL?url}" onclick="truncateTweetText();" target="_blank" title="Share on Twitter" id="twitter-share-link"><img src="/images/icon.twtr.16.png" width="16" height="16" alt="Twitter">Twitter</a></li>

          <@s.url id="emailArticleURL" namespace="/article" action="emailArticle" articleURI="${articleInfoX.doi}" />
          <li><a href="${emailArticleURL}" title="Email this article"><img src="/images/icon.email.16.png" width="16" height="16" alt="Email">Email</a></li>
        </ul>
      </div>
    </div>
  </div>

  <#if articleInfoX?? && articleInfoX.relatedArticles?size gt 0>
    <div class="block">
      <div class="header">
        <h3>Related Articles</h3>
      </div>
      <#list articleInfoX.relatedArticles as ra>
        <p><@s.a href="${freemarker_config.doiResolverURL}${ra.uri?replace('info:doi/','')}" title="Read Open-Access Article"><@articleFormat>${ra.title}</@articleFormat></@s.a></p>
      </#list>
    </div>
  </#if>

  <#if article.collections??>
    <div class="block">
      <div class="header">
        <h3>Included in the Following Collection</h3>
      </div>
      <p>${article.collections}</p>
    </div>
  </#if>

  <#if commentary?has_content>
    <div class="block">
      <div class="header">
        <h3>Comments</h3>
      </div>
      <#list commentary as comment>
        <@s.url action="listThread" namespace="/annotation" root="${comment.ID?c}" id="commentUrl"/>
        <p><a href="${commentUrl}">${comment.title}</a><br>Posted by ${comment.creatorDisplayName}</p>
        <#if comment_index == 2>
          <#break>
        </#if>
      </#list>
    </div>
  </#if>

  <div class="ad">
    <div class="title">Advertisement</div>
    <#include "article_rightbanner.ftl">
  </div>

</div><!-- sidebar -->
