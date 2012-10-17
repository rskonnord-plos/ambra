/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

$.fn.alm = function () {
  this.almHost = $('meta[name=almHost]').attr("content");
  this.pubGetHost = $('meta[name=pubGetHost]').attr("content");

  if(this.almHost == null) {
    jQuery.error('The related article metrics server is not defined.  Make sure the almHost is defined in the meta information of the html page.');
  }

  this.validateArticleDate = function(date) {
    //The article publish date should be stored in the current page is a hidden form variable
    var pubDateInMilliseconds = new Number(date);
    var todayMinus48Hours = (new Date()).getTime() - 172800000;

    if(todayMinus48Hours < pubDateInMilliseconds) {
      return false;
    } else {
      return true;
    }
  }

  this.isArticle = function(doi) {
    if(doi.indexOf("image") > -1) {
      return false;
    }
    return true;
  }

  this.validateDOI =function(doi) {
    if(doi == null) {
      throw new Error('DOI is null.');
    }

    doi = encodeURI(doi);

    return doi.replace(new RegExp('/', 'g'),'%2F').replace(new RegExp(':', 'g'),'%3A');
  }

  this.getIDs = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?history=0";
    this.getData(request, callBack, errorCallback);
  }

  this.getRelatedBlogs = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=Nature,Researchblogging,Wikipedia";
    this.getData(request, callBack, errorCallback);
  }

  this.getCites = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=CrossRef,PubMed,Scopus,Wos";
    this.getData(request, callBack, errorCallback);
  }

  this.getChartData = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=Counter,PMC";
    this.getData(request, callBack, errorCallback);
  }

  this.getBiodData = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=Biod";
    this.getData(request, callBack, errorCallback);
  }

  this.getCitesCrossRefOnly = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=CrossRef";
    this.getData(request, callBack, errorCallback);
  }

  this.getCitesTwitterOnly = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=Twitter";
    this.getData(request, callBack, errorCallback);
  }

  this.getSocialData = function(doi, callBack, errorCallback) {
    doi = this.validateDOI(doi);

    var request = "articles/" + doi + ".json?events=1&source=Citeulike,Connotea,Facebook,Twitter,Mendeley";
    this.getData(request, callBack, errorCallback);
  }

  /*
   * Get summaries and counter data for the collection of article IDs
   * passed in.  If an article is not found, or a source data is not found
   * The data will be missing in the resultset.
   * */

   this.getSummaryForArticles = function(dois, callBack, errorCallback) {
    idString = "";
    for (a = 0; a < dois.length; a++) {
      if(idString != "") {
        idString = idString + ",";
      }

      idString = idString + this.validateDOI("info:doi/" + dois[a]);
    }

    var request = "group/articles.json?id=" + idString + "&group=statistics";
    this.getData(request, callBack, errorCallback);
  }

  this.containsUsageStats = function(response) {
    var foundStats = false;

    //Check to see if there is counter data, don't bother with the PMC data if there is no data for counter
    for(var a = 0; a < response.article.source.length; a++) {
      var sourceData = response.article.source[a];

      if(sourceData.source == "Counter"
        && sourceData.events != null
        && sourceData.events.length > 0
        ) {
        foundStats = true;
      }
    }

    return foundStats;
  }

  /* Sort the chart data */
  this.sortByYearMonth = function(chartData1, chartData2) {
    if(parseInt(chartData1.year) < parseInt(chartData2.year)) {
      return -1;
    } else {
      if(parseInt(chartData1.year) == parseInt(chartData2.year)) {
        if(parseInt(chartData1.month) == parseInt(chartData2.month)) {
          return 0;
        } else {
          if(parseInt(chartData1.month) < parseInt(chartData2.month)) {
            return -1;
          } else {
            return 1;
          }
        }
      } else {
        return 1;
      }
    }
  },

  /*
   * Massage the chart data into a more 'chartable' structure
   **/
  this.massageChartData = function(sources, pubDateMS) {
    //Do some final calculations on the results
    var pubDate = new Date(pubDateMS);
    var pubYear = pubDate.getFullYear();
    //Add one as getMonth is zero based
    var pubMonth = pubDate.getMonth() + 1;
    var counterViews = null;
    var pmcViews = null;
    var result = {};

    for(var a = 0; a < sources.length; a++) {
      if(sources[a].source == "Counter") {
        counterViews = sources[a].events;
        //Make sure everything is in the right order
        counterViews = counterViews.sort(this.sortByYearMonth);
      }

      if(sources[a].source == "PubMed Central Usage Stats") {
        if(sources[a].events != null && sources[a].events.length > 0) {
          pmcViews = sources[a].events;
          //Make sure everything is in the right order
          pmcViews = pmcViews.sort(this.sortByYearMonth);
        }
      }
    }

    result.totalPDF = 0;
    result.totalXML = 0;
    result.totalHTML = 0;
    result.total = 0;
    result.history = {};

    //Don't display any data from counter for any date before the publication date
    for(var a = 0; a < counterViews.length; a++) {
      if(counterViews[a].year < pubYear || (counterViews[a].year == pubYear && counterViews[a].month < pubMonth)) {
        counterViews.splice(a,1);
        a--;
      }
    }

    var cumulativeCounterPDF = 0;
    var cumulativeCounterXML = 0;
    var cumulativeCounterHTML = 0;
    var cumulativeCounterTotal = 0;

    //Two loops here, the first one assumes there is no data structure
    //I also assume (for the cumulative counts) that results are in order date descending
    for(var a = 0; a < counterViews.length; a++) {
      var totalViews = this.parseIntSafe(counterViews[a].html_views) + this.parseIntSafe(counterViews[a].xml_views) +
        this.parseIntSafe(counterViews[a].pdf_views);
      var yearMonth = this.getYearMonth(counterViews[a].year, counterViews[a].month);

      result.history[yearMonth] = {};
      result.history[yearMonth].source = {};
      result.history[yearMonth].year = counterViews[a].year;
      result.history[yearMonth].month = counterViews[a].month;
      result.history[yearMonth].source["counterViews"] = {};
      result.history[yearMonth].source["counterViews"].month = counterViews[a].month;
      result.history[yearMonth].source["counterViews"].year = counterViews[a].year;
      result.history[yearMonth].source["counterViews"].totalPDF = this.parseIntSafe(counterViews[a].pdf_views);
      result.history[yearMonth].source["counterViews"].totalXML = this.parseIntSafe(counterViews[a].xml_views);
      result.history[yearMonth].source["counterViews"].totalHTML = this.parseIntSafe(counterViews[a].html_views);
      result.history[yearMonth].source["counterViews"].total = totalViews;

      cumulativeCounterPDF += this.parseIntSafe(counterViews[a].pdf_views);
      cumulativeCounterXML += this.parseIntSafe(counterViews[a].xml_views);
      cumulativeCounterHTML += this.parseIntSafe(counterViews[a].html_views);
      cumulativeCounterTotal += totalViews;

      //Total views so far (for counter)
      result.history[yearMonth].source["counterViews"].cumulativePDF = cumulativeCounterPDF;
      result.history[yearMonth].source["counterViews"].cumulativeXML = cumulativeCounterXML;
      result.history[yearMonth].source["counterViews"].cumulativeHTML = cumulativeCounterHTML;
      result.history[yearMonth].source["counterViews"].cumulativeTotal = cumulativeCounterTotal;

      //Total views so far (for all sources)
      result.history[yearMonth].cumulativeTotal = this.parseIntSafe(result.total) + totalViews;
      result.history[yearMonth].cumulativePDF = result.totalPDF + this.parseIntSafe(counterViews[a].pdf_views);
      result.history[yearMonth].cumulativeXML = result.totalXML + this.parseIntSafe(counterViews[a].xml_views);
      result.history[yearMonth].cumulativeHTML = result.totalHTML + this.parseIntSafe(counterViews[a].html_views);
      result.history[yearMonth].total = totalViews;

      //The grand totals
      result.totalPDF += this.parseIntSafe(counterViews[a].pdf_views);
      result.totalXML += this.parseIntSafe(counterViews[a].xml_views);
      result.totalHTML += this.parseIntSafe(counterViews[a].html_views);
      result.total += totalViews;
    }

    result.totalCounterPDF = cumulativeCounterPDF;
    result.totalCounterXML = cumulativeCounterXML;
    result.totalCounterHTML = cumulativeCounterHTML;
    result.totalCouterTotal = cumulativeCounterTotal;

    var cumulativePMCPDF = 0;
    var cumulativePMCHTML = 0;
    var cumulativePMCTotal = 0;

    if(pmcViews != null) {
      for(var a = 0; a < pmcViews.length; a++) {
        var totalViews = this.parseIntSafe(pmcViews[a]["full-text"]) + this.parseIntSafe(pmcViews[a].pdf);

        // even if we don't display all the pmc data, the running total we display should be correct
        cumulativePMCPDF += this.parseIntSafe(pmcViews[a].pdf);
        cumulativePMCHTML += this.parseIntSafe(pmcViews[a]["full-text"]);
        cumulativePMCTotal += totalViews;

        //Total views for the current period
        var yearMonth = this.getYearMonth(pmcViews[a].year, pmcViews[a].month);

        // if counter doesn't have data for this given month, we are going to ignore it.
        // we assume that counter data doesn't have any gaps

        // if we add the yearMonth here in the history,
        // * the order of the data in the history can get messed up.
        // * the code expects the counter data to exist for the given yearMonth in the history not just pmc data
        // See PDEV-1074 for more information
        if(result.history[yearMonth] == null) {
          continue;
        }

        result.history[yearMonth].source["pmcViews"] = {};

        //Total views so far (for PMC)
        result.history[yearMonth].source["pmcViews"].month = pmcViews[a].month;
        result.history[yearMonth].source["pmcViews"].year = pmcViews[a].year;
        result.history[yearMonth].source["pmcViews"].cumulativePDF = cumulativePMCPDF;
        result.history[yearMonth].source["pmcViews"].cumulativeHTML = cumulativePMCHTML;
        result.history[yearMonth].source["pmcViews"].cumulativeTotal = cumulativePMCTotal;

        result.history[yearMonth].source["pmcViews"].totalPDF = this.parseIntSafe(pmcViews[a].pdf);
        result.history[yearMonth].source["pmcViews"].totalXML = "n.a.";
        result.history[yearMonth].source["pmcViews"].totalHTML = this.parseIntSafe(pmcViews[a]["full-text"]);
        result.history[yearMonth].source["pmcViews"].total = totalViews;

        //Total views so far
        result.history[yearMonth].total += totalViews;
        result.history[yearMonth].cumulativeTotal += totalViews;
        result.history[yearMonth].cumulativePDF += this.parseIntSafe(pmcViews[a].pdf);
        result.history[yearMonth].cumulativeHTML += this.parseIntSafe(pmcViews[a]["full-text"]);

        //The grand totals
        result.totalPDF += this.parseIntSafe(pmcViews[a].pdf);
        result.totalHTML += this.parseIntSafe(pmcViews[a]["full-text"]);
        result.total += totalViews;
      }
    }

    result.totalPMCPDF = cumulativePMCPDF;
    result.totalPMCHTML = cumulativePMCHTML;
    result.totalPMCTotal = cumulativePMCTotal;

    //PMC data is sometimes missing months, here let's hack around it.
    for(year = pubYear; year <= (new Date().getFullYear()); year++) {
      var startMonth = (year == pubYear)?pubMonth:1;
      for(month = startMonth; month < 13; month++) {
        //Skips months in the future of the current year
        //Month is zero based, '(new Date().getMonth())' is 1 based.
        if(year == (new Date().getFullYear()) && (month - 1) > (new Date().getMonth())) {
          break;
        }

        yearMonth = this.getYearMonth(year, month);

        if(result.history[yearMonth] != null &&
          result.history[yearMonth].source["pmcViews"] == null)
        {
          result.history[yearMonth].source["pmcViews"] = {};

          result.history[yearMonth].source["pmcViews"].month = month + 1;
          result.history[yearMonth].source["pmcViews"].year = year;
          result.history[yearMonth].source["pmcViews"].cumulativePDF = 0;
          result.history[yearMonth].source["pmcViews"].cumulativeHTML = 0;
          result.history[yearMonth].source["pmcViews"].cumulativeTotal = 0;
          result.history[yearMonth].source["pmcViews"].totalPDF = 0;
          result.history[yearMonth].source["pmcViews"].totalXML = 0;
          result.history[yearMonth].source["pmcViews"].totalHTML = 0;
          result.history[yearMonth].source["pmcViews"].total = 0;

          //Fill in the cumulatives from the previous month (If it exists)
          var prevMonth = 0;
          var prevYear = 0;

          if(month == 1) {
            prevMonth = 12;
            prevYear = year - 1;
          } else {
            prevMonth = month - 1;
            prevYear = year;
          }

          var prevYearMonthStr = this.getYearMonth(prevYear, prevMonth);

          if(result.history[prevYearMonthStr] != null &&
            result.history[prevYearMonthStr].source["pmcViews"] != null) {
            result.history[yearMonth].source["pmcViews"].cumulativePDF =
              result.history[prevYearMonthStr].source["pmcViews"].cumulativePDF;
            result.history[yearMonth].source["pmcViews"].cumulativeHTML =
              result.history[prevYearMonthStr].source["pmcViews"].cumulativeHTML;
            result.history[yearMonth].source["pmcViews"].cumulativeTotal =
              result.history[prevYearMonthStr].source["pmcViews"].cumulativeTotal;
            result.history[yearMonth].source["pmcViews"].totalPDF = 0;
            result.history[yearMonth].source["pmcViews"].totalHTML = 0;
            result.history[yearMonth].source["pmcViews"].total = 0;
          } else {
            result.history[yearMonth].source["pmcViews"].cumulativePDF = 0;
            result.history[yearMonth].source["pmcViews"].cumulativeHTML = 0;
            result.history[yearMonth].source["pmcViews"].cumulativeTotal = 0;
            result.history[yearMonth].source["pmcViews"].totalPDF = 0;
            result.history[yearMonth].source["pmcViews"].totalHTML = 0;
            result.history[yearMonth].source["pmcViews"].total = 0;
          }
        }
      }
    }

    //If there are no PMC views at all, assume the data is just mising
    if(result.totalPMCTotal == 0) {
      result.totalPMCPDF = 0;
      result.totalPMCHTML = 0;
      result.totalPMCTotal = 0;
    }

    return result;
  }

  //Deprecated, should be removed once browse / search scripts refactored not to use
  //TODO: Confirm we can delete this
  this.massageCounterData = function(data, pubDateMS) {
    //Do some final calculations on the results
    var pubDate = new Date(pubDateMS);
    var pubYear = pubDate.getFullYear();
    //Add one as getMonth is zero based
    var pubMonth = pubDate.getMonth() + 1;

    data.totalPDF = 0;
    data.totalXML = 0;
    data.totalHTML = 0;
    data.total = 0;

    //Don't display any data from any date before the publication date
    for(var a = 0; a < data.length; a++) {
      if(data[a].year < pubYear || (data[a].year == pubYear && data[a].month < pubMonth)) {
        data.splice(a,1);
        a--;
      }
    }

    for(var a = 0; a < data.length; a++) {
      var totalViews = new Number(data[a].html_views)+ new Number(data[a].xml_views) + new Number(data[a].pdf_views);
      //Total views for the current period
      data[a].total = totalViews;

      //Total views so far
      data[a].cumulativeTotal = new Number(data.total) + totalViews;
      data[a].cumulativePDF = data.totalPDF + new Number(data[a].pdf_views);
      data[a].cumulativeXML = data.totalXML + new Number(data[a].xml_views);
      data[a].cumulativeHTML = data.totalHTML + new Number(data[a].html_views);

      //The grand totals
      data.totalPDF += new Number(data[a].pdf_views);
      data.totalXML += new Number(data[a].xml_views);
      data.totalHTML += new Number(data[a].html_views);
      data.total += totalViews;
    }

    return data;
  }

  this.parseIntSafe = function(value) {
    if(isNaN(value)) {
      return 0;
    }

    return parseInt(value);
  }

  this.getYearMonth = function(year, month) {
    if(this.parseIntSafe(month) < 10) {
      return year + "-0" + month;
    } else {
      return year + "-" + month;
    }
  }

  /**
   *  host is the host and to get the JSON response from
   *  chartIndex is the  current index of the charts[] array
   *  callback is the method that populates the chart of  "chartIndex"
   *  errorCallback is the method that gets called when:
   *    --The request fails (Network error, network timeout)
   *    --The request is "empty" (Server responds, but with nothing)
   *    --The callback method fails
   **/
  this.getData = function(request, callBack, errorCallback) {
    var url = this.almHost + "/" + request;

    //I use a third party plugin here for jsonp requests as jQuery doesn't
    //Handle errors well (with jsonp requests)

    $.jsonp({
      url: url,
      context: document.body,
      timeout: 10000,
      callbackParameter: "callback",
      success: callBack,
      error: errorCallback
    });

    console.log(url);
  }


  /**
   * Set cross ref text by DIO
   * @param doi the doi
   * @param crossRefID the ID of the document element to place the result
   * @param errorID the ID of the document element to place the error
   */
  this.setCrossRefText = function(doi, crossRefID, errorID) {
    //almService.getCitesCrossRefOnly(doi, setCrossRefLinks, setCrossRefLinksError);

    var success = function(response) {
        this.setCrossRefLinks(response, crossRefID);
      };

    var error = function(response) {
        $.("#" + errorID).val(response);
        $.("#" + errorID).show( "blind", 500 );
      };

    //The proxy function forces the success method to be run in "this" context.
    this.getCitesCrossRefOnly(doi, jQuery.proxy(success, this), error);
  }

  this.getPubGetPDF = function(dois) {
    var doiList = dois[0];

    for (var a = 1; a < dois.length; a++) {
      doiList = doiList + "|" + dois[a];
    }

    var getArgs = {
      url:this.pubGetHost,
      callbackParameter:"callback",
      content:{
        oa_only:"true",
        dois:doiList
      },

      success:function (response) {
        for (var a = 0; a < response.length; a++) {
          var doi = this.fixDoiForID(response[a].doi);
          var url = response[a].values.link;
          var image_src = appContext + "/images/icon_pubgetpdf.gif";
          var image_title = "Get the full text PDF from Pubget";

          var html = "<a href=\"" + url + "\"><img title=\"" +
            image_title + "\" src=\"" + image_src + "\"></a>";

          var domElement = $("#citation_" + doi);

          if (domElement == null) {
            console.warn("Citation not found on page: citation_" + doi);
          } else {
            domElement.innerHTML = html;
          }
        }

        return response;
      },

      error:function (response) {
        setPubGetError("Links to PDF files of open access articles " +
          "on Pubget are currently not available, please check back later.");
        return response;
      },

      timeout:3000
    };

    $.jsonp(getArgs);
  }

  /**
   * HTML IDs can not have a "/" character in them.
   * @param doi
   */
  this.fixDoiForID = function(doi) {
    return doi.replace(/\//g, ":");
  }

  this.setCrossRefLinks = function(response, crossRefID) {
    var doi = escape(response.article.doi);
    var citationDOIs = new Array();
    var numCitations = 0;

    if (response.article.source != null && response.article.source.length > 0
      && response.article.source[0].events != null && response.article.source[0].events.length > 0) {
      numCitations = response.article.source[0].events.length;
      var html = "";

      for (var a = 0; a < numCitations; a++) {
        var citation = response.article.source[0].events[a].event;
        var citation_url = response.article.source[0].events[a].event_url;
        //Build up list of citation DOIs to pass to pubget
        citationDOIs[a] = citation.doi;

        //  Assume there exists: URI, Title, and DOI.  Anything else may be missing.
        html = html + "<li><span class='article'><a href=\"" + citation_url + "\">"
          + citation.article_title + "</a> <span class=\"pubGetPDFLink\" "
          + "id=\"citation_" + this.fixDoiForID(citation.doi) + "\"></span></span>";

        if (citation.contributors != null) {
          var first_author = "";
          var authors = "";
          var contributors = citation.contributors.contributor;
          if (contributors == undefined) {
            contributors = citation.contributors;
            for (var b = 0; b < contributors.length; b++) {
              if (contributors[b].first_author === true) {
                first_author = contributors[b].surname + " " + contributors[b].given_name.substr(0, 1);
              } else {
                authors = authors + ", " + contributors[b].surname + " " + contributors[b].given_name.substr(0, 1);
              }
            }
            authors = first_author + authors;
          } else {
            if (contributors instanceof Array) {
              for (var b = 0; b < contributors.length; b++) {
                if (contributors[b].first_author === "true") {
                  first_author = contributors[b].surname + " " + contributors[b].given_name.substr(0, 1);
                } else {
                  authors = authors + ", " + contributors[b].surname + " " + contributors[b].given_name.substr(0, 1);
                }
              }
              authors = first_author + authors;
            } else {
              authors = contributors.surname + " " + contributors.given_name.substr(0, 1);
            }
          }
          html = html + "<span class='authors'>" + authors + "</span>";
        }
        html = html + "<span class='articleinfo'>";
        if (citation.journal_title != null && citation.journal_title.length > 0) {
          html = html + citation.journal_title;
        }
        if (citation.year != null && citation.year.length > 0) {
          html = html + " " + citation.year;
        }
        if (citation.volume != null && citation.volume.length > 0) {
          html = html + " " + citation.volume;
        }
        if (citation.issue != null && citation.issue.length > 0) {
          html = html + "(" + citation.issue + ")";
        }
        if (citation.first_page != null && citation.first_page.length > 0) {
          html = html + ": " + citation.first_page;
        }
        html = html + ". doi:" + citation.doi + "</span></li>";
      }
    }

    if (numCitations < 1) {
      html = "<h3>No related citations found</h3>";
    } else {
      var pluralization = "";
      if (numCitations != 1) { // This page should never be displayed if less than 1 citation.
        pluralization = "s";
      }

      html = "<h3>" + numCitations + " citation" + pluralization
        + " as recorded by <a href=\"http://www.crossref.org\">CrossRef</a>.  Article published "
        + $.format.date(response.article.published, 'MMM dd, yyyy')
        + ". Citations updated on "
        + $.format.date(response.article.source[0].updated_at, 'MMM dd, yyyy')
        + ".</h3>"
        + " <ol>" + html + "</ol>";
    }

    $("#" + crossRefID).html(html);
    $("#" + crossRefID).show( "blind", 500 );

    //There is physical limit on the number of DOIS I can put on one get.
    //AKA, a GET string can only be so long before the Web server borks.
    //Limit that here
    var doiLimit = 200;
    if (citationDOIs.length < doiLimit) {
      this.getPubGetPDF(citationDOIs);
    } else {
      for (var b = 0; b < (citationDOIs.length / doiLimit); b++) {
        var start = b * doiLimit;
        var end = (b + 1) * doiLimit;

        this.getPubGetPDF(citationDOIs.slice(start, end));
      }
    }
  }
}

//var alm = new $.fn.alm();
//
//function successHandler(response) {
//  var results = alm.massageChartData(response.article.source, 1166601600000);
//}
//
//function errorHandler(xOptions, textStatus) {
//  console.log('ALM Error: ' + textStatus);
//}
//
//alm.getChartData('10.1371/journal.pone.0000000', successHandler, errorHandler);
