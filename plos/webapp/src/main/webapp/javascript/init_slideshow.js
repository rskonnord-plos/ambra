  var figureWindow;
  var image;
  var imageWidth = 0;

  function init(e) {
    topaz.slideshow.setLinkView(dojo.byId("viewL"));
    topaz.slideshow.setLinkTiff(dojo.byId("downloadTiff"));
    topaz.slideshow.setLinkPpt(dojo.byId("downloadPpt"));
    topaz.slideshow.setFigImg(dojo.byId("figureImg"));
    topaz.slideshow.setFigTitle(dojo.byId("figureTitle"));
    topaz.slideshow.setFigCaption(dojo.byId("figure-window-description"));
    topaz.slideshow.setInitialThumbnailIndex();
    
    //dojo.event.connect(window, "onload", function () {
        topaz.slideshow.adjustViewerHeight();
    //  }
    //);
    
    dojo.event.connect(window, "onresize", function () {
        topaz.slideshow.adjustViewerHeight();
      }
    );
    
    /*figureWindow = dojo.byId("figure-window-wrapper");
    var imageMarginBox = dojo.html.getMarginBox(topaz.slideshow.figureImg);
    imageWidth = imageMarginBox.width;
    //alert("imageWidth = " + imageWidth);
    
    topaz.domUtil.setContainerWidth(figureWindow, imageWidth + 250);
    
    dojo.event.connect(window, "onresize", function() {
        setTimeout("topaz.domUtil.setContainerWidth(figureWindow, imageWidth + 250)", 100);
      }
    );*/
    
  }
  
  dojo.addOnLoad(init);
