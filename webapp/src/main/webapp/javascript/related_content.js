/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2012 by Public Library of Science
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


$(function() {

  var solrHost = $('meta[name=solrHost]').attr("content"), relatedAuthorQuery, doi,
      docs, i, views, citations, title, meta, info, li;

  relatedAuthorQuery = $('input[id="related_author_query"]').val();
  doi = $('meta[name="citation_doi"]').attr('content');

  if (relatedAuthorQuery) {
    $.jsonp({
      url: solrHost,
      callbackParameter: "json.wrf",
      data: {
        wt: "json",
        q: 'author:(' + relatedAuthorQuery + ') AND !id:"' + doi + '"',
        fq: 'doc_type:full AND !article_type_facet:"Issue Image"',
        fl: 'counter_total_all,alm_scopusCiteCount,title_display,author_display,article_type,publication_date,id,journal',
        sort: 'alm_scopusCiteCount desc',
        rows: '5'
      },
      success: function(json) {

        docs = json.response.docs;
        if (docs.length > 0) {
          for (i = 0; i < docs.length; i++) {
            views = $('<div></div>').attr("class", "views").html("<span>" + docs[i].counter_total_all + "</span> Views")
            citations = $('<div></div>').attr("class", "citations").html("<span>" + docs[i].alm_scopusCiteCount + "</span> Citations");
            title = $('<h4></h4>').append('<a href="http://dx.plos.org/' + docs[i].id + '">' + docs[i].title_display + "</a>");
            meta = $('<div></div>').attr("class", "meta").append(docs[i].article_type + " | " +
                docs[i].publication_date + " | " + docs[i].journal + "<br> doi:" + docs[i].id);
            info = $('<div></div>').attr('class', 'info').append(title, docs[i].author_display.join(", "), meta);
            li = $('<li></li>').attr("class", "cf").append(views, citations, info);
            $('div[id="more_by_authors"] > ul').append(li);
          }
        } else {
          $('div[id="more_by_authors"]').hide();
        }
      },
      error: function() {
        $('div[id="more_by_authors"]').hide();
      }
    });
  }

});