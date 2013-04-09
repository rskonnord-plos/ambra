<div id="pagebdy-wrap">
  <div id="pagebdy">
    <div id="static-wrap">
      <div id="error-500">

        <@s.url action="feedback" namespace="/feedback" includeParams="none" id="feedbackURL"/>

          <h1>Something's Broken!</h1>

          <div id="image">
          </div>

          <div id="text">
            <p>We're sorry, our server has encountered an internal error or misconfiguration and is unable to complete your
              request. This is likely a temporary condition so please try again later.</p>
          </div>

          <p class="in-between">If you continue to experience problems with the site, please provide a detailed account of
            the circumstances
            on
            our <a href="${feedbackURL}">feedback form</a>.</p>

          <p>Thank you for your patience.</p>
      </div>
      <#include "/includes/stacktrace.ftl">
    </div><!-- static-wrap -->
  </div><!-- pagebdy -->
</div><!-- pagebdy-wrap -->
