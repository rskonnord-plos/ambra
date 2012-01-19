/**
 * floatMenu()
 * 
 * The function is activated when the page is scrolled or the window is resized.
 * "postcomment" is the outer container of the sections that floats.  "floatMarker"
 * is the point that indicates the topmost point that the floated menu should stop
 * floating.  This doesn't work so well in Safari.  The best way to do in Safari is 
 * to have 2 of these items, the other one being postcommentfloat.  This second one
 * is on the page is hidden unless you're in safari.
 * 
 * @author		Joycelyn Chung		joycelyn@orangetowers.com
 **/

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




  