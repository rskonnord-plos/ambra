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

var responseConfig = {
  responseForm :"discussionResponse",
  discussionContainer :"discussionContainer"
};

var _dcr = new Object();
var _ldc;

dojo.addOnLoad( function() {
  _ldc = dijit.byId("LoadingCycle");

  _dcr.widget = dojo.byId("DiscussionPanel");
  _dcr.btnCancel = dojo.byId("btnCancelResponse");
  _dcr.btnSubmit = dojo.byId("btnPostResponse");
  _dcr.form = document.discussionResponse;
  _dcr.formAction = "/annotation/secure/createDiscussionSubmit.action";
  _dcr.responseTitleCue = "Enter your comment title...";
  _dcr.responseCue = "Enter your comment...";
  _dcr.error = dojo.byId('responseSubmitMsg');
  _dcr.requestType = "new";
  _dcr.baseId = _dcr.form.target.value;
  _dcr.replyId = _dcr.form.target.value;
  var responseTitle = _dcr.form.responseTitle;
  var responseArea = _dcr.form.responseArea;

  dojo.connect(_dcr.btnSubmit, "onclick", function(e) {
    ambra.responsePanel.submit(_dcr);
  });

  dojo.connect(responseTitle, "onfocus", function(e) {
    ambra.formUtil.textCues.off(responseTitle, _dcr.responseTitleCue);
  });

  dojo.connect(responseArea, "onfocus", function(e) {
    ambra.formUtil.textCues.off(responseArea, _dcr.responseCue);
  });

  dojo.connect(responseTitle, "onblur", function(e) {
    var fldResponseTitle = _dcr.form.commentTitle;
    if (responseTitle.value != "" && responseTitle.value != _dcr.responseCue) {
      fldResponseTitle.value = responseTitle.value;
    } else {
      fldResponseTitle.value = "";
    }
    ambra.formUtil.textCues.on(responseTitle, _dcr.responseTitleCue);
  });

  dojo.connect(responseArea, "onblur", function(e) {
    var fldResponse = _dcr.form.comment;
    if (responseArea.value != "" && responseArea.value != _dcr.responseCue) {
      fldResponse.value = responseArea.value;
    } else {
      fldResponse.value = "";
    }
    ambra.formUtil.textCues.on(responseArea, _dcr.responseCue);
  });

  dojo.connect(responseTitle, "onchange", function(e) {
    var fldResponseTitle = _dcr.form.commentTitle;
    if (responseTitle.value != "" && responseTitle.value != _dcr.responseCue) {
      fldResponseTitle.value = responseTitle.value;
    } else {
      fldResponseTitle.value = "";
    }
  });

  dojo.connect(responseArea, "onchange", function(e) {
    var fldResponse = _dcr.form.comment;
    if (responseArea.value != "" && responseArea.value != _dcr.responseCue) {
      fldResponse.value = responseArea.value;
    } else {
      fldResponse.value = responseArea.value;
    }
  });

});
