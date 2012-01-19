  var _annotationForm;
  
  function init(e) {

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
    // topaz.navigation.buildTOC(tocObj);
    
    if (dojo.render.html.safari) {
      var tocObj = dojo.byId('sectionNavTopFloat');
      // topaz.navigation.buildTOC(tocObj);
    }
        
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

    //errView = dojo.widget.byId("ErrorConsole");
    //var errClose = dojo.byId("btn_ok");
    //errView.setCloseControl(errClose);
    
  }
  
  dojo.addOnLoad(init);
