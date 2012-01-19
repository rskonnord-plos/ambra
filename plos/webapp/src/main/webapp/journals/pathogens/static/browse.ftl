<!-- begin : main content -->
<div id="content" class="static">

<h1>Browse Articles</h1>

<p class="intro">Browse our journal contents by:</p>

<@s.url action="browseIssue" field="issue" namespace="/article" includeParams="none" id="browseIssueURL"/>
<@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
<@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>
   <@s.url action="browseVolume" namespace="/article" field="volume" includeParams="none" id="archiveURL"/>

<ul>
    <li><@s.a href="${browseIssueURL}" title="PLoS Pathogens| Current Issue">Current Issue</@s.a> - Browse the Table of Contents for the most recently published issue</li>
	<li><a href="${archiveURL}">Journal Archive</a> - Browse the Table of Contents of past issues of the journal.</li>
    <li><@s.a href="${browseDateURL}" title="PLoS Pathogens| Browse by Publication Date">By Publication Date</@s.a> - Browse articles by choosing a specific week or month of publication</li>
    <li><@s.a href="${browseSubURL}" title="PLoS Pathogens| Browse by Subject">By Subject</@s.a> - Browse articles published in a specific subject area</li>

</ul>
  
</div>
<!-- end : main contents -->