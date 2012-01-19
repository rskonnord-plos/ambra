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

function toggleAnnotation(obj, userType) {
  ldc.show();
  var bugs = document.getElementsByTagAndClassName('a', 'bug');
  
  for (var i=0; i<bugs.length; i++) {
    var classList = new Array();
    classList = bugs[i].className.split(' ');
    for (var n=0; n<classList.length; n++) {
      if (classList[n].match(userType))
        bugs[i].style.display = (bugs[i].style.display == "none") ? "inline" : "none";
    }
  }
  
  toggleExpand(obj, null, "Show annotations", "Hide annotations");
  
  ldc.hide();
  
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
    if (textOn) obj.innerHTML = textOn;
  }
  else if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) obj.innerHTML = textOn;
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    if (textOff) obj.innerHTML = textOff;
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

