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
dojo.provide("ambra.savedSearch");
dojo.require("ambra.general");
dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");

ambra.savedSearch = {

  init: function() {
  },

  /**
   * clicking on save search link will show the dialog box.
   */
  show: function(action){
    console.log("saved search show");
    var searchEdit = dojo.byId("searchEdit");
    if (searchEdit && searchEdit.value) {
      dojo.byId("text_name_savedsearch").value = searchEdit.value;
    }

    dojo.byId("span_error_savedsearch").innerHTML = "";

    _savedSearchDlg.show();
    return false;
  },

  /**
   * clicking on cancel button will hide the dialog box.
   */
  hide: function() {
    console.log("saved search hide");
    dojo.byId("span_error_savedsearch").innerHTML = "";
    _savedSearchDlg.hide();
  },

  /**
   * clicking on save button will save the form data.
   */
  save: function(success_callback) {
    //Copy values up to the parent form
    dojo.byId("searchName").value = dojo.byId("text_name_savedsearch").value;
    dojo.byId("weekly").value = dojo.byId("cb_weekly_savedsearch").checked;
    dojo.byId("monthly").value = dojo.byId("cb_monthly_savedsearch").checked;

    var xhrArgs = {
      url: "/search/saveSearch.action",
      //Grab all for form values from the parent form
      form: dojo.byId("searchFormOnSearchResultsPage"),
      //postData: JSON.stringify(content),
      handleAs:'json-comment-filtered',
      load: function(response, ioArgs) {
        if(response.exception) {
          var errorMessage = response.exception.message;

          dojo.byId("span_error_savedsearch").innerHTML = "Exception: " + errorMessage;

          return;
        }

        if (response.actionErrors && response.actionErrors.length > 0) {
          //The action in question can only return one message
          var errorMessage = response.actionErrors[0];

          dojo.byId("span_error_savedsearch").innerHTML = "Error: " + errorMessage;
          return;
        }

        _savedSearchDlg.hide();
      },
      error: function(response, ioArgs) {
        dojo.byId("span_error_savedsearch").innerHTML = response;
        console.log("failed with error: " + response);
      }
    }

    dojo.xhrPost(xhrArgs);
  }
}
