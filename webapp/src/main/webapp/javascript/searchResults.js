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
      ids = new Array();

    $("li[doi]").each(function(index, element) {
      ids[ids.length] = $(element).attr("doi");
    });

    console.log(ids);

    almService.getSummaryForArticles(ids, setALMSearchWidgets, setALMSearchWidgetsError);

    function setALMSearchWidgets() {
      console.log('setALMSearchWidgets');
    }

    function setALMSearchWidgetsError() {
      console.log('setALMSearchWidgetsError');
    }
});