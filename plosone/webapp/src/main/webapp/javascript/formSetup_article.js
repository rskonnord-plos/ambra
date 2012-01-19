var _commentTitle;
var _comments;
var _ratingTitle;
var _ratingComments;
var _titleCue    			 = 'Enter your comment title...';
var _commentCue    		 = 'Enter your comment...';
var _ratingTitleCue		 = 'Enter your comment title...';
var _ratingCommentCue   = 'Enter your comment...';

function initAnnotationForm() {	
	_commentTitle    = _annotationForm.cTitle;
	_comments        = _annotationForm.cArea;
	var privateFlag  = _annotationForm.privateFlag;
	var publicFlag   = _annotationForm.publicFlag;
	var btnSave      = dojo.byId("btn_save");
	var btnPost      = dojo.byId("btn_post");
	var btnCancel    = dojo.byId("btn_cancel");
	var submitMsg    = dojo.byId('submitMsg');
	
	// Annotation Dialog Box: Title field
	dojo.event.connect(_commentTitle, "onfocus", function () { 
	  topaz.formUtil.textCues.off(_commentTitle, _titleCue); 
	});
	
	dojo.event.connect(_commentTitle, "onchange", function () {
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	});

	dojo.event.connect(_commentTitle, "onblur", function () { 
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_commentTitle, _titleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.event.connect(_comments, "onfocus", function () {
	  topaz.formUtil.textCues.off(_comments, _commentCue);
	});

	dojo.event.connect(_comments, "onchange", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	});
	
	dojo.event.connect(_comments, "onblur", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_comments, _commentCue); 
	  //topaz.formUtil.checkFieldStrLength(_comments);
	});
	
	// Annotation Dialog Box: Private/Public radio buttons
	dojo.event.connect(privateFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
	  _dlg.placeModalDialog();
  	//var btn = btnSave;
  	//__dlg.setCloseControl(btn);
  });
  
	dojo.event.connect(publicFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
	  _dlg.placeModalDialog();
  	//var btn = btnPost;
  	//_dlg.setCloseControl(btn);
	});
	
	// Annotation Dialog Box: Save button
	dojo.event.connect(btnSave, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  
	// Annotation Dialog Box: Post buttons
	dojo.event.connect(btnPost, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });

	dojo.event.connect(btnCancel, "onclick", function(e) {
    dojo.dom.removeChildren(submitMsg);
    _dlg.hide();
    topaz.formUtil.enableFormFields(_annotationForm);
	  getArticle();
    topaz.displayComment.processBugCount();
    e.preventDefault();
  });

	/******************************************************
	 * Ratings Initial Settings
	 ******************************************************/
	_ratingTitle               = _ratingsForm.cTitle;
	_ratingComments            = _ratingsForm.cArea;
	var btnPostRating        = dojo.byId("btn_post_rating");
	var btnCancelRating      = dojo.byId("btn_cancel_rating");
	var submitRatingMsg        = dojo.byId('submitRatingMsg');
	
	// Annotation Dialog Box: Title field
	dojo.event.connect(_ratingTitle, "onfocus", function () { 
	  topaz.formUtil.textCues.off(_ratingTitle, _ratingTitleCue); 
	});
	
	dojo.event.connect(_ratingTitle, "onchange", function () {
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	});

	dojo.event.connect(_ratingTitle, "onblur", function () { 
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_ratingTitle, _ratingTitleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.event.connect(_ratingComments, "onfocus", function () {
	  topaz.formUtil.textCues.off(_ratingComments, _ratingCommentCue);
	});

	dojo.event.connect(_ratingComments, "onchange", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	});
	
	dojo.event.connect(_ratingComments, "onblur", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(_ratingComments, _ratingCommentCue); 
	  //topaz.formUtil.checkFieldStrLength(_ratingComments);
	});
	
	// Rating Dialog Box: Post buttons
	dojo.event.connect(btnPostRating, "onclick", function(e) {
    updateRating();
    topaz.rating.resetDialog();
    e.preventDefault();
  });

	dojo.event.connect(btnCancelRating, "onclick", function(e) {
    dojo.dom.removeChildren(submitMsg);
    _ratingDlg.hide();
    topaz.formUtil.enableFormFields(_ratingsForm);
    topaz.rating.resetDialog();
	  getArticle("rating");
    topaz.displayComment.processBugCount();
    e.preventDefault();
  });

}

