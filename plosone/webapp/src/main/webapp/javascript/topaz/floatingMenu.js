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
  dojo.html.removeClass(el, 'fixed');
  
  if (scrollY > mOffset.top) {
    y = scrollY - mpOffset.top;
    dojo.html.addClass(el, 'fixed');
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




  