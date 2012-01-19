/**
  * topaz.commentDisplay
  * 
  * This object builds the dialog that displays the comments for a specific 
  * annotation bug.
  *
  * @author  Joycelyn Chung			joycelyn@orangetowers.com
  *
  **/
topaz.displayComment = new Object();

topaz.displayComment = {
  target: "",
  
  targetSecondary: "",
  
  sectionTitle: "",
  
  sectionDetail: "",
  
  sectionComment: "",
  
  sectionLink: "",
  
  retrieveMsg: "",
  
  init: function() {
    this.sectionTitle   = dojo.byId(commentConfig.sectionTitle);
    this.sectionDetail  = dojo.byId(commentConfig.sectionDetail);
    this.sectionComment = dojo.byId(commentConfig.sectionComment);
    this.sectionLink    = dojo.byId(commentConfig.sectionLink);
    this.retrieveMsg    = dojo.byId(commentConfig.retrieveMsg);    
  },
  
  isMultiple: function(attr) {
    var attrList = this.parseAttributeToList(attr);
    
    return (attrList.length > 1) ? true : false;
  },
  
  setTarget: function(obj) {
    this.target = obj;
  },
  
  setTargetSecondary: function(obj) {
    this.targetSecondary = obj;
  },
  
  setSectionTitle: function(configObj) {
    this.sectionTitle = dojo.byId(configObj.sectionTitle);
  },
  
  setSectionDetail: function(configObj) {
    this.sectionDetail = dojo.byId(configObj.sectionDetail);
  },
  
  setSectionComment: function(configObj) {
    this.sectionComment = dojo.byId(configObj.sectionComment);
  },
  
  setSectionLink: function(configObj) {
    this.sectionLink = dojo.byId(configObj.sectionLink);
  },
  
  setRetrieveMsg: function(configObj) {
    this.retrieveMsg = dojo.byId(configObj.retrieveMsg);
  },
  
  /**
   * topaz.displayComment.show(Node node)
   * 
   * Method that triggers the display of the dialog box.
   * 
   * @param		node		Node			Node where the action was triggered and the
   * 														 dialog box will positioned relative to.
   * @return	false		boolean		In the link that triggered this call, sending 
   * 															false back prevents the page from forwarding.			
   */
  show: function(node){
    this.setTarget(node);
    
		_commentDlg.setMarker(this.target);
		_commentMultiDlg.setMarker(this.target);
    getComment(this.target);
    
    return false;
  },

  /**
   * topaz.displayComment.buildDisplayHeader(JSON jsonObj)
   * 
   * Builds the header of the annotation comment display dialog.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	titleDocFrag	Document fragment		Resulting document fragment created.
   */
  buildDisplayHeader: function (jsonObj) {
    var titleDocFrag = document.createDocumentFragment();
    
    // Insert title link text
    var titleLink = document.createElement('a');
    titleLink.href = _namespace + '/annotation/listThread.action?inReplyTo=' + jsonObj.annotationId + '&root=' + jsonObj.annotationId; 
    titleLink.className = "discuss icon";
    titleLink.title="View full annotation";
    //alert("jsonObj.annotation.commentTitle = " + jsonObj.annotation.commentTitle);
    titleLink.innerHTML = jsonObj.annotation.commentTitle;
    titleDocFrag.appendChild(titleLink);

    return titleDocFrag;    
  },

  /**
   * topaz.displayComment.buildDisplayDetail(JSON jsonObj)
   * 
   * Builds the details of the annotation comment display dialog.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	detailDocFrag	Document fragment		Resulting document fragment created.
   */
  buildDisplayDetail: function (jsonObj) {
    // Insert creator detail info
    var annotationId = jsonObj.annotationId;
    var tooltipId = annotationId.substring(annotationId.lastIndexOf('/') + 1, annotationId.length);
    //alert("tooltipId = " + tooltipId);
    
    var creatorId = jsonObj.creatorUserName;
    var creatorLink = document.createElement('a');
    creatorLink.href = _namespace + '/user/showUser.action?userId=' + jsonObj.annotation.creator;
//   creatorLink.title = "Annotation Author";
    creatorLink.className = "user icon";
    creatorLink.appendChild(document.createTextNode(creatorId));
    creatorLink.id = tooltipId;
    
/*    var divTooltip = document.createElement('div');
    var dojoType = document.createAttribute('dojoType');
    dojoType.value = "PostTooltip";
    divTooltip.setAttributeNode(dojoType);
    var connectId = document.createAttribute('dojo:connectId');
    connectId.value = tooltipId;
    divTooltip.setAttributeNode(connectId);
    var uniqueId = document.createAttribute('dojo:uniqId');
    uniqueId.value = "tt" + tooltipId;
    divTooltip.setAttributeNode(uniqueId);
    var contentUrl = document.createAttribute('dojo:contentUrl');
    contentUrl.value = _namespace + "/user/displayUserAJAX.action?userId=" + creatorId;
    divTooltip.setAttributeNode(contentUrl);
    var executeScripts = document.createAttribute('dojo:executeScripts');
    executeScripts.value = "true";
    divTooltip.setAttributeNode(executeScripts);
    //var caption = document.createAttribute('dojo:caption');
    //caption.value = "The tooltip";
    //divTooltip.setAttributeNode(caption);*/
    
    var userInfoDiv = document.createElement('div');
    userInfoDiv.className = "userinfo";
    //divTooltip.appendChild(userInfoDiv);
    
    var d = new Date(jsonObj.annotation.createdAsMillis);
		var MONTH_NAMES = new String('JanFebMarAprMayJunJulAugSepOctNovDec');
    var dayInt = d.getUTCDate();
	  var day = (dayInt >= 10 ? "" : "0") + dayInt;
		var monthInt = d.getUTCMonth() * 3;
    var month = MONTH_NAMES.substring (monthInt, monthInt + 3);
    var year = d.getUTCFullYear();
		var hrsInt = d.getUTCHours(); 
    var hours = (hrsInt >= 10 ? "" : "0") + hrsInt;
    var minInt = d.getUTCMinutes();
    var minutes = (minInt >= 10 ? "" : "0") + minInt;
    
    var dateStr = document.createElement('strong');
    dateStr.appendChild(document.createTextNode(day + " " + month + " " + year));
    var timeStr = document.createElement('strong');
    timeStr.appendChild(document.createTextNode(hours + ":" + minutes + " GMT"));
    
    var detailDocFrag = document.createDocumentFragment();
    detailDocFrag.appendChild(document.createTextNode('Posted by '));
    detailDocFrag.appendChild(creatorLink);
    detailDocFrag.appendChild(document.createTextNode(' on '));
    detailDocFrag.appendChild(dateStr);
    detailDocFrag.appendChild(document.createTextNode(' at '));
    detailDocFrag.appendChild(timeStr);
    //detailDocFrag.appendChild(divTooltip);
    
    return detailDocFrag;
  },
  
  /**
   * topaz.displayComment.buildDisplayBody(JSON jsonObj)
   * 
   * Builds the comment body of the annotation comment display dialog.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	commentFrag		Document fragment		Resulting document fragment created.
   */
  buildDisplayBody: function (jsonObj) {
    // Insert formatted comment
    var commentFrag = document.createDocumentFragment();
    commentFrag = jsonObj.annotation.escapedTruncatedComment;
    
    return commentFrag;
  },
  
  /**
   * topaz.displayComment.buildDisplayViewLink(JSON jsonObj)
   * 
   * Builds the link that takes the user to the discussion section.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	commentLink	Document fragment		Resulting document fragment created.
   */
  buildDisplayViewLink: function (jsonObj) {
    var commentLink = document.createElement('a');
    commentLink.href = _namespace + '/annotation/listThread.action?inReplyTo=' + jsonObj.annotationId + '&root=' + jsonObj.annotationId;
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View/respond to this'));
    
    return commentLink;
  },
  
  /**
   * topaz.displayComment.buildDisplayView(JSON jsonObj)
   * 
   * Builds the comment dialog box for a single comment.  Empties out the inner 
   * containers if text already exists in it.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	<nothing>
   */
  buildDisplayView: function(jsonObj){
    if (topaz.displayComment.sectionTitle.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionTitle);
    topaz.displayComment.sectionTitle.appendChild(this.buildDisplayHeader(jsonObj));
    
    if (topaz.displayComment.sectionDetail.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionDetail);
    topaz.displayComment.sectionDetail.appendChild(this.buildDisplayDetail(jsonObj));

    //alert(commentFrag);
    if (topaz.displayComment.sectionComment.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionComment);
    topaz.displayComment.sectionComment.innerHTML = this.buildDisplayBody(jsonObj);
    //alert("jsonObj.annotation.commentWithUrlLinking = " + jsonObj.annotation.commentWithUrlLinking);
    
    if (topaz.displayComment.sectionLink.hasChildNodes) dojo.dom.removeChildren(topaz.displayComment.sectionLink);
    this.sectionLink.appendChild(this.buildDisplayViewLink(jsonObj));
  },
  
  /**
   * topaz.displayComment.buildDisplayView(JSON jsonObj)
   * 
   * Builds the comment dialog box for a multiple comments.  Empties out the inner 
   * containers if text already exists in it.
   * 
   * @param		jsonObj				JSON object					JSON object containing the data that
   * 														 						 		 retrieved from the database.
   * 
   * @return	<nothing>
   */
  buildDisplayViewMultiple: function(jsonObj, iter, container, secondaryContainer){
    var newListItem = document.createElement('li');
    
    if (iter <= 0)
      newListItem.className = 'active';
      
    newListItem.appendChild(document.createTextNode(jsonObj.annotation.commentTitle));
    //newListItem.appendChild(this.buildDisplayHeader(jsonObj));
    var detailDiv = document.createElement('div');
    detailDiv.className = 'detail';
    detailDiv.appendChild(this.buildDisplayDetail(jsonObj)); 
    newListItem.appendChild(detailDiv);   
    
    var contentDiv = document.createElement('div');
    if (iter <=0)
      contentDiv.className = 'contentwrap active';
    else
      contentDiv.className = 'contentwrap';

    contentDiv.innerHTML = this.buildDisplayBody(jsonObj);
    
    var cDetailDiv = document.createElement('div');
    cDetailDiv.className = 'detail';
    /*var commentLink = document.createElement('a');
    commentLink.href = '#';
    commentLink.className = 'commentary icon';
    commentLink.title = 'Click to view full thread and respond';
    commentLink.appendChild(document.createTextNode('View full commentary'));
    
    var responseLink = document.createElement('a');
    responseLink.href = '#';
    responseLink.className = 'respond tooltip';
    responseLink.title = 'Click to respond to this posting';
    responseLink.appendChild(document.createTextNode('Respond to this'));
    
    cDetailDiv.appendChild(commentLink);
    cDetailDiv.appendChild(responseLink);*/
    cDetailDiv.appendChild(this.buildDisplayViewLink(jsonObj));
    contentDiv.appendChild(cDetailDiv);

    if (iter <= 0) {
      container.appendChild(newListItem);
      secondaryContainer.appendChild(contentDiv);
    }
    else {
      var liList = topaz.domUtil.getChildElementsByTagAndClassName(container, 'li', null);
      dojo.dom.insertAfter(newListItem, liList[liList.length - 1]);
    
      var divList = topaz.domUtil.getChildElementsByTagAndClassName(secondaryContainer, 'div', null);
      dojo.dom.insertAfter(contentDiv, divList[divList.length - 1]);
    }
    
    var multiDetailDivChild = secondaryContainer.childNodes[secondaryContainer.childNodes.length - 1];
    newListItem.onclick = function() {
        topaz.displayComment.mouseoutComment(topaz.displayComment.target);
        topaz.displayComment.mouseoverComment(topaz.displayComment.target, jsonObj.annotationId);
        topaz.domUtil.swapClassNameBtwnSibling(this, this.nodeName, 'active');
        topaz.domUtil.swapClassNameBtwnSibling(multiDetailDivChild, multiDetailDivChild.nodeName, 'active');
        topaz.domUtil.swapAttributeByClassNameForDisplay(topaz.displayComment.target, ' active', jsonObj.annotationId);
        
        topaz.displayComment.adjustDialogHeight(container, secondaryContainer, 50);
      }

  },
  
  /**
   * topaz.displayComment.mouseoverComment(Node obj, String displayId)
   * 
   * This method gets a map of all element nodes that contain the same display ID
   * and iterates through the map and modifies the classname to show highlight.
   * 
   * @param		obj					Node object				Source element to start highlight.
   * @param		displayId		String						Id reference for display.
   * 
   * @return	<nothing>
   */
  mouseoverComment: function (obj, displayId) {
   var elementList = topaz.domUtil.getDisplayMap(obj, displayId);
   
   // Find the displayId that has the most span nodes containing that has a 
   // corresponding id in the annotationId attribute.  
   var longestAnnotElements;
   for (var i=0; i<elementList.length; i++) {
     if (i == 0) {
       longestAnnotElements = elementList[i];
     }
     else if (elementList[i].elementCount > elementList[i-1].elementCount){
       longestAnnotElements = elementList[i];
     }
   }
   
   //this.modifyClassName(obj);
   
   // the annotationId attribute, modify class name.
   for (var n=0; n<longestAnnotElements.elementCount; n++) {
     var classList = new Array();
     var elObj = longestAnnotElements.elementList[n];

     this.modifyClassName(elObj);
     
     if (n == 0) {
       var bugObj = topaz.domUtil.getChildElementsByTagAndClassName(elObj, 'a', 'bug');
       
       for (var i=0; i<bugObj.length; i++) {
         this.modifyClassName(bugObj[i]);
       }
     }
   }

  },

	/**
	 * topaz.displayComment.mouseoutComment(Node obj) 
	 * 
	 * Resets span tags that were modified to highlight to no highlight.
	 * 
	 * @param		obj		Node object				Object needed to be reset.
	 */
  mouseoutComment: function (obj) {
    var elList = document.getElementsByTagName('span');
    
    for(var i=0; i<elList.length; i++) {
      elList[i].className = elList[i].className.replace(/\-active/, "");
    }
    obj.className = obj.className.replace(/\-active/, "");
  },
  
  /**
   * topaz.displayComment.modifyClassName(Node obj)
   * 
   * Modifies the className
   * 
   * @param		obj		Node object		Source node.
   */
  modifyClassName: function (obj) {
     classList = obj.className.split(" ");
     for (var i=0; i<classList.length; i++) {
       if ((classList[i].match('public') || classList[i].match('private')) && !classList[i].match('-active')) {
         classList[i] = classList[i].concat("-active");
       }
     }
     
     obj.className = classList.join(' ');
  },
  
  /**
   * topaz.displayComment.processBugCount()
   * 
   * Searches the document for tags that has the classname of "bug" indicating
   * that it's an annotation bug.  Looks at the node id which should have a list
   * of IDs corresponding to an annotation.  This ID list is counted and the 
   * result is shown in the bug.
   */
  processBugCount: function () {
    var bugList = document.getElementsByTagAndClassName(null, 'bug');
    
    for (var i=0; i<bugList.length; i++) {
      var bugCount = topaz.domUtil.getDisplayId(bugList[i]);
      
      if (bugCount != null) {
        var displayBugs = bugCount.split(',');
        var count = displayBugs.length;
        var ctText = document.createTextNode(count);
      }
      else {
        var ctText = document.createTextNode('0');
      }

      dojo.dom.removeChildren(bugList[i]);
      bugList[i].appendChild(ctText);
    }
  },
  
  /**
   * topaz.displayComment.adjustDialogHeight(Node container1, Node container2, Integer addPx)
   * 
   * The height of the margin box of container1 and container2 are compared and
   * the height are adjusted accordingly.  
   * 
   * @param		container1		Node object			Container node object.
   * @param		container2		Node object			Container node object.
   * @param		addPx					Integer					Pixel amount to adjust height.
   */
  adjustDialogHeight: function(container1, container2, addPx) {
    var container1Mb = dojo.html.getMarginBox(container1).height;
    var container2Mb = dojo.html.getMarginBox(container2).height;
    
    if (container1Mb > container2Mb) {
      container1.parentNode.style.height = (container1Mb + addPx) + "px";
      
      var contentDivs = topaz.domUtil.getChildElementsByTagAndClassName(container2, 'div', 'contentwrap');
      for (var i=0; i<contentDivs.length; i++) {
        if (contentDivs[i].className.match('active')) {
          contentDivs[i].style.height = (container1Mb - container1Mb/(3.3*contentDivs.length)) + "px";
          //contentDivs[i].style.backgroundColor = "#fff";
        }
      }
    }
    else
      container1.parentNode.style.height = (container2Mb + addPx) + "px";
      
    _commentMultiDlg.placeModalDialog();
  }
}

