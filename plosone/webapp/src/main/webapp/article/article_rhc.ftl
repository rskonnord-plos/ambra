<!-- begin : right hand column -->
<div id="rhc">

<div id="sideNav">
  <div class="links">
    <ul>
      <@s.url id="articleArticleRepXML"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
        <@s.param name="representation" value="%{'XML'}"/>
      </@s.url>
      <li><a href="${articleArticleRepXML}" class="xml" title="Download XML">Download Article XML</a></li>
      <@s.url id="articleArticleRepPDF"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
        <@s.param name="representation" value="%{'PDF'}"/>
      </@s.url>
      <li><a href="${articleArticleRepPDF}" class="pdf" title="Download PDF">Download Article PDF</a></li>
      <@s.url id="articleCitationURL"  namespace="/article" action="citationList" includeParams="none" articleURI="${articleURI}" />
      <li><@s.a href="%{articleCitationURL}"  cssClass="citation" title="Download Citation">Download Citation</@s.a></li>
      <@s.url id="emailArticleURL" namespace="/article" action="emailArticle" articleURI="${articleURI}"/>
      <li><@s.a href="%{emailArticleURL}"  cssClass="email" title="E-mail This Article to a Friend or Colleague">E-mail this Article</@s.a></li>
      <li><a href="http://www.plos.org/journals/print.html" title="Order reprinted versions of this article" class="reprints icon">Order Reprints</a></li>
      <li><a href="#" onclick="window.print();return false;" class="print last" title="Print this article">Print this Article</a></li>
    </ul>
  </div>

  <div id="floatMarker"></div>

  <div id="postcomment">
    <div class="commentview">
      <h6>Commentary</h6>
      <ol>
        <#if Session[freemarker_config.userAttributeKey]?exists>
            <li><a href="#" id="addAnnotation" class="addannotation tooltip" title="First select text, then click here" onmousedown="topaz.annotation.createAnnotationOnMouseDown();">Add your annotation</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation tooltip">Add your annotation</a></li>
        </#if>
        <li><a href="#" onclick="toggleAnnotation(this, 'public'); return false;" class="collapse tooltip" title="Click to turn annotations on/off">Hide annotations</a>

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
        </li>
        <@s.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion" includeParams="none" target="${articleURI}" />

        <li><a href="${createDiscussionURL}" class="discuss icon">Start a discussion</a> about this article</li>

        <@s.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${commentsURL}" class="commentary icon">See all commentary</a> on this article
          <ul id="dcCount1">
            <#include "/article/article_rhc_count.ftl">
          </ul>
        </li>
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
      <h6>Commentary</h6>
      <ol>
        <#if Session[freemarker_config.userAttributeKey]?exists>
            <li><a href="#" id="addAnnotation" class="addannotation tooltip" title="First select text, then click here" onmousedown="topaz.annotation.createAnnotationOnMouseDown();">Add your annotation</a></li>
        <#else>
            <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation tooltip">Add your annotation</a></li>
        </#if>
        <li><a href="#" onclick="toggleAnnotation(this, 'public'); return false;" class="collapse tooltip" title="Click to turn annotations on/off">Hide annotations</a>

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
        </li>
        <@s.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion" includeParams="none" target="${articleURI}" />

        <li><a href="${createDiscussionURL}" class="discuss icon">Start a discussion</a> about this article</li>

        <@s.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
        <li><a href="${commentsURL}" class="commentary icon">See all commentary</a> on this article
          <ul id="dcCount2">
            <#include "/article/article_rhc_count.ftl">
          </ul>
        </li>
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