function validateNewComment() {
  var submitMsg = dojo.byId('submitMsg');
  dojo.dom.removeChildren(submitMsg);
  
  topaz.formUtil.disableFormFields(_annotationForm);
  
  _ldc.show();

  //if (str < 0) {
     var bindArgs = {
      url: _namespace + "/annotation/secure/createAnnotationSubmit.action",
      method: "post",
      error: function(type, data, evt){
       alert("An error occurred." + data.toSource());
       var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
       //topaz.errorConsole.writeToConsole(err);
       //topaz.errorConsole.show();
       topaz.formUtil.enableFormFields(_annotationForm);
       _ldc.hide();
       
       return false;
      },
      load: function(type, data, evt){
       var jsonObj = dojo.json.evalJson(data);
       
       //alert("jsonObj:\n" + jsonObj.toSource());
       //submitMsg.appendChild(document.createTextNode(jsonObj.toSource()));
       
       if (jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
         }
         
         //alert("ERROR: " + errorMsg);
         var err = document.createTextNode(errorMsg);
         submitMsg.appendChild(err);
         topaz.formUtil.enableFormFields(_annotationForm);
         _dlg.placeModalDialog();
         _ldc.hide();
         
         return false;
       }
       else if (jsonObj.numFieldErrors > 0) {
         var fieldErrors = document.createDocumentFragment();
         
         for (var item in jsonObj.fieldErrors) {
           var errorString = "";
           for (var ilist in jsonObj.fieldErrors[item]) {
             var err = jsonObj.fieldErrors[item][ilist];
             if (err) {
               errorString += err;
               var error = document.createTextNode(errorString.trim());
               var brTag = document.createElement('br');
  
               fieldErrors.appendChild(error);
               fieldErrors.appendChild(brTag);
             }
           }
         }
         
         //alert("ERROR: " + fieldErrors);
         //var err = document.createTextNode("ERROR [Field]:");
         //submitMsg.appendChild(err);
         submitMsg.appendChild(fieldErrors);
         topaz.formUtil.enableFormFields(_annotationForm);
         _dlg.placeModalDialog();
         _ldc.hide();
  
         return false;
       }
       else {
         if (djConfig.isDebug) {
           dojo.byId(djConfig.debugContainerId).innerHTML = "";
         }
         getArticle();
         _dlg.hide();

         topaz.formUtil.textCues.reset(_commentTitle, _titleCue);
         topaz.formUtil.textCues.reset(_comments, _commentCue);
          
         topaz.formUtil.enableFormFields(_annotationForm);
         return false;
       }
       
      },
      mimetype: "text/plain",
      formNode: _annotationForm,
      transport: "XMLHTTPTransport"
     };
     dojo.io.bind(bindArgs);
/*  }
  else {
    return false;
  }*/
}  

function getArticle(refreshType) {
  var refreshArea = dojo.byId(annotationConfig.articleContainer);
  var targetUri = _annotationForm.target.value;

  _ldc.show();
  
  var bindArgs = {
    url: _namespace + "/article/fetchBody.action?articleURI=" + targetUri,
    method: "get",
    error: function(type, error, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + error.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + error.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;
       if (djConfig.isDebug) {
         dojo.byId(djConfig.debugContainerId).innerHTML = 
             "type = " + type + "\n" +
             "evt = " + evt + "\n" +
             docFragment;
       }
       //alert(data);

      refreshArea.innerHTML = docFragment;
      //dojo.dom.removeChildren(refreshArea);
      //refreshArea.appendChild(docFragment);
      
      if (refreshType  == "rating") {
        refreshRating(targetUri);
     	}
     	else {
	      getAnnotationCount();
     	}
     	
      topaz.displayComment.processBugCount();
      
      _ldc.hide();

      return false;
    },
    mimetype: "text/html",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
  
}

function getAnnotationCount() {
  var refreshArea1 = dojo.byId(annotationConfig.rhcCount + "1");
  var refreshArea2 = dojo.byId(annotationConfig.rhcCount + "2");
  var targetUri = _annotationForm.target.value;

  var bindArgs = {
    url: _namespace + "/article/fetchArticleRhc.action?articleURI=" + targetUri,
    method: "get",
    error: function(type, error, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + error.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + error.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      refreshArea1.innerHTML = docFragment;
      refreshArea2.innerHTML = docFragment;

      return false;
    },
    mimetype: "text/html",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
  
}