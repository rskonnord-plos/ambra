
$(function () {

  var indexes = {
    profile:0,
    journalAlerts:1,
    savedSearchAlerts:2
  };

  var activeIndex = indexes[$("#user-forms").attr('active')] || 0;

  //TODO: Remove one day
  //Support for OLD URLs:
  //user/secure/editProfile.action?tabId=preferences
  //user/secure/editProfile.action?tabId=alerts$
  //user/secure/editProfile.action?tabId=savedSearchAlerts
  tabParam = getParameterByName("tabId");
  if(tabParam) {
    if(tabParam == "preferences") {
      activeIndex = 0;
    }

    if(tabParam == "alerts") {
      activeIndex = 1;
    }

    if(tabParam == "savedSearchAlerts") {
      activeIndex = 2;
    }
  }//End block to delete

  /*
  * Functions for the taxonomy browser
  **/
  var getJournalSubjectsFormValue = function(journal) {
    var results = [];

    $("input[name=journalSubjectFilters\\[\\'" + journal + "\\'\\]]").each(function(index, node) {
        results.push($(node).val())
      }
    );

    return results;
  }

  var setJournalSubjectsFormValue = function(journal, subjects) {
    var form = $("form[name=userAlerts]");

    $("input[name=journalSubjectFilters\\[\\'" + journal + "\\'\\]]").remove();

    for(var i = 0; i < subjects.length; i++) {
      form.append($("<input type=\"hidden\" name=\"journalSubjectFilters['" + journal + "']\" value=\"" + subjects[i] + "\">"));
    }
  }

  var selectSubject = function(subject) {
    var list = $('div.subjectsSelected ol');

    selectedSubjects = getJournalSubjectsFormValue(journal);
    selectedSubjects.push(subject);

    findAndDisableSubject(subject);
    setJournalSubjectsFormValue(journal, selectedSubjects);

    if($("div.noSubjectsSelected").is(":visible")) {
      $("div.noSubjectsSelected").slideUp({ complete: function() {
        $('div.subjectsSelected').slideDown();
      }});
    }

    var newNode = $("<li><div class=\"filter-item\">" + subject + "&nbsp;<img src=\"/images/btn.close.png\"></div></li>");

    newNode.find("img").click(function(event) {
      removeSubject($(event.target).parent().text().trim());
    });

    list.append(newNode);

    enforceFormState();
  };

  var removeSubject = function(subject) {
    var list = $('div.subjectsSelected ol');

    selectedSubjects = getJournalSubjectsFormValue(journal);
    selectedSubjects = selectedSubjects.filter(function(value) {
      return (subject != value);
    });

    list.find("div:contains(" + subject + ")").parent().slideUp({ complete: function() {
      list.find("div:contains(" + subject + ")").parent().remove();
    }});

    findAndEnableSubject(subject);
    setJournalSubjectsFormValue(journal, selectedSubjects);

    if(selectedSubjects.length == 0) {
      $("div.subjectsSelected").slideUp({ complete: function() {
        $('div.noSubjectsSelected').slideDown();
      }});
    }
  };

  /**
   * If the user selects a subject without first making other form selections
   * Auto selected appropriate items
   */
  var enforceFormState = function() {
    $("#subjectSome_" + journal).prop('checked','true');
    $("form[name=userAlerts] input[name=weeklyAlerts][value=" + journal + "]").prop('checked', 'true');
  };

  var toggleSubjectSelector = function(eventObj, journal) {
    var selector = $("li.subjectAreaSelector[journal=" + journal + "]");

    if(selector.is(":visible")) {
      $(selector).slideUp();
      $(eventObj.target).find("span.alertToggle").removeClass("alertToggleOn").addClass("alertToggleOff");
    } else {
      $(selector).slideDown();
      $(eventObj.target).find("span.alertToggle").removeClass("alertToggleOff").addClass("alertToggleOn");
    }
  };

  var getTaxonomyTreeText = function(node) {
    parent = $(node).parent().parent()[0];
    if(parent.tagName == 'LI') {
      return(getTaxonomyTreeText($(parent)) + '/' + $(node).attr('key'));
    }

    return $(node).attr('key');
  };

  /* Find matching subjects in the tree and mark them selected */
  var findAndDisableSubject = function(subject) {
    $("span:contains(" + subject + ")")
      .addClass("checked")
      .unbind('click');
  };

  /* Find matching subjects in the tree and enable them to be selected */
  var findAndEnableSubject = function(subject) {
    $("span:contains(" + subject + ")")
      .removeClass("checked")
      .click(function(event) {
        selectSubject($(event.target).text());
      });
  };

  var createTaxonomyNodes = function(rootNode, response) {
    $.each(response.categories, function(key, val) {
      var img = "";

      if(val > 0) {
        img = "<image class=\"expand\" src=\"/images/transparent.gif\"/>";
      } else {
        img = "<image src=\"/images/transparent.gif\"/>";
      }

      var node = $("<li key=\"" + key + "\">" + img + "<span>" + key + "</span><ol></ol></li>")
        .attr("value", key);

      $(rootNode).append(node);

      node.find("span").each(function() {
        var selected = false;

        selectedSubjects = getJournalSubjectsFormValue(journal);

        for (var i = 0; i < selectedSubjects.length; i++) {
          if(selectedSubjects[i] == $(this).text()) {
            selected = true;
          }
        }

        //If the subject is already selected, don't set up events
        if(selected) {
          $(this).addClass("checked");
        } else {
          $(this).click(function(event) {
            selectSubject($(event.target).text());
          });
        }
      });

      if(val > 0) {
        $(node).find('img').click(function(event) {
          //Already expanded state, remove children nodes
          if($(event.target).hasClass("expanded")) {
            $(node).find('ol').children().remove();
            $(event.target).removeClass("expanded");
            $(event.target).addClass("expand");
          } else {
            $(event.target).removeClass("expand");
            $(event.target).addClass("expanded");

            //Grab taxonomy data
            curTree = getTaxonomyTreeText(node);

            $.get("/taxonomy/json/" + curTree, $(this).serialize())
              .done(function(response) {
                createTaxonomyNodes($(node).find('ol'), response);
              })
              .fail(function(response) {
                displaySystemError($('form[name=userAlerts]'), response);
                console.log(response);
              });
          }
        });
      }
    });
  }

  var createTaxonomyNodesFromMap = function(rootNode, filter, map) {
    $.each(map, function(key, val) {
      var hasChildren = !$.isEmptyObject(val);
      var img = "<image " + (hasChildren?"class=\"expanded-nopointer\" ":"") + "src=\"/images/transparent.gif\"/>";
      var node = $("<li key=\"" + key + "\">" + img + "<span>" +
        key.replace(new RegExp("(" + filter + ")", "gi"), "<b>$1</b>")
         + "</span><ol></ol></li>")
        .attr("value", key);

      $(rootNode).append(node);

      node.find("span").each(function() {
        var selected = false;

        selectedSubjects = getJournalSubjectsFormValue(journal);

        for (var i = 0; i < selectedSubjects.length; i++) {
          if(selectedSubjects[i] == $(this).text()) {
            selected = true;
          }
        }

        //If the subject is already selected, don't set up events
        if(selected) {
          $(this).addClass("checked");
        } else {
          $(this).click(function(event) {
            selectSubject($(event.target).text());
          });
        }

        createTaxonomyNodesFromMap(node.find('ol'), filter, val);
      });
    });
  }

  //Grab base taxonomy data
  var getInitialSubjectList = function() {
    $.get("/taxonomy/json", $(this).serialize())
    .done(function(response) {
      createTaxonomyNodes($('#subjectAreaSelector'), response);
    })
    .fail(function(response) {
      displaySystemError($('form[name=userAlerts]'), response);
      console.log(response);
    });
  };

  $(".subjectSearchInput[type='text']").autocomplete({
    select: function(event, ul) {
      $(".subjectSearchInput[type='text']").val(ul.item.value);
      return false;
    },

    focus: function(event, ul) {
      //Don't update the text of the box until a selection is made
      return false;
    },

    source: function(entry, response) {
      var solrHost = $('meta[name=solrHost]').attr("content");

      //make the request to solr
      $.jsonp({
        url: solrHost + "/terms",
        context: document.body,
        timeout: 10000,
        callbackParameter: "json.wrf",
        data:{
          "terms": "true",
          "terms.fl" : "subject_facet",
          "terms.regex" : ".*" + entry.term + ".*",
          "terms.limit" : 25,
          "terms.sort" : "index",
          "terms.regex.flag" : "case_insensitive",
          "wt": "json"
        },
        success: function (data) {
          var options = [];
          //Every other element is what we want
          for(var i = 0; i < data.terms.subject_facet.length; i = i + 2) {
            options.push({
              label:data.terms.subject_facet[i].replace(new RegExp("(" + entry.term + ")", "gi"), "<b>$1</b>"),
              type: "html",
              value:data.terms.subject_facet[i]
            });
          }
          response(options);
        },
        error: function (xOptions, textStatus) {
          console.log(textStatus);
        }
      });
    }
  });

  /* There is partial support here for multiple journals using the selector for the future */
  var journal = $("li.subjectAreaSelector").attr("journal");

  //Bind to UI events
  $("li div.filter-item img").click(function(event) {
    removeSubject($(event.target).parent().text().trim());
  });

  $("#subjectSome_" + journal).click(function (eventObj) {
    enforceFormState();
  });

  $("#alert-form ol > li.filtered").click(function (eventObj) {
    toggleSubjectSelector(eventObj, journal);
  });

  $(":input[name='searchSubject_btn']").click(function(eventObj) {
    var filter = $(".subjectSearchInput[type='text']").val();

    $.get("/taxonomy/json?filter=" + filter, $(this).serialize())
      .done(function(response) {
        $('#subjectAreaSelector').children().remove();
        createTaxonomyNodesFromMap($('#subjectAreaSelector'), filter, response.map);
      })
      .fail(function(response) {
        displaySystemError($('form[name=userAlerts]'), response);
        console.log(response);
      });
  });

  $(".subjectSearchInput[type='text']").keyup(function(eventObj) {
    if ($(eventObj.target).val()) {
      //enforceFormState();
      $("div.clearFilter").css("display", "block");
    } else {
      $("div.clearFilter").css("display", "none");
    }
  });

  $("div.clearFilter").click(function(eventObj) {
    $(".subjectSearchInput[type='text']").val("");
    $("div.clearFilter").css("display", "none");
    $(".subjectSearchInput[type='text']").focus();
    $('#subjectAreaSelector').children().remove();
    getInitialSubjectList();
  });



  if($('#subjectAreaSelector')) {
    getInitialSubjectList();

    //Don't bubble up the event
    $("li.alerts-weekly input").click(function (eventObj) {
      eventObj.stopPropagation();
    });
  }

  /*
   * End functions for the taxonomy browser
   *
   **/

  //setup tabs
  $("#user-forms").tabs();

  var $panes = $(this).find('div.tab-pane');
  var $tab_nav = $(this).find('div.tab-nav');
  var $tab_lis = $tab_nav.find('li');

  $tab_lis.removeClass('active');
  $panes.hide();

  $tab_lis.eq(activeIndex).addClass('active');
  $panes.eq(activeIndex).show();

  //checkboxes on the alerts form
  $("#checkAllWeekly").change(function () {
    $("li.alerts-weekly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllMonthly").click(function () {
    $("li.alerts-monthly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });

  //checkboxes on the search alerts form
  $("#checkAllWeeklySavedSearch").change(function () {
    $("li.search-alerts-weekly input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllMonthlySavedSearch").click(function () {
    $("li.search-alerts-monthly input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  $("#checkAllDeleteSavedSearch").click(function () {
    $("li.search-alerts-delete input").not(":first")
        .attr("checked", $(this).is(":checked"));
  });

  var confirmedSaved = function() {
    var confirmBox = $("#save-confirm");

    //Set the center alignment padding + border see css style
    var popMargTop = ($(confirmBox).height() + 24) / 2;
    var popMargLeft = ($(confirmBox).width() + 24) / 2;

    $(confirmBox).css({
      'margin-top' : -popMargTop,
      'margin-left' : -popMargLeft
    });

    //Fade in the Popup
    $(confirmBox).fadeIn(500)
      .delay(1000)
      .fadeOut(1000);
  };

  //Remove error messages before adding new ones
  var cleanMesssages = function() {
    $("span.required.temp").remove();
    $("div.required.temp").remove();
    $("li").removeClass("form-error");
  }

  var validateProfileResponse = function(formObj, response) {
    var formBtn = $(formObj).find(":input[name='formSubmit']");

    if(!$.isEmptyObject(response.fieldErrors)) {
      var message = $('<span class="required temp">Please correct the errors above.</span>');

      formBtn.after(message);

      $.each(response.fieldErrors, function(index, value) {
        $(formObj).find(":input[name='" + index + "']").each(function(formIndex, element) {
          //Append class to parent LI
          $(element).parent().addClass("form-error");
          $(element).after(" <span class='required temp'>" + response.fieldErrors[index] + "</span>");
        });
      });
    } else {
      return true;
    }
  };

  var validateAlertsResponse = function(formObj, response) {
    //This action can only return one error
    if(response.actionErrors.length > 0) {
      //Display a message at the bottom of the form
      $(formObj).find(":input[name='formSubmit']:eq(1)").each(function(index, val) {
        var message = $('<span class="required temp">Please correct the error listed above.</span>');
        $(val).after(message);
      });

      if($(formObj).find("div.noSubjectsSelected").is(":visible")) {
        $(formObj).find("div.noSubjectsSelected:eq(0)").each(function(index, val) {
          var message = $('<div class="required temp">' + response.actionErrors[0] + '</div>');
          $(val).after(message);
        });
      } else {
        $(formObj).find(":input[name='formSubmit']:eq(0)").each(function(index, val) {
          var message = $('<div class="required temp">' + response.actionErrors[0] + '</div>');
          $(val).before(message);
        });
      }
    } else {
      return true;
    }
  };

  var displaySystemError = function(formObj, response) {
    var message = "System error.  Code: " + response.status + " (" + response.statusText + ")";
    var formBtn = $(formObj).find(":input[name='formSubmit']");

    if($(formObj).find("span.required.temp").size() == 0) {
      var errorP = $('<span class="required temp"/>');

      errorP.text(message);
      $(formBtn).after(errorP);
    } else {
      $(formBtn).find("span.required.temp").text(message);
    }
  };

  //Make the forms submit via ajax
  $('form[name=userForm]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveProfileJSON.action", $(this).serialize())
      .done(function(response) {
        cleanMesssages();
        if(validateProfileResponse($('form[name=userForm]'), response)) {
          confirmedSaved();
        }
      })
      .fail(function(response) {
        displaySystemError($('form[name=userForm]'), response);
        console.log(response);
      });
  });

  $('form[name=userAlerts]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveUserAlertsJSON.action", $(this).serialize())
      .done(function(response) {
        cleanMesssages();
        if(validateAlertsResponse($('form[name=userAlerts]'), response)) {
          confirmedSaved();
        }
      })
      .fail(function(response) {
        displaySystemError($('form[name=userAlerts]'), response);
        console.log(response);
      });
  });

  $('form[name=userSearchAlerts]').submit(function(event) {
    event.preventDefault();

    $.post("/user/secure/saveSearchAlertsJSON.action", $(this).serialize())
      .done(function(json) {
        //There is no form to validate
        $.each(json.deleteAlerts, function(index, value) {
          $("#saID" + value).slideUp(400, function(target) {
            $("#saID" + value).remove();

            if($(".saID").size() == 0) {
              $("#saOL").slideUp();
              $("#sa_none_defined").slideDown();
            }
          });
        });
        confirmedSaved();
      })
      .fail(function(response) {
        displaySystemError($('form[name=userSearchAlerts]'), response);
        console.log(response);
      });
  });
});