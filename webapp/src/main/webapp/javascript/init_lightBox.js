/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// lightBox related globals
var _ldc;

var _lightBoxDlg;

var _dij;

dojo.addOnLoad(function() {
  // ---------------------
  // lightBox dialog related
  // ---------------------
  _ldc = dijit.byId("LoadingCycle");

  _lightBoxDlg = dijit.byId("LightBox");

  //clicking outside the dialog box closes the lightbox.
  if(dijit._underlay === undefined)
  {
    dijit._underlay = new dijit.DialogUnderlay();
  }

  _dij = dijit._underlay.domNode;

  dojo.connect(_dij, "onclick", function (e) {
    ambra.lightBox.hide();
  });

  //hitting escape button will close the lightbox
  dojo.connect( _lightBoxDlg.containerNode, "onkeypress", function(e) {
    key = e.keyCode;
    if (key == dojo.keys.ESCAPE) {
      ambra.lightBox.hide();
    }
  });

  //hiding the lightbox will enable the scrolling of parent window
  dojo.connect(_lightBoxDlg, 'hide', function() {
    dojo.style(dojo.body(), 'overflow', 'auto');
  });

  //showing the lightbox will disable the scrolling of parent window
  dojo.connect(_lightBoxDlg, 'show', function() {
    dojo.style(dojo.body(), 'overflow', 'hidden');
  });

  var query = window.location.href.split("?");
  if (query.length > 1) {
    var query = dojo.queryToObject(query[1]);
    if(query['imageURI']){
      var index = query['imageURI'].lastIndexOf(".");
      if (index > 0) {
        var target = query['imageURI'].substr(0, index);
        ambra.lightBox.show(target, query['imageURI']);
      }
    }
  }

});
