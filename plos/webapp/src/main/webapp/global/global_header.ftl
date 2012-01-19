  <!-- begin : logo -->
  <div id="logo" title="${freemarker_config.getDisplayName(journalContext)}"><a href="${homeURL}" title="${freemarker_config.getDisplayName(journalContext)}"><span>${freemarker_config.getDisplayName(journalContext)}</span></a></div>
  <!-- end : logo -->
  <!-- begin : user controls -->
  <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
  <!-- remove duplicate articleURI specification, e.g. /article/doi?articleURL=doi -->
  <#if thisPageURL?matches(r"^(/.+)?/article/info(:|%3A)doi(/|%2F).+")>
    <#assign thisPage = thisPageURL?replace(r"\??articleURI=info%3Adoi%2F.{30}", "", "r")?replace("&amp;", "&")?url>
  <#else>
    <#assign thisPage = thisPageURL?replace("&amp;", "&")?url>
  </#if>
  <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPage}" encode="false"/>

  <#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
  <div id="user">
    <div>
    <@s.url id="editProfileURL" includeParams="none" namespace="/user/secure" action="editProfile" tabId="preferences"/>
        <p>Welcome, <!--<a href="${freemarker_config.context}/user/showUser.action?userId=${Session[freemarker_config.userAttributeKey].userId}" title="You are logged in as ${Session[freemarker_config.userAttributeKey].displayName}">--><strong>${Session[freemarker_config.userAttributeKey].displayName}</strong></a>! | </p>
        <ul>
          <@s.url id="logoutURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${freemarker_config.casLogoutURL}?service=${Request[freemarker_config.journalContextAttributeKey].baseUrl}/logout.action"/>
          <li><a href="${editProfileURL}" title="Edit your account preferences and alert settings">Preferences</a> | </li>
          <li><a href="${logoutURL}" title="Logout">Logout</a></li>
        </ul>
    </div>
  </div>

  <#else>

  <div id="user">
    <div>
      <ul>
        <@s.url id="loginURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${thisPage}"/>
        <li><a href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${thisPage}" class="feedback"><strong>Login</strong></a> | </li>
        <li><a href="${freemarker_config.registrationURL}">Create Account</a> | </li>
        <li class="feedback"><a href="${feedbackURL}" title="Send us your feedback">Feedback</a></li>
      </ul>
    </div>
  </div>

  </#if>

  <!-- end : user controls -->
  <!-- begin search links -->
  <ul id="links"><@s.url id="browseURL" includeParams="none" namespace="/article" action="browse"/><li class="browse"><a href="${browseURL}?field=date" title="Browse Articles">Browse</a></li><@s.url id="rssURL" includeParams="none" namespace="/static" action="rssFeeds"/><li class="rss"><a href="${rssURL}" title="RSS Feeds">RSS</a></li></ul>
  <!-- end : search links -->
  <!-- begin : dashboard -->
  <div id="db">
    <@s.url id="searchURL" includeParams="none" namespace="/search" action="simpleSearch" />
    <form name="searchForm" action="${searchURL}" method="get">
      <fieldset>
        <legend>Search</legend>
        <label for="search">Search</label>
        <div class="wrap"><input id="search" type="text" name="query" value="Search articles..." onfocus="if(this.value=='Search articles...')value='';" onblur="if(this.value=='')value='Search articles...';" class="searchField" alt="Search articles..."/></div>
        <input src="${freemarker_config.context}/images/pone_search_btn1.gif" onclick="submit();" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
      </fieldset>
    </form>
    <@s.url action="advancedSearch" namespace="/search" includeParams="none" id="advancedSearch"/>
    <a href="${advancedSearch}" id="advSearch">Advanced Search</a>
  </div>
  <!-- end : dashboard -->
  <!-- begin : navigation -->

  <#-- BEGIN MAJOR HACK FOR CONDITIONAL JOURNAL INCLUDE -->
  <#if journalContext = "PLoSClinicalTrials" >
    <#include "/journals/clinicalTrials/global/global_navigation.ftl">
  <#elseif journalContext = "PLoSCompBiol"> 
     <#include "/journals/compbiol/global/global_navigation.ftl">
  <#elseif journalContext = "PLoSGenetics"> 
     <#include "/journals/genetics/global/global_navigation.ftl">
  <#elseif journalContext = "PLoSNTD"> 
     <#include "/journals/ntd/global/global_navigation.ftl">
  <#elseif journalContext = "PLoSPathogens"> 
     <#include "/journals/pathogens/global/global_navigation.ftl">
  <#else>
     <#include "../global/global_navigation.ftl">
  </#if>
  <#-- END HACK -->


  <!-- end : navigation -->
