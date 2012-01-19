dojo.provide("topaz.annotation");

/**
  * topaz.annotation
  *
  * @param
  *
  **/
topaz.annotation = new Object();

topaz.annotation = {
/*
  initialize: function() {
  	if ( document.addEventListener )
    {
  		document.addEventListener( 'keyup', this._createAnnotationOnkeyup.bindAsEventListener(this), false );
    }
  	else if ( document.attachEvent ) 
    {
      document.attachEvent('onkeyup', this._createAnnotationOnkeyup.bindAsEventListener(this));
    }  
  	else  // for IE:
  	{
  		if ( document.onkeyup )
  			document.onkeyup = function( event ) { this._createAnnotationOnkeyup(event).bindAsEventListener(this); document.onkeyup; }
  		else
  			document.onkeyup = this._createAnnotationOnkeyup(event).bindAsEventListener(this);
  	}
  	
  },*/
/*  
  _LAST_ANCESTOR: "researchArticle",
  _XPOINTER_MARKER: "xpt",
  _ANNOTATION_MARKER: "note",
  _ANNOTATION_IMG_MARKER: "noteImg",
  _DIALOG_MARKER: "rdm",
  _IS_AUTHOR: false,  //TODO: *** Default to false when the hidden input is hooked up.
  _IS_PUBLIC: false,
  rangeInfoObj: new Object(),
*/  
  _createAnnotationOnkeyup: function (event) {
  	if ( keyname(event) == ENTER ) {
      var captureText = this.createNewAnnotation();
  		
      if ( captureText ) {
      	if (!event) var event = window.event;
      	event.cancelBubble = true;
      	if (event.stopPropagation) event.stopPropagation();
      }
    }
    return true;
  },
  
  createAnnotationOnMouseDown: function (event) {
	  topaz.formUtil.textCues.reset(commentTitle, titleCue); 
	  topaz.formUtil.textCues.reset(comments, commentCue); 
	  annotationForm.commentTitle.value = "";
	  annotationForm.comment.value = "";
	  
    var captureText = this.createNewAnnotation();
		
/*    if ( captureText ) {
    	if (!event) var event = window.event;
    	event.cancelBubble = true;
    	if (event.stopPropagation) event.stopPropagation();
    }
*/
    //dojo.event.browser.preventDefault();

    return false;
  },
  
  createNewAnnotation: function () {
    annotationConfig.rangeInfoObj = this.getRangeOfSelection();
    
    if (annotationConfig.rangeInfoObj == annotationConfig.excludeSelection) {
      alert("This area of text cannot be annotated.");
      getArticle();
      return false;
    }
    else if (!annotationConfig.rangeInfoObj) {
      alert("Using your mouse, select the area of the article you wish to annotate.");
      return false;
    }
    else {      
      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML += 
              "annotationConfig.rangeInfoObj.range.text = '"    + annotationConfig.rangeInfoObj.range.text + "'<br>" +
              "annotationConfig.rangeInfoObj.startPoint = "     + annotationConfig.rangeInfoObj.startPoint + "<br>" +
              "annotationConfig.rangeInfoObj.endPoint = "       + annotationConfig.rangeInfoObj.endPoint + "<br>" +
              "annotationConfig.rangeInfoObj.startParent = "    + annotationConfig.rangeInfoObj.startParent + "<br>" +
              "annotationConfig.rangeInfoObj.endParent = "      + annotationConfig.rangeInfoObj.endParent + "<br>" +
              "annotationConfig.rangeInfoObj.startParentId = "  + annotationConfig.rangeInfoObj.startParentId + "<br>" +
              "annotationConfig.rangeInfoObj.endParentId = "    + annotationConfig.rangeInfoObj.endParentId + "<br>" +
              "annotationConfig.rangeInfoObj.startXpath = "     + annotationConfig.rangeInfoObj.startXpath + "<br>" +
              "annotationConfig.rangeInfoObj.endXpath = "       + annotationConfig.rangeInfoObj.endXpath;
      }
      
      annotationForm.startPath.value = annotationConfig.rangeInfoObj.startXpath;
      annotationForm.startOffset.value = annotationConfig.rangeInfoObj.startPoint + 1;
      annotationForm.endPath.value = annotationConfig.rangeInfoObj.endXpath;
      annotationForm.endOffset.value = annotationConfig.rangeInfoObj.endPoint + 1;
     
      var mod = this.analyzeRange(annotationConfig.rangeInfoObj, 'span');
     
      if (mod == annotationConfig.excludeSelection) {
        alert("This area of text cannot be annotated.");
        getArticle();
        return false;
      }

      return true;
    }
  },
  
  getHTMLOfSelection: function () {
    var range;
    if (document.selection && document.selection.createRange) {
      range = document.selection.createRange();
      return this.getHTMLOfRange(range); //range.htmlText;
    }
    else if (window.getSelection) {
      var selection = window.getSelection();
      if (selection.rangeCount > 0) {
        range = selection.getRangeAt(0);
        return this.getHTMLOfRange(range); 
      }
      else {
        return '';
      }
    }
    else {
      return '';
    }
  },
  
  getHTMLOfRange: function (range) {
    if (document.selection && document.selection.createRange) {
      return range.htmlText;
    }
    else if (window.getSelection) {
      var clonedSelection = range.cloneContents();
      var div = document.createElement('div');
      div.appendChild(clonedSelection);
      return div.innerHTML;
    }
    else {
      return '';
    }
  },
  
  getRangeOfSelection: function () {
    var rangeInfo = new Object();

    if (document.selection && document.selection.createRange) {
      rangeInfo = this.findIeRange();
    
      return rangeInfo;
    }
    else if (window.getSelection || document.getSelection) {
      rangeInfo = this.findMozillaRange();
    
      return rangeInfo;
    }
    else {
      return false;
    }
  },

  analyzeRange: function (rangeInfo, element) {
    var startParent = rangeInfo.startParent;
    var endParent   = rangeInfo.endParent;
    var childList   = new Array();
    var html        = this.getHTMLOfSelection();
    
    if (startParent.hasChildNodes) {
      childList = this.getChildList(startParent, element); 
    }
  
    var mod = this.insertHighlightWrapper(rangeInfo);
    
    if (mod == annotationConfig.excludeSelection) {
      return annotationConfig.excludeSelection;
    }

  	var marker = dojo.byId(annotationConfig.regionalDialogMarker);
  	dlg.setMarker(marker);
    dlg.show();
  },

  findIeRange: function() {
    if (document.selection.type == "Text") {
      var range      = document.selection.createRange();
      var startRange = range.duplicate();
      var endRange   = startRange.duplicate();
      
      startRange.collapse(true);
      endRange.collapse(false);
  
      var startPoint = this.getRangePoint(startRange);
      if (startPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
      startRange.pasteHTML("<span id=\"tempStartPoint\" class=\"temp\"></span>");

      var endPoint = this.getRangePoint(endRange);
      if (endPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
        
      endRange.pasteHTML("<span id=\"tempEndPoint\" class=\"temp\"></span>");
        
      var isAncestor = this.isAncestorOf(startPoint.element, endPoint.element, "xpathLocation", endPoint.xpathLocation);
      
      if (isAncestor) {
        range.moveEnd("character", -1);
        endRange = range.duplicate();
        endRange.collapse(false);
        
        endPoint = this.getRangePoint(endRange);
        if (endPoint == annotationConfig.excludeSelection)
          return annotationConfig.excludeSelection;
      }
  
      if ( startPoint.element == null || endPoint.element == null ) {
        return null;
      }
      else {
  	    var startParent    = startPoint.element;
  	    var endParent      = endPoint.element;
  	    var startXpath     = startPoint.xpathLocation;
  	    var endXpath       = endPoint.xpathLocation;
  	    var startParentId  = startPoint.element.id;
  	    var endParentId    = endPoint.element.id;
  	    
        var ieRange = new Object();
        ieRange  = {range:          range,
                    startPoint:     startPoint.offset,
                    endPoint:       endPoint.offset,
                    startParent:    startParent,
                    endParent:      endParent,
                    startXpath:     startXpath,
                    endXpath:       endXpath,
                    startParentId:  startParentId,
                    endParentId:    endParentId,
                    selection:      null};
        
        return ieRange;
      }
    }
    else {
      return false;
    }
  },

  findMozillaRange: function() {
    var rangeSelection = window.getSelection ? window.getSelection() : 
                         document.getSelection ? document.getSelection() : 0;
                         
    if (rangeSelection != "" && rangeSelection != null) {
      if (djConfig.isDebug) 
        dojo.byId(djConfig.debugContainerId).innerHTML += "<br><br>Inside findMozillaRange";
      var startRange;
      
      if (typeof rangeSelection.getRangeAt != "undefined") {
         startRange = rangeSelection.getRangeAt(0);
      }
      else if (typeof rangeSelection.baseNode != "undefined") {
        if (djConfig.isDebug) {
          dojo.byId(djConfig.debugContainerId).innerHTML += 
                "rangeSelection.baseNode = '"     + rangeSelection.baseNode + "'<br>" +
                "rangeSelection.baseOffset = '"   + rangeSelection.baseOffset + "'<br>" +
                "rangeSelection.extentNode = '"   + rangeSelection.extentNode + "'<br>" +
                "rangeSelection.extentOffset  = " + rangeSelection.extentOffset ;
        }
        
        startRange = window.createRange ? window.createRange() :
                     document.createRange ? document.createRange() : 0;
        startRange.setStart(rangeSelection.baseNode, rangeSelection.baseOffset);
        startRange.setEnd(rangeSelection.extentNode, rangeSelection.extentOffset);
        
        if (startRange.collapsed) {
          startRange.setStart(rangeSelection.extentNode, rangeSelection.extentOffset);
          startRange.setEnd(rangeSelection.baseNode, rangeSelection.baseOffset);
        }
      }

      var endRange   = startRange.cloneRange();
      var range      = startRange.cloneRange();
      
      startRange.collapse(true);
      endRange.collapse(false);
  
      var tempNode = document.createElement("span");
      endRange.insertNode(tempNode);

      var endPoint       = this.getRangePoint(endRange);
      
      if (endPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
      var startPoint     = this.getRangePoint(startRange);
      
      if (startPoint == annotationConfig.excludeSelection)
        return annotationConfig.excludeSelection;
      
      range.setEndAfter(tempNode);
      tempNode.parentNode.removeChild(tempNode);

      if ( startPoint.element == null || endPoint.element == null ) {
        return null;
      }
      else {
  	    var startParent    = startPoint.element;
  	    var endParent      = endPoint.element;
  	    var startXpath     = startPoint.xpathLocation;
  	    var endXpath       = endPoint.xpathLocation;
  	    var startParentId  = startPoint.element.id;
  	    var endParentId    = endPoint.element.id;
   	    
        var mozRange = new Object();
        mozRange = {range:          range,
                    startPoint:     startPoint.offset,
                    endPoint:       endPoint.offset,
                    startParent:    startParent,
                    endParent:      endParent,
                    startXpath:     startXpath,
                    endXpath:       endXpath,
                    startParentId:  startParentId,
                    endParentId:    endParentId,
                    selection:      rangeSelection};    
                          
        return mozRange;
      }
    }
    else {
      return false;
    }
  },
  
  getRangePoint: function (range) {
    var POINT_SPAN_ID = "POINT_SPAN";
    
    if (range.pasteHTML) {
      range.pasteHTML("<span id=\"" + POINT_SPAN_ID + "\"></span>");
    }
    else {
      var ptSpan = document.createElement("span");
      ptSpan.id = POINT_SPAN_ID;
      range.insertNode(ptSpan);
    }

    var pointSpan = document.getElementById(POINT_SPAN_ID);

    var pointEl = this.getFirstAncestorByXpath(pointSpan);
    //alert("pointEl = " + pointEl.element.nodeName + ", " + pointEl.xpathLocation);
    
    if (pointEl == annotationConfig.excludeSelection) 
      return annotationConfig.excludeSelection;
    
   
    var point = new Object();
    point.element = pointEl.element;
    point.xpathLocation = pointEl.xpathLocation;
    point.offset = this.getPointOffset(pointSpan, pointEl);

    pointSpan.parentNode.removeChild(pointSpan);
    
    return point;
  },
  
  
  getPointOffset: function (obj, targetNode) {
    var offset = 0;
    var node = obj;
    
    for (var currentNode = node.previousSibling; currentNode != null; currentNode = currentNode.previousSibling) {
      if (currentNode.nodeType == 1) { // element
        if (currentNode.className.match('bug') || (dojo.render.html.ie && currentNode.className.match('temp'))) {
          // skip this
        }
        else {
          var normalText = this.normalizeText( currentNode, "");
          //alert("normalText = '" + normalText + "'\nlength = " + normalText.length + "\ncurrentNode = " + currentNode.nodeName + ", " + currentNode.name);
          offset += normalText.length;
        }
      }
      else if (currentNode.nodeType == 3) { // text
        //alert("currentNode = " + currentNode.nodeValue + "\nlength = " + currentNode.length);
        offset += currentNode.length;
      }
      else { // other
        // don't change the pointIndex
      }
    }
    
    if (targetNode) {
      var objParent = obj.parentNode;
      
      //objParent.getAttributeNode('xpathLocation').nodeValue != targetNode.getAttributeNode('targetNode').nodeValue
      
      if (objParent.getAttributeNode('xpathLocation') == null) {
        //alert("objParent = " + objParent.nodeName);
        offset += this.getPointOffset(objParent, targetNode);
      }
    }
    
    //alert("offset = " + offset);
    return offset;    
  },

  getAncestors: function ( selfNode, lastAncestorId ) {
    var familyTree = new Array();
    //familyTree.push(selfNode);
    var parentalNode = selfNode;
    
    while ( parentalNode.id != lastAncestorId ) {
      parentalNode = parentalNode.parentNode;
      
      var nodeObj = new Object();
      nodeObj.element = parentalNode;
      nodeObj.id = parentalNode.id;
      nodeObj.name = parentalNode.name;
      nodeObj.xpathLocation = (parentalNode.getAttributeNode("xpathLocation")) ? parentalNode.getAttributeNode("xpathLocation").nodeValue : "";
      familyTree.push(nodeObj);
    }
    
    return familyTree;
  },
  
  isAncestorOf: function ( srcNode, compareNode, attributeName, attributeValue ) {
    var familyTree = this.getAncestors(srcNode, annotationConfig.lastAncestor);
    var parentalNode = srcNode;
    
    for (var i=0; i<familyTree.length; i++) {
      if (familyTree[i][attributeName] == attributeValue) {
        return true;
      }
    }
    
    return false;
  },
  
  getFirstAncestorByXpath: function ( selfNode ) {
    var parentalNode = selfNode;

   {
    try {
      if (parentalNode.getAttributeNode('xpathLocation') == null) {
        for (var pNode=parentalNode; pNode.getAttributeNode('xpathLocation') == null; pNode = pNode.parentNode) {
          //alert("pNode = " + pNode.nodeName);
          parentalNode = pNode;
        }
        //alert("parentalNode [before] = " + parentalNode.nodeName);
        parentalNode = parentalNode.parentNode;
        //alert("parentalNode [after] = " + parentalNode.nodeName);
        
        //alert("parentalNode.nodeName = " + parentalNode.nodeType);
        if (parentalNode.getAttributeNode('xpathLocation') != null && parentalNode.getAttributeNode('xpathLocation').nodeValue == annotationConfig.excludeSelection) {
          //alert("getFirstAncestorByXpath: noSelect");
          return annotationConfig.excludeSelection;
        }
        else if (parentalNode.nodeName == 'BODY') {
          return annotationConfig.excludeSelection;
        }
      }
          
      //alert("parentalNode.getAttributeNode('xpathLocation').nodeValue = " + parentalNode.getAttributeNode('xpathLocation').nodeValue + ", " + parentalNode.getAttributeNode('xpathLocation').nodeValue.length);

      var parentObj = new Object();
      parentObj.element = ( parentalNode.getAttributeNode('xpathLocation')!= null & parentalNode.getAttributeNode('xpathLocation').nodeValue  == annotationConfig.excludeSelection ) ? null : parentalNode;
      parentObj.xpathLocation = (parentalNode.getAttributeNode('xpathLocation') != null) ? parentalNode.getAttributeNode('xpathLocation').nodeValue : "";
      return parentObj;
    }
    catch(err) {
      //txt="There was an error on this page.\n\n";
      //txt+="Error description: [getFirstAncestorByXpath] " + err.description + "\n\n";
      //txt+="Click OK to continue.\n\n";
      //alert(txt);
    }
   }
   
   return false;
  },
  
  getChildList: function (obj, element, elName) {
    var childSearch = obj.getElementsByTagName(element);
    
    var childList = new Array();
    for (var i=0; i<childSearch.length; i++) {
      var tmpText = this.normalizeText(childSearch[i], '');
      var startOffset = this.getPointOffset(childSearch[i]);
      var endOffset = tmpText.length;
      
      var listIndex = childList.length;
      //childList[i] = new Object();
      childList[childList.length] = {startOffset: startOffset,
                                     endOffset:   endOffset};
      //alert("tmpText = " + tmpText + "\n" +
      //      "childList[" + listIndex + "] = startOffset: " + childList[listIndex].startOffset + ", endOffset: " + childList[listIndex].endOffset);
    }
    
    
    return childList;
    
  },

  insertHighlightWrapper: function (rangeObj) {
  	var noteClass = annotationConfig.annotationMarker + " " + 
    //      					(annotationConfig.isAuthor ? "author-" : "self-") +
          					(annotationConfig.isPublic ? "public" : "private") +
          					"-active";
  	var noteTitle = (annotationConfig.isAuthor ? "Author" : annotationConfig.isPublic ? "User" : "My") + 
          					" Annotation " + 
          					(annotationConfig.isPublic ? "(Public)" : "(Private)");
  	var markerId     = annotationConfig.regionalDialogMarker;
  	var noteImg   = djConfig.namespace + "/images/" + "pone_note_" + (annotationConfig.isAuthor ? "author" : "private") + "_active.gif";
  	var noteImgClass = annotationConfig.annotationImgMarker;
    var contents = document.createDocumentFragment();
  	  
    // create a new span and insert it into the range in place of the original content
    var newSpan = document.createElement('span');
    newSpan.className = noteClass;
    newSpan.title     = noteTitle;
    //newSpan.id        = markerId;
    newSpan.annotationId = "";

	  var newImg = document.createElement('img');
	  newImg.src       = noteImg;
	  newImg.title     = noteTitle;
	  newImg.className = noteImgClass;
    
    //newSpan.appendChild(newImg);
    
	  var link = document.createElement("a");
	  link.className = 'bug public';
	  //link.href = '#';
	  //link.id = markerId;
	  link.title = 'Click to preview this annotation';
	  link.displayId = "";
	  link.onclick = function() { topaz.displayComment.show(this); }
	  link.onmouseover = function() { topaz.displayComment.mouseoverComment(this); }
	  link.onmouseout = function() { topaz.displayComment.mouseoutComment(this); }
	  link.appendChild(document.createTextNode('1'));

    // Insertion for IE
    if (rangeObj.range.pasteHTML) {
      var html = rangeObj.range.htmlText;
/*      rangeObj.range.pasteHTML('<span class="' + noteClass + 
          								     '" title="'     + noteTitle +
          							        '"  annotationId=""' +
          							       '">' + 
                               '<a href="#" class="bug public" id="' + markerId + 
                               '"  onclick="topaz.displayComment.show(this);"' + 
                               ' onmouseover="topaz.displayComment.mouseoverComment(this);"' + 
                               ' onmouseout="topaz.displayComment.mouseoutComment(this);"' + 
                               ' title="Click to preview this annotation">1</a>' +
          							       html + '</span>');
*/
      var tempNode = document.createElement("div");
      tempNode.innerHTML = html;
      dojo.dom.copyChildren(tempNode, contents);
  
        
      var modContents = this.modifySelection(rangeObj, contents, newSpan, link, markerId);
      
      if (modContents == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML += "<br><br>============================ Modified Content ==================" 
        dojo.byId(djConfig.debugContainerId).appendChild(modContents.cloneNode(true));
      }
      
      if (modContents.hasChildNodes()) {  
        dojo.dom.removeChildren(tempNode);
        dojo.dom.copyChildren(modContents, tempNode);
        rangeObj.range.pasteHTML(tempNode.innerHTML);
      }
      
/*      var startPoint = dojo.byId("tempStartPoint");
      if (!modContents.hasChildNodes()) {
        for (var currentNode = startPoint.nextSibling; currentNode != null; currentNode = currentNode.nextSibling) {
          dojo.dom.removeNode(currentNode);
        }
      }
      dojo.dom.removeNode(startPoint);
      
      var endPoint = dojo.byId("tempEndPoint");
      if (!modContents.hasChildNodes()) {
        for (var currentNode = endPoint.previousSibling; currentNode != null; currentNode = currentNode.previousSibling) {
          dojo.dom.removeNode(currentNode);
        }
        
        document.selection.empty();
      }
      dojo.dom.removeNode(endPoint);
*/
      var startPoint = dojo.byId("tempStartPoint");
      dojo.dom.removeNode(startPoint);
      var endPoint = dojo.byId("tempEndPoint");
      dojo.dom.removeNode(endPoint);
      document.selection.empty();
    }
    else {
      if (dojo.render.html.safari) {  //Insertion for Safari
          contents = rangeObj.range.cloneContents();
          rangeObj.range.deleteContents();
      }
      else {  // Insertion for Firefox
        contents = rangeObj.range.extractContents();
      }

      //if (djConfig.isDebug) 
      //  dojo.byId(djConfig.debugContainerId).innerHTML += "<br><br>======================== Calling modifySelection ===========================";
      
      var modContents = this.modifySelection(rangeObj, contents, newSpan, link, markerId);

      if (modContents == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }

      rangeObj.range.insertNode(modContents);
      return;
    }
  },
  
  modifySelection: function(rangeObj, contents, newSpan, link, markerId) {
    if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML += "<br><br>=========== Inside modifySelelection ============================";
    }
        
    var modContents = document.createDocumentFragment();

    if (rangeObj.startXpath == rangeObj.endXpath) {
      modContents.appendChild(this.insertWrapper(rangeObj, contents, newSpan, link, markerId, null));
    }
    else {
      modContents = this.modifyMultiSelection(rangeObj, contents, newSpan, link, markerId);    
    }
    
    return modContents;
  },
  
  modifyMultiSelection: function(rangeObj, contents, newSpan, link, markerId) {
    var modContents = document.createDocumentFragment();
    var multiContent = contents.childNodes;
    
    //var xpathloc;
    for (var i=0; i < multiContent.length; i++) {
      // If the node is a text node and the value is either a linefeed and/or carriage return, skip this loop
      if (multiContent[i].nodeName == "#text" && (multiContent[i].nodeValue.match(new RegExp("\n")) || multiContent[i].nodeValue.match(new RegExp("\r")))) {
        continue;
      }


      var xpathloc = (multiContent[i].getAttribute) ? multiContent[i].getAttribute("xpathlocation") : null;
      var insertMode = 0;
      
      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML +=
              "<br><br>=============== MODIFYMULTISELECTION ================================="
              + "<br>" + "[MODIFYMULTISELECTION] i = " + i
              + "<br>" + "node = " + multiContent[i].nodeName + ", " + xpathloc + ", " + multiContent[i].nodeValue 
              + "<br>" + "multiContent.length = " + multiContent.length
              ;
      }
                  
      if (xpathloc != null) {
        if (xpathloc == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;  
        }
        else if (xpathloc == rangeObj.startXpath || xpathloc == rangeObj.endXpath) {
  				var parentEl = null;
  			
  				if (xpathloc == rangeObj.startXpath) {
  					parentEl = rangeObj.startParent.nodeName;
  				}
  				else if (xpathloc == rangeObj.endXpath) {
  					parentEl = rangeObj.endParent.nodeName;
  				}
          var xpathMatch = document.getElementsByAttributeValue(parentEl, "xpathlocation", xpathloc);
  
          if (xpathMatch != null && xpathMatch.length > 0) {
            if (i == 0) {
              insertMode = -1;
            }
            else {
              insertMode = 1;
            }
          }
        }
      }

      var doesChildrenContainXpath = this.isContainXpath(multiContent[i], insertMode);
      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML +=
              "<br>" + "doesChildrenContainXpath = " + doesChildrenContainXpath 
              ;
      }
      
      if (doesChildrenContainXpath == annotationConfig.excludeSelection) {
        return annotationConfig.excludeSelection;
      }
      else if (doesChildrenContainXpath == "true") {
        var newContent = this.modifyMultiSelection(rangeObj, multiContent[i], newSpan, link, markerId);
        
        if (newContent == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;
        }
        
        modContents.appendChild(newContent);
      }
      else if (multiContent[i].hasChildNodes()){
        var modFragment = this.insertWrapper(rangeObj, multiContent[i], newSpan, link, markerId, insertMode);
        
        if (dojo.render.html.ie) {
          if (insertMode == 0) {
            modContents.appendChild(modFragment);
            --i;
          }
        }
        else {
          modContents.appendChild(modFragment);
          --i;
        }
      }
    }
    
    //if (dojo.render.html.ie && multiContent.length == 2)
      //modContents = null;
    
    return modContents;
  },
  
  isContainXpath: function(node, multiPosition) {
    var xpathAttr = topaz.domUtil.isChildContainAttributeValue(node, "xpathlocation", null);
    if (djConfig.isDebug) {
      dojo.byId(djConfig.debugContainerId).innerHTML +=
            "<br>" + "xpathAttr.length = " + xpathAttr.length 
            ;
    }
    
    
    if (xpathAttr.length == 0) {
      return "false";
    }
    else {
      var start = 0;
      var arrayLength = xpathAttr.length;
      
      if (multiPosition == -1) {
        start = 1;
      }
      else if (multiPosition == 1) {
        arrayLength -= 1;
      }
      
      for (var i=start; i<arrayLength; i++) {  //exclude first and last node
        if (xpathAttr[i].value == annotationConfig.excludeSelection) {
          return annotationConfig.excludeSelection;
        }
      }
      
      return "true";
    }
  },
      
  /** 
   *  function insertWrapper(rangeObject, rangeContent, refWrapperNode, linkObject, markerId, multiPosition)
   *
   *  Inner function to process selection and place the appropriate markers for displaying the selection 
   *  highlight and annotation dialog box.
   *  
   *  @param rangeObject      Range object         Range object containing the range and additional xpath information.
   *  @param rangeContent     Document fragment    Extracted html from the selection contained in either a block node
   *                                               or a document fragment.
   *  @param refWrapperNode   Node object          Reference to the node object, which contains the correct 
   *                                                attributes, that will contain the selection.
   *  @param linkObject       Node oject           Link node object containing the annotation bug and the marker used
   *                                                to position dialog box.
   *  @param markerId         String               Marker ID string.
   *  @param multiPosition    Number               Numerical indication of a multiple selection.
   *                                                 null = Not a multiple selection
   *                                                   -1 = The first container of a multi-selection
   *                                                    0 = The middle container(s) of a multi-selection
   *                                                    1 = The last container of a multi-selection
   *  @param xpath            String               Value of xpathlocation node attribute.  Used for second passes for 
   *                                                the first and last container of a multiple selection.
   * 
   *  @return rangeContent
  */ 
  insertWrapper: function(rangeObject, rangeContent, refWrapperNode, linkObject, markerId, multiPosition, elXpathValue) {
    var childContents = rangeContent.childNodes;  
    var nodelistLength = childContents.length;
    var insertIndex = 0;
    var nodesToRemove = new Array();
    var indexFound = null;
    var startTime = new Date();
    var ieDocFrag = document.createDocumentFragment();
    
    if (dojo.render.html.safari) {
      if (multiPosition == null) {
        nodelistLength = childContents.length - 1;
      }
      else if (multiPosition == 1) {
        //nodelistLength = childContents.length + 1;
      } 
    }
    
    // populate the span with the content extracted from the range
    for (var i = 0; i < nodelistLength; i++) {
      var xpathloc = (childContents[i].getAttribute) ? childContents[i].getAttribute("xpathlocation") : null;
      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML +=
              "<br><br>=============== INSERTWRAPPER ================================="
              + "<br>" + "node = " + childContents[i].nodeName + ", " + xpathloc + ", " + childContents[i].nodeValue 
              + "<br>" + "multiPosition = " + multiPosition
              + "<br>" + "i = " + i
              + "<br>" + "childContents[" + i + "].nodeName = " + childContents[i].nodeName
              + "<br>" + "childContents[" + i + "].nodeType = " + childContents[i].nodeType
              + "<br>" + "childContents[" + i + "].className = " + childContents[i].className
              + "<br>" + "childContents[" + i + "].hasChildNodes = " + childContents[i].hasChildNodes()
              + "<br>"
             ;
      }
      
      // If the node is a text node and the value is either a linefeed and/or carriage return, skip this loop
      if (childContents[i].nodeName == "#text" && (childContents[i].nodeValue.match(new RegExp("\n")) || childContents[i].nodeValue.match(new RegExp("\r")))) {
        continue;
      }

      var spanToInsert;
      var existingNode = childContents[i];
      
      // modify the existing span
      if (existingNode.nodeName == "SPAN" && topaz.domUtil.isClassNameExist(existingNode, "note")) {
        spanToInsert = existingNode.cloneNode(true);
        spanToInsert.className = refWrapperNode.getAttributeNode("class").nodeValue;
      }
      // wrap in a new span
      else {
        spanToInsert = refWrapperNode.cloneNode(true);
        spanToInsert.appendChild(existingNode.cloneNode(true));
      }

      // insert the marker ID and bug
      if (i == 0 && (multiPosition == null || multiPosition == -1)) {
        if (dojo.render.html.ie) {
          linkObject.setAttribute("id", markerId);
        }
        else {
          spanToInsert.setAttribute("id", markerId);
        }

        dojo.dom.insertBefore(linkObject, spanToInsert.firstChild, false);
      }

      // insert into the range content before the existing node (the existing node will be deleted, leaving only the new one)
      if (multiPosition == null || multiPosition == 0) {
        dojo.dom.replaceNode(existingNode, spanToInsert);
      }
      // insert into the document 
      else {
        var elXpathValue = rangeContent.getAttributeNode("xpathlocation").nodeValue;
				var parentEl = null;
				
				if (multiPosition == -1) {
					parentEl = rangeObject.startParent.nodeName;
				}
				else if (multiPosition == 1) {
					parentEl = rangeObject.endParent.nodeName;
				}
				
        var elements = document.getElementsByAttributeValue(parentEl, "xpathlocation", elXpathValue);

        if (elements.length > 0) {
          var tempPointStart = dojo.byId("tempStartPoint");
          var tempPointEnd = dojo.byId("tempEndPoint");
          
          if (multiPosition < 0) {
            if (tempPointStart && tempPointStart != null) {
              dojo.dom.insertBefore(spanToInsert, tempPointStart);
              dojo.dom.removeNode(tempPointStart.nextSibling);
            }
            else {
              elements[elements.length-1].appendChild(spanToInsert);
            }
          }
          else {
            var elToInsert = document.createDocumentFragment();
            if (dojo.render.html.safari && multiPosition == 1 && i == (childContents.length-1)) {
              dojo.dom.copyChildren(spanToInsert, elToInsert);
            }
            else {
              elToInsert = spanToInsert;
            }

            if (dojo.render.html.ie && insertIndex == 0) {
              ieDocFrag.appendChild(elToInsert);
              dojo.dom.removeNode(tempPointEnd.previousSibling);
            }
            else {
              elements[elements.length-1].insertBefore(elToInsert, elements[elements.length-1].childNodes[insertIndex]);
              ++insertIndex;
            }            
          }
        }
       
        nodesToRemove.push(existingNode);
      } 
      
    }
    
    if (dojo.render.html.ie && multiPosition == 1) {
      dojo.dom.insertAfter(ieDocFrag, tempPointEnd);
      return;
    }     
   
    // remove the existing node from the range content
    if (nodesToRemove.length > 0) {
      for (var i = 0; i < nodesToRemove.length; i++) {
        dojo.dom.removeNode(nodesToRemove[i]);
      }
    }   
   

    var endTime = new Date();
    
    if (djConfig.isDebug) {
      dojo.byId(djConfig.debugContainerId).innerHTML += 
        "<br><br>" 
  //      + "Start time: " + startTime.getTime()
  //      + "<br>End Time: " + endTime.getTime()
        + "<br>Duration: " + (endTime.getTime() - startTime.getTime() + "ms");
      ;
    } 
    
      return rangeContent;
  },

  normalizeText: function ( documentObj, resultStr ) {
    var tempStr = resultStr;
    
    for (var i=0; i<documentObj.childNodes.length; i++) {
      if (documentObj.childNodes[i].nodeType == 1) {
        if (documentObj.childNodes[i].className.match('bug')) {
          // skip this
        }
        else {
          tempStr = tempStr + this.normalizeText(documentObj.childNodes[i], '');
        }
      }
      else if (documentObj.childNodes[i].nodeType == 3) {
        tempStr = tempStr + documentObj.childNodes[i].nodeValue;
      }
    }
    
    return tempStr;
  }
}







