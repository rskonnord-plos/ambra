<#include "journal_include.ftl">

<!-- begin : main content -->
<div id="content" class="static">

<h1>For <em>PLoS Pathogens</em> Authors and Reviewers</h1>

<@s.url action="policies" namespace="/static" includeParams="none" id="policiesURL"/>
<@s.url action="competing" namespace="/static" includeParams="none" id="competingURL"/>
<@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelinesURL"/>
<@s.url action="latexGuidelines" namespace="/static" includeParams="none" id="latexURL"/>
<@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figureGuidelinesURL"/>
<@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
<@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewerGuidelinesURL"/>

<ul>
	<li><@s.a href="${policiesURL}" title="PLoS Pathogens | Editorial and Publishing Policies">Editorial and Publishing Policies</@s.a> - <em>PLoS Pathogens</em> editorial and publishing policies
		<ul>
			<li><@s.a href="${competingURL}" title="PLoS Pathogens | Competing Interests Policy ">Competing Interests Policy</@s.a> - PLoS policy on competing interests of authors, reviewers, and editors</li>
		</ul>
	</li>
	<li><@s.a href="${guidelinesURL}" title="PLoS Pathogens | Guidelines for Authors">Author Guidelines</@s.a> - Detailed guidelines for preparing your manuscript
		<ul>
			<li><@s.a href="${latexURL}" title="PLoS Pathogens | LaTeX Guidelines">LaTeX Guidelines</@s.a> - Instructions for submitting LaTeX files</li>
		</ul> 
	</li>	
	<li><@s.a href="${figureGuidelinesURL}" title="PLoS Pathogens | Figure Guidelines">Table and Figure Preparation</@s.a> - Detailed guidelines for preparing publication-quality figures</li>
	<li><@s.a href="${cklistURL}" title="PLoS Pathogens | Checklist for Manuscript Submission">Submitting Your Manuscript</@s.a> - Check this list before you submit your manuscript.
			<ul>
		<li><a href="${jms_link}" title="PLoS Pathogens | Online Manuscript Submission and Review System">Submit Manuscript</a></li>
	</ul>
	</li>
	<li><@s.a href="${reviewerGuidelinesURL}" title="PLoS Pathogens | Reviewer Guidelines ">Reviewer Guidelines</@s.a> - Criteria for publication, the review process, and other guidelines</li>
</ul>

</div>
<!-- end : main contents -->