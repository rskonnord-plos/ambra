var el;
var changeHeight;

function floatMenu() {
  el = dojo.byId('postcomment');
  var marker = dojo.byId('floatMarker');
  var markerParent = marker.parentNode;
  var mOffset = topaz.domUtil.getCurrentOffset(marker);
  var mpOffset = topaz.domUtil.getCurrentOffset(markerParent);
  var scrollOffset = dojo.html.getScroll().offset;
  var vpOffset = dojo.html.getViewport();

  var scrollY = scrollOffset.y;
  
  var y = 0;
  if (dojo.render.html.safari) {
    var floatDiv = dojo.byId("postcommentfloat");
    if (el.style.display == "none") {
      floatDiv.style.display = "none";
      el.style.display = "block";
    }
  }
  else {
    dojo.html.removeClass(el, 'fixed');
  }
  
  if (scrollY > mOffset.top) {
    y = scrollY - mpOffset.top;
    if (dojo.render.html.safari) {
      var floatDiv = dojo.byId("postcommentfloat");
      if (floatDiv.style.display = "none") {
        floatDiv.style.display = "block";
        el.style.display = "none";
      }
    }
    else {
      dojo.html.addClass(el, 'fixed');
    }
  }
  
  if (BrowserDetect.browser == "Explorer" && BrowserDetect.version < 7 && ((document.body.offsetHeight-scrollY) >= vpOffset.height)) {
    //el.style.top = y + "px";
    changeHeight = y;
    window.setTimeout("changeTopPosition()", 100); 
  }  
}

function changeTopPosition() {
  el.style.top = changeHeight + "px";
}




  