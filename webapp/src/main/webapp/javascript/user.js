
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
});

