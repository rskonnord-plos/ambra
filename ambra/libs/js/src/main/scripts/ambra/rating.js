/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
  * ambra.rating
  *
  * This class uses a css-based ratings star and sets up the number of star 
  * rating to be displayed in the right hand column.  This also displays the 
  * rating dialog.
  *
  **/
dojo.provide("ambra.rating");
dojo.require("ambra.general");
dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");
ambra.rating = {
	rateScale: 5,
	
  init: function() {
  },
  
  show: function(action){
    if (action && action == 'edit') {
      getRatingsForUser();
    }
    else {
      this.resetDialog();
      _ratingDlg.show();
    }
    
    return false;
  },
  
  hide: function() { _ratingDlg.hide(); },
  
  buildCurrentRating: function(liNode, rateIndex) {
		var ratedValue = (parseInt(rateIndex)/this.rateScale)*100;
		liNode.className += " pct" + ratedValue;
		dojox.data.dom.textContent(liNode, "Currently " + rateIndex + "/" + this.rateScale + " Stars");
  },
  
  buildDialog: function(jsonObj) {
    var ratingList = document.getElementsByTagAndClassName('ul', 'star-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
    	var currentNode = ratingList[i];
    	
    	if (currentNode.className.match("edit") != null) {
	    	var rateChildNodes = currentNode.childNodes;
	      var rateItem = currentNode.id.substr(4);
        rateItem = rateItem.charAt(0).toLowerCase() + rateItem.substring(1); 
	      var rateItemCount = jsonObj[rateItem];
					     
	      var indexInt = 0;
				for (var n=0; n<rateChildNodes.length; n++) {
					var currentChild = rateChildNodes[n];
		      if (currentChild.nodeName == "#text" && (currentChild.nodeValue.match(new RegExp("\n")) || currentChild.nodeValue.match(new RegExp("\r")))) {
		        continue;
		      }
		      
		      if (currentChild.className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
		      	continue;
		      }
		      
		      if(currentChild.className.match("current-rating")) {
						this.buildCurrentRating(currentChild, rateItemCount);
						firstSet = true;
						continue;
		      }
		      
					if (indexInt < rateItemCount) {
						currentChild.onmouseover = function() { ambra.rating.hover.on(this); }
						currentChild.onmouseout  = function() { ambra.rating.hover.off(this); }
						
						indexInt++;
					}
				}
				
	    	_ratingsForm[rateItem].value = jsonObj[rateItem];
				
    	}
    }
    
    // add title
    if (jsonObj.commentTitle != null) {
    	_ratingsForm.commentTitle.value = jsonObj.commentTitle;
    	_ratingsForm.cTitle.value = jsonObj.commentTitle;
    }
    
    // add comments
    if (jsonObj.comment) {
    	_ratingsForm.comment.value = jsonObj.comment;
    	_ratingsForm.cArea.value = jsonObj.comment;
    }
  },
  
  resetDialog: function() {
    ambra.domUtil.removeChildren(dojo.byId('submitMsg'));
    var ratingList = document.getElementsByTagAndClassName('li', 'current-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
	      if (ratingList[i].className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
	      	continue;
	      }
	      
	      ratingList[i].className = ratingList[i].className.replaceStringArray(" ", "pct", "pct0");
    }
    
		ambra.formUtil.textCues.reset(_ratingTitle, _ratingTitleCue);
		ambra.formUtil.textCues.reset(_ratingComments, _ratingCommentCue);
  	
  },
  
  hover: {
  	on: function(node) {
  		var sibling = ambra.domUtil.firstSibling(node);
  		sibling.style.display = "none"
  	},
  	
  	off: function(node) {
  		var sibling = ambra.domUtil.firstSibling(node);
  		sibling.style.display = "block";
  	}
  },
  
  setRatingCategory: function(node, categoryId, rateNum) {
  	_ratingsForm[categoryId].value = rateNum;
  	var sibling = ambra.domUtil.firstSibling(node.parentNode);
  	var rateStyle = "pct" + (parseInt(rateNum) * 20);  
  	sibling.className = sibling.className.replaceStringArray(" ", "pct", rateStyle);
		this.buildCurrentRating(sibling, rateNum);
  }
}
  
function getRatingsForUser() {
	var targetUri = _ratingsForm.articleURI.value;
	dojo.xhrGet({
    url: _namespace + "/rate/secure/getRatingsForUser.action?articleURI=" + targetUri,
    handleAs:'json-comment-filtered',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
       var jsonObj = response;
       if (jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
         }
         alert("ERROR: " + errorMsg);
       }
       else {
  		   _ratingDlg.show();
         ambra.rating.buildDialog(jsonObj);
       }
    }
  });
}

function updateRating() {
	ambra.formUtil.disableFormFields(_ratingsForm);
  var submitMsg = dojo.byId('submitRatingMsg');
  ambra.domUtil.removeChildren(submitMsg);
  var articleUri = _ratingsForm.articleURI.value;

  _ldc.show();
  dojo.xhrPost({
    url: _namespace + "/rate/secure/rateArticle.action",
    handleAs:'json-comment-filtered',
    form: _ratingsForm,
    sync: true,
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     var jsonObj = response;
     if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg += jsonObj.actionErrors[i] + "\n";
       }
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       ambra.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else if (jsonObj.numFieldErrors > 0) {
       var fieldErrors = document.createDocumentFragment();
       for (var item in jsonObj.fieldErrors) {
         var errorString = "";
         for (var ilist in jsonObj.fieldErrors[item]) {
           var err = jsonObj.fieldErrors[item][ilist];
           if (err) {
             errorString += err;
             var error = document.createTextNode(errorString.trim());
             var brTag = document.createElement('br');

             fieldErrors.appendChild(error);
             fieldErrors.appendChild(brTag);
           }
         }
       }
	     submitMsg.appendChild(fieldErrors);
       ambra.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();
     }
     else {
       _ratingDlg.hide();
       ambra.formUtil.enableFormFields(_ratingsForm);
       refreshRating(articleUri);
       _ldc.hide();
     }
   }
  });
}

function refreshRating(uri) {
  dojo.xhrGet({
    url: _namespace + "/rate/getUpdatedRatings.action?articleURI=" + uri,
    handleAs:'text',
    error: function(response, ioArgs){
     handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
     dojo.byId('ratingRhc1').innerHTML = response;
    }
  });
}

  
  
  
