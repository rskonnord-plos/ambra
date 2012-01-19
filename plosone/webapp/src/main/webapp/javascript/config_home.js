/*var djConfig = {
	isDebug: true,
	debugContainerId : "dojoDebug",
	debugAtAllCosts: false,
  bindEncoding: "UTF-8"
};*/

var tabsListMap = new Array();

tabsListMap[tabsListMap.length] = {tabKey:    "recentlyPublished",
                                   title:     "Recently Published",
                                   className: "published",
                                   urlLoad:   "/article/recentArticles.action",
                                   urlSave:   ""};

tabsListMap[tabsListMap.length] = {tabKey:    "mostCommented",
                                   title:     "Most Annotated",
                                   className: "annotated",
                                   urlLoad:   "/article/mostCommented.action",
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