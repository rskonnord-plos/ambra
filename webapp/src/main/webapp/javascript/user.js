
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
    $("li").removeClass("form-error");
  }

  var validateResponse = function(formObj, response) {
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
        if(validateResponse($('form[name=userForm]'), response)) {
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
      .done(function(json) {
        //There is no form to validate
        confirmedSaved();
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
        confirmedSaved();
      })
      .fail(function(response) {
        displaySystemError($('form[name=userSearchAlerts]'), response);
        console.log(response);
      });
  });
});

