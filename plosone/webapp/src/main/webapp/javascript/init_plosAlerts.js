var ldc;
var formObj;
function init(e) {
  ldc = dojo.widget.byId("LoadingCycle");
  
  formObj = document.userAlerts;

  formObj.action = namespace + "/user/secure/saveAlerts.action";
  dojo.event.connect(formObj.formSubmit, "onclick", function() {
      formObj.submit(); 
      return true;
    }
  );
  
  var alertsForm = document.userAlerts;
  topaz.formUtil.selectCheckboxPerCollection(alertsForm.checkAllWeekly, alertsForm.weeklyAlerts);
  topaz.formUtil.selectCheckboxPerCollection(alertsForm.checkAllMonthly, alertsForm.monthlyAlerts);
}

dojo.addOnLoad(init);
