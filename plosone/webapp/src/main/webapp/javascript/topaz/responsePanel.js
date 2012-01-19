var togglePanel = new Object();

topaz.responsePanel = new Object();

topaz.responsePanel = {
  upperContainer: "",
  
  newPanel: "",
  
  targetForm: "",
  
  previousNode: "",
  
  setPanel: function(panel) {
    this.newPanel = panel;
  },
  
  setForm: function(formObj) {
    this.targetForm = formObj;
  },
  
  show: function(curNode, targetObj, targetElClassName, baseId, replyId, threadTitle, actionIndex) {
    this.setPanel(targetObj.widget);
    this.setForm(targetObj.form);
    targetObj.baseId = (baseId) ? baseId : "";
    targetObj.replyId = (replyId)? replyId : "";
    targetObj.actionIndex = (actionIndex) ? actionIndex : 0;
    this.upperContainer = topaz.domUtil.getFirstAncestorByClass(curNode, targetElClassName);
    this.upperContainer.style.display = "none";
    togglePanel.newPanel = this.newPanel;
    togglePanel.upperContainer = this.upperContainer;
    
    dojo.dom.insertAfter(this.newPanel, this.upperContainer, false);

    if (this.previousUpperContainer) this.previousUpperContainer.style.display = "block";

    if (targetObj.requestType == "flag"){
      this.resetFlaggingForm(targetObj);
    }
    
    this.newPanel.style.display = "block";

    if (threadTitle) {
      this.targetForm.responseTitle.value = 'RE: ' + threadTitle;
      this.targetForm.commentTitle.value = 'RE: ' + threadTitle;
    }
        
    this.previousUpperContainer = this.upperContainer;
  },
  
  hide: function() {
    if (togglePanel.newPanel) togglePanel.newPanel.style.display = "none";
    if (togglePanel.upperContainer) togglePanel.upperContainer.style.display = "block";
  },
  
  submit: function(targetObj) {
    submitResponseInfo(targetObj);
  },
  
  resetFlaggingForm: function(targetObj) {
    this.getFlagForm();  
    this.targetForm.reasonCode[0].checked = true;
    this.targetForm.comment.value = "";
    this.targetForm.responseArea.value = targetObj.responseCue;
    var submitMsg = targetObj.error;
    dojo.dom.removeChildren(submitMsg);
  },
  
  getFlagConfirm: function() {
    dojo.byId('flagForm').style.display = "none";
    dojo.byId('flagConfirm').style.display = "block";  
  },
  
  getFlagForm: function() {
    dojo.byId('flagForm').style.display = "block";
    dojo.byId('flagConfirm').style.display = "none";  
  }
}  

function submitResponseInfo(targetObj) {
  var submitMsg = targetObj.error;
  var targetForm = targetObj.form;
  dojo.dom.removeChildren(submitMsg);
  //topaz.formUtil.disableFormFields(targetForm);

  var urlParam = "";
  if (targetObj.requestType == "flag"){
    urlParam = targetObj.formAction[targetObj.actionIndex] + "?target=" + targetObj.baseId;
  }
  else if (targetObj.requestType == "new"){
    urlParam = targetObj.formAction;
    topaz.formUtil.disableFormFields(targetForm);
  }
  else { 
    urlParam = targetObj.formAction + "?root=" + targetObj.baseId + "&inReplyTo=" + targetObj.replyId;
    topaz.formUtil.disableFormFields(targetForm);
  }
   
  _ldc.show();

   var bindArgs = {
    url: _namespace + urlParam,
    method: "post",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     topaz.formUtil.enableFormFields(targetForm);
     _ldc.hide();
     
     return false;
    },
    load: function(type, data, evt){
     var jsonObj = dojo.json.evalJson(data);
     
     //alert("jsonObj:\n" + jsonObj.toSource());
     //submitMsg.appendChild(document.createTextNode(jsonObj.toSource()));
     
     if (jsonObj.actionErrors.list.length > 0) {
       var errorMsg = "";
       
       for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
         errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
       }
       
       //alert("ERROR: " + errorMsg);
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       topaz.formUtil.enableFormFields(targetForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
       _ldc.hide();
  
       return false;
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();

       for (var item in jsonObj.fieldErrors.map) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors.map[item]) {
           for (var i=0; i<jsonObj.numFieldErrors; i++) {
             var err = jsonObj.fieldErrors.map[item][ilist][i];
             if (err) {
               errorString += err;
               var error = document.createTextNode(errorString.trim());
               var brTag = document.createElement('br');
               
               fieldErrors.appendChild(error);
               fieldErrors.appendChild(brTag);
             }
           }
         }
       }
       
       //alert("ERROR: " + fieldErrors);
       //var err = document.createTextNode("ERROR [Field]:");
       //submitMsg.appendChild(err);
       submitMsg.appendChild(fieldErrors);
       topaz.formUtil.enableFormFields(targetForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');

       _ldc.hide();
  
       return false;
     }
     else {
       if (targetObj.requestType == "flag"){
         _ldc.hide();
         topaz.responsePanel.getFlagConfirm();
       }
       else if (targetObj.requestType == "new"){
         var rootId = jsonObj.annotationId;
         window.location.href = _namespace + "/annotation/listThread.action?inReplyTo=" + rootId +"&root=" + rootId;
       }
       else {
         if (dojo.render.html.ie)
           dojo.dom.insertAfter(togglePanel.newPanel, document.lastChild, false);
         getDiscussion(targetObj);
         topaz.responsePanel.hide();
         topaz.formUtil.textCues.reset(targetForm.responseArea, targetObj.responseCue);
         topaz.formUtil.enableFormFields(targetForm);
       }
       return false;
     }
     
    },
    mimetype: "text/plain",
    formNode: targetForm
   };
   dojo.io.bind(bindArgs);
}

function getDiscussion(targetObj) {
  var refreshArea = dojo.byId(responseConfig.discussionContainer);

  _ldc.show();
  
  var bindArgs = {
    url: _namespace + "/annotation/listThreadRefresh.action?root=" + targetObj.baseId + "&inReplyTo=" + targetObj.baseId,
    method: "get",
    error: function(type, data, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + data.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      refreshArea.innerHTML = docFragment;
      
      _ldc.hide();

      return false;
    },
    mimetype: "text/html"
   };
   dojo.io.bind(bindArgs);
  
}

