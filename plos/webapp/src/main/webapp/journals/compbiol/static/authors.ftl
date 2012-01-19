<#include "journal_include.ftl">

<!-- begin : main content -->
<div id="content" class="static">

<h1>For <em>PLoS Computational Biology</em> Authors and Reviewers</h1>

<@s.url action="policies" namespace="/static" includeParams="none" id="policiesURL"/>
<@s.url action="competing" namespace="/static" includeParams="none" id="competingURL"/>
<@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelinesURL"/>
<@s.url action="latexGuidelines" namespace="/static" includeParams="none" id="latexURL"/>
<@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figureGuidelinesURL"/>
<@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
<@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewerGuidelinesURL"/>

<ul>
	<li><@s.a href="${policiesURL}" title="PLoS Computational Biology | Editorial and Publishing Policies">Editorial and Publishing Policies</@s.a> - <em>PLoS Computational Biology</em> editorial and publishing policies
		<ul>
			<li><@s.a href="${competingURL}" title="PLoS Computational Biology | Competing Interests Policy ">Competing Interests Policy</@s.a> - PLoS policy on competing interests of authors, reviewers, and editors</li>
		</ul>
	</li>
	<li><@s.a href="${guidelinesURL}" title="PLoS Computational Biology | Guidelines for Authors">Author Guidelines</@s.a> - Detailed guidelines for preparing your manuscript
		<ul>
			<li><@s.a href="${latexURL}" title="PLoS Pathogens | LaTeX Guidelines">LaTeX Guidelines</@s.a> - Instructions for submitting LaTeX files</li>
		</ul> 
		</li>
	<li><@s.a href="${figureGuidelinesURL}" title="PLoS Computational Biology | Figure Guidelines">Table and Figure Preparation</@s.a> - Detailed guidelines for preparing publication-quality figures</li>
	<li><@s.a href="${cklistURL}" title="PLoS Computational Biology | Checklist for Manuscript Submission">Submitting Your Manuscript</@s.a> - Check this list before you submit your manuscript.
			<ul>
		<li><a href="${jms_link}" title="PLoS Computational Biology | Online Manuscript Submission and Review System">Submit Manuscript</a></li>
	</ul>
	</li>
	<li><@s.a href="${reviewerGuidelinesURL}" title="PLoS Computational Biology | Reviewer Guidelines ">Reviewer Guidelines</@s.a> - Criteria for publication, the review process, and other guidelines</li>
</ul>

</div>
<!-- end : main contents -->