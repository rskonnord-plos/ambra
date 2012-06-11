/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

dojo.provide("ambra.lightBox");
dojo.require("ambra.general");
dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");

ambra.lightBox = {
  showing: false,
  jsonObj: null,
  currentImageIndex: -1,

  init: function() {
  },

  /**
   * get the data of figures for an article
   * this function is called from two places: view-all-figures button
   * and from inline images. In case of button, imageURI will be null.
   *
   */
  show: function(targetURI, imageURI){

    if (!this.jsonObj) {
      this.getFigureInfo(targetURI, imageURI);
    }
    var e = window.event;
    if(e) {
      e.returnValue = false;
    } else {
      return false;
    }
  },

  /**
   * clicking on close button will hide the dialog box.
   */
  hide: function() {

    _lightBoxDlg.hide();
    this.jsonObj = null;
    this.showing = false;

    return false;
  },

  /**
   * call the action to get the figures info.
   * @param targetURI
   * @param imageURI
   */
  getFigureInfo: function(targetURI, imageURI) {
    dojo.xhrGet({
      url: _namespace + "/article/lightbox.action?uri=" + targetURI,
      handleAs:'json-comment-filtered',
      error: function(response, ioArgs){
        handleXhrError(response, ioArgs);
        if (ambra.lightBox.showing) {
          ambra.lightBox.showing = false;
          _lightBoxDlg.hide();
        }
      },
      load: function(response, ioArgs) {
        var jsonObj = response;
        if (jsonObj.actionErrors.length > 0) {
          var errorMsg = "";
          for (var i=0; i<jsonObj.actionErrors.length; i++) {
            errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
          }
          alert("ERROR: " + errorMsg);
        }
        else {
          try {

            /**
             *  execute the below logic only when article has any images.
             *  or in other words if we click on view-all-figure button,don't
             *  display blank dialog box.
             */
            if (jsonObj.secondaryObjects.length > 0) {
              ambra.lightBox.jsonObj = jsonObj;

              /**
               * show the dialog box only when it is not present
               * clicking on other images should not pop-up another dialog
               * box on top of each other.
               */

              if (!this.showing) {
                this.showing = true;
                _lightBoxDlg.show();
              }

              ambra.lightBox.showFigures();
              ambra.lightBox.selectFigure(imageURI);
            }
          } catch (e) {
            alert("ERROR: " + e);
          }
        }
      }
    });
  },

  /**
   * build html for filmstrip
   */
  showFigures: function() {
    if (this.jsonObj) {
      var imagesCount = this.jsonObj.secondaryObjects.length;
      dojo.byId("figure-window-nav").innerHTML = "";
      for(var i=0; i< imagesCount; ++i) {
        var image = this.jsonObj.secondaryObjects[i];
        var createdivNode =  dojo.create("div");
        createdivNode.id = "figure-thumb-div-" + i;
        createdivNode.className = "figure-window-nav-item";
        dojo.byId("figure-window-nav").appendChild(createdivNode);
        createdivNode.innerHTML = '<span>Fig. ' + (i+1) + '</span><br/><a title="Click for larger image" href="#"><img border="0" class="thumbnail" id="tn' + i + '" src="/article/fetchObject.action?uri=' + image.uri + '&representation=' + image.repSmall + '" onclick="return ambra.lightBox.selectFigure(\'' + image.uri + '\');" title="' + image.title + ' ' + image.plainCaptionTitle + '"/>';
      }
    }
  },

  /**
   * highlight selected image and show its data in the figure window
   * @param imageURI
   */
  selectFigure: function(imageURI) {
    try {
      if (this.jsonObj) {
        var currentImageIndex = -1;
        var imagesCount = this.jsonObj.secondaryObjects.length;
        for(var i=0; i< imagesCount; ++i) {
          var image = this.jsonObj.secondaryObjects[i];
          var createdivNode = dojo.byId("figure-thumb-div-" + i);
          if (typeof imageURI != 'undefined' && image.uri == imageURI) {
            createdivNode.className = "figure-window-nav-item current";
            currentImageIndex = i;
          } else {
            createdivNode.className = "figure-window-nav-item";
          }
        }

        //highlight the first image
        if (currentImageIndex < 0 && imagesCount > 0) {
          currentImageIndex = 0;
          var createdivNode = dojo.byId("figure-thumb-div-0");
          createdivNode.className = "figure-window-nav-item current";
        }

        if (this.currentImageIndex != currentImageIndex) {
          this.currentImageIndex = currentImageIndex;
        }

        if (currentImageIndex >= 0) {
          var currentImage = this.jsonObj.secondaryObjects[currentImageIndex];

          dojo.byId("figure-window-img").onload = function() {
            var doi = currentImage.doi;
            var newDoi = doi.replace("info:doi/", "");
            dojo.byId("figure-window-doi").innerHTML = 'doi:' + newDoi;
            dojo.byId("figure-window-doi").style.width = dojo.byId("figure-window-img").width + "px";
            dojo.byId("figure-window-doi").style.bottom = "";
            dojo.byId("figure-window-doi").style.top = 323 + (dojo.byId("figure-window-img").height)/2 + "px";
          };

          dojo.byId("figure-window-img").src = '/article/fetchObject.action?uri=' + currentImage.uri + '&representation=' + currentImage.repMedium;
          dojo.byId("figure-window-img").title = currentImage.title + ' ' + currentImage.plainCaptionTitle;

          dojo.byId("figure-window-title").innerHTML = (currentImage.title ? currentImage.title + '. ' : "") + currentImage.transformedCaptionTitle;
          dojo.byId("figure-window-description").innerHTML = currentImage.transformedDescription;

          dojo.byId("figure-window-ppt").href = '/article/fetchPowerPoint.action?uri=' + currentImage.uri;
          dojo.byId("figure-window-tiff").href = '/article/fetchObjectAttachment.action?uri=' + currentImage.uri + '&representation=TIF';
          dojo.byId("figure-window-large").href = '/article/fetchObjectAttachment.action?uri=' + currentImage.uri + '&representation=' + currentImage.repLarge;
          dojo.byId("figure-window-tiff-size").innerHTML =  this.displaySize(currentImage.sizeTiff) + " TIFF";
          dojo.byId("figure-window-large-size").innerHTML = this.displaySize(currentImage.sizeLarge) + " PNG";

          dojo.byId("figure-window-previous").className = (currentImageIndex == 0 ? "hidden" : "");
          dojo.byId("figure-window-next").className = (currentImageIndex == (imagesCount - 1) ? "hidden" : "");
        }
      }
    } catch (e) {
      alert(e);
    }
    return false;
  },

  /**
   * size conversion function
   * @param value
   */
  displaySize: function(value) {
    if(value < 0) {
      return "unknown";
    } else {
      if(value<1000) {
        return "" + value + "B";
      } else if (value < 1000000) {
        return "" + Math.round(value / 1000) + "KB";
      } else {
        return "" + Math.round(value / 10000) / 100 + "MB";
      }
    }
  },

  /**
   * show previous image
   */
  showPrevious: function() {
    if (this.jsonObj) {
      var imagesCount = this.jsonObj.secondaryObjects.length;
      if (this.currentImageIndex > 0 && imagesCount > 0 && this.currentImageIndex < imagesCount) {
        var currentImage = this.jsonObj.secondaryObjects[this.currentImageIndex - 1];
        this.selectFigure(currentImage.uri);
      }
    }
    return false;
  },

  /**
   *  show next image
   */
  showNext: function() {
    if (this.jsonObj) {
      var imagesCount = this.jsonObj.secondaryObjects.length;
      if (this.currentImageIndex >= 0 && imagesCount > 0 && this.currentImageIndex < (imagesCount - 1)) {
        var currentImage = this.jsonObj.secondaryObjects[this.currentImageIndex + 1];
        this.selectFigure(currentImage.uri);
      }
    }
    return false;
  },

  /**
   *  show the image in context. For example, If reader is on the 4th image inside the lightbox,
   *  if he click on show-in-context button, the lightbox should get hide and the article page should get
   *  scroll to the image.
   */
  showInContext: function() {
    if (this.jsonObj) {
      var imagesCount = this.jsonObj.secondaryObjects.length;
      if (this.currentImageIndex >= 0 && imagesCount > 0 && this.currentImageIndex < imagesCount) {
        var currentImage = this.jsonObj.secondaryObjects[this.currentImageIndex];
        var uri = currentImage.uri;
        //example: uri is in format info:doi/10.1371/journal.pone.0002519.t002. split it on slash.
        var parts = uri.split("/");
        //get the last item from array and split it on dot.
        var parts = parts[parts.length-1].split(".");
        //remove the first item from the array
        parts.shift();
        //join it with dash. assuming image has always id in format like pone-0002519-t002.
        var name = parts.join("-");
        this.hide();
        window.location = "#" + name;
      }
    }
    return false;
  },

  /**
   * show more or less text when user click on more or less button
   */
  showMoreOrLess: function() {
    if (this.jsonObj) {
      var box = dojo.byId("figure-window-title-box");
      var more = dojo.byId("figure-window-more");
      if (box.className == "figure-window-title-less") {
        box.className = "figure-window-title-more";
        more.className = "figure-window-less";
      } else {
        box.className = "figure-window-title-less";
        more.className = "figure-window-more";
      }
    }
    return false;
  }
}

