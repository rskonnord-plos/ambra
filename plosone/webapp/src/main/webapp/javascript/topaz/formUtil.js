topaz.formUtil = new Object();

topaz.formUtil = {
  textCues: {
  	on: function ( formEl, textCue ) {
    if (formEl.value == "")
    	formEl.value = textCue;
	  },
	  
	  off: function ( formEl, textCue ) {
	    if (formEl.value == textCue)
	    	formEl.value = "";
	  },
	  
	  reset: function ( formEl, textCue ){
	    formEl.value = textCue;
	  }
  },
  
  toggleFieldsByClassname: function ( toggleClassOn, toggleClassOff ) {
    var targetElOn = document.getElementsByTagAndClassName(null, toggleClassOn);
    var targetElOff = document.getElementsByTagAndClassName(null, toggleClassOff);

    for (var i=0; i<targetElOn.length; i++) {
      targetElOn[i].style.display = "block";
    }

    for (var i=0; i<targetElOff.length; i++) {
      targetElOff[i].style.display = "none";
    }
  },
  
  checkFieldStrLength: function ( fieldObj, maxLength ) {
    if(fieldObj.value && fieldObj.value.length > maxLength) {
      alert("Your comment exceeds the allowable limit of " + maxLength + " characters by " + (fieldObj.value.length - maxLength) + " characters.");
      fieldObj.focus();
      return 0;
    }
    else {
      return -1;
    }
  },
  
  disableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = true;
      } 
    }
  },
  
  enableFormFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type != 'hidden') {
        formObj.elements[i].disabled = false;
      } 
    }
  },
  
  createHiddenFields: function (formObj) {
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        if (formObj.elements["hdn" + formObj.elements[i].name] == null) {
          var newHdnEl = document.createElement("input");
          
          newHdnEl.type = "hidden";
          newHdnEl.name = "hdn" + formObj.elements[i].name;
          
          if (formObj.elements[i].type == "radio") {
            var radioName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[radioName].length; n++) {
              if (formObj.elements[radioName][n].checked) {
                newHdnEl.value = formObj.elements[radioName][n].value;

                break;
              }
            }
          }
          else if (formObj.elements[i].type == "checkbox") {
            var checkboxName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[checkboxName].length; n++) {
              if (formObj.elements[checkboxName][n].checked) {
                newHdnEl.value = (newHdnEl.value == "") ? formObj.elements[checkboxName].value : newHdnEl.value + "," + formObj.elements[checkboxName].value;
              }
            }
          }
          else if (formObj.elements[i].type == "select-one") {
            //alert("formObj.elements[" + i + "][" + formObj.elements[i].selectedIndex + "].value = " + formObj.elements[i][formObj.elements[i].selectedIndex].value);
            newHdnEl.value = formObj.elements[i][formObj.elements[i].selectedIndex].value; 
          }
          else {
            newHdnEl.value = formObj.elements[i].value;
          }
    
          formObj.appendChild(newHdnEl);
        }
      }
    }
    
  },
  
  createFormValueObject: function (formObj) {
    var formValueObject = new Object();
    
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        
        if (formObj.elements[i].type == "radio") {
          var radioName = formObj.elements[i].name;
          var radioObj = formObj.elements[radioName];
          
          for (var n=0; n<radioObj.length; n++) {
            if (radioObj[n].checked) {
              formValueObject[radioObj[n].name] = radioObj[n].value;
  
              break;
            }
          }
        }
        else if (formObj.elements[i].type == "checkbox") {
          var checkboxName = formObj.elements[i].name;
          var checkboxObj = formObj.elements[checkboxName];
          
          var cbArray = new Array();
          if (checkboxObj.length) {
            for (var n=0; n<checkboxObj.length; n++) {
              if (checkboxObj[n].checked) {
                 cbArray.push(checkboxObj[n].value);
              }
            }
            
            formValueObject[checkboxName] = cbArray;
          }
          else {
            formValueObject[checkboxObj.name] = checkboxObj.value;
          }
        }
        else if (formObj.elements[i].type == "select-one") {
          formValueObject[formObj.elements[i].name] = formObj.elements[i][formObj.elements[i].selectedIndex].value;
        }
        else {
          formValueObject[formObj.elements[i].name] = formObj.elements[i].value;
        }
      }
    }

    return formValueObject;
  },
  
  hasFieldChange: function (formObj) {
    var thisChanged = false;
    
    for (var i=0; i<formObj.elements.length; i++) {
      if(formObj.elements[i].type != 'hidden' && 
         formObj.elements[i].type != 'button' && 
         formObj.elements[i].type != 'submit' && 
         formObj.elements[i].name != null) {
        
        var hdnFieldName = "hdn" + formObj.elements[i].name;
        
        //alert("formObj.elements[" + hdnFieldName + "] = " + formObj.elements[hdnFieldName]);
        
        if (formObj.elements[hdnFieldName] != null) {
          
          //alert("formObj.elements[" + i + "].type = " + formObj.elements[i].type);
          if (formObj.elements[i].type == "radio") {
            var radioName = formObj.elements[i].name;
            
            for (var n=0; n<formObj.elements[radioName].length; n++) {
              if (formObj.elements[radioName][n].checked) {
                alert("formObj.elements[" + radioName + "][" + n + "].value = " + formObj.elements[radioName][n].value + "\n" +
                      "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
                if (formObj.elements[radioName][n].value != formObj.elements[hdnFieldName].value) {
                  thisChanged = true;
                  break;
                }
              }
            }
          }
          else if (formObj.elements[i].type == "checkbox") {
            var checkboxName = formObj.elements[i].name;
            
            var hdnCheckboxList = formObj.elements[hdnFieldName].value.split(",");
            
            for (var n=0; n<formObj.elements[checkboxName].length; n++) {
              if (formObj.elements[checkboxName][n].checked) {
                var isCheckedPreviously = false;
                
                for (var p=0; p<hdnCheckboxList; p++) {
                  if (formObj.elements[checkboxName][n].value == hdnCheckboxList[p])
                    isCheckedPreviously = true;
                }
                
                alert("isCheckedPreviously = " + isCheckedPreviously);
                if (!isCheckedPreviously) {
                  thisChanged = true;
                  break;
                }
              }
            }
          }
          else if (formObj.elements[i].type == "select-one") {
            alert("formObj.elements[" + i + "][" + formObj.elements[i].selectedIndex + "].value = " + formObj.elements[i][formObj.elements[i].selectedIndex].value + "\n" +
                  "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
            if (formObj.elements[hdnFieldName].value != formObj.elements[i][formObj.elements[i].selectedIndex].value) {
              thisChanged = true; 
              break;
            }
          }
          else {
            alert("formObj.elements[" + i + "].value = " + formObj.elements[i].value + "\n" +
                  "formObj.elements[" + hdnFieldName + "].value = " + formObj.elements[hdnFieldName].value);
            if (formObj.elements[hdnFieldName].value != formObj.elements[i].value) {
              thisChanged = true;
              break;
            }
          }
        }
      }
    }
    
    //alert("thisChanged = " + thisChanged);
    
    return thisChanged;
  },
  
  removeHiddenFields: function (formObj) {
    alert("removeHiddenFields");
    for (var i=0; i<formObj.elements.length; i++) {
      if (formObj.elements[i].type == 'hidden') {
        dojo.dom.removeNode(formObj.elements[i]);
      }
    }
  },
  
  addItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      alert("array[" + i + "] = " + array[i] + "\n" +
            "item = " + item);
      if (array[i] == item) 
        foundItem = true;
    }
    
    alert("foundItem = " + foundItem);
    
    if (!foundItem)
      array.push(item);
  },
  
  isItemInArray: function (array, item) {
    var foundItem = false;
    for (var i=0; i<array.length; i++) {
      if (array[i] == item) 
        foundItem = true;
    }
    
    if (foundItem)
      return true;
    else
      return false;
  },
  
  selectAllCheckboxes: function (srcObj, targetCheckboxObj) {
    if (srcObj.checked) {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = true;
      }
    }
    else {
      for (var i=0; i<targetCheckboxObj.length; i++) {
        targetCheckboxObj[i].checked = false;
      }
    }
  },
  
  selectCheckboxPerCollection: function (srcObj, collectionObj) {
    var count = 0;
    
    for (var i=0; i<collectionObj.length; i++) {
      if (collectionObj[i].checked)
        count++;
    }
    
    srcObj.checked = (count == collectionObj.length) ? true : false;
  }
}
