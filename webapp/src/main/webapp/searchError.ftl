<div id="pagebdy-wrap">
  <div id="pagebdy">
    <div id="static-wrap">
      <div id="error-500">
        <@s.url action="searchHelp" namespace="/static" includeParams="none" id="searchHelp"/>

        <@s.url action="feedback" namespace="/feedback" includeParams="none" id="feedbackURL"/>

          <h1>Search Error!</h1>

          <div id="image">
          </div>

          <div id="text">
            <p>Unfortunately your search could not be processed at this time.
            It is possible that you have entered search criteria that could not be processed by our search engine.<br/>
            <br/>
            Please visit our <a href="${searchHelp}">search help</a> for additional details on how to use search.
            If you have checked the search help page and your search is a valid search,
            please try again later as our search engine may be down at this time.</p>

            <p class="in-between">If you continue to experience problems with the site, please provide a detailed account of
              the circumstances
              on
              our <a href="${feedbackURL}">feedback form</a>.</p>
          </div>
        </div>
        <#include "/includes/stacktrace.ftl">
      </div>
    </div>
  </div>
</div>