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
 * ambra.advsearch
 * Advanced search methods.
 * @author jkirton (jopaki@gmail.com)
 **/
//dojo.provide("ambra.advsearch");
ambra.advsearch = {
	Config: {
	  idAuthNmePrototype:'as_anp',
	  idOlAuthNmes:'as_ol_an',
	  idInptAuthNme:'authorName',
	  idLiAuthNmesOpts:'as_an_opts',

	  idSpnRmvAuthNme:'as_spn_ra',
	  idLnkRmvAuthNme:'as_a_ra',
	  idLnkAddAuthNme:'as_a_aa',
	  idSpnSpcr:'as_a_spcr',

	  idPublishDate:'dateSelect',
	  idPubDateOptions:'pubDateOptions',

	  idSubjCatsAll:'subjectCatOpt_all',
	  idSubjCatsSlct:'subjectCatOpt_slct',
	  idFsSubjectCatOpt:'fsSubjectCatOpt',

	  maxNumAuthNames: 10
	},
  authNmeProto:null,
  olAuthNmes:null,
  liAuthNmeOptions:null,
  liAuthNmesOpts:null,

  init: function() {
    // search by author section...
    ambra.advsearch.authNmeProto = dojo.byId(ambra.advsearch.Config.idAuthNmePrototype);
    ambra.advsearch.olAuthNmes = dojo.byId(ambra.advsearch.Config.idOlAuthNmes);
    ambra.advsearch.liAuthNmeOptions = dojo.query('.options', ambra.advsearch.olAuthNmes)[0];
    ambra.advsearch.liAuthNmesOpts = dojo.byId(ambra.advsearch.Config.idLiAuthNmesOpts);


    //Manage date entry section, but only if is defined
    if (dojo.byId(ambra.advsearch.Config.idPubDateOptions) != null) {
      var slct = dojo.byId(ambra.advsearch.Config.idPublishDate);
      var showDates = (slct.options[slct.selectedIndex].value == 'range');
      dojo.byId(ambra.advsearch.Config.idPubDateOptions).style.display = showDates ? '' : 'none';
      dojo.connect(dojo.byId(ambra.advsearch.Config.idPublishDate), "onchange", ambra.advsearch.onChangePublishDate);

      // date part comment cue event bindings...
      dojo.connect(dojo.byId('startDateId'), "onfocus", ambra.advsearch.onFocusCommentCueInputHandler);
      dojo.connect(dojo.byId('endDateId'), "onfocus", ambra.advsearch.onFocusCommentCueInputHandler);
    }

    // subject categories section...
    if(document.selection) {
      // IE
      dojo.connect(dojo.byId(ambra.advsearch.Config.idSubjCatsAll), "onclick", ambra.advsearch.onChangeSubjectCategories);
      dojo.connect(dojo.byId(ambra.advsearch.Config.idSubjCatsSlct), "onclick", ambra.advsearch.onChangeSubjectCategories);
    } else {
      // gecko et al.
      dojo.connect(dojo.byId(ambra.advsearch.Config.idSubjCatsAll), "onchange", ambra.advsearch.onChangeSubjectCategories);
      dojo.connect(dojo.byId(ambra.advsearch.Config.idSubjCatsSlct), "onchange", ambra.advsearch.onChangeSubjectCategories);
    }

    // hijack form submission for validation...
    dojo.connect(dojo.byId('button-search'), "onclick", ambra.advsearch.onSubmitHandler);

    ambra.advsearch.liAuthNmesOpts.style.display = 'none';
    ambra.advsearch.tglSubCategories();

    ambra.advsearch.explodeAuthNames();

    //Prime the startDate and endDate pop-up calendars only if they are defined 
    if (dojo.byId(ambra.advsearch.Config.idPubDateOptions) != null) {
      Calendar.setup({
        inputField     :    "startDateId",    // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
      });
      Calendar.setup({
        inputField     :    "endDateId",      // id of the input field
        ifFormat       :    "%Y-%m-%d",       // format of the input field
        step           :    1                // show all years in drop-down boxes (instead of every other year as default)
      });
    }
  },

  onSubmitHandler: function(e) {
    ambra.advsearch.handleSubmit();
    dojo.stopEvent(e);
    return false;
  },

  handleSubmit: function() {
    var errs = this.validate();
    if(errs.length > 0) {
      var msg = '';
      for(var i=0; i<errs.length; i++) {
        msg += errs[i] + '\r\n';
      }
      alert(msg);
      return;
    }
    document.advSearchForm.submit();
  },

  // validates the adv search form data
  // returns array of error messages
  validate: function() {
    var errs = [];

    // validate published date range (if applicable)
    if (dojo.byId(ambra.advsearch.Config.idPubDateOptions) != null) {
      var slct = dojo.byId(ambra.advsearch.Config.idPublishDate);
      if(slct.options[slct.selectedIndex].value == 'range') {
        var startDate = dojo.byId('startDateId');
        var endDate = dojo.byId('endDateId');
  
        var startDateAsDate = new Date(startDate.value.replace(/-/g, "/"));
        var endDateAsDate = new Date(endDate.value.replace(/-/g, "/"));
  
        //  Make sure there is a Start date and an End date.
        if (isNaN(startDateAsDate.getMilliseconds()) && isNaN(endDateAsDate.getMilliseconds())) {
          errs.push("Please choose valid start and end dates.");
        }
        else if (isNaN(startDateAsDate.getMilliseconds())) {
          errs.push("Please choose a valid start date.");
        }
        else if (isNaN(endDateAsDate.getMilliseconds())) {
          errs.push("Please choose a valid end date.");
        }
        //  Make sure the Start date is before the End date.
        else if (startDateAsDate.getTime() > endDateAsDate.getTime()) {
          errs.push("The end date must occur after the start date.");
        }
      }
    }

    return errs;
  },

  getCueText: function(inptId) {
    if(inptId.indexOf(ambra.advsearch.Config.idMonthPart) > 0) {
      return ambra.advsearch.Config.monthCue;
    }
    else if(inptId.indexOf(ambra.advsearch.Config.idDayPart) > 0) {
      return ambra.advsearch.Config.dayCue;
    }
    else {
      return ambra.advsearch.Config.yearCue;
    }
  },

  onFocusCommentCueInputHandler: function(e) {
    ambra.advsearch.onFocusCommentCueInput(e.target);
    dojo.stopEvent(e);
    return false;
  },

  onBlurCommentCueInputHandler: function(e) {
    ambra.advsearch.onBlurCommentCueInput(e.target);
    dojo.stopEvent(e);
    return false;
  },

  onBlurCommentCueInput: function(inpt) {
    if(inpt.value == '') inpt.value = this.getCueText(inpt.id);
  },

  onFocusCommentCueInput: function(inpt) {
    if(inpt.value == this.getCueText(inpt.id)) inpt.value = '';
  },

  onChangePublishDate: function(e) {
    var slct = e.target;
    var show = (slct.options[slct.selectedIndex].value == 'range');
    dojo.byId(ambra.advsearch.Config.idPubDateOptions).style.display = (show ? '' : 'none');
    dojo.stopEvent(e);
    return false;
  },

  onChangeSubjectCategories: function(e) {
    ambra.advsearch.tglSubCategories();
    return true;
  },

  tglSubCategories: function() {
    var rbAll = dojo.byId(ambra.advsearch.Config.idSubjCatsAll);
    var rbSlct = dojo.byId(ambra.advsearch.Config.idSubjCatsSlct);
    var enable = rbSlct.checked;
    var fs = dojo.byId(ambra.advsearch.Config.idFsSubjectCatOpt);
    if (enable) ambra.formUtil.enableFormFields(fs); else ambra.formUtil.disableFormFields(fs);
  },

  // get the 1-based ordinal number for the author name list element associated with
  // a given child element
  _getAuthNmeNum: function(child) {
    var id = child.id;
    var num;
    var indx = id.lastIndexOf('__');
    if(indx > 0) {
      var num = parseInt(id.substr(indx+2));
      return isNaN(num) ? 1 : num;
    }
    return 1;
  },

  _assembleId: function(idTmplte, num) {
    return (!num || num == 1) ? idTmplte : (idTmplte + '__' + num);
  },

  // resursively sets the relevant ids for an auth name node set
  _setAuthNmeCopyIds: function(n, num) {
    if(n.id && n.id.length > 0) {
      var idi = n.id.lastIndexOf('__');
      if(idi >0) n.id = n.id.substring(0, idi);
      n.id = n.id + '__' + num;
    }
    if(n.nodeType == 1 && n.childNodes.length > 0) {
      var cns = n.childNodes;
      for(var i=0; i<cns.length; i++) this._setAuthNmeCopyIds(cns[i], num);
    }
  },

  // handles adding additional auth names
  onClickAddAuthNameHandler: function(e) {
    dojo.fixEvent(e);
    ambra.advsearch.addAuthName(e.target);
    dojo.stopEvent(e);
  },

  // handles removing previously added auth names
  onClickRmvAuthNameHandler: function(e) {
    dojo.fixEvent(e);
    ambra.advsearch.rmvAuthName(e.target);
    dojo.stopEvent(e);
  },

  _handleAddError: function(msg, elmInpt) {
    alert(msg);
    elmInpt.focus();
  },

  addAuthName: function(lnkAddCrnt) {
    var num = this._getAuthNmeNum(lnkAddCrnt);
    var inpt = dojo.byId(this._assembleId(ambra.advsearch.Config.idInptAuthNme, num));
    if(inpt.value == '') {
      this._handleAddError('Specify an Author Name to add another.', inpt);
      return;
    }
    else if(num >= ambra.advsearch.Config.maxNumAuthNames) {
      this._handleAddError('Only ' + ambra.advsearch.Config.maxNumAuthNames + ' Author Names are allowed.', inpt);
      return;
    }
    lnkAddCrnt.style.display = 'none';
    var spnSpcr = dojo.byId(this._assembleId(ambra.advsearch.Config.idSpnSpcr, num));
    spnSpcr.style.display = 'none';
    num++;

    var cln = this.authNmeProto.cloneNode(true);
    this._setAuthNmeCopyIds(cln, num);

    // clear input value in clone
    var inpt = cln.getElementsByTagName('input')[0];
    inpt.value = '';

    // insert clone under a new li tag under the auth names list
    var li = document.createElement('li');
    var lbl = document.createElement('label');
    lbl.appendChild(document.createTextNode(' '));
    li.appendChild(lbl);
    li.appendChild(cln);
    this.olAuthNmes.insertBefore(li, this.liAuthNmeOptions);
    inpt.focus();

    var spnRmv = dojo.byId(this._assembleId(ambra.advsearch.Config.idSpnRmvAuthNme, num));
    var lnkRmv = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkRmvAuthNme, num));
    var lnkAdd = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkAddAuthNme, num));
    spnSpcr = dojo.byId(this._assembleId(ambra.advsearch.Config.idSpnSpcr, num));

    spnRmv.style.display = '';
    lnkAdd.style.display = '';
    spnSpcr.style.display = '';

    this.liAuthNmesOpts.style.display = '';
  },

  rmvAuthName: function(lnkRmvCrnt) {
    var num = this._getAuthNmeNum(lnkRmvCrnt);
    if(num < 1) return;

    // seek the parent li node to remove
    var liToRmv = lnkRmvCrnt;
    while(liToRmv.nodeName != 'LI') liToRmv = liToRmv.parentNode;
    var isLast = dojo.hasClass(liToRmv.nextSibling, 'options');

    // restore links above
    if(--num == 1) {
      if(isLast) {
        lnkAdd = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }
    else {
      var lnkRmv = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkRmvAuthNme, num));
      lnkRmv.style.display = '';
      var isLast = dojo.hasClass(liToRmv.nextSibling, 'options');
      if(isLast) {
        var spnSpcr = dojo.byId(this._assembleId(ambra.advsearch.Config.idSpnSpcr, num));
        spnSpcr.style.display = '';
        var lnkAdd = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkAddAuthNme, num));
        lnkAdd.style.display = '';
      }
    }

    // kill it
    liToRmv.parentNode.removeChild(liToRmv);

    // reset the ids
    var cns = this.olAuthNmes.getElementsByTagName('li');
    var num = 0;
    for(var i=1; i<cns.length; i++) {
      var li = cns[i];
      if(dojo.hasClass(li, 'options')) break;
      num++;
      this._setAuthNmeCopyIds(li, i+1);
    }

    this.liAuthNmesOpts.style.display = (num>0 ? '' : 'none');
  },

  // auto-adds auth name edit fields based on the current value in the initial auth name edit field
  explodeAuthNames: function() {
    var fan = dojo.byId(this._assembleId(ambra.advsearch.Config.idInptAuthNme));
    var auths = fan.value;
    if(!auths || auths.length < 1) return;
    var j, arr = auths.split(','), auth, lnkAdd;
    if(arr.length > 1) {
      for(var i=0; i<arr.length; i++) {
        auth = arr[i];
        j = i + 1;
        if(i>0) this.addAuthName(lnkAdd);
        lnkAdd = dojo.byId(this._assembleId(ambra.advsearch.Config.idLnkAddAuthNme, j));
        dojo.byId(this._assembleId(ambra.advsearch.Config.idInptAuthNme, j)).value = auth;
      }
    }
  }

};
dojo.addOnLoad(function() { ambra.advsearch.init(); });