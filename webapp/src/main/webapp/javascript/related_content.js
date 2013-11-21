/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
 *   http://plos.org
 *   http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


$(function () {

  var solrHost = $('meta[name=searchHost]').attr("content"), relatedAuthorQuery, doi,
    docs, i, views, citations, title, meta, info, li;

  relatedAuthorQuery = $('input[id="related_author_query"]').val();
  doi = $('meta[name="citation_doi"]').attr('content');

  if (relatedAuthorQuery) {
    $.jsonp({
      url:solrHost,
      callbackParameter:"json.wrf",
      data:{
        wt:"json",
        q:'author:(' + relatedAuthorQuery + ') AND !id:"' + doi + '"',
        fq:'doc_type:full AND !article_type_facet:"Issue Image"',
        fl:'counter_total_all,alm_scopusCiteCount,title_display,author_display,article_type,publication_date,id,journal',
        sort:'alm_scopusCiteCount desc',
        rows:'5'
      },
      success:function (json) {
        docs = json.response.docs;
        if (docs.length > 0) {
          for (i = 0; i < docs.length; i++) {
            //Parse the date without UTC logic
            var dateParts = /^(\d{4})-(\d{2})-(\d{2})T(.*)Z$/.exec(docs[i].publication_date);
            var pubDate = new Date(dateParts[1], dateParts[2] - 1, dateParts[3]);
            var views = $('<div></div>').attr("class", "views").html("<span>" + docs[i].counter_total_all + "</span> Views");
            var citations = $('<div></div>').attr("class", "citations").html("<span>" + docs[i].alm_scopusCiteCount + "</span> Citations");
            var title = $('<h4></h4>').append('<a href="http://dx.plos.org/' + docs[i].id + '">' + docs[i].title_display + "</a>");
            var authors = $('<div></div>').append(docs[i].author_display.join(", ")).css('float','none');;
            var meta = $('<div></div>').attr("class", "meta").append(docs[i].article_type + " | " +
              $.datepicker.formatDate('dd M yy', pubDate) + " | " + docs[i].journal + "<br> doi:" + docs[i].id).css('float','none');;
            var info = $('<div></div>').attr('class', 'info').append(title, authors, meta);
            var li = $('<li></li>').attr("class", "cf").append(views, citations, info);
            $('div[id="more_by_authors"] > ul').append(li);
          }
          $('div[id="more_by_authors"]').show("blind", 500);
        }
      }
    });
  }


  var categoryDisplayNameMap = { "News": "News Media Coverage", "Blog": "Blog Coverage", "Other": "Related Resources" };

  //Put the ALM mediaTracker response array into buckets matching their types
  var categorizeReferences = function(response) {
    //The names of these buckets may have to be changed to reflect API changes
    var result = { "News": [], "Blog": [], "Other": [] };
    var typeGroupOther = result["Other"];

    for(var a = 0; a < response.length; a++) {
      var cur = response[a].event;
      var typeGroup = result[cur.type];

      if(typeof typeGroup === 'undefined') {
        typeGroupOther.push(cur);
      } else {
        typeGroup.push(cur);
      }
    }

    return result;
  };

  //Create the LI block for one referral
  var createReferenceLI = function(curReference) {
    var publication = "Unkown"
    if(curReference.publication.length > 0) {
      publication = curReference.publication;
    }

    var title = "Unkown"
    if(curReference.title.length > 0) {
      title = curReference.title;
    }

    var publication_date = "Unkown"
    if(curReference.published_on.length > 0) {
      publication_date = $.datepicker.formatDate('dd M yy', new Date(Date.parse(curReference.published_on)));
    }

    var liItem = $('<li></li>').html('<b>' + publication + '</b>: "<a href="' + curReference.referral + '">' + title +
      '</a>"&nbsp;&nbsp;' + publication_date);

    return liItem;
  };

  //Create the HTML block for the media coverage
  var createReferencesHTML = function(categorizedResults) {
    var html = $('<div></div>');
    var keys = Object.keys(categorizedResults);
    for(var a = 0; a < keys.length; a++) {
      var category = keys[a];
      var categoryDisplay = categoryDisplayNameMap[category];
      $(html).append($('<h3></h3>').html(categoryDisplay));

      if(categorizedResults[category].length > 0) {
        var list = $('<ul></ul>');
        for(var b = 0; b < categorizedResults[category].length; b++) {
          var curReference = categorizedResults[category][b];
          list.append(createReferenceLI(curReference));
        }

        $(html).append(list);
      }
    }

    return html;
  };


  var mediaReferenceSucces = function(result) {
    //Put results into buckets

    //assuming here the request was properly formatted to only get media information
    var mediaSource = result[0].sources[0]

    // don't display anything if there isn't any data
    if (mediaSource.events.length > 0) {
      var categorizedResults = categorizeReferences(mediaSource.events);

      //Build up the UI
      var docFragment = createReferencesHTML(categorizedResults);

      $("#media_coverage").append(docFragment);
      $("#media_coverage").show("blind", 500);
    }
  };

  var mediaReferenceFailure = function(result) {
    // don't display anything if there is an error
    $("#media_coverage").hide();

    console.error(result);
  };

  var almService = new $.fn.alm();

  almService.getMediaReferences(doi, mediaReferenceSucces, mediaReferenceFailure)
});