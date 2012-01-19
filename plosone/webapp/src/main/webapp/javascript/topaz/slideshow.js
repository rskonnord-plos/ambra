topaz.slideshow = new Object();

topaz.slideshow = {
  imgS: "PNG_S",
  
  imgM: "PNG_M",
  
  imgL: "PNG_L",
  
  imgTif: "TIF",
  
  linkView: "",
  
  linkTiff: "",
  
  linkPpt: "",
  
  figImg: "",
  
  figImgWidth: "",
  
  figTitle: "",
  
  figCaption: "",
  
  targetDiv: "",
  
  activeItemIndex: "",
  
  itemCount: "",
  
  setLinkView: function(aObj) {
    this.linkView = aObj;
  },
  
  setLinkTiff: function(aObj) {
    this.linkTiff = aObj;
  },
  
  setLinkPpt: function(aObj) {
    this.linkPpt = aObj;
  },
  
  setFigImg: function(dObj) {
    this.figImg = dObj;
  },

  setFigTitle: function(dObj) {
    this.figTitle = dObj;
  },
  
  setFigCaption: function(dObj) {
    this.figCaption = dObj;
  },
  
  setInitialThumbnailIndex: function() {
    var tn = document.getElementsByTagAndClassName('div', 'figure-window-nav-item');
    this.itemCount = tn.length;
    
    for (var i=0; i<this.itemCount; i++) {
      if (tn[i].className.match('current')) {
        this.activeItemIndex = i;
      }
    }
  },
  
  show: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageLargeUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageAttachUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      //tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      dojo.html.removeClass(tbCurrent[i], "current");
    }
    
    var tbNew = obj.parentNode.parentNode;
    //tbNew.className = tbNew.className.concat("-current");
    dojo.html.addClass(tbNew, "current");
    
    if (index == 0) 
      dojo.html.addClass(dojo.byId("previous"), "hidden");
    else
      dojo.html.removeClass(dojo.byId("previous"), "hidden");
    
    if (index == this.itemCount-1) 
      dojo.html.addClass(dojo.byId("next"), "hidden");
    else
      dojo.html.removeClass(dojo.byId("next"), "hidden");
    
    
    this.activeItemIndex = index;
    
    window.setTimeout("topaz.slideshow.adjustViewerHeight()", 100);
    
  },
  
  showSingle: function (obj, index) {
    if (this.linkView) this.linkView.href = slideshow[index].imageLargeUri + "&representation=" + this.imgL;
    if (this.linkTiff) this.linkTiff.href = slideshow[index].imageAttachUri + "&representation=" + this.imgTif;
    if (this.linkPpt) this.linkPpt.href  = slideshow[index].imageAttachUri + "&representation=" + this.imgM;
    
    if (this.figImg) {
      this.figImg.src = slideshow[index].imageUri + "&representation=" + this.imgM;
      this.figImg.title = slideshow[index].titlePlain;
    }
    
    if (this.figTitle) this.figTitle.innerHTML = slideshow[index].title;
    
    if (this.figCaption) this.figCaption.innerHTML = slideshow[index].description;
    
    var tbCurrent = document.getElementsByTagAndClassName('div', 'figure-window-nav-item-current');
    
    for (var i=0; i<tbCurrent.length; i++) {
      //alert("tbCurrent[" + i + "] = " + tbCurrent[i].nodeName + "\ntbCurrent[" + i + "].className = " + tbCurrent[i].className);
      tbCurrent[i].className = tbCurrent[i].className.replace(/\-current/, "");
      
    }
    
    var tbNew = obj.parentNode.parentNode;
    tbNew.className = tbNew.className.concat("-current");
    
  },
  
  getFigureInfo: function (figureObj) {
    if (figureObj.hasChildNodes) {
      var caption = document.createDocumentFragment();
      
      for (var i=0; i<figureObj.childNodes.length; i++) {
        var child = figureObj.childNodes[i];
        
        if (child.nodeName == 'A') {
          for (var n=0; n<child.childNodes.length; n++) {
            var grandchild = child.childNodes[n];
            
            if (grandchild.nodeName == 'IMG') {
              this.figImg = grandchild;
            }
          }
        }
        else if (grandchild.nodeName == 'H5') {
          dojo.dom.copyChildren(grandchild, this.figTitle);
        }
        else {
          var newChild = grandchild;
          newChild.getAttributeNode('xpathlocation')='noSelect';
          caption.appendChild(newChild);
        }
      }
      
      dojo.dom.copyChildren(caption, this.figCaption);
      
      return;
    }
    else {
      return false;
    }
  },
  
  adjustContainerHeight: function (obj) {
    // get size viewport
    var viewportSize = dojo.html.getViewport();
    
    // get the offset of the container
		var objOffset = topaz.domUtil.getCurrentOffset(obj);
		
		// find the size of the container
		var objMb = dojo.html.getMarginBox(obj);

    var maxContainerHeight = viewportSize.height - (10 * objOffset.top);
    //alert("objOffset.top = " + objOffset.top + "\nviewportSize.height = " + viewportSize.height + "\nmaxContainerHeight = " + maxContainerHeight);
    
    obj.style.height = maxContainerHeight + "px";
    obj.style.overflow = "auto";
  },
  
  adjustViewerHeight: function() {
    var container1 = dojo.byId("figure-window-nav");
    var container2 = dojo.byId("figure-window-container");
    var container1Mb = dojo.html.getMarginBox(container1).height;
    var container2Mb = dojo.html.getMarginBox(container2).height;
    
    if (container1Mb > container2Mb) {
      container2.parentNode.style.height = container1Mb + "px";
      container1.style.borderRight = "2px solid #ccc";
      container2.style.borderLeft = "none";
    }
    else {
      container2.parentNode.style.height = "auto";
      container1.style.borderRight = "none";
      container2.style.borderLeft = "2px solid #ccc";
    }    
  },
  
  adjustViewerWidth: function(figureWindow, maxWidth) {
    var imageMarginBox = dojo.html.getMarginBox(topaz.slideshow.figureImg);
    imageWidth = imageMarginBox.width;
    topaz.domUtil.setContainerWidth(figureWindow, imageWidth, maxWidth, 1);
  },

  showPrevious: function(obj) {
    if (this.activeItemIndex <= 0) {
      return false;
    }
    else {
      var newIndex = this.activeItemIndex - 1;
      var newTnObj = dojo.byId('tn' + newIndex);
      this.show(newTnObj, newIndex);
      
      if (newIndex == 0) 
        dojo.html.addClass(obj, 'hidden');
      
      if (this.activeItemIndex == this.itemCount-1)
        dojo.html.removeClass(dojo.byId('next'), 'hidden');
        
      this.activeItemIndex = newIndex;
    }
  },
  
  showNext: function(obj) {
    if (this.activeItemIndex == this.itemCount-1) {
      return false;
    }
    else {
      var newIndex = this.activeItemIndex + 1;
      var newTnObj = dojo.byId('tn' + newIndex);
      this.show(newTnObj, newIndex);
      
      if (newIndex == this.itemCount-1) 
        dojo.html.addClass(obj, 'hidden');
      
      if (this.activeItemIndex == 0)
        dojo.html.removeClass(dojo.byId('previous'), 'hidden');
        
      this.activeItemIndex = newIndex;
    }
  },
  
  openViewer: function(url) {
    var newWindow = window.open(url,'plosSlideshow','directories=no,location=no,menubar=no,resizable=yes,status=no,scrollbars=yes,toolbar=no,height=600,width=800');
    
    return false;
  },
  
  closeReturn: function() {
    self.close();
    window.opener.focus();
  }
}  