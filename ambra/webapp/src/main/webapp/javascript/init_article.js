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

// the "loading..." widget
var _ldc;

// rating related globals
var ratingConfig =  {
  insight:  "rateInsight",
  reliability: "ratingReliability",
  style: "rateStyle"
};
var _ratingDlg;
var _ratingsForm;
var _ratingTitle;
var _ratingComments;

// annotation related globals
var annotationConfig = {
  articleContainer: "articleContainer",
  rhcCount: "dcCount",
  trigger: "addAnnotation",
  lastAncestor: "researchArticle",
  xpointerMarker: "xpt",
  // NOTE: 'note-pending' class is used to identify js-based annotation 
  //  related document markup prior to persisting the annotation
  annotationMarker: "note note-pending",
  pendingAnnotationMarker: 'note-pending',
  annotationImgMarker: "noteImg",
  regionalDialogMarker : "rdm",
  excludeSelection: "noSelect",
  tipDownDiv: "dTip",
  tipUpDiv: "dTipu",
  isAuthor: false,  //TODO: *** Default to false when the hidden input is hooked up.
  isPublic: true,
  dfltAnnSelErrMsg: 'This area of text cannot be notated.',
  annSelErrMsg: null,
  rangeInfoObj: new Object(),
  annTypeMinorCorrection: 'MinorCorrection',
  annTypeFormalCorrection: 'FormalCorrection',
  styleMinorCorrection: 'minrcrctn', // generalized css class name for minor corrections
  styleFormalCorrection: 'frmlcrctn' // generalized css class name for formal corrections
};
var formalCorrectionConfig = {
  styleFormalCorrectionHeader: 'fch', // css class name for the formal correction header node
  fchId: 'fch', // the formal correction header node dom id
  fcListId: 'fclist', // the formal correction header sub-node referencing the ordered list
  annid: 'annid' // dom node attribute name to use to store annotation ids 
};
var _annotationDlg;
var _annotationForm;

// comment/multi-comment globals
var commentConfig = {
  cmtContainer: "cmtContainer",
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment", 
  sectionLink: "viewLink", 
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "cTip",
  tipUpDiv: "cTipu"
};  
var multiCommentConfig = {
  sectionTitle: "viewCmtTitle",
  sectionDetail: "viewCmtDetail",  
  sectionComment: "viewComment",  
  retrieveMsg: "retrieveMsg",  
  tipDownDiv: "mTip",
  tipUpDiv: "mTipu"
};  
var _commentDlg;
var _commentMultiDlg;

var _titleCue          = 'Enter your note title...';
var _commentCue        = 'Enter your note...';
var _ratingTitleCue    = 'Enter your comment title...';
var _ratingCommentCue   = 'Enter your comment...';

var elLocation;

/*
var activeToggleId = "";
var activeWidget = "";

function setActiveToggle(widgetId, boxId) {
  activeToggleId = boxId;
  activeWidget = dojo.byId(widgetId);
}

function singleView(obj) {
  if (activeToggleId != "") {
    ambra.domUtil.swapDisplayMode(activeToggleId, "none");
    toggleExpand(activeWidget, false); 
  }
}

function singleExpand(obj, targetId) {
  if (targetId != activeToggleId) {
    singleView(obj);
  }
  setActiveToggle
   (obj.id, targetId);
  ambra.domUtil.swapDisplayMode(targetId);
  toggleExpand(obj); 
}
*/

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
}

function getAnnotationEl(annotationId) {
  var elements = document.getElementsByTagAndAttributeName('a', 'displayid');
  var targetEl;
  for (var i=0; i<elements.length; i++) {
    var elDisplay = ambra.domUtil.getDisplayId(elements[i]);
    var displayList = elDisplay.split(',');
    for (var n=0; n<displayList.length; n++) {
      if (displayList[n] == annotationId) {
        targetEl = elements[i];
        return targetEl;
      }
    }
  }
  return null;
}

function jumpToAnnotation(annotationId) {
  if(!annotationId) return;
  var anNode = getAnnotationEl(annotationId);
  if(anNode) jumpToElement(anNode);
}

