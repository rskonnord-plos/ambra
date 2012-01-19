  var dlg;
  var popup;
  var popupm;
  var ldc;
  var annotationForm;
  var menu;
  
  function init(e) {
    ldc = dojo.widget.byId("LoadingCycle");
    ldc.show();
    
    if (loggedIn) {    
      var triggerNode = dojo.byId(annotationConfig.trigger);
    	dojo.event.connect(triggerNode, 'onmousedown', function(e) {
  	     topaz.annotation.createAnnotationOnMouseDown();
  	     e.preventDefault();
  	   }
    	);
    }
    
    var tocObj = dojo.byId('sectionNavTop');
    topaz.navigation.buildTOC(tocObj);
    
 		annotationForm = document.createAnnotation;
    initAnnotationForm();
    
    topaz.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
    
  	dlg = dojo.widget.byId("AnnotationDialog");
  	var dlgCancel = dojo.byId('btn_cancel');
  	dlg.setCloseControl(dlgCancel);
  	dlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  	dlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  	popup = dojo.widget.byId("CommentDialog");
  	var popupClose = dojo.byId('btn_close');
    popup.setCloseControl(popupClose);
  	popup.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  	popup.setTipUp(dojo.byId(commentConfig.tipUpDiv));
    dojo.event.connect(popupClose, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupClose, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
  	
  	popupm = dojo.widget.byId("CommentDialogMultiple");
  	var popupCloseMulti = dojo.byId('btn_close_multi');
    popupm.setCloseControl(popupCloseMulti);
  	popupm.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  	popupm.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
    dojo.event.connect(popupCloseMulti, 'onclick', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    dojo.event.connect(popupCloseMulti, 'onblur', function(e) {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
      }
    );
    
    dojo.event.connect(window, "onload", function() {
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
    
    var anId = document.articleInfo.annotationId.value;
    var anEl = getAnnotationEl(anId);
    jumpToAnnotation(anId);

    //errView = dojo.widget.byId("ErrorConsole");
    //var errClose = dojo.byId("btn_ok");
    //errView.setCloseControl(errClose);
    
    ldc.hide();
    
  }
  
  dojo.addOnLoad(init);
