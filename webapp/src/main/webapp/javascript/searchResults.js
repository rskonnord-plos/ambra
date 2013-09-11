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
      //Note there is also some logic in global.js that binds to the submit event
      //***************************************

      $("#clearJournalFilter").click(function(eventObj) {
        $("input[name|='filterJournals']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterJournals']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearSubjectFilter").click(function(eventObj) {
        $("input[name|='filterSubjects']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterSubjects']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearDateFilter").click(function(eventObj) {
        $("input[name|='filterStartDate']").each(function (index, element) {
          $(element).val('');
        });

        $("input[name|='filterEndDate']").each(function (index, element) {
          $(element).val('');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterDateButton']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#clearAuthorFilter").click(function(eventObj) {
        $("input[name|='filterAuthors']").each(function (index, element) {
          $(element).removeAttr('checked');
        });

        $("#searchStripForm").submit();
      });

      $("#clearArticleTypeFilter").click(function(eventObj) {
        $("input[name|='filterArticleType']").each(function (index, element) {
          $(element).val('');
        });

        $("#searchStripForm").submit();
      });

      $("input[name|='filterAuthors']").click(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#sortPicklist").change(function(eventObj) {
        $("#searchStripForm").submit();
      });

      $("#pageSizePicklist").change(function(eventObj) {
        $('#db input[name="pageSize"]').val($("#pageSizePicklist").val());
        $("#searchStripForm").submit();
      });

      $("#pageSizePicklistFig").change(function(eventObj) {
        $('#db input[name="pageSize"]').val($("#pageSizePicklistFig").val());
        $("#searchStripForm").submit();
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

      $('#pageSizePicklist').uniform();

      $('#pageSizePicklistFig').uniform();

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

      almService.getArticleSummaries(ids, setALMSearchWidgets, setALMSearchWidgetsError);

      function setALMSearchWidgets(articles) {
        for (a = 0; a < articles.length; a++) {
          var article = articles[a];
          var doi = article.doi;
          var sources = article.sources;
          var scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter;
          scopus = citeulike = counter = mendeley = crossref, wos, pmc, pubmed, facebook, twitter = null;


          //get references to specific sources
          var sourceNames = [];
          for (var s = 0; s < sources.length; s++) {
            sourceNames.push(sources[s].name);
          }
          scopus = sources[sourceNames.indexOf('scopus')];
          citeulike = sources[sourceNames.indexOf('citeulike')];
          pubmed = sources[sourceNames.indexOf('pubmed')];
          counter = sources[sourceNames.indexOf('counter')];
          mendeley = sources[sourceNames.indexOf('mendeley')];
          crossref = sources[sourceNames.indexOf('crossref')];
          wos = sources[sourceNames.indexOf('wos')];
          pmc = sources[sourceNames.indexOf('pmc')];
          facebook = sources[sourceNames.indexOf('facebook')];
          twitter = sources[sourceNames.indexOf('twitter')];

          //determine if article cited, bookmarked, or socialised, or even seen
          var hasData = false;
          if (scopus.metrics.total > 0 ||
              citeulike.metrics.total > 0 ||
              pmc.metrics.total + counter.metrics.total > 0 ||
            mendeley.metrics.total > 0 ||
            facebook.metrics.shares + twitter.metrics.total > 0) {
            hasData = true;
          }

          //show widgets only when you have data
          if (hasData) {
            confirmed_ids[confirmed_ids.length] = doi;
            makeALMSearchWidget(doi, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter);
          }
        }
        confirmALMDataDisplayed();
      }

      function makeALMSearchWidget(doi, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter) {
        var nodeList = getSearchWidgetByDOI(doi);
        var metricsURL = getMetricsURL(doi);

        var anim = $(nodeList).fadeOut(250, function() {
          var searchWidget = $("<span></span>");
          searchWidget.addClass("almSearchWidget");

          buildWidgetText(searchWidget, metricsURL, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter);

          $(nodeList).html("");
          $(nodeList).append(searchWidget);
          $(nodeList).fadeIn(250);
        });
      }

      //TODO: messy but correct - clean up
      function buildWidgetText(node, metricsURL, scopus, citeulike, counter, mendeley, crossref, wos, pmc, pubmed, facebook, twitter) {
        var newNode = null;

        var total = pmc.metrics.total + counter.metrics.total;
        var totalHTML = pmc.metrics.html + counter.metrics.html;
        var totalPDF = pmc.metrics.pdf + counter.metrics.pdf;
        //alm response json has no metric for xml, but xml = total - pdf - html
        var totalXML = total - totalPDF - pmc.metrics.html - counter.metrics.html;
        if (total > 0) {
          newNode = $("<a></a>")
            .attr("href", metricsURL + "#usage")
            .html("Views: " + total.format(0, '.', ','))
            .addClass("data");

          newNode.tooltip({
            delay: 250,
            fade: 250,
            top: -40,
            left: 20,
            track: true,
            showURL: false,
            bodyHandler: function () {
              return "<span class=\"searchResultsTip\">HTML: <b>" + totalHTML.format(0, '.', ',') + "</b>"
                + ", PDF: <b>" + totalPDF.format(0, '.', ',') + "</b>"
                + ", XML: <b>" + totalXML.format(0, '.', ',') + "</b>"
                + ", Grand Total: <b>" + total.format(0, '.', ',') + "</b></span>";
            }
          });


          node.append($("<span></span>").append(newNode));
        } else {
          node.appendChild($("<span></span>")
            .addClass("no-data")
            .html("Views: Not available"));
        }

        //using scopus for display
        if (scopus.metrics.total > 0) {
          newNode = $("<a></a>")
            .attr("href", metricsURL + "#citations")
            .html("Citations: " + scopus.metrics.total.format(0, '.', ','))
            .addClass("data");

          newNode.tooltip({
            delay: 250,
            fade: 250,
            top: -40,
            left: 20,
            track: true,
            showURL: false,

            bodyHandler: function () {
              //adding citation sources manually and IN ALPHABETIC ORDER
              //if this is generified, remember to sort, and remember the comma
              var someSources = [crossref, pubmed, wos];
              var tipText = scopus.display_name + ": <b>" + scopus.metrics.total.format(0, '.', ',') + "</b>"; //scopus.metrics.total always > 0

              for (var s = 0; s < someSources.length; s++) {
                var source = someSources[s];
                if (source.metrics.total > 0) {
                  tipText += ', ' + source.display_name + ": <b>" + source.metrics.total.format(0, '.', ',') + "</b>";
                }
              }

              return "<span class=\"searchResultsTip\">" + tipText + "</span>";
            }
          });

          //new dijit.Tooltip({ connectId: newNode, label: tipText });
          appendBullIfNeeded(node);
          node.append($("<span></span>").append(newNode));
        } else {
          appendBullIfNeeded(node);
          node.append($("<span></span>")
            .html("Citations: None")
            .addClass("no-data"));
        }

        var markCount = mendeley.metrics.total + citeulike.metrics.total;
        if (markCount > 0) {
          newNode = $("<a></a>")
            .attr("href", metricsURL + "#other")
            .html("Saves: " + markCount.format(0, '.', ','))
            .addClass("data");

          appendBullIfNeeded(node);

          newNode.tooltip({
            delay: 250,
            fade: 250,
            top: -40,
            left: 20,
            track: true,
            showURL: false,
            bodyHandler: function () {
              var tipText = "";

              if (mendeley.metrics.total > 0) {
                tipText += mendeley.display_name + ": <b>" + mendeley.metrics.total.format(0, '.', ',') + "</b>";
              }

              if (citeulike.metrics.total > 0) {
                if (tipText != "") {
                  tipText += ", "
                }
                tipText += citeulike.display_name + ": <b>" + citeulike.metrics.total.format(0, '.', ',') + "</b>";
              }


              return "<span class=\"searchResultsTip\">" + tipText + "</span>";
            }
          });

          node.append($("<span></span>").append(newNode));
        } else {
          appendBullIfNeeded(node);
          node.append($("<span></span>")
            .html("Saves: None")
            .addClass("no-data"));
        }

        var shareCount = facebook.metrics.shares + twitter.metrics.total;
        if (shareCount > 0) {
          newNode = $("<a></a>")
            .attr("href", metricsURL + "#other")
            .html("Shares: " + shareCount)
            .addClass("data");

          appendBullIfNeeded(node);

          newNode.tooltip({
            delay: 250,
            fade: 250,
            top: -40,
            left: 20,
            track: true,
            showURL: false,
            bodyHandler: function () {
              var tipText = "";

              if (facebook.metrics.shares > 0) {
                tipText += facebook.display_name + ": <b>" + facebook.metrics.shares.format(0, '.', ',') + "</b>";
              }

              if (twitter.metrics.total > 0) {
                if (tipText != "") {
                  tipText += ", "
                }
                tipText += twitter.display_name + ": <b>" + twitter.metrics.total.format(0, '.', ',') + "</b>";
              }

              return "<span class=\"searchResultsTip\">" + tipText + "</span>";
            }
          });

          node.append($("<span></span>").append(newNode));
        } else {
          appendBullIfNeeded(node);
          node.append($("<span></span>")
            .html("Shares: None")
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

      function getMetricsURL(doi){
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
                "<img src=\"../images/icon_error.png\"/>&nbsp;Metrics unavailable. Please check back later.");
          }
        }
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

        //if the request is from author/editor facet for save search, setting the search name with anchor id.
        //else the regular search term is used.
        if($(this).attr('id')) {
          $('#text_name_savedsearch').val($(this).attr('id'));
        }
        else if ($('#searchOnResult')) {
          $('#text_name_savedsearch').val($('#searchOnResult').val());
        }
        $('#span_error_savedsearch').html('');

        //logic to show the pop-up
        var saveSearchBox = $(this).attr('href');

        //Fade in the Popup
        $(saveSearchBox).fadeIn(300);

        //Set the center alignment padding + border see css style
        var popMargTop = ($(saveSearchBox).height() + 24) / 2;
        var popMargLeft = ($(saveSearchBox).width() + 24) / 2;

        $(saveSearchBox).css({
          'margin-top' : -popMargTop,
          'margin-left' : -popMargLeft
        });

        // Add the mask to body
        $('body').append('<div id="mask"></div>');
        $('#mask').fadeIn(300);

        return false;

      });

      $('#btn_save_savedsearch').click(function() {

        $('#searchName').val($('#text_name_savedsearch').val());
        $('#weekly').val($('#cb_weekly_savedsearch').is(':checked'));
        $('#monthly').val($('#cb_monthly_savedsearch').is(':checked'));

        var uqry="";
        var query = $('#searchOnResult').attr('value');

        // if saving the author/editor facet search, then set the query to empty and
        //unformattedQry to facet value. (author:name or editor:name)
        if($('#saveSearchFacetVal').val()) {
          $('#searchOnResult').attr('value',"");
          uqry = "?unformattedQuery="+$('#saveSearchFacetVal').val();
        }
        $.ajax({
          type: 'POST',
          url: '/search/saveSearch.action'+uqry,
          data: $('#searchStripForm').serialize(),
          dataFilter: function (data, type) {
            return data.replace(/(\/\*|\*\/)/g, '');
          },
          dataType:'json',
          success: function(response) {
            if (response.exception) {
              var errorMessage = response.exception.message;
              $("#span_error_savedsearch").html("Exception: " + errorMessage);
              return;
            }
            if (response.actionErrors && response.actionErrors.length > 0) {
              //The action in question can only return one message
              var errorMessage = response.actionErrors[0];
              $("#span_error_savedsearch").html("Error: " + errorMessage);
              return;
            }

            $('#mask , #save-search-box').fadeOut(300 , function() {
              $('#mask').remove();
            });
          },
          error: function(req, textStatus, errorThrown) {
            $('#span_error_savedsearch').html(errorThrown.message);
            console.log("error: " + errorThrown.message);
          }
        });

        // Setting back the original value of the query.
        //This is required if the user wants to save the original search.
        $('#searchOnResult').attr('value',query);
        $('#saveSearchFacetVal').attr('value',"");
      });

      $('#btn_cancel_savedsearch').click(function() {
        $('#mask , #save-search-box').fadeOut(300 , function() {
          $('#mask').remove();
        });
      });

      $(document).bind('keydown', function(e) {
        if (e.which == 27) {
          $('#mask , #save-search-box').fadeOut(300 , function() {
            $('#mask').remove();
          });
        }
      });

    });

function setFacetSearchValue(id){
  $('#saveSearchFacetVal').attr('value',id);
}
