/**
 * topaz.horizontalTabs()
 * 
 * The horizontal tabs are the secondary navigation that can be found on the
 * home page and the profile page.  This class uses a map object set in the 
 * configuration file that will be used for building the tabs.  This map
 * object contains key-value pairs, for example,
 * 			tabKey:    "recentlyPublished",
 *      title:     "Recently Published",
 *      className: "published",
 *      urlLoad:   "/article/recentArticles.action",
 *      urlSave:   ""
 * 
 * These values are set for each tab.  Using the setters, initialize the 
 * tab in the page init.js files.
 **/


var proceedFlag = false;
var tempValue = "";
var changeFlag = false;

topaz.horizontalTabs = new Object();

topaz.horizontalTabs = {
  tabPaneSet: "",
  
  tabsListObject: "",
  
  tabsContainer: "",
  
  targetFormObj: "",
  
  targetObj: "",
  
  newTarget: "",
  
  setTabPaneSet: function (obj) {
    this.tabPaneSet = obj;
  },
  
  setTabsListObject: function (listObj) {
    this.tabsListObject = listObj;
  },
  
  setTabsContainer: function (listObj) {
    this.tabsContainer = listObj;
  },
  
  setTargetFormObj: function (formObj) {
    this.targetFormObj = formObj;
  },
  
  setTargetObj: function (targetObj) {
    this.targetObj = targetObj;
  },
  
  setNewTarget: function (newTarget) {
    this.newTarget = newTarget;
  },
  
  getMapObject: function (value) {
    if (value) {
      for (var i=0; i<this.tabsListObject.length; i++) {
        if (this.tabsListObject[i].tabKey == value)
          return this.tabsListObject[i];
      }
    }
    else {
      return this.tabsListObject[0];
    }
  },
  
  init: function(initId) {
    var targetObj;
    
    if (initId)
      targetObj = this.getMapObject(initId);
    else 
      targetObj = this.getMapObject();
    
    this.buildTabs(targetObj);
    this.tabSetup(targetObj);
    //this.attachFormEvents(formObj);
  },
  
  initSimple: function(initId) {
    var targetObj;
    
    if (initId)
      targetObj = this.getMapObject(initId);
    else 
      targetObj = this.getMapObject();
    
    this.buildTabsHome(targetObj);
    this.setTargetObj(targetObj);
  },
  
  tabSetup: function (targetObj) {
    this.setTargetObj(targetObj);
    
    var formName = this.targetObj.formName;
    var formObj = document.forms[formName];
    
    this.setTargetFormObj(formObj);
    //topaz.formUtil.createHiddenFields(this.targetFormObj);
    
    //alert("formObj.formSubmit = " + formObj.formSubmit.value);
    /*dojo.event.connect(formObj.formSubmit, "onclick", function() {
        //alert("tabKey = " + topaz.horizontalTabs.targetObj.tabKey);
        submitContent(topaz.horizontalTabs.targetObj);
      }
    );*/
    
    formObj.formSubmit.onclick = function () {
        submitContent();
      }
  },

  setTempValue: function (obj) {
    if (obj.type == "radio") {
      var radioName = obj.name;
      
      var radioObjs = obj.form.elements[radioName];
        
      for (var n=0; n<radioObjs.length; n++) {
        if (radioObjs[n].checked) {
          tempValue = radioObjs[n].value;
        }
      }
    }
    else if (obj.type == "checkbox") {
      var checkboxName = obj.name;
      
      var checkboxObjs = obj.form.elements[checkboxName];
      
      if (checkboxObjs.length) {  
        for (var n=0; n<checkboxObjs.length; n++) {
          if (checkboxObjs[n].checked) {
            tempValue = checkboxObjs[n].value;
          }
        }
      }
      else {
        tempValue = checkboxObjs.checked;
      }
    }
    else if (obj.type == "select-one") {
      //alert("formObj.elements[" + i + "][" + obj.selectedIndex + "].value = " + obj[obj.selectedIndex].value);
      tempValue = obj[obj.selectedIndex].value; 
    }
    else {
      tempValue = obj.value;
    }
    
    //alert("tempValue = " + tempValue);
  },
  
  checkValue: function (obj) {
    //alert("obj = " + obj.type);
    if (obj.type == "radio") {
      var radioName = obj.name;
      
      var radioObjs = obj.form.elements[radioName];

      //alert("obj.form.elements[" + checkboxName + "].length = " + obj.form.elements[checkboxName].toSource());
      for (var n=0; n<radioObjs.length; n++) {
        if (radioObjs[n].checked) {
          if (tempValue != radioObjs[n].value)
            changeFlag = true;
        }
      }
    }
    else if (obj.type == "checkbox") {
      var checkboxName = obj.name;
      
      var checkboxObjs = obj.form.elements[checkboxName];
        
      //alert("obj.form.elements[" + checkboxName + "].length = " + obj.form.elements[checkboxName].toSource());
      if (checkboxObjs.length) {
        for (var n=0; n<checkboxObjs.length; n++) {
          if (checkboxObjs[n].checked) {
            if (tempValue != checkboxObjs[n].value)
              changeFlag = true;
          }
        }
      }
      else {
        if (tempValue != checkboxObjs.checked) 
          changeFlag = true;
      }
    }
    else if (obj.type == "select-one") {
      //alert("formObj.elements[" + i + "][" + obj.selectedIndex + "].value = " + obj[obj.selectedIndex].value);
      if (tempValue != obj[obj.selectedIndex].value)
        changeFlag = true;
    }
    else {
      if (tempValue != obj.value)
        changeFlag = true;
    }
    
    //alert("changeFlag = " + changeFlag);
  },
  
  attachFormEvents: function (formObj) {
    tempValue = "";

    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        var formName = formObj.name;
        var fieldName = formObj.elements[i].name;
        //alert("formName = " + formName + "\n" +
        //      "fieldName = " + fieldName);
        dojo.event.connect(document.forms[formName].elements[fieldName], "onfocus", function() {
        //    alert("tempValue = " + tempValue + "\n" +
        //          "this.id = " + document.forms[formName].elements[fieldName].value);
            tempValue = this.value;
          }  
        );

        dojo.event.connect(formObj.elements[i], "onchange", function() {
        //    alert("tempValue = " + tempValue + "\n" +
        //          "this.value = " + this.value);
          
            if (tempValue == this.value) 
              changeFlag = true;
          }  
        );
      }
    }
  },
  
  buildTabs: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var li = document.createElement("li");
      li.id = this.tabsListObject[i].tabKey;
      if (obj.className) li.className = obj.className;
      if (this.tabsListObject[i].tabKey == obj.tabKey) {
        //li.className = li.className.concat(" active");
        dojo.html.addClass(li, "active");
      }
      li.onclick = function () { 
          topaz.horizontalTabs.show(this.id);
          return false; 
        }
      li.appendChild(document.createTextNode(this.tabsListObject[i].title));

      this.tabsContainer.appendChild(li);
    }
    
    tempValue = "";
  },
  
  buildTabsHome: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var li = document.createElement("li");
      li.id = this.tabsListObject[i].tabKey;
      if (this.tabsListObject[i].className) li.className = this.tabsListObject[i].className;
      if (this.tabsListObject[i].tabKey == obj.tabKey) {
        //li.className = li.className.concat(" active");
        dojo.html.addClass(li, "active");
      }
      li.onclick = function () { 
          topaz.horizontalTabs.showHome(this.id);
          return false; 
        }
      var span = document.createElement("span");
      span.appendChild(document.createTextNode(this.tabsListObject[i].title));
      li.appendChild(span);

      this.tabsContainer.appendChild(li);
    }
  },
  
  toggleTab: function(obj) {
    for (var i=0; i<this.tabsListObject.length; i++) {
      var tabNode = dojo.byId(this.tabsListObject[i].tabKey);
      
      if (tabNode.className.match("active"))
        dojo.html.removeClass(tabNode, "active");
        //tabNode.className = tabNode.className.replace(/active/, "").trim();
    }
    
    var targetNode = dojo.byId(obj.tabKey);
    dojo.html.addClass(targetNode, "active");
    //targetNode.className = targetNode.className.concat(" active");
  },
  
  confirmChange: function (formObj) {
    //var isChanged = false;
    //isChanged = topaz.formUtil.hasFieldChange(topaz.horizontalTabs.targetFormObj);
   
    //alert("[confirmChange] changeFlag = " + changeFlag);
    if (changeFlag) {
      var warning = confirm("You have made changes, are you sure you want to leave this tab without saving?  If you want to proceed, click \"OK\".  Otherwise click \"Cancel\" to go to save.");
      
      proceedFlag = warning;
    }
    else {
      proceedFlag = true;
    }
  },
    
  getContent: function() {
    if (!proceedFlag) {
      _ldc.hide();
  
      this.targetFormObj.formSubmit.focus();
      return false;
    }
    else {
      //topaz.formUtil.removeHiddenFields(this.targetFormObj);
      loadContent(this.newTarget);
    }
  },

  saveContent: function(targetId) {
    var newTarget = this.getMapObject(targetId);
    
    submitContent(newTarget);
  },

  show: function(id) {
    var newTarget = this.getMapObject(id);
    this.setNewTarget(newTarget);
    _ldc.show();
    this.confirmChange();
    
    setTimeout("getContentFunc()", 1000);
  },
  
  showHome: function(id) {
    var newTarget = this.getMapObject(id);
    this.setNewTarget(newTarget);
    
    loadContentHome(newTarget);
  }
  
}  

