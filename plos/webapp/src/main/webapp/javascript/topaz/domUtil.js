/**
  * topaz.domUtil
  *
  * DOM Utilities.
  *
  * @author  Joycelyn Chung  joycelyn@orangetowers.com
  **/
topaz.domUtil = new Object();

topaz.domUtil = {
  /**
   * topaz.domUtil.getDisplayId(Node obj)
   * 
   * Gets the values of the custom attribute displayId.
   * 
   * @param    obj         Node object    Element node from which to search for the displayId
   * 
   * @return  displayId    String         Display ID
   */
  getDisplayId: function(obj) {
    if (obj.getAttributeNode('displayid')) {
      var displayId = obj.getAttributeNode('displayid').nodeValue;
      return displayId;
    }
    else {
      return null;
    }
  },
  
  /**
   * topaz.domUtil.getAnnotationId(Node obj)
   * 
   * Gets the values of the custom attribute annotationId.
   * 
   * @param    obj            Node object    Element node from which to search for the annotationId.
   * 
   * @return  annotationId    String         Annotation ID
   */
  getAnnotationId: function(obj) {
    if (obj.getAttributeNode('annotationid') != null) {
      var annotationId = obj.getAttributeNode('annotationid').nodeValue;
      return annotationId;
    }
    else {
      return null;
    }
  },
  
  /**
   * topaz.domUtil.ISOtoJSDate(Date ISO_DT)
   * 
   * Converts ISO date formats to a javascript date format.
   * 
   * @param    ISO_DT      Date    ISO date.
   * 
   * @return   newDate     Date    JS date.
   */
  ISOtoJSDate: function (ISO_DT) {
     var temp = ISO_DT.split(/^(....).(..).(..).(..).(..).(..).*$/);
  
     var newDate = new Date();
     newDate.setUTCFullYear(temp[1], temp[2]-1, temp[3]);
     newDate.setUTCHours(temp[4]);
     newDate.setUTCMinutes(temp[5]);
     newDate.setUTCSeconds(temp[6]);
  
    //alert (newDate);
    return newDate;
  },
  
  /**
   * topaz.domUtil.removeChildNodes(Node obj)
   * 
   * Removes the child nodes of the node object that was passed in.
   * 
   * @param    obj          Node    Element node.
   */
  removeChildNodes: function(obj) {
    if (obj.hasChildNodes()) {
      //alert("obj has child nodes");
      for (var i=0; i<obj.childNodes.length; i++) {
        alert(childNodes[i].hasChildNodes);
        if (obj.removeChild) {
          obj.removeChild(childNodes[i]);
        }
        else {
          obj.childNodes[i].removeNode(true);
        }
      }
    }
  },

  /**
   * topaz.domUtil.getDisplayMap(Node obj, String/Arraylist displayId)
   * 
   * Parses the list of displayId(s).  Based on each displayId, searches for the matching ID in
   * the custom attribute annotationId.  Takes the displayId, the list of elements that has the
   * matching ID in the annotationId, and the count of the matching elements and puts it into an
   * object list.
   * 
   * @param    obj              Node                Element node
   * @param    displayId        String/Arraylist    displayId attribute value
   * 
   * @return   elDisplayList    Arraylist           List of displayId, matching element nodes, and
   *                                                 count.
   */
  getDisplayMap: function(obj, displayId) {
    var displayIdList = (displayId != null) ? [displayId] : this.getDisplayId(obj).split(',');
    
    //alert("displayId = " + displayId + "\n" +
    //      "displayIdList = " + displayIdList);
    
    var annoteEl = document.getElementsByTagAndAttributeName('span', 'annotationid');
    var elDisplayList = new Array();
    
    // Based on the list of displayId from the element object, figure out which element 
    // has an annotationId list in which there's an annotation id that matches the 
    // display id.
    for (var i=0; i<displayIdList.length; i++) {
      var elAttrList = new Array();
      for (var n=0; n<annoteEl.length; n++) {
        var attrList = this.getAnnotationId(annoteEl[n]).split(',');
        
        for (var x=0; x<attrList.length; x++) {
          if(attrList[x] == displayIdList[i]) {
            elAttrList.push(annoteEl[n]);
          }
        }
      }
      
      elDisplayList.push({'displayId': displayIdList[i],
                          'elementList': elAttrList,
                          'elementCount': elAttrList.length});
    }
    
    return elDisplayList;
  },
  
  /**
   * topaz.domUtil.addNewClass(String sourceClass, String newClass, Node el)
   * 
   * Searches for elements with the sourceClass and adds the newClass to each of them.
   * 
   * @param    sourceClass  String    Existing class name for reference.
   * @param    newClass     String    New class name to be added.
   * @param    el           Node      Element node from which to search.
   */
  addNewClass: function (sourceClass, newClass, el) {
    var elObj = (el) ? el : null;
    var elList = document.getElementsByTagAndClassName(elObj, sourceClass);
    
    for (var i=0; i<elList.length; i++) {
       dojo.html.addClass(elList[i], newClass);
    }
  },
  
  /**
   * topaz.domUtil.swapClassNameBtwnSibling(Node obj, String tagName, String classNameValue)
   * 
   * Finds sibling elements that has a classNameValue that matches and removes them.  
   * Then adds the className to the source node object.
   * 
   * @param    obj              Node      Element node
   * @param    tagName          String    Element name to search.
   * @param    classNameValue   String    Class name to match.
   */
  swapClassNameBtwnSibling: function (obj, tagName, classNameValue) {
    var parentalNode = obj.parentNode;
    var siblings = parentalNode.getElementsByTagName(tagName);
    
    for (var i=0; i<siblings.length; i++) {
      if (siblings[i].className.match(classNameValue)){
        dojo.html.removeClass(siblings[i], classNameValue);   
      }
    }
    
    dojo.html.addClass(obj, classNameValue);
  },

  /**
   * topaz.domUtil.swapAttributeByClassNameForDisplay(Node obj, String triggerClass, String displayId)
   * 
   * Using the source node obj and the displayId, get a list of displayIds mapped to elements that has 
   * a matching ID in its annotationId attribute node.  Using this list of maps, iterate through to find
   * the element with the matching triggerClass.  Remove the triggerClass from the matching element.
   * Add it to the source node obj.
   * 
   * @param    obj            Node      Element node.
   * @param    triggerClass   String    ClassName string used for matching.
   * @param    displayId      String    DisplayId string also used for matching.
   */
  swapAttributeByClassNameForDisplay: function (obj, triggerClass, displayId) {
    var elements = this.getDisplayMap(obj, displayId);
    
    for (var i=0; i<elements.elementCount; i++) {
      if (elements.elementList[i].className.match(triggerClass)) {
        var strRegExp = new RegExp(triggerClass);
        elements.elementList[i].className = elements.elementList[i].className.replace(strRegExp, "");
      }
    }
    
    dojo.html.addClass(obj, triggerClass);
  },
  
  /**
   * topaz.domUtil.getCurrentOffset(Node obj)
   * 
   * Gets the offset of the node obj from it's parent.
   * 
   * @param    obj          Node        Element node.
   * 
   * @return   offset      Integer      Offset of the node obj.
   */
  getCurrentOffset: function(obj) {
    var curleft = curtop = 0;
    if (obj.offsetParent) {
      curleft = obj.offsetLeft
      curtop = obj.offsetTop
      while (obj = obj.offsetParent) {
        curleft += obj.offsetLeft  
        curtop += obj.offsetTop
      }
    }

    var offset = new Object();
    offset.top = curtop;
    offset.left = curleft;
    
    return offset;
  },

  /**
   * topaz.domUtil.getFirstAncestorByClass(Node selfNode, String ancestorClassName)
   * 
   * Gets the parent node of selfNode that has the matching classname to ancestorClassName.
   * 
   * @param    selfNode            Node object      Element node.
   * @param    ancestorClassName   String           Classname string.
   * 
   * @return   parentalNode        Node object      Parent node that has the matching
   *                                                 ancestorClassName.
   */
  getFirstAncestorByClass: function ( selfNode, ancestorClassName ) {
    var parentalNode = selfNode;
    
    while ( parentalNode.className.search(ancestorClassName) < 0) {
      parentalNode = parentalNode.parentNode;
    }
    
    return parentalNode;
  },
  
  /**
   * topaz.domUtil.swapDisplayMode(Node obj, String state)
   * 
   * Swaps the display mode of node obj between "block" and "none".
   * 
   * @param    obj          Node object    Element node.
   * @param    state        String         Optional value if there's a particular state that 
   *                                        the obj needs to be in.
   *                                        "block" = Show obj.
   *                                        "none"  = Hide obj.
   * 
   * @return   false        boolean        If obj is a link, prevents the link from fowarding.
   */
  swapDisplayMode: function(objId, state) {
    var obj = dojo.byId(objId);
    
    if (state && (state == "block" || state == "none")) 
      obj.style.display = state;
    else if(obj.style.display != "block")
      obj.style.display = "block";
    else
      obj.style.display = "none";
      
    return false;
  },
  
  /**
   * topaz.domUtil.swapDisplayTextMode(Node node, String objId, String state, String textOn, String textOff)
   * 
   * Swaps the display mode of node object based on the objId between "block" and "none".  
   * Also changes the text of node object node to the text specified in textOn and textOff.
   * 
   * @param    node        Node object       Element source node that triggers this method.
   * @param    objId       String            Id of remote element that you want to show or hide.
   * @param    state       String            Specific state the remote element needs to be in.
   * @param    textOn      String            Text string for the source node when the remote
   *                                          element is shown.
   * @param    textOff     String            Text string for the source node when the remote
   *                                          element is hidden.
   * 
   * @return   false       boolean           If node is a link, prevents the link from fowarding.
   */
  swapDisplayTextMode: function(node, objId, state, textOn, textOff) {
    var obj = dojo.byId(objId);
    
    if (state) 
      obj.style.display = state;
    else if(obj.style.display != "block")
      obj.style.display = "block";
    else
      obj.style.display = "none";
      
    if (obj.style.display == "block")
      dojo.dom.textContent(node, textOn);
    else
      dojo.dom.textContent(node, textOff);
      
    return false;
  },
  
  /**
   * topaz.domUtil.removeExtraSpaces(String text)
   * 
   * Removes all extra spaces, carriage returns, and line feeds and replacing them with 
   * one space.
   * 
   * @param    text     String    Text string to be modified.
   * 
   * @return  <text>    String   Modified text string.
   */
  removeExtraSpaces: function(text) {
    //alert("text = '" + text + "'");
    return text.replace(/([\r\n]+\s+)/g," ");
  },
  
  /**
   * topaz.domUtil.getChildElementsByTagAndClassName(Node node, String tagName, String className)
   * 
   * Looks through the child nodes of node object node for matching tagName and/or
   * matching className.
   * 
   * @param    node        Node object    Element node.
   * @param    tagName     String         Text string of the tag name to search for.
   * @param    className   String         Text string of the class name to search for.
   * 
   * @return   children    Array          Array of child nodes of node if neither the tag name or
   *                                       class name are passed in.
   * @return   elements    Array          Array of element nodes that matches the tag name and/or
   *                                       class name.
   */
  getChildElementsByTagAndClassName: function(node, tagName, className) {
    var children = node.childNodes;
    var elements = new Array();
    tagName = tagName.toUpperCase();
    
    //alert("node = " + node + "\ntagName = " + tagName + "\nclassName = " + className);
    
    if ( className != null || tagName != null) {
      //alert("children = " + children.length);

      for (var i = 0; i < children.length; i++) {
        var child = children[i];
        
        if (tagName != null) {
        
          //alert("child.nodeName.match(tagName) = " + child.nodeName.match(tagName));
          
          if (child.nodeName.match(tagName)) {
            if (className != null) {
              var classNames = child.className.split(' ');
              for (var j = 0; j < classNames.length; j++) {
                if (classNames[j] == className) {
                  elements.push(child);
                  break;
                }
              }
            }
            else {
              elements.push(child);
            }
          }
        }
        else if (className != null){
          var classNames = child.className.split(' ');
          for (var j = 0; j < classNames.length; j++) {
            if (classNames[j] == className) {
              elements.push(child);
              break;
            }
          }
        }
      }
    }
    else {
      return children;
    }    

    //alert("elements = " + elements);
    return elements;
  },

  /**
   * topaz.domUtil.adjustContainerHeight(Node obj)
   * 
   * Adjusts the height of node object obj.
   * 
   * @param    obj          Node    Element node.
   */
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
  
  /**
   * topaz.domUtil.setContainerWidth(Node obj, Integer minWidth, Integer maxWidth, Integer variableWidth)
   * 
   * Resets the size of the node object obj based on minWidth, maxWidth and variableWidth.
   * 
   * @param    obj            Node object    Element node.
   * @param    minWidth       Integer        Minimum width.
   * @param    maxWidth       Integer        Maximum width.
   * @param    variableWidth  Integer        Variable width.
   */
  setContainerWidth: function (obj, minWidth, maxWidth, variableWidth /* if the container between min and max */) {
    var viewport = dojo.html.getViewport();
    
    // min-width: 675px; max-width: 910px;
    obj.style.width = (minWidth && viewport.width < minWidth) ? minWidth + "px" : 
                      (maxWidth && viewport.width > maxWidth) ? maxWidth + "px" :
                      (!variableWidth && viewport.width < maxWidth) ? maxWidth + "px" : "auto" ;
    //alert("container.style.width = " + obj.style.width);
  },
  
  /**
   * topaz.domUtil.removeNode(Node node, boolean deep)
   * 
   * Removes a node.  If deep is set to true, the children of the node object node is 
   * also removed.  
   * 
   * @param    node     Node object   Element node from which to search for the displayId
   * @param    deep     boolean       Is set to true, the children of the node is also 
   *   removed.
   */
  removeNode: function(node, /* boolean */ deep) {
    if (deep && node.hasChildNodes)
      dojo.dom.removeChildren(node);
      
    dojo.dom.removeNode(node);
  },
  
  /**
   * topaz.domUtil.insertAfterLast(Node srcNode, Node refNode)
   * 
   * Inserts the srcNode after the refNode.
   * 
   * @param    srcNode     Node object   Element node source.
   * @param    refNode     Node object   Element node reference.
   */
  insertAfterLast: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      dojo.dom.insertAfter(srcNode, refNode[refNode.childNodes.length-1]);
    else
      refNode.appendChild(srcNode);
  },

  /**
   * topaz.domUtil.insertBeforeFirst(Node srcNode, Node refNode)
   * 
   * Inserts the srcNode before the refNode.
   * 
   * @param    srcNode     Node object   Element node source.
   * @param    refNode     Node object   Element node reference.
   */
  insertBeforeFirst: function(srcNode, refNode) {
    if (refNode.hasChildNodes) 
      dojo.dom.insertBefore(srcNode, refNode[0]);
    else
      refNode.appendChild(srcNode);
  },
  
  /**
   * topaz.domUtil.modifyNodeChildClassname(Node node, String targetNodeName, String className, boolean isAdd)
   * 
   * Modifies the class name of child nodes of node.  The child nodes must match the targetNodeName.  If isAdd
   * is set to true, the className is added.  Otherwise, the className is removed.
   * 
   * @param    node             Node object    Element node source.
   * @param    targetNodeName   String         Text string of the name of the node that will be target of the 
   *                                            search.
   * @param    classNameString  Text           string of the class name that will either be added or removed.
   * @param    isAdd            boolean        If set to true, the class name will be added to the child elements'
   *                                            class attribute.  Otherwise, the class name will be removed.
   * 
   * @return  false             boolean        The node has no child nodes.
   * @return  true              boolean        The node does have child nodes and the modification was successful.
   */
  modifyNodeChildClassname: function(/* Node */node, /* String */targetNodeName, /* String */className, /* Boolean */isAdd) {
    if (node.hasChildNodes) {
      var nodeChildren = node.childNodes;
  
      for (var i=0; i<nodeChildren.length; i++) {
        if (nodeChildren[i].nodeName == targetNodeName) {
          if (isAdd) {
            dojo.html.addClass(nodeChildren[i], className);
          }
          else {
            dojo.html.removeClass(nodeChildren[i], className);
          }
        }
        
        if (nodeChildren[i].hasChildNodes) {
          this.modifyNodeChildClassname(nodeChildren[i], refNodeName, className, isAdd);
        }
      }
      
      return true;
    }
    else {
      return false;
    }
  },
  
  /**
   * topaz.domUtil.isClassNameExist(Node node, String className)
   * 
   * Determines whether the className exists in the class attribute of node.
   * 
   * @param    node        Node object   Element node.
   * @param className String  Text string of the class name.
   * 
   * @return   falsebooleanNo match found.
   * @return truebooleanMatch found.
   */
  isClassNameExist: function(node, className) {
    var classArray = new Array();
    classArray = node.className.split(" ");
    
    for (var i=0; i<classArray.length; i++) {
      if (classArray[i] == className) 
        return true;
    }
    
    return false;
  },
  
  /**
   * topaz.domUtil.isChildContainAttributeValue(Node node, String attributeName, String attributeValue)
   * 
   * Determines whether the child nodes contain an attributeName attribute and, if specified, is equal
   * to attributeValue.
   * 
   * @param    node            Node object    Element node
   * @param    attributeName   String         Text string of the attribute name to search.
   * @param    attributeValue  String         If specified, text string of an attribute value to search.
   * 
   * @return   itemsFound      Array          A collection of element nodes that matches the criteria.
   */
  isChildContainAttributeValue: function (node, attributeName, attributeValue) {
    var childlist = node.childNodes;
    var itemsFound = new Array();
    
    if (djConfig.isDebug) {
      dojo.byId(djConfig.debugContainerId).innerHTML +=
            "<br><br>" + "[topaz.domUtil.isChildContainAttributeValue]"
            ;
    }
    
    for (var i=0; i<=childlist.length-1; i++) {
      var attrObj = new Object();

      if (djConfig.isDebug) {
        dojo.byId(djConfig.debugContainerId).innerHTML +=
              "<br>" + "attributeValue = " + attributeValue 
              + "<br>" + "childlist[" + i + "].nodeName = " + childlist[i].nodeName 
              ;
      }
      
      if (childlist[i].nodeType == 1 &&
          (((attributeValue || attributeValue !=null) && childlist[i].getAttribute(attributeName) == attributeValue) ||
          ((!attributeValue || attributeValue == null) && childlist[i].getAttributeNode(attributeName) != null))) {

        if (djConfig.isDebug) {
          dojo.byId(djConfig.debugContainerId).innerHTML +=
                "<br>" + "childlist[" + i + "].getAttribute(" + attributeName + ") = " + childlist[i].getAttribute(attributeName) 
                ;
        }
        
        attrObj.node = childlist[i];
        attrObj.value = childlist[i].getAttribute(attributeName);
        itemsFound.push(attrObj);
      }
    }
    
    if (djConfig.isDebug) {
      dojo.byId(djConfig.debugContainerId).innerHTML +=
            "<br>" + "-----------------------------------------------<br>"
            ;
    }
    
    return itemsFound;
  },
  
  /**
   * topaz.domUtil.firstSibling(Node siblingObj)
   * 
   * Finds the first sibling of siblingObj.
   * 
   * @param    siblingObj    Node object    Element node
   * 
   * @return   fSibling      Node object    Element node that is the first sibling of siblingObj.
   */
  firstSibling: function(siblingObj) {
    var fSibling;
    for (var current = siblingObj; current != null; current = current.previousSibling) {
      if (current.nodeName == "#text" && (current.nodeValue.match(new RegExp("\n")) || current.nodeValue.match(new RegExp("\r")))) {
        continue;
      }
      
      fSibling = current;
    }
    
    return fSibling;
  }
}