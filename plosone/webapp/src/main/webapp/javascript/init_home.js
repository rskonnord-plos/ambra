var ldc;
var preferenceForm;

function init(e) {
  topaz.horizontalTabs.setTabPaneSet(dojo.byId(homeConfig.tabPaneSetId));
  topaz.horizontalTabs.setTabsListObject(tabsListMap);
  topaz.horizontalTabs.setTabsContainer(dojo.byId(homeConfig.tabsContainer));
  topaz.horizontalTabs.initSimple(tabSelectId);
}

dojo.addOnLoad(init);