function toggleExpand(obj, isOpen, textOn, textOff) {
  if (isOpen == false) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else if (obj.className.match('collapse')) {
    obj.className = obj.className.replace(/collapse/, "expand");
    if (textOn) dojox.data.dom.textContent(obj, textOn);
  }
  else {
    obj.className = obj.className.replace(/expand/, "collapse");
    if (textOff) dojox.data.dom.textContent(obj, textOff);
  }
  
}

function showAnnotationDialog() {
   // reset
  _annotationForm.cNoteType.selectedIndex = 0;
  dojo.byId('cdls').style.visibility = 'hidden';
  _annotationDlg.show();
}

function validateNewComment() {
  var submitMsg = dojo.byId('submitMsg');
  ambra.domUtil.removeChildren(submitMsg);
  ambra.formUtil.disableFormFields(_annotationForm);
  
  _annotationForm.noteType.value = _annotationForm.cNoteType.value;
  
  dojo.xhrPost({
     url: _namespace + "/annotation/secure/createAnnotationSubmit.action",
     handleAs:'json-comment-filtered',
     form: _annotationForm,
     error: function(response, ioArgs){
       handleXhrError(response, ioArgs);
       ambra.formUtil.enableFormFields(_annotationForm);
     },
     load: function(response, ioArgs){
       var jsonObj = response;
       if(jsonObj.actionErrors.length > 0) {
         var errorMsg = "";
         for (var i=0; i<jsonObj.actionErrors.length; i++) {
           errorMsg += jsonObj.actionErrors[i] + "\n";
         }
         var err = document.createTextNode(errorMsg);
         submitMsg.appendChild(err);
         ambra.formUtil.enableFormFields(_annotationForm);
         _annotationDlg.placeModalDialog();
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
         ambra.formUtil.enableFormFields(_annotationForm);
         _annotationDlg.placeModalDialog();
       }
       else {
         _annotationDlg.hide();
         ambra.formUtil.textCues.reset(_annotationForm.cTitle, _titleCue);
         ambra.formUtil.textCues.reset(_annotationForm.cArea, _commentCue);
         ambra.formUtil.enableFormFields(_annotationForm);
         // remember the newly added annotation
         document.articleInfo.annotationId.value = jsonObj.annotationId;
         // re-fetch article body
         getArticle();
         markDirty(true); // set dirty flag (this ensures a later re-visit of this page will pull fresh article data from the server rather than relying on the browser cache) 
       }
     }//load
  });
}

/**
 * Quasi-unique cookie name to use for storing article dirty flag.
 */
var dirtyToken = '@__sra__@';

/**
 * Determines whether the article was marked as dirty or not.
 * @return true/false
 */
function shouldRefresh() { return (dojo.cookie(dirtyToken) == 'a'); }

/**
 * Marks or un-marks the article as "dirty" via a temporary browser cookie.
 * @param dirty true/false  
 */
function markDirty(dirty) { dojo.cookie(dirtyToken,dirty?'a':'b'); }

/**
 * getArticle
 * 
 * Re-fetches the article from the server 
 * refreshing the article content area(s) of the page.
 */
function getArticle() {
  _ldc.show();
  dojo.xhrGet({
    url: _namespace + "/article/fetchBody.action?articleURI=" + _annotationForm.target.value,
    handleAs:'text',
    error: function(response, ioArgs){
      handleXhrError(response);
    },
    
    load: function(response, ioArgs){
      // refresh article HTML content
      dojo.byId(annotationConfig.articleContainer).innerHTML = response;
      // re-apply article "decorations"
      getAnnotationCount();
      ambra.displayComment.processBugCount();
      ambra.corrections.apply();
      document.articleInfo.annotationId.value = ''; // reset
      _ldc.hide();
    }
  });
}

function getAnnotationCount() {
  var refreshArea1 = dojo.byId(annotationConfig.rhcCount + "1");
  var targetUri = _annotationForm.target.value;
  dojo.xhrGet({
    url: _namespace + "/article/fetchArticleRhc.action?articleURI=" + targetUri,
    handleAs:'text',
    error: function(response, ioArgs){
      handleXhrError(response, ioArgs);
    },
    load: function(response, ioArgs){
      var docFragment = document.createDocumentFragment();
      docFragment = response;
      refreshArea1.innerHTML = docFragment;
    }
  });
}

