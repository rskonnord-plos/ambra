  <div id="content">
    <h1>Ratings</h1>
      <div class="source">
        <span>Original Article</span>
        <@s.url id="fetchArticleURL" namespace="/article" action="fetchArticle" articleURI="${articleURI}"/>

        <a href="${fetchArticleURL}" title="Back to original article" class="article icon">${articleTitle}
          <#if articleOverallRounded?exists>
            <span class="inline-rating inlineRatingEnd">
              <ul class="star-rating pone_rating" title="overall">
                <#assign overallPct = (20 * articleOverallRounded)?string("##0")>
                <li class="current-rating pct${overallPct}">Currently ${articleOverallRounded?string("0.#")}/5 Stars.</li>
              </ul>
            </span>
          </#if>
        </a>
        <!--<p><a href="/annotation/getCommentary.action?target=${articleURI}" class="commentary icon">See all commentary</a> on this article</p>-->
      </div>

      <#list articleRatingSummaries as articleRatingSummary>
        <@s.url id="fetchUserURL" namespace="/user" action="showUser" userId="${articleRatingSummary.creatorURI}"/>
        <div class="response ratingComment">
          <div class="hd">
            <!-- begin : response title : user -->
            <h3>
              <#if articleRatingSummary.commentTitle?exists>
                ${articleRatingSummary.commentTitle}
              </#if>
              <span class="detail">Posted by <a href="${fetchUserURL}" title="Annotation Author" class="user icon">${articleRatingSummary.creatorName}</a>
                on <strong>${articleRatingSummary.created?string("dd MMM yyyy '</strong>at<strong>' HH:mm zzz")}</strong>
              </span>
            </h3>
            <!-- end : response title : user -->
          </div>
          <!-- begin : response body text -->
          <div class="ratingDetail">
            <div class="posterRating">
              <ol class="ratingAvgs">
                <#if articleRatingSummary.insight?exists>
                  <li><label for="insight">Insight</label>
                      <ul class="star-rating pone_rating" title="insight">
                        <#assign insightPct = (20 * articleRatingSummary.insight)?string("##0")>
                        <li class="current-rating average pct${insightPct}">Currently ${articleRatingSummary.insight?string("0.#")}/5 Stars.</li>
                      </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.reliability?exists>
                  <li><label for="reliability">Reliability</label>
                    <ul class="star-rating pone_rating" title="reliability">
                      <#assign reliabilityPct = (20 * articleRatingSummary.reliability)?string("##0")>
                      <li class="current-rating average pct${reliabilityPct}">Currently ${articleRatingSummary.reliability?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.style?exists>
                  <li><label for="style">Style</label>
                    <ul class="star-rating pone_rating" title="style">
                      <#assign stylePct = (20 * articleRatingSummary.style)?string("##0")>
                      <li class="current-rating average pct${stylePct}">Currently ${articleRatingSummary.style?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
                <#if articleRatingSummary.overallRounded?exists>
                  <li><label for="overall">Overall</label>
                    <ul class="star-rating pone_rating" title="overall">
                      <#assign overallPct = (20 * articleRatingSummary.overallRounded)?string("##0")>
                      <li class="current-rating average pct${overallPct}">Currently ${articleRatingSummary.overallRounded?string("0.#")}/5 Stars.</li>
                    </ul>
                  </li>
                </#if>
              </ol>
            </div>
            <blockquote>
              <#if articleRatingSummary.commentValue?exists>
                <p>${articleRatingSummary.commentValue}</p>
              </#if>
            </blockquote>
          </div>
          <!-- end : response body text -->

          <!-- begin : toolbar options -->
          <div class="toolbar">
            <#if articleRatingSummary.commentTitle?exists>
              <#assign flagTitle = "${articleRatingSummary.commentTitle}">
            <#else>
              <#assign flagTitle = "Flag this rating">
            </#if>
            <ul>
              <li>
                <#if Session[freemarker_config.userAttributeKey]?exists>
                  <a href="#" onclick="topaz.responsePanel.show(this, _dcf, 'toolbar', '${articleRatingSummary.ratingId}', null, null, 2); return false;" class="flag tooltip" title="Request Review">Request review</a>
                <#else>
                  <a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="flag tooltip" title="Request review">Request review</a>
                </#if>
              </li>
            </ul>
          </div>
          <!-- end : toolbar options -->
        </div>

        <div class="rsep"></div>
      </#list>
    </div>
