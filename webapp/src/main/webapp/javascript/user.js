$(function () {

  var indexes = {
    alerts:1,
    preferences:0
  };
  var activeIndex = indexes[getParameterByName("tabId")] || 0;

//  setup tabs
  $("#user-forms").tabs({
    selected:activeIndex,
    beforeLoad:function (event, ui) {
      ui.jqXHR.error(function () {
        ui.panel.html("Couldn't load this tab. We'll try to fix this as soon as possible.");
      });
    }
  });


//  checkboxes on the alerts form
  $("#checkAllWeekly").change(function () {
    $("li.alerts-weekly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });
  $("#checkAllMonthly").click(function () {
    $("li.alerts-monthly input").not(":first")
      .attr("checked", $(this).is(":checked"));
  });

});