/** 
 * createAnnotationOnMouseDown()
 * 
 * Method triggered on onmousedown or onclick event of a tag.  When this method is 
 * triggered, it initiates an annotation creation using the currently-selected text.
 */
function createAnnotationOnMouseDown() {
  // reset form
  ambra.formUtil.textCues.reset(_annotationForm.cTitle, _titleCue); 
  ambra.formUtil.textCues.reset(_annotationForm.cArea, _commentCue); 
  _annotationForm.noteType.value = "";
  _annotationForm.commentTitle.value = "";
  _annotationForm.comment.value = "";
  // create it
  ambra.annotation.createNewAnnotation();
  return false;
}

dojo.addOnLoad(function() {
  // int loading "throbber"
  _ldc = dijit.byId("LoadingCycle");
  
  // ---------------------
  // rating dialog related
  // ---------------------
  _ratingsForm = document.ratingForm;
  _ratingTitle = _ratingsForm.cTitle;
  _ratingComments = _ratingsForm.cArea;
  _ratingDlg = dijit.byId("Rating");
  //_ratingDlg.setCloseControl(dojo.byId('btn_cancel_rating'));
  
  dojo.connect(_ratingTitle, "onfocus", function () { 
    ambra.formUtil.textCues.off(_ratingTitle, _ratingTitleCue); 
  });
  
  dojo.connect(_ratingTitle, "onchange", function () {
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
  });

  dojo.connect(_ratingTitle, "onblur", function () { 
    var fldTitle = _ratingsForm.commentTitle;
    if(_ratingsForm.cTitle.value != "" && _ratingsForm.cTitle.value != _ratingTitleCue) {
      fldTitle.value = _ratingsForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
    ambra.formUtil.textCues.on(_ratingTitle, _ratingTitleCue); 
  });
  
  dojo.connect(_ratingComments, "onfocus", function () {
    ambra.formUtil.textCues.off(_ratingComments, _ratingCommentCue);
  });

  dojo.connect(_ratingComments, "onchange", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
  });
  
  dojo.connect(_ratingComments, "onblur", function () {
    var fldTitle = _ratingsForm.comment;
    if(_ratingsForm.cArea.value != "" && _ratingsForm.cArea.value != _ratingCommentCue) {
      fldTitle.value = _ratingsForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
    ambra.formUtil.textCues.on(_ratingComments, _ratingCommentCue); 
    //ambra.formUtil.checkFieldStrLength(_ratingComments, 500);
  });
  
  dojo.connect(dojo.byId("btn_post_rating"), "onclick", function(e) {
    updateRating();
    e.preventDefault();
    return false;
  });

  dojo.connect(dojo.byId("btn_cancel_rating"), "onclick", function(e) {
    ambra.rating.hide();
    e.preventDefault();
    return false;
  });
  
  // --------------------------------
  // annotation (note) dialog related
  // --------------------------------
  _annotationForm = document.createAnnotation;
  
  dojo.connect(_annotationForm.cNoteType, "change", function () {
    dojo.byId('cdls').style.visibility = _annotationForm.cNoteType.value == 'correction' ? 'visible' : 'hidden';
  });
  
  dojo.connect(_annotationForm.cTitle, "focus", function () { 
    ambra.formUtil.textCues.off(_annotationForm.cTitle, _titleCue); 
  });
  
  dojo.connect(_annotationForm.cTitle, "change", function () {
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
  });

  dojo.connect(_annotationForm.cTitle, "blur", function () { 
    var fldTitle = _annotationForm.commentTitle;
    if(_annotationForm.cTitle.value != "" && _annotationForm.cTitle.value != _titleCue) {
      fldTitle.value = _annotationForm.cTitle.value;
    }
    else {
      fldTitle.value = "";
    }
    ambra.formUtil.textCues.on(_annotationForm.cTitle, _titleCue); 
  });
  
  dojo.connect(_annotationForm.cArea, "focus", function () {
    ambra.formUtil.textCues.off(_annotationForm.cArea, _commentCue);
  });

  dojo.connect(_annotationForm.cArea, "change", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
  });
  
  dojo.connect(_annotationForm.cArea, "blur", function () {
    var fldTitle = _annotationForm.comment;
    if(_annotationForm.cArea.value != "" && _annotationForm.cArea.value != _commentCue) {
      fldTitle.value = _annotationForm.cArea.value;
    }
    else {
      fldTitle.value = "";
    }
    ambra.formUtil.textCues.on(_annotationForm.cArea, _commentCue); 
    //ambra.formUtil.checkFieldStrLength(_annotationForm.cArea, 500);
  });
  
  /*
  dojo.connect(privateFlag, "onclick", function() {
    ambra.formUtil.toggleFieldsByClassname('commentPrivate', 'commentPublic'); 
    _annotationDlg.placeModalDialog();
    //var btn = btnAnnotationSave;
    //__annotationDlg.setCloseControl(btn);
  });
  dojo.connect(publicFlag, "onclick", function() {
    ambra.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate'); 
    _annotationDlg.placeModalDialog();
    //_annotationDlg.setCloseControl(dojo.byId("btn_post"));
  });
  
  // Annotation Dialog Box: Save button
  dojo.connect(btnAnnotationSave, "onclick", function(e) {
    //getCommentValues();
    validateNewComment();
    e.preventDefault();
  });
  */
  
  dojo.connect(dojo.byId("btn_post"), "click", function(e) {
    validateNewComment();
    e.preventDefault();
    return false;
  });

  dojo.connect(dojo.byId("btn_cancel"), "click", function(e) {
    ambra.domUtil.removeChildren(dojo.byId('submitMsg'));
    _annotationDlg.hide();
    ambra.formUtil.enableFormFields(_annotationForm);
    if(!annotationConfig.rangeInfoObj.isSimpleText) {
      // we are in an INDETERMINISTIC state for annotation markup
      // Article re-fetch is necessary to maintain the integrity of the existing annotation markup
      getArticle();
    }
    else {
      // we can safely rollback the pending annotation markup from the dom
      ambra.annotation.undoPendingAnnotation();
    }
    e.preventDefault();
    return false;
  });

  ambra.formUtil.toggleFieldsByClassname('commentPublic', 'commentPrivate');
  
  _annotationDlg = dijit.byId("AnnotationDialog");
  //var dlgCancel = dojo.byId('btn_cancel');
  //_annotationDlg.setCloseControl(dlgCancel);
  _annotationDlg.setTipDown(dojo.byId(annotationConfig.tipDownDiv));
  _annotationDlg.setTipUp(dojo.byId(annotationConfig.tipUpDiv));

  
  // -------------------------
  // comment dialog related
  // -------------------------
  _commentDlg = dijit.byId("CommentDialog");
  var commentDlgClose = dojo.byId('btn_close');
  //_commentDlg.setCloseControl(commentDlgClose);
  _commentDlg.setTipDown(dojo.byId(commentConfig.tipDownDiv));
  _commentDlg.setTipUp(dojo.byId(commentConfig.tipUpDiv));
  
  dojo.connect(commentDlgClose, 'click', function(e) {
    _commentDlg.hide();
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
    return false;
  });
  
  // -------------------------
  // multi-comment dialog related
  // -------------------------
  _commentMultiDlg = dijit.byId("CommentDialogMultiple");
  var popupCloseMulti = dojo.byId('btn_close_multi');
  //_commentMultiDlg.setCloseControl(popupCloseMulti);
  _commentMultiDlg.setTipDown(dojo.byId(multiCommentConfig.tipDownDiv));
  _commentMultiDlg.setTipUp(dojo.byId(multiCommentConfig.tipUpDiv));
  
  dojo.connect(popupCloseMulti, 'click', function(e) {
    _commentMultiDlg.hide();
    ambra.displayComment.mouseoutComment(ambra.displayComment.target);
    return false;
  });
  
  // init routines
  ambra.rating.init();
  ambra.displayComment.init();
  ambra.displayComment.processBugCount();
  ambra.corrections.apply();

  // jump to annotation?
  jumpToAnnotation(document.articleInfo.annotationId.value);

  // re-fetch article if "dirty" for firefox only as their page cache is not updated via xhr based dom alterations. 
  if(dojo.isFF && shouldRefresh()) getArticle();
  
  markDirty(false);	// unset dirty flag
});
