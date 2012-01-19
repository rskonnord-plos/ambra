var _containerDiv;
var _topBannerDiv;

function globalInit() {
  if (dojo.render.html.ie) {
    _containerDiv = dojo.byId("container");
    _topBannerDiv = dojo.byId("topBanner");
    
    if (_containerDiv) {
      topaz.domUtil.setContainerWidth(_containerDiv, 675, 940);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(_containerDiv, 675, 940)", 100);
        }
      );
    }
    
    if (_topBannerDiv) {
      topaz.domUtil.setContainerWidth(_topBannerDiv, 942, 944);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(_topBannerDiv, 942, 944)", 100);
        }
      );
    }
  }
}

dojo.addOnLoad(globalInit);
