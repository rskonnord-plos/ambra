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
$(document).ready(
  function() {
    //***************************************
    //Form events linking in:
    //***************************************
    $("#clearJournalFilter").click(function(eventObj) {
      $("input[name|='filterJournals']").each(function (index, element) {
        $(element).removeAttr('checked');
      });

      $("#searchFormOnSearchResultsPage").submit();
    });

    $("input[name|='filterJournals']").click(function(eventObj) {
      $("#searchFormOnSearchResultsPage").submit();
    });

    $("#clearSubjectFilter").click(function(eventObj) {
      $("input[name|='filterSubjects']").each(function (index, element) {
        $(element).removeAttr('checked');
      });

      $("#searchFormOnSearchResultsPage").submit();
    });

    $("input[name|='filterSubjects']").click(function(eventObj) {
      $("#searchFormOnSearchResultsPage").submit();
    });

    $("#clearAuthorFilter").click(function(eventObj) {
      $("input[name|='filterAuthors']").each(function (index, element) {
        $(element).removeAttr('checked');
      });

      $("#searchFormOnSearchResultsPage").submit();
    });

    $("input[name|='filterAuthors']").click(function(eventObj) {
      $("#searchFormOnSearchResultsPage").submit();
    });

    $("#sortPicklist").change(function(eventObj) {
      $("#searchFormOnSearchResultsPage").submit();
    });

    //***************************************
    //UI control events linking in:
    //***************************************
    var $hdr_search = $('#hdr-search-results');
    var $srch_facets = $('#search-facets');
    var $facets = $srch_facets.find('.facet');
    var $menu_itms = $srch_facets.find('div[data-facet]');

    $menu_itms.each(function() {
      $this = $(this);
      ref = $this.data('facet');
      if ($('#' + ref).length == 0) {
        $this.addClass('inactive');
      }
    });

    $menu_itms.on('click', function() {
      $this = $(this);
      if ($this.hasClass('active') || $this.hasClass('inactive')) { return false; }
      $menu_itms.removeClass('active');
      $facets.hide();
      $facets.find('dl.more').hide();
      $facets.find('.view-more').show();
      $this.addClass('active');
      ref = $this.data('facet');
      $('#' + ref).show();
    });

    $chkbxs = $srch_facets.find(':checkbox');
    $chkbxs.each(function() {
      chkbx = $(this);
      if (chkbx.prop('checked')) {
        chkbx.closest('dd').addClass('checked');
      }
    });

    $chkbxs.on('change', function() {
      chkbx = $(this);
      if (chkbx.prop('checked')) {
        chkbx.closest('dd').addClass('checked');
      } else {
        chkbx.closest('dd').removeClass('checked');
      }
    });

    $srch_facets.find('.view-more').on('click', function() {
      $(this).hide()
        .closest('div.facet').find('dl.more').show();
    });

    $srch_facets.find('.view-less').on('click', function() {
      this_facet = $(this).closest('div.facet');
      this_facet.find('dl.more').hide();
      this_facet.find('.view-more').show();
    });

    $('#startDateAsStringId').datepicker({
      changeMonth: true,
      changeYear: true,
      maxDate: 0,
      dateFormat: "yy-mm-dd",
      onSelect: function( selectedDate ) {
        $('#endDateAsStringId').datepicker('option', 'minDate', selectedDate );
      }
    });

    $('#endDateAsStringId').datepicker({
      changeMonth: true,
      changeYear: true,
      maxDate: 0,
      dateFormat: "yy-mm-dd",
      onSelect: function( selectedDate ) {
        $('#startDateAsStringId').datepicker('option', 'maxDate', selectedDate );
      }
    });

    $toggle_filter = $('<div class="toggle btn">filter by &nbsp;+</div>').toggle(function() {
        $srch_facets.show();
        $toggle_filter.addClass('open');
      }, function() {
        $srch_facets.hide();
        $toggle_filter.removeClass('open');
      }
    ).prependTo($hdr_search.find('div.options'));

    $('#sortPicklist').uniform();

    //***************************************
    //Wire in ALM Stats
    //***************************************
    var almService = new $.fn.alm(),
      ids = new Array(),
      //When we get the results back, we put those IDs into this list.
      confirmed_ids = new Array();

    $("li[doi]:visible").each(function(index, element) {
      ids[ids.length] = $(element).attr("doi");
    });

    almService.getSummaryForArticles(ids, setALMSearchWidgets, setALMSearchWidgetsError);

    function setALMSearchWidgets(articles) {
      for(a = 0; a < articles.length; a++) {
        var article = articles[a];
        var doi = article.article.article.doi;
        var bookmarks = null;
        var cites = null;
        var viewsData = null;

        for(b = 0; b < article.groupcounts.length; b++) {
          if(article.groupcounts[b].name.toLowerCase() === "citations" &&
            article.groupcounts[b].count > 0) {
            cites = article.groupcounts[b].sources;
          }

          if(article.groupcounts[b].name.toLowerCase() === "social bookmarks" &&
            article.groupcounts[b].count > 0) {
            bookmarks = article.groupcounts[b].sources;
          }
        }

        for(b = 0; b < article.groups.length; b++) {
          if(article.groups[b].name.toLowerCase() === "statistics") {
            //Attempt to find the pub date
            var nodeList = $("li[doi='" + doi + "']");
            var pubDateNode = nodeList[0];
            var pubDate = $(pubDateNode).attr("pdate");

            if(pubDate == null) {
              throw new Error('Can not find publish date attribute for doi:' + article.article.article.doi);
            } else {
              //Total and perform business logic
              viewsData = almService.massageChartData(article.groups[b].sources, pubDate);
            }
          }
        }

        //show widgets only when you have data
        if(cites != null ||  bookmarks != null || viewsData !=null) {
          confirmed_ids[confirmed_ids.length] = doi;
          makeALMSearchWidget(doi, cites, bookmarks, viewsData);
        }
      }
      confirmALMDataDisplayed();
    }

    function makeALMSearchWidget(doi, cites, bookmarks, data) {
      var nodeList = getSearchWidgetByDOI(doi);
      var metricsURL = getMetricsURL(doi);

      var anim = $(nodeList).fadeOut(250, function() {
        var searchWidget = $("<span></span>");
        searchWidget.addClass("almSearchWidget");

        buildWidgetText(searchWidget, metricsURL, cites, bookmarks, data);

        $(nodeList).html("");
        $(nodeList).append(searchWidget);
        $(nodeList).fadeIn(250);
      });
    }

    //<a class="data" href="TEST">Views: 7611</a> &bull; <a class="data" href="TEST">Citations: Yes</a> &bull; <a class="data" href="TEST">Bookmarks: Yes</a>
    function buildWidgetText(node, metricsURL, cites, bookmarks, data) {
      var newNode = null;

      if(data != null) {
        newNode = $("<a></a>")
          .attr("href",metricsURL + "#usage")
          .html("Views: " + data.total.format(0,'.',','))
          .addClass("data");

        newNode.tooltip({
          delay: 250,
          fade: 250,
          top: -40,
          left: 20,
          track: true,
          showURL: false,
          bodyHandler: function() {
            return "<span class=\"searchResultsTip\">HTML: <b>" + data.totalHTML + "</b>"
              + ", PDF: <b>" + data.totalPDF + "</b>";
            + ", XML: <b>" + data.totalXML + "</b>";
            + ", Grand Total: <b>" + data.total + "</b></span>";
          }
        });

        node.append(newNode);
      } else {
        node.appendChild($("<span></span>")
          .addClass("no-data")
          .html("Views: Not available"));
      }

      if(cites != null) {
        // Citation Sources should always start with Scopus (if an entry for Scopus exists)
        //   followed by the rest of the sources in alphabetical order.
        cites = cites.sort(sortCitesByName);

        newNode = $("<a></a>")
          .attr("href", metricsURL + "#citations")
          .html("Citations: Yes")
          .addClass("data");

        newNode.tooltip({
          delay: 250,
          fade: 250,
          top: -40,
          left: 20,
          track: true,
          showURL: false,

          bodyHandler: function() {
            var tipText = "";

            for(a = 0; a < cites.length; a++) {
              if(tipText != "") {
                tipText += ", "
              }
              tipText += cites[a].source + ": <b>" + cites[a].count.format(0,'.',',') + "</b>";
            }

            return "<span class=\"searchResultsTip\">" + tipText + "</span>";
          }
        });

        //new dijit.Tooltip({ connectId: newNode, label: tipText });
        appendBullIfNeeded(node);
        node.append(newNode);
      } else {
        appendBullIfNeeded(node);
        node.append($("<span></span>")
          .html("Citations: None")
          .addClass("no-data"));
      }

      if(bookmarks != null) {
        newNode = $("<a></a>")
          .attr("href", metricsURL + "#other")
          .html("Bookmarks: Yes")
          .addClass("data");
        //new dijit.Tooltip({ connectId: newNode, label: tipText });

        appendBullIfNeeded(node);

        newNode.tooltip({
          delay: 250,
          fade: 250,
          top: -40,
          left: 20,
          track: true,
          showURL: false,
          bodyHandler: function() {
            var tipText = "";

            for(a = 0; a < bookmarks.length; a++) {
              if(tipText != "") {
                tipText += ", "
              }
              tipText += bookmarks[a].source + ": <b>" + bookmarks[a].count.format(0,'.',',') + "</b>";
            }

            return "<span class=\"searchResultsTip\">" + tipText + "</span>";
          }
        });

        node.append(newNode);
      } else {
        appendBullIfNeeded(node);
        node.append($("<span></span>")
          .html("Bookmarks: None")
          .addClass("no-data"));
      }
    }

    function appendBullIfNeeded(node) {
      if(node.size() > 0) {
        node.append("&nbsp;&bull;&nbsp;");
      }
    }


    function getSearchWidgetByDOI(doi) {
      return $("li[doi='" + doi  + "'] span.metrics");
    }

    function getMetricsURL(doi)
    {
      return $($("li[doi='" + doi  + "']")[0]).attr("metricsURL");
    }

    function setALMSearchWidgetsError() {
      confirmALMDataDisplayed();
    }

    function makeALMSearchWidgetError(doi, message) {
      var nodeList = getSearchWidgetByDOI(doi);
      var spanNode = nodeList[0];

      var errorMsg = $("<span></span>");
      errorMsg.addClass("inlineError");
      errorMsg.css("display","none");
      errorMsg.html(message);

      $(spanNode).find("span").fadeOut(250, function() {
        $(spanNode).append(errorMsg);
        $(errorMsg).fadeIn(250);
      });
    }

    /*
     * Walk through the ids and confirmed_ids list.  If
     * If some ids are not confirmed.  Lets let the
     * front end know that no data was received.
     * */
    function confirmALMDataDisplayed() {
      if(confirmed_ids != null) {
        for(a = 0; a < confirmed_ids.length; a++) {
          for(b = 0; b < ids.length; b++) {
            if(confirmed_ids[a] == ids[b]) {
              ids.remove(b);
            }
          }
        }
      }

      //if any ids are left.  We know there is no data
      //Make note of that now.
      for(a = 0; a < ids.length; a++) {
        var nodeList = $("li[doi='" + ids[a] + "']");
        var pubDate = $(nodeList[0]).attr("pdate");

        //If the article is less then two days old and there is no data,
        //it's not really an error, alm is a few days behind
        if(pubDate > ((new Date().getTime()) -  172800000)) {
          makeALMSearchWidgetError(ids[a],
            "Metrics unavailable for recently published articles. Please check back later.");
        } else {
          makeALMSearchWidgetError(ids[a],
            "<img src=\"../images/icon_error.gif\"/>&nbsp;Metrics unavailable. Please check back later.");
        }
      }
    }

    function sortCitesByName(a,b) {
      if (b.source.toLowerCase() === 'scopus') {
        return 1;
      } else if (a.source.toLowerCase() === 'scopus' || a.source.toLowerCase() < b.source.toLowerCase()) {
        return -1;
      } else if (a.source.toLowerCase() > b.source.toLowerCase()) {
        return 1;
      }
      return 0;
    }

    function showFigSearchView() {
      $('#search-results-block').hide();
      $('#fig-search-block').show();

      $('#resultView').val("fig");

      $('div[class="figure"] > img[fakesrc]').each(function () {
        $(this).attr("src", $(this).attr("fakesrc"));
        $(this).removeAttr("fakesrc");
      });

      $('a[href]').attr('href', function(index, href) {
        var startIndex = href.indexOf('resultView=');
        var endIndex = 0;
        var newResultView = "";
        if (startIndex >= 0) {
          endIndex = href.indexOf('&', startIndex);
          if (endIndex >= 0) {
            href = href.replace(href.substring(startIndex, endIndex), 'resultView=fig')
          }
        }
        return href;
      });
    }

    if ($('#resultView').val() === "fig") {
      showFigSearchView();
    }

    $('#search-view > span[class="list"]').click(function() {
      $('#fig-search-block').hide();
      $('#search-results-block').show();

      $('#resultView').val("");

      $('a[href]').attr('href', function(index, href) {
        var startIndex = href.indexOf('resultView=');
        var endIndex = 0;
        var newResultView = "";
        if (startIndex >= 0) {
          endIndex = href.indexOf('&', startIndex);
          if (endIndex >= 0) {
            href = href.replace(href.substring(startIndex, endIndex), 'resultView=')
          }
        }
        return href;
      });
    });

    $('#search-view > span[class="figs"]').click(function() {
      showFigSearchView();
    });

    $('a.save-search').click(function() {
      //logic to show the pop-up
    });

  });