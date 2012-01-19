<!-- begin : main content -->
<div id="content" class="static">

  <h1>Browse Articles in <em>PLoS ONE</em></h1>

  <p class="intro"><em>PLoS ONE</em> publishes new articles weekly. You can browse the journal contents by:</p>
  <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubjectURL"/>
  <@s.url action="browse" namespace="/article" field="date" includeParams="none" id="browseDateURL"/>

  <ul>
    <li><a href="${browseDateURL}" title="PLoS ONE | Browse by Publication Date">By Publication Date</a> - Browse articles by choosing a specific week or month of publication.</li>
    <li><a href="${browseSubjectURL}" title="PLoS ONE | Browse by Subject">By Subject</a> -  Browse articles published in a specific subject area.</li>
  </ul>

</div>
<!-- end : main contents -->
