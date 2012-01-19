	<!-- begin : logo -->
	<div id="logo" title="PLoS ONE: Publishing science, acclerating research"><a href="${plosOneUrl}${plosOneContext}" title="PLoS ONE: Publishing science, accelerating research"><span>PLoS ONE</span></a></div>
	<!-- end : logo -->
	<!-- begin search links -->
	<ul id="links">
<!--			<li><a href="#" title="Search PLoS ONE with advanced criteria" class="icon advanced">Advanced Search</a></li>-->
			<li><a href="${plosOneUrl}${plosOneContext}/static/rssFeeds.action" title="PLoS ONE RSS Feeds" class="icon rss">RSS</a></li>
			<li><a href="${plosOneUrl}${plosOneContext}/feedbackCreate.action" title="Send us your feedback" class="feedback">Feedback</a></li>
	</ul>
	<!-- end : search links -->
	<!-- begin : dashboard -->
	<div id="db">
		<form name="searchForm" action="${plosOneUrl}${plosOneContext}/search/simpleSearch.action" method="get">
			<fieldset>
				<legend>Search PLoS ONE</legend>
				<label for="search">Search</label>
				<div class="wrap"><input type="text" name="query" value="Search PLoS ONE..." onfocus="if(this.value=='Search PLoS ONE...')value='';" onblur="if(this.value=='')value='Search PLoS ONE...';" class="searchField" alt="Search PLoS ONE..." name="search"/></div>
				<input src="images/pone_searchinput_btn.gif" onclick="submit();" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
			</fieldset>
		</form>
	</div>
	<!-- end : dashboard -->
	<!-- begin : navigation -->
	<#include "../global/global_navigation.ftl">
	<!-- end : navigation -->