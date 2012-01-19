/**
  * topaz.errorConsole
  *
  * @param
  *
  **/
topaz.errorConsole = new Object();

topaz.errorConsole = {
  show: function() {
    errView.show();
  },
  
  writeToConsole: function(errMsg) {
    alert("inside writeToConsole = " + errMsg);
    var msgArea = dojo.byId('errorMsg');
    
    msgArea.innerHTML = errMsg;
  }
  
}