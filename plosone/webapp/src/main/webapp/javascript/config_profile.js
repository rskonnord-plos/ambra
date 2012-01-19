var tabsListMap = new Array();

tabsListMap[tabsListMap.length] = {tabKey:   "preferences",
                                   title:    "Preferences",
                                   formName: "userForm",
                                   urlLoad:  "/user/secure/editAjaxProfile.action",
                                   urlSave:  "/user/secure/saveAjaxProfile.action"};

tabsListMap[tabsListMap.length] = {tabKey:   "alerts",
                                   title:    "Alerts",
                                   formName: "userAlerts",
                                   urlLoad:  "/user/secure/editAjaxAlerts.action",
                                   urlSave:  "/user/secure/saveAjaxAlerts.action"};

var querystring = topaz.htmlUtil.getQuerystring();
var tabSelectId = "";

for (var i=0; i<querystring.length; i++) {
  if (querystring[i].param == "tabId") {
    tabSelectId = querystring[i].value;
  }
}

var profileConfig = {
    tabPaneSetId: "tabPaneSet",
    tabsContainer: "tabsContainer",
    tabSelectId: tabSelectId
  }                                 