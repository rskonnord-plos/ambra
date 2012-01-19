	<!-- begin : logo -->
	<div id="logo" title="PLoS ONE: Publishing science, acclerating research"><a href="http://${freemarker_config.plosOneHost}${freemarker_config.context}" title="PLoS ONE: Publishing science, accelerating research"><span>PLoS ONE</span></a></div>
	<!-- end : logo -->
	<!-- begin : user controls -->
	<@ww.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
	<#assign thisPage = thisPageURL?replace("&amp;", "&")?url>
	<#if Session.PLOS_ONE_USER?exists>
	<div id="user">
		<div>
		<@ww.url id="editProfileURL" includeParams="none" namespace="/user/secure" action="editProfile" tabId="preferences"/>
			<p>Logged in as <a href="${freemarker_config.context}/user/showUser.action?userId=${Session.PLOS_ONE_USER.userId}" class="icon user" title="Logged in username">${Session.PLOS_ONE_USER.displayName}</a></p>
				<ul>
					<li><a href="${editProfileURL}" class="icon preferences" title="View and edit my account preferences and alerts">Preferences</a></li>
					<li><a href="${freemarker_config.casLogoutURL}?service=http://${freemarker_config.plosOneHost}${freemarker_config.context}/logout.action" class="icon logout" title="Logout of my PLoS ONE account">Logout</a></li>
				</ul>
		</div>
	</div>
	
	<#else>

	<div id="user">
		<div>
			<ul>
				<li><a href="${freemarker_config.registrationURL}">Create Account</a></li>
				<@ww.url id="loginURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${thisPage}"/>
				<li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="feedback">Login</a></li>
			</ul>
		</div>
	</div>

	</#if>
	
	<!-- end : user controls -->
	<!-- begin search links -->
	<ul id="links">
<!--			<li><a href="#" title="Search PLoS ONE with advanced criteria" class="icon advanced">Advanced Search</a></li>-->
			<@ww.url id="rssURL" includeParams="none" namespace="/static" action="rssFeeds"/>
			<li><a href="${rssURL}" title="PLoS ONE RSS Feeds" class="icon rss">RSS</a></li>
			<@ww.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPageURL?url}"/>
			<li><a href="${feedbackURL}" title="Send us your feedback" class="feedback">Feedback</a></li>
	</ul>
	<!-- end : search links -->
	<!-- begin : dashboard -->
	<div id="db">
		<@ww.url id="searchURL" includeParams="none" namespace="/search" action="simpleSearch" />
		<form name="searchForm" action="${searchURL}" method="get">
			<fieldset>
				<legend>Search PLoS ONE</legend>
				<label for="search">Search</label>
				<div class="wrap"><input type="text" name="query" value="Search PLoS ONE..." onfocus="if(this.value=='Search PLoS ONE...')value='';" onblur="if(this.value=='')value='Search PLoS ONE...';" class="searchField" alt="Search PLoS ONE..."/></div>
				<input src="${freemarker_config.context}/images/pone_searchinput_btn.gif" onclick="submit();" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
			</fieldset>
		</form>
	</div>
	<!-- end : dashboard -->
	<!-- begin : navigation -->
	<#include "../global/global_navigation.ftl">
	<!-- end : navigation -->