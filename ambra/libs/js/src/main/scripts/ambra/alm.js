/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
 * ambra.alm
 *
 * This class has utilities for fetching data from the ALM application.
 **/

dojo.provide("ambra.alm");

dojo.require("ambra.domUtil");
dojo.require("ambra.formUtil");
dojo.require("dojo.io.script");

(function() {
  dojo.declare("ambra.alm", null, {
    constructor:function(hostName, journalName) {
      this.host = hostName;
      this.journal = journalName;
    },

    getIDs:function(doi, callBack) {
      var request = "articles/" + doi + ".json?history=0";
      this.getData(request, callBack);
    },

    getRelatedBlogs:function(doi, callBack) {
      var request = "articles/" + doi + ".json?citations=1&source=Bloglines,Nature,Postgenomic";
      this.getData(request, callBack);
    },

    getSocialBookMarks:function(doi, callBack) {
      var request = "articles/" + doi + ".json?citations=1&source=Citeulike,Connotea";
      this.getData(request, callBack);
    },

    getCites:function(doi, callBack) {
      var request = "";

      //TODO: This is very PLOS specific, this should be moved to an override
      if(this.journal == "PLoSONE") {
        request = "articles/" + doi + ".json?citations=1&source=CrossRef,PubMed%20Central,ScopusDOI";
      } else {
        request = "articles/" + doi + ".json?citations=1&source=CrossRef,PubMed%20Central,ScopusEISSN,ScopusISSN";
      }
      
      this.getData(request, callBack);
    },

    /**
      *  host is the host and to get the JSON response from
      *  chartIndex is the  current index of the charts[] array
      *  callback is the method that populates the chart of  "chartIndex"
      **/
    getData:function(request, callBack) {
      var url = this.host + "/" + request;

      console.log(url);

      var getArgs = {
        callbackParamName: "callback",
        url:url,
        caller:this,
        callback:callBack,

        load:function(response, args) {
          /**
           * Callback is the method being called.
           * args.args.caller is the object the method is part of
           **/
          callBack.call(args.args.caller, response, args.args);
          return response;
        },

        error:function(response, args) {
          /**
            * Callback is the method being called.
            * args.args.caller is the object the method is part of
            **/
          callBack.call(args.args.caller, null, args.args);
          return response;
        },
        
        timeout:10000
      };

      var deferred = dojo.io.script.get(getArgs);
    }
  });
})();
