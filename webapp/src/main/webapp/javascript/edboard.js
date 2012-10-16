/*
 * $HeadURL$
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
 * Provides functionality for the dynamic academic editor search on solr.
 * (/static/edboard.action).
 */

/**
 * Main function to get the Academic Editors from SOLR.
 * This clears out existing editors and searches for editors,
 * appending them to the main content area by last name.
 * All arguments are optional.  Possible arguments are:
 * {
 *    query : a 'q' parameter passed to solr (e.g. "expertise:Biology" or
 *        "expertise:Biology AND expertise:Chemistry"). Defaults to "*:*"
 *    formatSectionEds: boolean indicating whether to format section editors
 *        differently from academic editors. If true, the function will pull
 *        an image html string from a div with id "section_editor_icon".
 *        Defaults to true
 *    subjectsOnLine: Maximum number of subjects to show under an editor
 *        before the "show all" link.  -1 to show them all.  Defaults to 5
 *    highlight: boolean indicating whether to highlight snippets of results
 *        or not. Defaults to false.
 * }
 */

$.fn.edBoard = function () {
  var solrHost = $('meta[name=solrHost]').attr("content");

  this.getEditors = function (args) {
    //set the default arguments
    args = $.extend({
      query:"*:*",
      formatSectionEds:true,
      subjectsOnLine:5,
      highlight:false
    }, args);

    //clear out the existing editors
    $("div.editor").each(function (index, obj) { $(this).remove(); });
    $("#all_editors").css("display","none");
    $("#loading").css("display","block");

    //asking solr to do the highlighting is too slow
    //(qtime goes from ~4ms to ~90ms,
    //and you still have to do client side work)
    //so we'll do the highlighting on the client side
    args.queryTerms = [];
    if (args.highlight) {
      $.each(args.query.split(" AND "), function (index, item) {
        var term = item
          .replace(/.*:/, "")
          .replace(/[^a-zA-Z\- ()]/g, "")//filter out characters that break regex
          .replace(/\(/, "\\(")
          .replace(/\)/, "\\)");
        args.queryTerms.push(new RegExp("(" + term + ")", "ig"));
      });
    }

    if (args.formatSectionEds) {
      args.icon_html = $("#section_editor_icon").html();
    }

    //load callback function
    //defining it here allows us to reference the
    //query terms for highlighting
    var loadCallback = function (args) {
      var editorsDiv = $("#all_editors");

      $.each(args.data.response.docs, function (index, editor) {

        var firstLetter = editor.ae_name
          .replace(/[^\w\s]|_/g, "")
          .split(" ")
          .pop()[0]
          .toUpperCase();

        var container = $("#" + firstLetter + "_editors");

        //create a div for the editor
        var entry = $("<div></div>").addClass("editor");
        container.append(entry);

        var nameDiv = $("<div></div>").addClass("name").html(editor.ae_name);
        entry.append(nameDiv);

        //show an icon if the person is a section editor
        if (args.formatSectionEds && editor.doc_type == "section_editor") {
          nameDiv.prepend($("<div></div>").addClass("icon_holder").html(args.icon_html));
          nameDiv.append($("<span></span>").addClass("section_ed").html(" Section Editor"));
        }

        entry.append($("<div></div>")
          .addClass("organization")
          .html(editor.ae_institute.join(", ")));

        entry.append($("<div></div>")
          .addClass("location")
          .html(editor.ae_country.join(", ")));

        if (editor.ae_subject) {
          //highlight the subjects
          if (args.highlight) {
            $.each(editor.ae_subject, function (index, subject) {
              $.each(args.queryTerms, function (index, term) {
                subject = subject.replace(term, "<span class=\"highlight\">$1</span>");
              });
              editor.ae_subject[index] = subject;
            });
          }

          //limit the number of subjects we show for expertise, with a show more link
          //we don't want to split by number of characters, since then we might end up
          //splitting in the middle of a highlight span tag
          if (args.subjectsOnLine <= 0 ||
            editor.ae_subject.length <= args.subjectsOnLine) {
            entry.append($("<div></div>").addClass("expertise")
              .html("<b>Expertise:</b> " + editor.ae_subject.join(", ")));
          } else {
            //node with the first n subjects
            var editorNode = $("<div></div>").addClass("expertise").html(
              "<b>Expertise:</b> " +
                editor.ae_subject.slice(0, args.subjectsOnLine).join(", ") + ","
            );
            entry.append(editorNode);

            var elipses = $("<span></span>").html("... ");
            editorNode.append(elipses);

            //rest of subjects, showable

            var more = $("<span></span>").css("display","none").html(
              " " + editor.ae_subject.slice(args.subjectsOnLine).join(", "));
            editorNode.append(more);

            var showMore = $("<a></a>").html("Show all")
              .attr("href", "javascript:void(0)")
              .addClass("show_all")
              .click(function (eventObj) {
                $(this).css("display", "none");
                elipses.css("display", "none");
                more.css("display", "inline");
              });

            editorNode.append(showMore);
          }
        }

        entry.append($("<hr>").addClass("editor_separator"));
      });

      //unhide the editor sections that were
      //hidden on previous searches
      $(".hidden").removeClass("hidden");

      //hide the sections with no editors
      $(".editor_section").each(function (index, div) {
        var parent = $(".editor_section:eq(" + index + ")");
        var childrens = $(".editor_section:eq(" + index + ") > div.editor");

        if (childrens.length == 0) {
          parent.addClass("hidden");
          $("a[href=\"#" + div.id + "\"]").addClass("hidden");
        }
      });

      //show the loaded data
      $("#loading").fadeOut();
      $(editorsDiv).show( "blind", 500 );
    }; //end loadCallback

    //make the request to solr
    $.jsonp({
      url: solrHost,
      context: document.body,
      timeout: 10000,
      callbackParameter: "json.wrf",
      data:{
        q: args.query,
        wt: "json",
        fq: "doc_type:(section_editor OR academic_editor) AND cross_published_journal_key:PLoSONE",
        fl: "doc_type,ae_name,ae_institute,ae_country,ae_subject",
        sort: "ae_last_name asc,ae_name asc",
        rows: 9999
      },
      success: function (data) {
        args["data"] = data;
        loadCallback(args);
      },
      error: function (xOptions, textStatus) {
        console.log(textStatus);
      }

    });
  };

  /**
   * Initialize an autosuggest search box that queries solr for the
   * subjects starting with the user entered string.  Arguments should
   * be of the form:
   * {
   *   textbox: id text input box
   *   button: id search button for the box
   *   searchFunction: callback to take the value from the search box
   *                    and run a search.  Note that the autocomplete
   *                    supports multiple values, separated by commas
   * }
   */
  this.initializeAutoSuggest = function (args) {
    //the textbox to do auto suggext
    var textbox = $("#" + args.textBox);
    //search button to attach a listener to
    var searchButton = $("#" + args.searchButton);
    //reset button to attach a listener to
    var resetButton = $("#" + args.resetButton);

    //show a shaded suggestion in the box
    var default_value = "Biology, Chemistry, ...";
    textbox.css("opacity", 0.5);
    textbox.val(default_value);

    textbox.focus(function (eventObj) {
      if ($(this).val() == default_value) {
        $(this).val("");
      }
      $(this).css("opacity", 1);
    });

    textbox.keyup(function (eventObj) {
      if(eventObj.keyCode == 13) {
        //Enter pressed
        //Select first element (if any) as value
        //Simulate filter click
        console.log($(this).data("autocomplete").menu.options);
        if($(this).data("autocomplete").menu.options) {
          console.log($(this).data("autocomplete").menu.options[0]);
        }
      }
    });

    searchButton.click(function (eventObj) {
      var textbox = $("#" + args.textBox);

      if (textbox.val() == default_value) {
        textbox.val("");
      }

      args.searchFunction(textbox.val());
    });

    $("#" + args.textBox).autocomplete({
      select: function(event, ul) {
        var terms = event.target.value.split(",");

        //Pop last value
        terms.pop();

        if(terms.length > 0) {
          event.target.value = terms.join(", ") + ", " + ul.item.value;
        } else {
          event.target.value = ul.item.value;
        }

        return false;
      },

      focus: function(event, ul) {
        //Don't update the text of the box until a selection is made
        return false;
      },

      source: function(entry, response) {
        var query = [];
        var terms = entry.term.split(",");

        if(terms.length > 0) {
          var prefix = terms.pop().trim().toLowerCase();

          $.each(terms, function(index, subject) {
            if(subject.trim().length > 0) {
              query.push("ae_subject:\"" + subject.trim() + "\"");
            }
          });

          if(query.length == 0) {
            query.push("*:*")
          }

          $.jsonp({
            url: solrHost,
            context: document.body,
            timeout: 10000,
            data: {
              wt:"json",
              q:query.join(" AND "),
              fq:"doc_type:(section_editor OR academic_editor) AND cross_published_journal_key:PLoSONE",
              "facet":true,
              "facet.field":"ae_subject_facet",
              "facet.sort":"index",
              "facet.prefix":prefix
            },
            callbackParameter: "json.wrf",
            success: function(json, textStatus, xOptions) {
              var options = [];
              var subjects = json.facet_counts.facet_fields.ae_subject_facet;

              $.each(subjects, function (index, subject) {
                //solr returns a list that looks like
                // ["biology",2411, "biophysics",344]

                //Onlu push terms that haven't been selected. :-)
                if($.inArray(subject, terms) == -1) {
                  if (index % 2 == 0 && subjects[index + 1] > 0) {
                    var countString = " (" + subjects[index + 1] + ")";
                    options.push({ label:subject + countString, value:subject });
                  }
                }
              });

              response(options);
            },
            error: function(xOptions, error) {
              console.log(error);
            }
          });
        }
      }
    });
  };
};