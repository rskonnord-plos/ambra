dojo.provide("topaz.topaz");


var topaz = new Object( );

document.getElementsByTagAndClassName = function(tagName, className) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( className == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    var classNames = child.className.split(' ');
    for (var j = 0; j < classNames.length; j++) {
      if (classNames[j] == className) {
        elements.push(child);
        break;
      }
    }
  }

  return elements;
}

document.getElementsByTagAndAttributeName = function(tagName, attributeName) {
  if ( tagName == null )
    tagName = '*';
   
  var children = document.getElementsByTagName(tagName);
  var elements = new Array();
  
  if ( attributeName == null )
    return children;
  
  for (var i = 0; i < children.length; i++) {
    var child = children[i];
    if (child.getAttributeNode(attributeName) != null) {
      elements.push(child);
    }
  }

  return elements;
}

document.getElementsByAttributeValue = function(tagName, attributeName, attributeValue) {
  if ( tagName == null )
    tagName = '*';
  else if ( attributeName == null )
    return "[getElementsByAttributeValue] attributeName is required.";
  else if ( attributeValue == null )
    return "[getElementsByAttributeValue] attributeValue is required.";

  var elements = document.getElementsByTagAndAttributeName(tagName, attributeName);
  var elValue = new Array();
  
  for (var i = 0; i < elements.length; i++) {
    var element = elements[i];
    if (element.getAttributeNode(attributeName).nodeValue == attributeValue) {
      elValue.push(element);
    }
  }

  return elValue;
}


/**
 * Extending the String object
 *
 **/
String.prototype.trim = function() {
  return this.replace(/(?:(?:^|\n)\s+|\s+(?:$|\n))/g,"");
}

String.prototype.rtrim = function() {
  return this.replace(/\s+$/,"");
}

String.prototype.ltrim = function() {
  return this.replace(/^\s+/, "");
}

String.prototype.isEmpty = function() {
  return (this == null || this == "");
}

String.prototype.replaceStringArray = function(delimiter, strMatch, newStr) {
	if (!strMatch || !newStr) {
		return "Missing required value";
	}
	
	var strArr = (delimiter) ? this.split(delimiter) : this.split(" ");
	var matchIndexStart = -1;
	var matchIndexEnd = -1;
	for (var i=0; i<strArr.length; i++) {
		if (strArr[i].match(strMatch) != null) {
			if (matchIndexStart < 0)
				matchIndexStart = i;
			
			matchIndexEnd = i;
		}
	}
	
	if (matchIndexEnd >= 0) {
		var diff = matchIndexEnd - matchIndexStart + 1;
		strArr.splice(matchIndexStart, diff, newStr);
	}
	
	var newStr = strArr.join(" ");
	
	return newStr;
}

function toggleAnnotation(obj, userType) {
  _ldc.show();
  var bugs = document.getElementsByTagAndClassName('a', 'bug');
  
  for (var i=0; i<bugs.length; i++) {
    var classList = new Array();
    classList = bugs[i].className.split(' ');
    for (var n=0; n<classList.length; n++) {
      if (classList[n].match(userType))
        bugs[i].style.display = (bugs[i].style.display == "none") ? "inline" : "none";
    }
  }
  
  toggleExpand(obj, null, "Show notes", "Hide notes");
  
  _ldc.hide();
  
  return false;
}

function getAnnotationEl(annotationId) {
  var elements = document.getElementsByTagAndAttributeName('a', 'displayid');
     
  var targetEl
  for (var i=0; i<elements.length; i++) {
    var elDisplay = topaz.domUtil.getDisplayId(elements[i]);
    var displayList = elDisplay.split(',');

    for (var n=0; n<displayList.length; n++) {
      if (displayList[n] == annotationId) {
        targetEl = elements[i];
        return targetEl;
      }
    }
    
  }
  
  return false;
}

var elLocation;
function jumpToAnnotation(annotationId) {
  var targetEl = getAnnotationEl(annotationId);

  jumpToElement(targetEl);
}

function jumpToElement(elNode) {
  if (elNode) {
    elLocation = topaz.domUtil.getCurrentOffset(elNode);
    window.scrollTo(0, elLocation.top);
  }
}

function toggleExpand(obj, isOpen, textOn, textOff) {
  if (isOpen == false) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojo.dom.textContent(obj, textOn);
  }
  else if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojo.dom.textContent(obj, textOn);
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    if (textOff) dojo.dom.textContent(obj, textOff);
  }
  
}

var activeToggleId = "";
var activeWidget = "";

function setActiveToggle(widgetId, boxId) {
  activeToggleId = boxId;
  activeWidget = dojo.byId(widgetId);
}

function singleView(obj) {
  if (activeToggleId != "") {
    topaz.domUtil.swapDisplayMode(activeToggleId, "none");
    toggleExpand(activeWidget, false); 
  }
}

function singleExpand(obj, targetId) {
  if (targetId != activeToggleId) {
    singleView(obj);
  }
  setActiveToggle
   (obj.id, targetId);
  topaz.domUtil.swapDisplayMode(targetId);
  toggleExpand(obj); 
  
  return false;
}

