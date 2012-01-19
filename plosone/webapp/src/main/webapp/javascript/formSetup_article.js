var commentTitle;
var comments;
var titleCue     = 'Enter your annotation title...';
var commentCue   = 'Enter your annotation...';
var btn_save;
var btn_post;
var btn_cancel;

function initAnnotationForm() {	
	commentTitle     = annotationForm.cTitle;
	comments         = annotationForm.cArea;
	var privateFlag  = annotationForm.privateFlag;
	var publicFlag   = annotationForm.publicFlag;
	btn_save         = document.getElementById("btn_save");
	btn_post         = document.getElementById("btn_post");
	btn_cancel       = document.getElementById("btn_cancel");
	var submitMsg    = dojo.byId('submitMsg');
	
	// Annotation Dialog Box: Title field
	dojo.event.connect(commentTitle, "onfocus", function () { 
	  topaz.formUtil.textCues.off(commentTitle, titleCue); 
	});
	
	dojo.event.connect(commentTitle, "onchange", function () {
    var fldTitle = annotationForm.commentTitle;
    if(annotationForm.cTitle.value != "" && annotationForm.cTitle.value != titleCue) {
      fldTitle.value = annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	});

	dojo.event.connect(commentTitle, "onblur", function () { 
    var fldTitle = annotationForm.commentTitle;
    if(annotationForm.cTitle.value != "" && annotationForm.cTitle.value != titleCue) {
      fldTitle.value = annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(commentTitle, titleCue); 
	});
	
	// Annotation Dialog Box: Comment field
	dojo.event.connect(comments, "onfocus", function () {
	  topaz.formUtil.textCues.off(comments, commentCue);
	});

	dojo.event.connect(comments, "onchange", function () {
    var fldTitle = annotationForm.comment;
    if(annotationForm.cArea.value != "" && annotationForm.cArea.value != commentCue) {
      fldTitle.value = annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	});
	
	dojo.event.connect(comments, "onblur", function () {
    var fldTitle = annotationForm.comment;
    if(annotationForm.cArea.value != "" && annotationForm.cArea.value != commentCue) {
      fldTitle.value = annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
	  topaz.formUtil.textCues.on(comments, commentCue); 
	  //topaz.formUtil.checkFieldStrLength(comments);
	});
	
	// Annotation Dialog Box: Private/Public radio buttons
	dojo.event.connect(privateFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
	  dlg.placeModalDialog();
  	//var btn = btn_save;
  	//dlg.setCloseControl(btn);
  });
  
	dojo.event.connect(publicFlag, "onclick", function() {
	  topaz.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
	  dlg.placeModalDialog();
  	//var btn = btn_post;
  	//dlg.setCloseControl(btn);
	});
	
	// Annotation Dialog Box: Save button
	dojo.event.connect(btn_save, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  
	// Annotation Dialog Box: Post buttons
	dojo.event.connect(btn_post, "onclick", function(e) {
	  //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });

	dojo.event.connect(btn_cancel, "onclick", function(e) {
    dojo.dom.removeChildren(submitMsg);
    dlg.hide();
    topaz.formUtil.enableFormFields(annotationForm);
	  getArticle();
    topaz.displayComment.processBugCount();
    e.preventDefault();
  });

}

function validateNewComment() {
  //topaz.formUtil.textCues.off(commentTitle, titleCue);
  //topaz.formUtil.textCues.off(comments, commentCue);
  //var str = topaz.formUtil.checkFieldStrLength(comments);
  var submitMsg = dojo.byId('submitMsg');
  dojo.dom.removeChildren(submitMsg);
  
  topaz.formUtil.disableFormFields(annotationForm);
  //topaz.domUtil.addNewClass('post', ' disable');
  
  ldc.show();

  //if (str < 0) {
     var bindArgs = {
      url: namespace + "/annotation/secure/createAnnotationSubmit.action",
      method: "post",
      error: function(type, data, evt){
       alert("An error occurred." + data.toSource());
       var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
       //topaz.errorConsole.writeToConsole(err);
       //topaz.errorConsole.show();
       topaz.formUtil.enableFormFields(annotationForm);
       //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
       ldc.hide();
       
       return false;
      },
      load: function(type, data, evt){
       var jsonObj = dojo.json.evalJson(data);
       
       //alert("jsonObj:\n" + jsonObj.toSource());
       //submitMsg.appendChild(document.createTextNode(jsonObj.toSource()));
       
       if (jsonObj.actionErrors.list.length > 0) {
         var errorMsg;
         
         for (var i=0; i<jsonObj.actionErrors.list.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors.list[i] + "\n";
         }
         
         //alert("ERROR: " + errorMsg);
         var err = document.createTextNode(errorMsg);
         submitMsg.appendChild(err);
         topaz.formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
         dlg.placeModalDialog();
         ldc.hide();
         
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
         topaz.formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
         dlg.placeModalDialog();
         ldc.hide();
  
         return false;
       }
       else {
         if (djConfig.isDebug) {
           dojo.byId(djConfig.debugContainerId).innerHTML = "";
         }
         getArticle();
         dlg.hide();

         topaz.formUtil.textCues.reset(commentTitle, titleCue);
         topaz.formUtil.textCues.reset(comments, commentCue);
          
         topaz.formUtil.enableFormFields(annotationForm);
         //topaz.domUtil.removeNewClass('post', '\sdisable', 'div');
         return false;
       }
       
      },
      mimetype: "text/plain",
      formNode: annotationForm,
      transport: "XMLHTTPTransport"
     };
     dojo.io.bind(bindArgs);
/*  }
  else {
    return false;
  }*/
}  

function getArticle() {
  var refreshArea = dojo.byId(annotationConfig.articleContainer);
  var targetUri = annotationForm.target.value;

  ldc.show();
  
  var bindArgs = {
    url: namespace + "/article/fetchBody.action?articleURI=" + targetUri,
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
      getAnnotationCount();
      topaz.displayComment.processBugCount();
      
      ldc.hide();

      return false;
    },
    mimetype: "text/html",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
  
}

function getAnnotationCount() {
  var refreshArea = dojo.byId(annotationConfig.rhcCount);
  var targetUri = annotationForm.target.value;

  var bindArgs = {
    url: namespace + "/article/fetchArticleRhc.action?articleURI=" + targetUri,
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
      //alert(data);

      refreshArea.innerHTML = docFragment;
      //dojo.dom.removeChildren(refreshArea);
      //refreshArea.appendChild(docFragment);

      return false;
    },
    mimetype: "text/html",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
  
}










