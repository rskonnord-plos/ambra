/**
  * topaz.rating()
  *
  * This class uses a css-based ratings star and sets up the number of star 
  * rating to be displayed in the right hand column.  This also displays the 
  * rating dialog.
  *
  **/
topaz.rating = new Object();

topaz.rating = {
	rateScale: 5,
	
  init: function() {
  },
  
  show: function(action){
    if (action && action == 'edit') {
      getRatingsForUser();
    }
    else {
      _ratingDlg.show();
    }
    
    return false;
  },
  
  buildCurrentRating: function(liNode, rateIndex) {
		var ratedValue = (parseInt(rateIndex)/this.rateScale)*100;
		liNode.className += " pct" + ratedValue;
		dojo.dom.textContent(liNode, "Currently " + rateIndex + "/" + this.rateScale + " Stars");
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
						currentChild.onmouseover = function() { topaz.rating.hover.on(this); }
						currentChild.onmouseout  = function() { topaz.rating.hover.off(this); }
						
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
    var ratingList = document.getElementsByTagAndClassName('li', 'current-rating');
    
    // build rating stars
    for (var i=0; i<ratingList.length; i++) {
	      if (ratingList[i].className.match("average") != null || ratingList[i].className.match("overall-rating") != null) {
	      	continue;
	      }
	      
	      ratingList[i].className = ratingList[i].className.replaceStringArray(" ", "pct", "pct0");
    }
    
		topaz.formUtil.textCues.reset(_ratingTitle, _ratingTitleCue);
		topaz.formUtil.textCues.reset(_ratingComments, _ratingCommentCue);
  	
  },
  
  hover: {
  	on: function(node) {
  		var sibling = topaz.domUtil.firstSibling(node);
  		sibling.style.display = "none"
  	},
  	
  	off: function(node) {
  		var sibling = topaz.domUtil.firstSibling(node);
  		sibling.style.display = "block";
  	}
  },
  
  setRatingCategory: function(node, categoryId, rateNum) {
  	_ratingsForm[categoryId].value = rateNum;
  	var sibling = topaz.domUtil.firstSibling(node.parentNode);
  	var rateStyle = "pct" + (parseInt(rateNum) * 20);  
  	sibling.className = sibling.className.replaceStringArray(" ", "pct", rateStyle);
		this.buildCurrentRating(sibling, rateNum);
  }
}
  
function getRatingsForUser() {
	 var targetUri = _ratingsForm.articleURI.value;
	 
   var bindArgs = {
    url: _namespace + "/rate/secure/getRatingsForUser.action?articleURI=" + targetUri,
    method: "get",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     
     return false;
    },
    load: function(type, data, evt){
     var jsonObj = dojo.json.evalJson(data);
     
     //alert("jsonObj:\n" + jsonObj.toSource());
     //submitMsg.appendChild(document.createTextNode(jsonObj.toSource()));
     
     if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
       }
       
       alert("ERROR: " + errorMsg);
       
       return false;
     }
     else {
       
		   _ratingDlg.show();
       topaz.rating.buildDialog(jsonObj);
       return false;
     }
     
    },
    mimetype: "text/plain",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
}

function updateRating() {
	topaz.formUtil.disableFormFields(_ratingsForm);
  var submitMsg = dojo.byId('submitRatingMsg');
  dojo.dom.removeChildren(submitMsg);
  var articleUri = _ratingsForm.articleURI.value;

  _ldc.show();
   
  var bindArgs = {
    url: _namespace + "/rate/secure/rateArticle.action",
    method: "post",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     
     return false;
   },
   load: function(type, data, evt){
     var jsonObj = dojo.json.evalJson(data);
     
     if (jsonObj.actionErrors.length > 0) {
       var errorMsg = "";
       
       for (var i=0; i<jsonObj.actionErrors.length; i++) {
         errorMsg += jsonObj.actionErrors[i] + "\n";
       }
       
       //alert("ERROR: " + errorMsg);
       var err = document.createTextNode(errorMsg);
       submitMsg.appendChild(err);
       topaz.formUtil.enableFormFields(_ratingsForm);
       //_ratingDlg.placeModalDialog();
       _ldc.hide();
       
       return false;
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
       topaz.formUtil.enableFormFields(_ratingsForm);
       _ldc.hide();

       return false;
     }
     else {
       if (djConfig.isDebug) {
         dojo.byId(djConfig.debugContainerId).innerHTML = "";
       }
       _ratingDlg.hide();
       getArticle("rating");
        
       topaz.formUtil.enableFormFields(_ratingsForm);
       return false;
     }
     
   },
   mimetype: "text/plain",
   formNode: _ratingsForm,
   transport: "XMLHTTPTransport",
   sync: true
  };
  dojo.io.bind(bindArgs);
}

function refreshRating(uri) {
	 var refreshArea1 = dojo.byId(ratingConfig.ratingContainer + "1");
	 var refreshArea2 = dojo.byId(ratingConfig.ratingContainer + "2");
	 
   var bindArgs = {
    url: _namespace + "/rate/getUpdatedRatings.action?articleURI=" + uri,
    method: "get",
    error: function(type, data, evt){
     alert("An error occurred." + data.toSource());
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     
     return false;
    },
    load: function(type, data, evt){
     var jsonObj = dojo.json.evalJson(data);
     
     var docFragment = document.createDocumentFragment();
     docFragment = data;
     
     refreshArea1.innerHTML = docFragment;
     refreshArea2.innerHTML = docFragment;
     
     return false;
    },
    mimetype: "text/plain",
    transport: "XMLHTTPTransport"
   };
   dojo.io.bind(bindArgs);
}

  
  
  
