<!-- begin : right hand column -->
<div id="rhc">

<div id="sideNav">
	<div class="links">
		<ul>
	    <@ww.url id="articleArticleRepXML"  action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
        <@ww.param name="representation" value="%{'XML'}"/>
	    </@ww.url>
			<li><a href="${articleArticleRepXML}" class="xml" title="Download XML">Download Article XML</a></li>
	    <@ww.url id="articleArticleRepPDF"  action="fetchObjectAttachment" includeParams="none" uri="${articleURI}">
	      <@ww.param name="representation" value="%{'PDF'}"/>
      </@ww.url>
			<li><a href="${articleArticleRepPDF}" class="pdf" title="Download PDF">Download Article PDF</a></li>
      <@ww.url id="emailArticleURL" namespace="/article" action="emailArticle" articleURI="${articleURI}"/>
      <li><@ww.a href="%{emailArticleURL}"  cssClass="email" title="E-mail This Article to a Friend or Colleague">E-mail this Article</@ww.a></li>
		<li><a href="http://www.plos.org/journals/print.html" title="Order reprinted versions of this article" class="reprints icon">Order Reprints</a></li>
      <li><a href="#" onclick="window.print();return false;" class="print last" title="Print this article">Print this Article</a></li>
		</ul>
	</div>

	<div id="floatMarker"></div>
	<div id="postcomment">
		<div class="commentview">
			<h6>Commentary</h6>
			<ol>
				<#if Session.PLOS_ONE_USER?exists>
						<li><a href="#" id="addAnnotation" class="addannotation tooltip" title="First select text, then click here">Add your annotation</a>
			  <#else>
						<li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" id="addAnnotation" class="addannotation tooltip">Add your annotation</a>
			  </#if>
				<li><a href="#" onclick="toggleAnnotation(this, 'public'); return false;" class="collapse tooltip" title="Click to turn annotations on/off">Hide annotations</a>
				<!--<li><a href="#" onclick="return topaz.domUtil.swapDisplayMode('toggleAnnotations');" class="expand tooltip" title="Click to turn annotations on/off">Turn annotations on/off</a>-->
				<!-- begin : expanded block -->
				<!--	<fieldset>
						<form>
						<ol id="toggleAnnotations">
							<li>Yours  <div><input class="input" type="radio" title="Choose from one of the options" name="yours" value="On" checked>
										<label for="yours">On</label>
										<input class="input" type="radio" title="Choose from one of the options" name="yours" value="Off">
										<label for="yours">Off</label></div>
							</li>
							<li>Authors	<div><input class="input" type="radio" title="Choose from one of the options" name="authors" value="On" checked>
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
		    <@ww.url id="createDiscussionURL" namespace="/annotation/secure" action="startDiscussion" includeParams="none" target="${articleURI}" />

				<li><a href="${createDiscussionURL}" class="discuss icon">Start a discussion</a> about this article</li>

		    <@ww.url id="commentsURL" namespace="/annotation" action="getCommentary" includeParams="none" target="${articleURI}"/>
				<li><a href="${commentsURL}" class="commentary icon">See all commentary</a> on this article
					<ul id="dcCount">
					  <#include "/article/article_rhc_count.ftl">
					</ul>
				</li>
				
	<!-- show this if there is no commentary at all <li>Be the first to <a href="${createDiscussionURL}" class="discuss icon">start a discussion</a> or use the tools above to add your annotation!</li> -->
			</ol>
		</div> 
		<div id="sectionNavTop" class="tools">
		</div>
		<!--<div id="dojoDebug"></div>-->
	</div>
	
	
</div>


</div>
<!-- end : right hand column -->
