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

var _ldc;
var _savedSearchDlg;

dojo.addOnLoad(function() {
  // ---------------------
  // saved search dialog related
  // ---------------------
  _ldc = dijit.byId("LoadingCycle");

  _savedSearchDlg = dijit.byId("SavedSearch");

  dojo.connect(dojo.byId("btn_save_savedsearch"), "onclick", function(e) {
    e.preventDefault();
    ambra.savedSearch.save(function() {
      ambra.savedSearch.hide();
    });
    return false;
  });

  dojo.connect(dojo.byId("btn_cancel_savedsearch"), "onclick", function(e) {
    e.preventDefault();
    ambra.savedSearch.hide();
    return false;
  });
});