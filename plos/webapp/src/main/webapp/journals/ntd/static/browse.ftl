<!-- begin : main content -->
<div id="content" class="static">

<h1>Browse Articles</h1>

<p class="intro"><em>PLoS Neglected Tropical Diseases</em> publishes new articles as often as once per week. You can browse the journal contents by:</p>

<@s.url action="browseIssue" field="issue" namespace="/article" includeParams="none" id="browseIssueURL"/>
<@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
<@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>

<ul>
    <li><@s.a href="${browseIssueURL}" title="PLoS NTDs | Current Issue">Current Issue</@s.a> - Browse the Table of Contents for the most recently published issue</li>
    <li><@s.a href="${browseDateURL}" title="PLoS NTDs | Browse by Publication Date">By Publication Date</@s.a> - Browse articles by choosing a specific week or month of publication</li>
    <li><@s.a href="${browseSubURL}" title="PLoS NTDs | Browse by Subject">By Subject</@s.a> - Browse articles published in a specific subject area</li>
	<li><a href="http://collections.plos.org/plosntds/" title="Collections.plos.org | PLoS NTDs Collections">Collections</a> - Browse selected articles; watch videos from the Tri-I Forum on Neglected Diseases</li>
</ul>
  
</div>
<!-- end : main contents -->