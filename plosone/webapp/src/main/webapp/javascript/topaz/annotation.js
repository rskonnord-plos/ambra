dojo.provide("topaz.annotation");

/**
  * topaz.annotation
  *
  * This object takes a selection that has been made on screen and locates the XPath and 
  * offset for the start and end of the selection.  It then takes the selection and wraps 
  * it in span tags specifically used for highlighting the text.  The selection is 
  * deselected when the annotation dialog box appears but is replaced with the spans.  
  * This is triggered by the user clicking on the "Add your annotation" in the right hand 
  * column on the article page.
  * 
  * @author  Joycelyn Chung			joycelyn@orangetowers.com
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

  /** 
   * topaz.annotation._createAnnotationOnkeyup(event)
   * 
	 * Method triggered when the event is tied to the document and the user presses a key.
	 * If the key pressed is ENTER, creates an annotation using the currently-selected text.
	 * Parameter 'event' is a key press event.
	 * 
   * @param  event      Event object         Event triggered by the keypress of the "ENTER" 
   * 																				  button.
   * 
	 * @return true														 Success.
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
  
  /** 
   * topaz.annotation.createAnnotationOnMouseDown()
   * 
   * Method triggered on onmousedown or onclick event of a tag.  When this method is 
   * triggered, it initiates an annotation creation using the currently-selected text.
	 * 
	 * @return true														 Success.
   */
  createAnnotationOnMouseDown: function () {
	  topaz.formUtil.textCues.reset(_commentTitle, _titleCue); 
	  topaz.formUtil.textCues.reset(_comments, _commentCue); 
	  _annotationForm.commentTitle.value = "";
	  _annotationForm.comment.value = "";
	  
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
  
  /** 
   * topaz.annotation.createNewAnnotation()
   * 
   * Method that takes in the selection that was made and sends it to getRangeOfSelection to 
   * figure out the XPath and offset of the selection.  An object is returned with the text of 
   * the selection, the start and end points, the start and end xpath, and the start and end 
   * parent node and the nodes' ids.  This information is then sent to have the spans placed 
   * around the selection.  If getRangeOfSelection or analyzeRange() returns "noSelect", that 
   * indicates that the user have selected in a region of the page that is not selectable.  
   * createNewAnnotation() then returns the appropriate message.  If getRangeOfSelection returns 
   * null, that indicates that the user have not made a selection but have clicked on the "Add 
   * your annotation" link.  createNewAnnotation() then returns the appropriate message.
   * Otherwise createNewAnnotation() return true;
	 * 
	 * @return null														No selection was made.
	 * @return false													A non-selectable region was selected.
	 * @return true														Success.
   */
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
      
      _annotationForm.startPath.value = annotationConfig.rangeInfoObj.startXpath;
      _annotationForm.startOffset.value = annotationConfig.rangeInfoObj.startPoint + 1;
      _annotationForm.endPath.value = annotationConfig.rangeInfoObj.endXpath;
      _annotationForm.endOffset.value = annotationConfig.rangeInfoObj.endPoint + 1;
     
      var mod = this.analyzeRange(annotationConfig.rangeInfoObj);
     
      if (mod == annotationConfig.excludeSelection) {
        alert("This area of text cannot be annotated.");
        getArticle();
        return false;
      }

      return true;
    }
  },
  
  /** 
   * topaz.annotation.getHTMLOfSelection()
   * 
   * Method returns the html markup of the selection.
   * 
	 * @return ""													  	No html fragment available.
	 * @return html		Document fragment 			Success.
   */
  getHTMLOfSelection: function () {
    var range;
    
    // IE
    if (document.selection && document.selection.createRange) {
      range = document.selection.createRange();
      return this.getHTMLOfRange(range); //range.htmlText;
    }
    // Mozilla
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
  
  /** 
   * topaz.annotation.getHTMLOfRange()
   * 
   * Method takes in the range object and returns the html markup for the selection.
   * 
	 * @return ""													  	No html fragment available.
	 * @return html		Document fragment 			Success.
   */
  getHTMLOfRange: function (range) {
  	// IE
    if (document.selection && document.selection.createRange) {
      return range.htmlText;
    }
    // Mozilla
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
  
  /**
   * topaz.annotation.getRangeOfSelection()
   * 
   * Method determines which selection object that the user's browser recognizes and 
   * forwards to the appropriate method that will the range.
   * 
	 * @return false													Failure.
	 * @return rangeInfo			Object				 	Object containing the range and the start and 
	 * 																				 end point offsets, parent elements, xpaths, 
	 * 																				 parent ID, and the selection.
   */
  getRangeOfSelection: function () {
    var rangeInfo = new Object();

		// IE
    if (document.selection && document.selection.createRange) {
      rangeInfo = this.findIeRange();
    
      return rangeInfo;
    }
    // Mozilla
    else if (window.getSelection || document.getSelection) {
      rangeInfo = this.findMozillaRange();
    
      return rangeInfo;
    }
    else {
      return false;
    }
  },

	/**
	 * topaz.annotation.analyzeRange(Object rangeInfo)
	 * 
	 * This method takes in the rangeInfo object and passes it to insertHighlightWrapper().
	 * If insertHighlightWrapper() returns with "noSelect", this method returns "noSelect".
	 * Otherwise, the marker for the regionalDialog is set and show() is called on it to 
	 * display the dialog box.
	 * 
	 * @param	 rangeInfo			Object					Object containing the range and the start and 
	 * 																				 end point offsets, parent elements, xpaths, 
	 * 																				 parent ID, and the selection.
	 *
	 * @return "noSelect"			text 				 	  Non-selectable area was selected.
	 * @return <nothing>											Success. 
	 */
  analyzeRange: function (rangeInfo) {
    var mod = this.insertHighlightWrapper(rangeInfo);
    
    if (mod == annotationConfig.excludeSelection) {
      return annotationConfig.excludeSelection;
    }

  	var marker = dojo.byId(annotationConfig.regionalDialogMarker);
  	_dlg.setMarker(marker);
    _dlg.show();
  },

	/**
	 * topaz.annotation.findIeRange()
	 * 
   * This method is only valid for Internet Explorer because the selection and range 
   * object is different for this browser.
   * 
   * Method first checks to make sure a selection was made and is of type text.  If 
   * it's not, false is returned.
   * 
   * It then locates the selection and sets up the range.  This range is then 
   * duplicated twice.  One is collapsed to the beginning of the range and is used 
   * as the start of range reference.  The other duplicate range is collapsed to the
   * end and is used as the end of range reference.  These two references are then 
   * passed to getRangePoint(<range>) method.  If getRangePoint returns "noSelect",
   * that value is returned by this method also.
   * 
   * Once the points are found, a temporary span is placed at the beginning and end
   * of the range.  These temporary spans are needed when the actual highlighting 
   * spans are wrapped around the range.  This is explained further in 
   * insertHighlightWrapper().  The start and end points and the xpathLocation for 
   * the end point is passed into isAncestorOf to check if the the xpathLocation
   * for the end point is an ancestor of the start point.  If it is, it means that the
   * user probably selected to the end of a paragraph, for example.  In IE, when the 
   * user selects to the end of a container, the end pointer is usually outside the
   * container and not in the next sibling container.  When this happens, the parent 
   * container is the parent of the parent container of the start point.  In this
   * case, the range is moved at the end back by one character so the endpoint will be
   * within the container the selection is actually in.
   * 
   * If neither start or end points are found, this method returns a null.  Otherwise, 
   * the range and the start and end point offsets, parent elements, xpaths, parent ID,
   * and the selection are placed into an object that stores these values.  This new
   * object is then returned.
	 * 
	 * @return null														No selection was made.
	 * @return "noSelect"			text 				 	  Non-selectable area was selected.
	 * @return false					boolean					Failure.
	 * @return ieRange				Object					Object containing the range and the start and 
	 * 																				 end point offsets, parent elements, xpaths, 
	 * 																				 parent ID, and the selection. 
	 */
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

	/**
	 * topaz.annotation.findMozillaRange()
	 * 
   * This method works for many Gecko based browsers such as Firefox and Safari.
   * 
   * Method first locates the selection.  It then checks to make sure a selection was 
   * made.  If it's empty or null, false is returned.  
   * 
   * It then checks to see if the getRangeAt() method exists.  If it doesn't, that means
   * the browser doesn't support getRangeAt which appears to occur in Safari.  In Safari,
   * you have to set the range manually.
   * 
   * Once the range is set, it is then cloned twice.  One of the cloned range is collapsed
   * to the beginning of the range.  The other cloned range is collapsed to the end of the
   * range.  Both of these collapsed ranges will be used as references to mark the start 
   * and end of the selection.  Then each of these are sent to the getRangePoint(<range>)
   * method.  If getRangePoint returns "noSelect", that value is returned by this method 
   * also.  The temporary spans are then removed.
   * 
   * If neither start or end points are found, this method returns a null.  Otherwise, 
   * the range and the start and end point offsets, parent elements, xpaths, parent ID,
   * and the selection are placed into an object that stores these values.  This new
   * object is then returned.
   * 
	 * @return null														No selection was made.
	 * @return "noSelect"			text 				 	  Non-selectable area was selected.
	 * @return false					boolean					Failure.
	 * @return mozRange				Object					Object containing the range and the start and 
	 * 																				 end point offsets, parent elements, xpaths, 
	 * 																				 parent ID, and the selection. 
	 */
  findMozillaRange: function() {
    var rangeSelection = window.getSelection ? window.getSelection() : 
                         document.getSelection ? document.getSelection() : 0;
                         
    if (rangeSelection != "" && rangeSelection != null) {
      if (djConfig.isDebug) 
        dojo.byId(djConfig.debugContainerId).innerHTML += "<br><br>Inside findMozillaRange";
      var startRange;
      
      // Firefox
      if (typeof rangeSelection.getRangeAt != "undefined") {
         startRange = rangeSelection.getRangeAt(0);
      }
      // Safari
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
  
	/**
	 * topaz.annotation.getRangePoint(Object range)
	 * 
	 * This method takes in the range object that has been collapsed.  A temporary span is 
	 * created with an id of "POINT_SPAN".  The temporary span is then inserted into the range.
	 * When the span is inserted back into the range, it is then within the context of the document
	 * object.  So it's necessary to look for the temporary span again based on the id to use as a
	 * marker.  Using getFirstAncestorByXpath(), the parent element that has a xpathlocation is 
	 * located.  If it returns "noSelect", that in turn is returned.  Otherwise, the offset of the 
	 * marker from the parent element is calculated.  An object is created where the parent element
	 * node, xpathlocation, and the offset is set called point.  The temporary span is removed.  The 
	 * point is the return value.
	 * 
	 * @param  range					Range object		Collapsed range.
	 * 
	 * @return "noSelect"			text 				 	  Non-selectable area was selected.
	 * @return point				  Object					Object containing the parent element, the parent element
	 * 																				 xpathlocation attribute value, and the offset from the
	 * 																				 parent.
	 */
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
  
  /**
   * topaz.annotation.getPointOffset(Node sourceNode, Node targetNode)
   * 
   * Method takes in the sourceNode from which the offset is counted from and the targetNode
   * where the counting ends.  Recursively looping through the sourceNode's previousSibling,
   * element nodes that does not contain the classname "bug", which indicates an existing
   * annotation span, or, if in IE, classname of "temp", are normalized.  The normalized 
   * text of the element nodes and preceeding siblings that are text nodes are counted and 
   * added to the running total named offset.
   * 
   * If the targetNode was not passed in, the count ends when the loop reaches the first 
   * child node.  If the targetNode exists and the parent node of the source node does not
   * contain the xpathlocation attribute, the parent of the sourceNode and the targetNode
   * gets sent into this method again.  The total from which is added to the running total.
   * Offset is the returning value.  
   * 
	 * @param  sourceNode			Node object 		Origin from which to start the count.
	 * @param	 targetNode			Node object 		If passed in, counting continues until this node
	 * 																				 has been reached.
	 * 
	 * @return offset					Integer					Count of the text and normalized text between the
	 * 																				 sourceNode and targetNode. 
   */
  getPointOffset: function (sourceNode, targetNode) {
    var offset = 0;
    var node = sourceNode;
    
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
        // don't change the offset
      }
    }
    
    if (targetNode) {
      var nodeParent = sourceNode.parentNode;
      
      if (nodeParent.getAttributeNode('xpathLocation') == null) {
        offset += this.getPointOffset(nodeParent, targetNode);
      }
    }
    
    //alert("offset = " + offset);
    return offset;    
  },

  /**
   * topaz.annotation.getAncestors(Node sourceNode, text lastAncestorId)
   * 
   * This method traverses up the node tree until it finds the parent where the id matches 
   * lastAncestorId collecting an array of the parents at each level of the node tree.
   * 
   * @param		sourceNode			Node object 	Source node from which the traversal begins.
   * @param	  lastAncestorId	String					Id of the last parent.
   * 
   * @return	familyTree			Array					Collection of parents at each node level and in
   * 																				 the path of the traversal from the sourceNode 
   * 																				 to the parent with the id matching lastAncestorId.
   */
  getAncestors: function ( sourceNode, lastAncestorId ) {
    var familyTree = new Array();
    //familyTree.push(selfNode);
    var parentalNode = sourceNode;
    
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
  
  /**
   * topaz.annotation.isAncestorOf(Node sourceNode, text attributeName, text attributeValue)
   * 
   * Method takes in a sourceNode and an attribute and it's value to match.  First it finds all the 
   * ancestors of the sourceNode up to the last ancestor of the annotation section.  As it recurses 
   * through the familyTree, it looks to see if one of the ancestors have the attributeName and 
   * attributeValue.
   * 
   * @param		sourceNode			Node object					Node used as the source.
   * @param		attributeName		String							Name of attribute used for matching.
   * @param		attributeValue	String							Value of the attribute to match.
   * 
   * @return  false						boolean							Failed to find a match.
   * @return  true						boolean							Match found.
   */
  isAncestorOf: function ( sourceNode, attributeName, attributeValue ) {
    var familyTree = this.getAncestors(sourceNode, annotationConfig.lastAncestor);
    var parentalNode = sourceNode;
    
    for (var i=0; i<familyTree.length; i++) {
      if (familyTree[i][attributeName] == attributeValue) {
        return true;
      }
    }
    
    return false;
  },
  
  /**
   * topaz.annotation.getFirstAncestorByXpath(Node sourceNode)
   * 
   * Method traverses up the node tree looking for the first parent that contains the 
   * xpathlocation attribute.  When a parent node is found with the correct attribute,
   * the parent element and the xpathlocation value is stored in a parentObj object.
   * 
   * @param		sourceNode		Node object					Node source to begin the search.
   * 
   * @return	"noSelect"		String							Non-selectable area has been selected.
   * @return	false					boolean							Failed search.
   * @return	parentObj			Object							Object that stores the parent element
   * 																						 and its xpathlocation value.
   */
  getFirstAncestorByXpath: function ( sourceNode ) {
    var parentalNode = sourceNode;

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
  
  /**
   * topaz.annotation.getChildList(Object parentObj, Node element)
   * 
   * Method takes in the parent object and the element node to search for.  A search is
   * made on the parent object to see if it has any child that matches that specific element
   * node.  A list of the child nodes are returned.  Based on this list, the text within the
   * child node is normalized.  The normalized text is used to calculate the end offset of the
   * child.  The start offset of the child is sent to getPointOffset to calculate the offset.
   * Both information is placed into an array of objects called childList.
   * 
   * @param		parentNode			Object					Source node for searching.
   * @param		element					Node object			Element to search for in the children of the 
   * 																					 parentNode.
   * 
   * @return	childList				Array						Collection of childnodes and their start and end
   * 																					 end offsets from the parent.
   */
  getChildList: function (parentNode, element) {
    var childSearch = parentNode.getElementsByTagName(element);
    
    var childList = new Array();
    for (var i=0; i<childSearch.length; i++) {
      var tmpText = this.normalizeText(childSearch[i], '');
      var startOffset = this.getPointOffset(childSearch[i]);
      var endOffset = tmpText.length;
      
      childList[childList.length] = {startOffset: startOffset,
                                     endOffset:   endOffset};
      //alert("tmpText = " + tmpText + "\n" +
      //      "childList[" + listIndex + "] = startOffset: " + childList[listIndex].startOffset + ", endOffset: " + childList[listIndex].endOffset);
    }
    
    
    return childList;
    
  },

	/**
	 * topaz.annotation.insertHighlightWrapper(Object rangeObj)
	 * 
	 * This method creates a span tag with all the attribute set to highlight some text.
	 * It also creates a linked image to be used at the beginning of the selection.  Both 
	 * of these elements are used as templates and are passed into modifySelection() along 
	 * with the html fragment of the selection and an id that marks the beginning of the 
	 * selection.  The modifySelection() method then returns the modified html fragment with
	 * the spans that allows it to be highlighted are put back into the document context.
	 * 
	 * The method for extracting the html fragment is straightforward in Firefox.  In IE, 
	 * however, the range object has a method for getting the html fragment of the selection 
	 * but it's read-only.  To get around this, the read-only fragment is converted to an 
	 * html fragment using innerHTML and stored in a temporary node.  This fragment is then 
	 * copied into a variable named content.  Content is the html fragment that get sent into
	 * modifySelection().  Since this is not a true extraction of the html fragment, a couple of 
	 * temporary spans are placed at the beginning and end to keep the place of the start and
	 * end of the range.  After the modified content is returned, these temporary spans are 
	 * removed and the selection is cleared from the document.  In Safari, on the other hand, 
	 * will allow you to manipulate the html directly but calling the extraction method on it 
	 * causes the browser to crash.  To get around this, the html fragment is cloned and the 
	 * original fragment is removed.
	 * 
	 * @param		rangeObj		Range object			Object containing the parent element, the parent element
	 * 																				 xpathlocation attribute value, and the offset from the
	 * 																				 parent.
	 * 
	 * @return	"noSelect"	String						Non-selectable area has been selected.
	 * @return	<nothing>											Success.
	 */
  insertHighlightWrapper: function (rangeObj) {
  	var noteClass = annotationConfig.annotationMarker + " " + 
    //      					(annotationConfig.isAuthor ? "author-" : "self-") +
          					(annotationConfig.isPublic ? "public" : "private") +
          					"-active";
  	var noteTitle = (annotationConfig.isAuthor ? "Author" : annotationConfig.isPublic ? "User" : "My") + 
          					" Annotation " + 
          					(annotationConfig.isPublic ? "(Public)" : "(Private)");
  	var markerId     = annotationConfig.regionalDialogMarker;
  	var noteImg   = _namespace + "/images/" + "pone_note_" + (annotationConfig.isAuthor ? "author" : "private") + "_active.gif";
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
  
  /**
   * topaz.annotation.modifySelection(Object rangeObj, Document fragment  contents, Node newspan, Node link, String markerId)
   * 
   * Method determines whether the start and end of the range is within the same container 
   * and routes the content accordingly.
   * 
   * @param		rangeObj			Range object				Object containing the parent element, the parent element
	 * 																				 		 xpathlocation attribute value, and the offset from the
	 * 																				 		 parent.
	 * @param		contents			Document fragment		HTML fragment of the range.
	 * @param		newSpan				Node object					Template of span node used to highlight the text.
	 * @param		link					Node object					Template of linked image of the annotation bug marking the
	 * 																						 beginning of the selection.
	 * @param		markerId			String							Id of marker to indicate the beginning of the selection.
	 *    
	 * @return	modContents		Document fragment		Modified document fragment of the selection.
   */
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
  
  /**
   * topaz.annotation.modifyMultiSelection(Object rangeObj, Document fragment  contents, Node newspan, Node link, String markerId)
   * 
   * Method takes selections that spans across multiple containers, such as <p>, and figures 
   * out if the container is at the beginning, middle, or end of the selection and a value of 
   * -1, 0, 1, respectively, is assigned to insertMode.  As the document fragment is recursively 
   * looped through, each child node is inspected to see if they contain a xpathlocation 
   * attribute.  If it has a xpathlocation attribute in one of the child nodes, that child node 
   * is sent through this method again for further parsing.  Otherwise, the fragment is sent to 
   * have the wrapper placed around the content and the modified content is then returned from 
   * this method.  In all cases, if "noSelect" is returned, that gets returned from this method.
   * 
   * The reason the selections over multiple containers are treated specially is because of the 
   * way the range object extracts the fragment.  Both IE and Mozilla terminates the container of
   * the start and end pieces so effectively splitting each of the two containers.  To avoid 
   * creating new paragraphs, the first and last fragment of the selection are removed from
   * the html fragment extracted from the range and that modified fragment gets inserted directly 
   * into the document. Since they are being removed, the number of childnodes in the overall range
   * fragment gets smaller so the loop index has to be decremented.  In IE, since the html fragment 
   * cannot be modified directly, the copied version replaces the actual fragment in the document.  
   * In this case, only the middle containers are being decremented.
   * 
   * @param		rangeObj			Range object				Object containing the parent element, the parent element
	 * 																				 		 xpathlocation attribute value, and the offset from the
	 * 																				 		 parent.
	 * @param		contents			Document fragment		HTML fragment of the range.
	 * @param		newSpan				Node object					Template of span node used to highlight the text.
	 * @param		link					Node object					Template of linked image of the annotation bug marking the
	 * 																						 beginning of the selection.
	 * @param		markerId			String							Id of marker to indicate the beginning of the selection.
	 *    
	 * @return	"noSelect"		String							Non-selectable area has been selected.
	 * @return	modContents		Document fragment		Modified document fragment of the selection.
   */
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
  
  /**
   * topaz.annotation.isContainXpath(Node node, Integer multiPosition)
   * 
   * Method examines a node to determines if any of its children contain the xpathlocation attribute.
   * 
   * @param		node						Node object				Parent node to start search.
   * @param multiPosition     Integer           Numerical indication of a multiple selection.
   *                                                null = Not a multiple selection
   *                                                  -1 = The first container of a multi-selection
   *                                                   0 = The middle container(s) of a multi-selection
   *                                                   1 = The last container of a multi-selection
   * 
   * @return	"false"					String						No children has an xpathlocation attribute.
   * @return	"true"					String						A child node has been found to contain the xpathlocation
   * 																						 attribute.
   */
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
   * topaz.annotation.insertWrapper(rangeObject, rangeContent, refWrapperNode, linkObject, markerId, multiPosition)
   *
   * Inner function to process selection and place the appropriate markers for displaying the selection 
   * highlight and annotation dialog box.
   *  
   * The reason the selections over multiple containers are treated specially is because of the 
   * way the range object extracts the fragment.  Both IE and Mozilla terminates the container of
   * the start and end pieces so effectively splitting each of the two containers.  To avoid 
   * creating new paragraphs, the first and last fragment of the selection are removed from
   * the html fragment extracted from the range and that modified fragment gets inserted directly 
   * into the document. 
   * 
   * @param rangeObject      Range object         Range object containing the range and additional xpath information.
   * @param rangeContent     Document fragment    Extracted html from the selection contained in either a block node
   *                                               or a document fragment.
   * @param refWrapperNode   Node object          Reference to the node object, which contains the correct 
   *                                               attributes, that will contain the selection.
   * @param linkObject       Node oject           Link node object containing the annotation bug and the marker used
   *                                               to position dialog box.
   * @param markerId         text	                Marker ID string.
   * @param multiPosition    Integer              Numerical indication of a multiple selection.
   *                                                null = Not a multiple selection
   *                                                  -1 = The first container of a multi-selection
   *                                                   0 = The middle container(s) of a multi-selection
   *                                                   1 = The last container of a multi-selection
   * @param xpath            String                Value of xpathlocation node attribute.  Used for second passes for 
   *                                                the first and last container of a multiple selection.
   * 
   * @return rangeContent    Document fragment
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

	/**
	 * topaz.annotation.normalizeText( Document object  documentObj, String resultStr)
	 * 
	 * Method nomalizes a string.  This method is used instead of the prebuilt version
	 * for IE is because it has a tendency to crash the browser.
	 * 
	 * @param		documentObj			Document object				Document fragment to be normalize.
	 * @param		resultStr				String								An existing string that subsequent normal
	 * 																								 strings should be added to.
	 * 
	 * @return	tempStr					String								The new normalized string.
	 */
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







