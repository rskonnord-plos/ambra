/*var djConfig = {
	isDebug: true,
	debugContainerId : "dojoDebug",
	debugAtAllCosts: false,
  bindEncoding: "UTF-8"
};*/

var tabsListMap = new Array();

tabsListMap[tabsListMap.length] = {tabKey:    "recentResearch",
                                   title:     "Recent Research",
                                   className: "published",
                                   urlLoad:   "/article/recentArticles.action",
                                   urlSave:   ""};

tabsListMap[tabsListMap.length] = {tabKey:    "featuredComments",
                                   title:     "Featured Comments",
                                   className: "annotated",
                                   urlLoad:   "/article/mostCommented.action",
                                   urlSave:   ""};

tabsListMap[tabsListMap.length] = {tabKey:    "mostViewed",
                                   title:     "Most Viewed",
                                   className: "viewed",
                                   urlLoad:   "/article/mostViewed.action",
                                   urlSave:   ""};
                                   
var querystring = topaz.htmlUtil.getQuerystring();
var tabSelectId = "";

for (var i=0; i<querystring.length; i++) {
  if (querystring[i].param == "tabId") {
    tabSelectId = querystring[i].value;
  }
}

var homeConfig = {
    tabPaneSetId: "tabPaneSet",
    tabsContainer: "tabsContainer",
    tabSelectId: tabSelectId
  }                                 