function getContentFunc () {
  topaz.horizontalTabs.getContent();
}

function loadContent(targetObj) {
  var refreshArea = dojo.byId(profileConfig.tabPaneSetId);
  var targetUri = targetObj.urlLoad + "?tabId=" + targetObj.tabKey;

  _ldc.show();
  
  var bindArgs = {
    url: _namespace + targetUri,
    method: "get",
    error: function(type, data, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + data.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      refreshArea.innerHTML = docFragment;
      
      //alert("targetObj.tabKey = " + targetObj.tabKey);
      topaz.horizontalTabs.toggleTab(targetObj);
      topaz.horizontalTabs.tabSetup(targetObj);
      tempValue = "";
      changeFlag = false;
      
      proceedFlag = true;
      _ldc.hide();

      return false;
    },
    mimetype: "text/html",
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version }
   };
   dojo.io.bind(bindArgs);
}  

function loadContentHome(targetObj) {
  var refreshArea = dojo.byId(homeConfig.tabPaneSetId);
  var targetUri = targetObj.urlLoad;

  var bindArgs = {
    url: _namespace + targetUri,
    method: "get",
    error: function(type, data, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + data.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;
      
      refreshArea.innerHTML = docFragment;
      
      //alert("targetObj.tabKey = " + targetObj.tabKey);
      topaz.horizontalTabs.setTargetObj(targetObj);
      topaz.horizontalTabs.toggleTab(targetObj);

      return false;
    },
    mimetype: "text/html",
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version }
   };
   dojo.io.bind(bindArgs);
}  

