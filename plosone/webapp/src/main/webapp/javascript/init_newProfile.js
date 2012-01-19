var _ldc;
var _profileForm;

function init(e) {
  _ldc = dojo.widget.byId("LoadingCycle");
  
  _profileForm = document.userForm;
//  alert ("setting form action to createNewUser.action");
  _profileForm.action = _namespace + "/user/createNewUser.action";
  dojo.event.connect(_profileForm.formSubmit, "onclick", function() {
      _profileForm.submit(); 
      return true;
    }
  );
  
}

dojo.addOnLoad(init);
