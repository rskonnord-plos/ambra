  var _dlg;
  var _commentDlg;
  var _commentMultiDlg;
  var _ldc;
  var _annotationForm;
  var _ratingDlg;
  var _ratingsForm;
  
  function init(e) {
    _ldc = dojo.widget.byId("LoadingCycle");
    _ldc.show();
    
/*    if (loggedIn) {    
      var triggerNode = dojo.byId(annotationConfig.trigger);
    	dojo.event.connect(triggerNode, 'onmousedown', function(e) {
  	     topaz.annotation.createAnnotationOnMouseDown();
  	     e.preventDefault();
  	   }
    	);
    }
*/    
    var tocObj = dojo.byId('sectionNavTop');
    topaz.navigation.buildTOC(tocObj);
    
    if (dojo.render.html.safari) {
      var tocObj = dojo.byId('sectionNavTopFloat');
      topaz.navigation.buildTOC(tocObj);
    }
        
 		_annotationForm = document.createAnnotation;

  	_ratingDlg = dojo.widget.manager.getWidgetById("Rating");
  	var ratingCancel = dojo.byId('btn_cancel_rating');
  	_ratingDlg.setCloseControl(ratingCancel);
  	_ratingsForm = document.ratingForm;

    initAnnotationForm();
    
    topaz.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
    
  	_dlg = dojo.widget.manager.getWidgetById("AnnotationDialog");
  	var dlgCancel = dojo.byId('btn_cancel');
  	_dlg.setCloseControl(dlgCancel);
  	_dlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  	_dlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  	_commentDlg = dojo.widget.manager.getWidgetById("CommentDialog");
  	var commentDlgClose = dojo.byId('btn_close');
    _commentDlg.setCloseControl(commentDlgClose);
  	_commentDlg.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  	_commentDlg.setTipUp(dojo.byId(commentConfig.tipUpDiv));
    dojo.event.connect(commentDlgClose, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(commentDlgClose, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
  	
  	_commentMultiDlg = dojo.widget.manager.getWidgetById("CommentDialogMultiple");
  	var popupCloseMulti = dojo.byId('btn_close_multi');
    _commentMultiDlg.setCloseControl(popupCloseMulti);
  	_commentMultiDlg.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  	_commentMultiDlg.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
    dojo.event.connect(popupCloseMulti, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupCloseMulti, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    
    dojo.event.connect(window, "onload", function() {
        if (dojo.render.html.safari) {
    //      var origDiv = dojo.byId("postcomment");
    //      var newDiv = origDiv.cloneNode(true);
    //      newDiv.id = "postcommentfloat";
    //      dojo.dom.insertAfter(newDiv, origDiv, true);
          var newDiv = dojo.byId("postcommentfloat");
          newDiv.style.display = "none";
        }
        floatMenu();
      }  
    );
    // Hack for browser compatibility on the use of scrolling in the viewport.
    //
    //if (BrowserDetect.browser == "Explorer") {
      dojo.event.connect(window, "onscroll", function() {
          floatMenu();
        }  
      );
    //}
    //else if (BrowserDetect.browser == "Firefox" && BrowserDetect.version < 2) {
      dojo.event.connect(document.documentElement, "onscroll", function() {
          floatMenu();
        }  
      );
    //}
    //else {
      dojo.event.connect(document.documentElement, "onkey", function() {
          floatMenu();
        }  
      );
    //}
    dojo.event.connect(window, "onresize", function() {
        floatMenu();
      }  
    );

    topaz.displayComment.init();
    topaz.displayComment.processBugCount();
    //topaz.rating.init();
    
    var anId = document.articleInfo.annotationId.value;
    var anEl = getAnnotationEl(anId);
    jumpToAnnotation(anId);

    //errView = dojo.widget.byId("ErrorConsole");
    //var errClose = dojo.byId("btn_ok");
    //errView.setCloseControl(errClose);
    
    _ldc.hide();
    
  }
  
  dojo.addOnLoad(init);
