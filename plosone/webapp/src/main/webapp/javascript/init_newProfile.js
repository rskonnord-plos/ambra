var ldc;
var formObj;
function init(e) {
  ldc = dojo.widget.byId("LoadingCycle");
  
  formObj = document.userForm;
//  alert ("setting form action to createNewUser.action");
  formObj.action = namespace + "/user/createNewUser.action";
  dojo.event.connect(formObj.formSubmit, "onclick", function() {
      formObj.submit(); 
      return true;
    }
  );
  
}

dojo.addOnLoad(init);