function submitContent() {
  var refreshArea = dojo.byId(profileConfig.tabPaneSetId);
  var srcObj = topaz.horizontalTabs.targetObj;
  var targetUri = srcObj.urlSave;
  
  //alert("formName = " + srcObj.formName + "\ntargetUri = " + targetUri);
  
  var formObj = document.forms[srcObj.formName];
  var formValueObj = topaz.formUtil.createFormValueObject(formObj);
  
  //alert("formValueObj = " + formValueObj.toSource());

  _ldc.show();
  
  var bindArgs = {
    url: _namespace + targetUri,
    method: "post",
    content: formValueObj,
    error: function(type, data, evt){
     var err = document.createTextNode("ERROR [AJAX]:" + data.toSource());
     //topaz.errorConsole.writeToConsole(err);
     //topaz.errorConsole.show();
     alert("ERROR:" + data.toSource());
     return false;
    },
    load: function(type, data, evt){
      var docFragment = document.createDocumentFragment();
      docFragment = data;

      topaz.horizontalTabs.tabSetup(srcObj);

      refreshArea.innerHTML = docFragment;
      
      tempValue = "";
      changeFlag = false;
      
      var formObj = document.forms[srcObj.formName];
      
      formObj.formSubmit.onclick = function () {
          submitContent();
        }
        
      var errorNodes = document.getElementsByTagAndClassName(null, "form-error");
      
      if (errorNodes.length >= 0)
        jumpToElement(errorNodes[0]);
      else
        jumpToElement(errorNodes);
        
      //topaz.formUtil.createHiddenFields(targetObj.targetFormObj);
      //topaz.horizontalTabs.attachFormEvents(document.forms[targetObj.formName]);
      //topaz.horizontalTabs.toggleTab(targetObj);
      
      _ldc.hide();

      return false;
    },
    mimetype: "text/html",
    headers: { "AJAX_USER_AGENT": "Dojo/" +  dojo.version }
   };
   dojo.io.bind(bindArgs);
}  

