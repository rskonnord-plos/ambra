  var _dcr = new Object();
  var _ldc;
  
  function init(e) {
    _ldc = dojo.widget.byId("LoadingCycle");

    _dcr.widget = dojo.byId("DiscussionPanel");
    _dcr.btnCancel = dojo.byId("btnCancelResponse");
    _dcr.btnSubmit = dojo.byId("btnPostResponse");
    _dcr.form = document.discussionResponse;
    _dcr.formAction = "/annotation/secure/createDiscussionSubmit.action";
    _dcr.responseTitleCue = "Enter your comment title...";
    _dcr.responseCue = "Enter your comment...";
    _dcr.error = dojo.byId('responseSubmitMsg');
    _dcr.requestType = "new";
    _dcr.baseId = _dcr.form.target.value;
    _dcr.replyId = _dcr.form.target.value;
    var responseTitle = _dcr.form.responseTitle;
    var responseArea = _dcr.form.responseArea;
    
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
      }
    );    
       
    dojo.event.connect(responseArea, "onchange", function(e) {
        var fldResponse = _dcr.form.comment;
        if(responseArea.value != "" && responseArea.value != _dcr.responseCue) {
          fldResponse.value = responseArea.value;
        }
        else {
          fldResponse.value = responseArea.value;
        }
      }
    );    
    
  }
  
  dojo.addOnLoad(init);
