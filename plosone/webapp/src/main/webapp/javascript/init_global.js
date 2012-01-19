var container;
var topBanner;

function globalInit() {
  if (dojo.render.html.ie) {
    container = dojo.byId("container");
    topBanner = dojo.byId("topBanner");
    
    if (container) {
      topaz.domUtil.setContainerWidth(container, 675, 940);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(container, 675, 940)", 100);
        }
      );
    }
    
    if (topBanner) {
      topaz.domUtil.setContainerWidth(topBanner, 942, 944);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(topBanner, 942, 944)", 100);
        }
      );
    }
  }
}

dojo.addOnLoad(globalInit);
