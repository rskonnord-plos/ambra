var _ldc;
var _alertForm;

function init(e) {
  _ldc = dojo.widget.byId("LoadingCycle");
  
  _alertForm = document.userAlerts;

  _alertForm.action = _namespace + "/user/secure/saveAlerts.action";
  dojo.event.connect(_alertForm.formSubmit, "onclick", function() {
      _alertForm.submit(); 
      return true;
    }
  );
  
  topaz.formUtil.selectCheckboxPerCollection(_alertForm.checkAllWeekly, _alertForm.weeklyAlerts);
  topaz.formUtil.selectCheckboxPerCollection(_alertForm.checkAllMonthly, _alertForm.monthlyAlerts);
}

dojo.addOnLoad(init);
