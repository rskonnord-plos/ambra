  var _dcr = new Object();
  var _dcf = new Object();
  var _ldc;
  
  function init(e) {
    _ldc = dojo.widget.byId("LoadingCycle");

    _dcr.widget = dojo.byId("DiscussionPanel");
    _dcr.widget.style.display = "none";
    _dcr.btnCancel = dojo.byId("btnCancelResponse");
    _dcr.btnSubmit = dojo.byId("btnPostResponse");
    _dcr.form = document.discussionResponse;
    _dcr.formAction = "/annotation/secure/createReplySubmit.action";
    _dcr.responseTitleCue = "Enter your response title...";
    _dcr.responseCue = "Enter your response...";
    _dcr.error = dojo.byId('responseSubmitMsg');
    _dcr.requestType = "response";
    var responseTitle = _dcr.form.responseTitle;
    var responseArea = _dcr.form.responseArea;
    
    dojo.event.connect(_dcr.btnCancel, "onclick", function(e) {
        topaz.responsePanel.hide(_dcr.widget);
        var submitMsg = _dcr.error;
        dojo.dom.removeChildren(submitMsg);
      }
    );    

    dojo.event.connect(_dcr.btnSubmit, "onclick", function(e) {
        topaz.responsePanel.submit(_dcr);
      }
    );    
       
    dojo.event.connect(responseTitle, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseTitle, _dcr.responseTitleCue);
      }
    );    
       
    dojo.event.connect(responseArea, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseArea, _dcr.responseCue);
      }
    );    
       
    dojo.event.connect(responseTitle, "onblur", function(e) {
        var fldResponseTitle = _dcr.form.commentTitle;
        if(responseTitle.value != "" && responseTitle.value != _dcr.responseCue) {
          fldResponseTitle.value = responseTitle.value;
        }
        else {
          fldResponseTitle.value = "";
        }

        topaz.formUtil.textCues.on(responseTitle, _dcr.responseTitleCue);
      }
    );    

    dojo.event.connect(responseArea, "onblur", function(e) {
        var fldResponse = _dcr.form.comment;
        if(responseArea.value != "" && responseArea.value != _dcr.responseCue) {
          fldResponse.value = responseArea.value;
        }
        else {
          fldResponse.value = "";
        }
        topaz.formUtil.textCues.on(responseArea, _dcr.responseCue);
      }
    );    
       
    dojo.event.connect(responseTitle, "onchange", function(e) {
        var fldResponseTitle = _dcr.form.commentTitle;
        if(responseTitle.value != "" && responseTitle.value != _dcr.responseCue) {
          fldResponseTitle.value = responseTitle.value;
        }
        else {
          fldResponseTitle.value = "";
        }

        topaz.formUtil.textCues.on(responseTitle, _dcr.responseTitleCue);
      }
    );    
       
    dojo.event.connect(responseArea, "onchange", function(e) {
        var fldResponse = _dcr.form.comment;
        if(responseArea.value != "" && responseArea.value != _dcr.responseCue) {
          fldResponse.value = responseArea.value;
        }
        else {
          fldResponse.value = "";
        }
        topaz.formUtil.textCues.on(responseArea, _dcr.responseCue);
      }
    );    

    _dcf.widget = dojo.byId("FlaggingPanel");
    _dcf.widget.style.display = "none";
    _dcf.btnCancel = dojo.byId("btnCloseFlag");
    _dcf.btnSubmit = dojo.byId("btnSubmit");
    _dcf.btnFlagClose = dojo.byId("btnFlagConfirmClose");
    _dcf.form = document.discussionFlag;
    _dcf.formAction = new Array("/annotation/secure/createAnnotationFlagSubmit.action", "/annotation/secure/createReplyFlagSubmit.action");
    _dcf.responseCue = "Add any additional information here...";
    _dcf.error = dojo.byId('flagSubmitMsg');
    _dcf.requestType = "flag";
    var responseAreaFlag = _dcf.form.responseArea;
    
    dojo.event.connect(_dcf.btnCancel, "onclick", function(e) {
        topaz.responsePanel.hide();
      }
    );    

    dojo.event.connect(_dcf.btnFlagClose, "onclick", function(e) {
        topaz.responsePanel.hide();
        topaz.responsePanel.resetFlaggingForm(_dcf);
      }
    );    

    dojo.event.connect(_dcf.btnSubmit, "onclick", function(e) {
        topaz.responsePanel.submit(_dcf);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onfocus", function(e) {
        topaz.formUtil.textCues.off(responseAreaFlag, _dcf.responseCue);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onblur", function(e) {
        var fldResponse = _dcf.form.comment;
        if(responseAreaFlag.value != "" && responseAreaFlag.value != _dcf.responseCue) {
          fldResponse.value = responseAreaFlag.value;
        }
        else {
          fldResponse.value = "";          
        }
        topaz.formUtil.textCues.on(responseAreaFlag, _dcf.responseCue);
      }
    );    
       
    dojo.event.connect(responseAreaFlag, "onchange", function(e) {
        var fldResponse = _dcf.form.comment;
        if(responseAreaFlag.value != "" && responseAreaFlag.value != _dcf.responseCue) {
          fldResponse.value = responseAreaFlag.value;
        }
        else {
          fldResponse.value = "";
        }
      }
    );    
    
  }
  
  dojo.addOnLoad(init);
