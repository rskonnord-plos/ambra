var ldc;
var preferenceForm;

function init(e) {
  ldc = dojo.widget.byId("LoadingCycle");
  
  ldc.show();

  topaz.horizontalTabs.setTabPaneSet(dojo.byId(profileConfig.tabPaneSetId));
  topaz.horizontalTabs.setTabsListObject(tabsListMap);
  topaz.horizontalTabs.setTabsContainer(dojo.byId(profileConfig.tabsContainer));
  topaz.horizontalTabs.init(tabSelectId);
  
  ldc.hide();
  
  dojo.event.connect(document, "onunload", topaz.horizontalTabs.confirmChange(topaz.horizontalTabs.targetFormObj));
  
  if (tabSelectId == "alerts") {
    var alertsForm = document.userAlerts;
    topaz.formUtil.selectCheckboxPerCollection(alertsForm.checkAllWeekly, alertsForm.weeklyAlerts);
    topaz.formUtil.selectCheckboxPerCollection(alertsForm.checkAllMonthly, alertsForm.monthlyAlerts);
  }
  
}

dojo.addOnLoad(init);