function getComment(obj) {
    _ldc.show();
    
    var targetUri = topaz.domUtil.getDisplayId(obj);
          
    var uriArray = targetUri.split(",");

    if (uriArray.length > 1) {
      var targetContainer = document.getElementById('multilist');
      dojo.dom.removeChildren(targetContainer);
      var targetContainerSecondary = document.getElementById('multidetail');
      dojo.dom.removeChildren(targetContainerSecondary);
    }
    else {
      var targetContainer =  dojo.widget.byId("CommentDialog");
    }
    
    var maxShown = 4;
    var stopPt = (uriArray.length < maxShown) ? uriArray.length : maxShown;
    
    var count = 0;
    
    for (var i=0; i<stopPt; i++) {
      //alert("uriArray[" + i + "] = " + uriArray[i]);
      var bindArgs = {
        url: _namespace + "/annotation/getAnnotation.action?annotationId=" + uriArray[i],
        method: "get",
        error: function(type, data, evt){
         alert("ERROR [AJAX]:" + data.toSource());
         var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
         //topaz.errorConsole.writeToConsole("ERROR [AJAX]:" + data.toSource());
         //errView.show();
         return false;
        },
        load: function(type, data, evt){
         var jsonObj = dojo.json.evalJson(data);
         
         //alert("jsonObj:\n" + jsonObj.toSource());
         
         if (jsonObj.actionErrors.length > 0) {
           var errorMsg = "";
           //alert("jsonObj.actionErrors.list.length = " + jsonObj.actionErrors.list.length);
           for (var i=0; i<jsonObj.actionErrors.length; i++) {
             errorMsg = errorMsg + jsonObj.actionErrors[i] + "\n";
           }
           
           alert("ERROR [actionErrors]: " + errorMsg);
           var err = document.createTextNode("ERROR:" + errorMsg);
           //topaz.displayComment.retrieveMsg.innerHTML = err;
           _ldc.hide();
           
           return false;
         }
         else if (jsonObj.numFieldErrors > 0) {
           var fieldErrors;
           //alert("jsonObj.numFieldErrors = " + jsonObj.numFieldErrors);
           for (var item in jsonObj.fieldErrors) {
             var errorString = "";
             for (var i=0; i<jsonObj.fieldErrors[item].length; i++) {
               errorString += jsonObj.fieldErrors[item][i];
             }
             fieldErrors = fieldErrors + item + ": " + errorString + "<br/>";
           }
           
           alert("ERROR [numFieldErrors]: " + fieldErrors);
           var err = document.createTextNode("ERROR:" + fieldErrors);
           //topaz.displayComment.retrieveMsg.innerHTML = err;
           _ldc.hide();
  
           return false;
         }
         else {
           if (uriArray.length > 1) {             
             topaz.displayComment.buildDisplayViewMultiple(jsonObj, targetContainer.childNodes.length, targetContainer, targetContainerSecondary);
             
             if (targetContainer.childNodes.length == stopPt) {
               topaz.displayComment.mouseoverComment(topaz.displayComment.target, uriArray[0]);
                
               _commentMultiDlg.show();

               topaz.displayComment.adjustDialogHeight(targetContainer, targetContainerSecondary, 50);

               _ldc.hide();
            
               return false;
             }
           }
           else {
             topaz.displayComment.buildDisplayView(jsonObj);
             topaz.displayComment.mouseoverComment(topaz.displayComment.target);
 
             _commentDlg.show();
             _ldc.hide();
          
             return false;
           }
           
           //alert("targetContainer.childNodes.length = " + targetContainer.childNodes.length + "\n" + "stopPt = " + stopPt);
           
         }
        },
        mimetype: "text/html"
       };
       dojo.io.bind(bindArgs);
    }

  }