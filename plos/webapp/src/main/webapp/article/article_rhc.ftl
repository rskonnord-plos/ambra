<!-- begin : right hand column -->
<div id="rhc">

<div id="sideNav">

	<#if articleInfoX.relatedArticles?size gt 0>
		<dl class="related">
			<dt>Related <em>PLoS</em> Articles</dt>
			<#list articleInfoX.relatedArticles as ra>
			<@s.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${ra.uri}" includeParams="none"/>
			<dd><@s.a href="%{fetchArticleURL}" title="Read Open Access Article">${ra.title}</@s.a></dd>
			</#list>
		</dl>
	</#if>
  
  <div id="floatMarker"></div>

  <div id="postcomment">
    <div class="commentview">
      <h6>Start a discussion on this article</h6>
      <ol>
        <#if Session[freemarker_config.userAttributeKey]?exists>
            <li><a href="#" id="addAnnotation" class="addannotation icon" title="First select text, then click here" onmousedown="topaz.annotation.createAnnotationOnMouseDown();">Add a note to the text</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation icon">Add a note to the text</a></li>
        </#if>

        <!-- begin : expanded block -->
        <!--  <fieldset>
            <form>
            <ol id="toggleAnnotations">
              <li>Yours  <div><input class="input" type="radio" title="Choose from one of the options" name="yours" value="On" checked>
                    <label for="yours">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="yours" value="Off">
                    <label for="yours">Off</label></div>
              </li>
              <li>Authors  <div><input class="input" type="radio" title="Choose from one of the options" name="authors" value="On" checked>
                    <label for="authors">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="authors" value="Off">
                    <label for="authors">Off</label></div>
              </li>
              <li>All Public
                    <div><input class="input" type="radio" title="Choose from one of the options" name="public" value="On" checked>
                    <label for="public">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="public" value="Off">
                    <label for="public">Off</label></div>
              </li>
            </ol>
            </form>
          </fieldset>-->
        <!-- end : expanded block -->
        
        <@s.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion" includeParams="none" target="${articleURI}" />

        <#if Session[freemarker_config.userAttributeKey]?exists>
        	<li><a href="${createDiscussionURL}" class="discuss icon">Make a general comment</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="discuss icon">Make a general comment</a></li>
        </#if>

        <@s.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${commentsURL}" class="commentary icon">View/join ongoing discussions</a>
          <ul id="dcCount1">
            <#include "/article/article_rhc_count.ftl">
          </ul>
        </li>

        <#if numCorrections gt 0>
        <@s.url id="correctionsURL" namespace="/annotation" action="getCorrectionsCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${correctionsURL}" class="corrections icon">View all corrections</a></li>
        </#if>
        
        <li><a href="#" onclick="toggleAnnotation(this, 'public'); return false;" class="collapse tooltip" title="Click to turn notes on/off">Hide notes</a></li>

        <@s.url id="trackbackURL" namespace="/article" action="listTrackbacks" includeParams="none" trackbackId="${articleURI}"/>
        <li><a href="${trackbackURL}" class="trackback icon">Trackbacks (${trackbackList?size})</a></li>

  <!-- show this if there is no commentary at all <li>Be the first to <a href="${createDiscussionURL}" class="discuss icon">start a discussion</a> or use the tools above to add your annotation!</li> -->
      </ol>
    </div>


    <div class="commentview" id="ratingRhc1">
      <#include "/article/article_rhc_rating.ftl">
    </div>


    <div id="sectionNavTop" class="tools">
    </div>
    <!--<div id="dojoDebug"></div>-->
  </div>


  <!-- This extra container is for Safari who has a really broken javascript engine. -->
  <div id="postcommentfloat">
    <div class="commentview">
      <h6>Start a discussion on this article</h6>
      <ol>
        <#if Session[freemarker_config.userAttributeKey]?exists>
            <li><a href="#" id="addAnnotation" class="addannotation icon" title="First select text, then click here" onmousedown="topaz.annotation.createAnnotationOnMouseDown();">Add a note to the text</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation icon">Add a note to the text</a></li>
        </#if>

        <!-- begin : expanded block -->
        <!--  <fieldset>
            <form>
            <ol id="toggleAnnotations">
              <li>Yours  <div><input class="input" type="radio" title="Choose from one of the options" name="yours" value="On" checked>
                    <label for="yours">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="yours" value="Off">
                    <label for="yours">Off</label></div>
              </li>
              <li>Authors  <div><input class="input" type="radio" title="Choose from one of the options" name="authors" value="On" checked>
                    <label for="authors">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="authors" value="Off">
                    <label for="authors">Off</label></div>
              </li>
              <li>All Public
                    <div><input class="input" type="radio" title="Choose from one of the options" name="public" value="On" checked>
                    <label for="public">On</label>
                    <input class="input" type="radio" title="Choose from one of the options" name="public" value="Off">
                    <label for="public">Off</label></div>
              </li>
            </ol>
            </form>
          </fieldset>-->
        <!-- end : expanded block -->
        
        <#if Session[freemarker_config.userAttributeKey]?exists>
        	<@s.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion" includeParams="none" target="${articleURI}" />
        	<li><a href="${createDiscussionURL}" class="discuss icon">Make a general comment</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="discuss icon">Make a general comment</a></li>
        </#if>

        <@s.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${commentsURL}" class="commentary">View/join ongoing discussions</a>
          <ul id="dcCount2">
            <#include "/article/article_rhc_count.ftl">
          </ul>
        </li>
        
        <#if numCorrections gt 0>
        <@s.url id="correctionsURL" namespace="/annotation" action="getCorrectionsCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${correctionsURL}" class="corrections icon">View all corrections</a></li>
        </#if>
        
        <li><a href="#" onclick="toggleAnnotation(this, 'public'); return false;" class="collapse tooltip" title="Click to turn notes on/off">Hide notes</a></li>
        <li><a href="${trackbackURL}" class="trackback icon">Trackbacks (${trackbackList?size})</a></li>

  <!-- show this if there is no commentary at all <li>Be the first to <a href="${createDiscussionURL}" class="discuss icon">start a discussion</a> or use the tools above to add your annotation!</li> -->
      </ol>
    </div>

    <div class="commentview" id="ratingRhc2">
      <#include "/article/article_rhc_rating.ftl">
    </div>

    <div id="sectionNavTopFloat" class="tools">
    </div>
  </div>


</div>


</div>
<!-- end : right hand column -->
