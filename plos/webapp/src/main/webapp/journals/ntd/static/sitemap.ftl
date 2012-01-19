
<!-- begin : main content -->
<div id="content" class="static">

<h1><em>PLoS Neglected Tropical Diseases</em> Site Map</h1>

<h2>Home</h2>
<ul>
    <@s.url action="home" namespace="/" includeParams="none" id="homeURL"/>
    <@s.url action="rssFeeds" namespace="/static" includeParams="none" id="rssFeedURL"/>
    <@s.url action="releaseNotes" namespace="/static" includeParams="none" id="releaseURL"/>
    <@s.url namespace="/user/secure" includeParams="none" id="loginURL" action="secureRedirect" goTo="${thisPage}"/>
	
    <li><@s.a href="${homeURL}" title="PLoS NTDs | Home page">Home page</@s.a></li>
    <li><@s.a href="${rssFeedURL}" title="PLoS NTDs | RSS Feeds">RSS Feeds</@s.a></li>
    <li><@s.a href="${loginURL}" title="PLoS NTDs | Account Login">Login</@s.a></li>
    <li><@s.a href="${freemarker_config.registrationURL}" title="PLoS NTDs | Create a New Account">Create Account</@s.a></li>
    <li><@s.a href="${feedbackURL}" title="PLoS NTDs | Send Us Your Feedback">Send Us Feedback</@s.a></li>
    <li><@s.a href="${releaseURL}" title="PLoS NTDs | Release Notes">Release Notes</@s.a></li>
</ul>

<h2>Browse Articles</h2>
<ul>
    <@s.url action="toc" namespace="/static" includeParams="none" id="browseIssueURL"/>
    <@s.url action="archive" namespace="/static" includeParams="none" id="browseVolumeURL"/>
    <@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
    <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>
	
    <li><@s.a href="${browseIssueURL}" title="PLoS NTDs | Current Issue">Current Issue</@s.a></li>
	<li><@s.a href="${browseVolumeURL}" title="PLoS NTDs | Journal Archive">Journal Archive</@s.a></li>
    <li><@s.a href="${browseDateURL}" title="PLoS NTDs | Browse by Publication Date">By Publication Date</@s.a></li>
    <li><@s.a href="${browseSubURL}" title="PLoS NTDs | Browse by Subject">By Subject</@s.a></li>
    <li><a href="http://collections.plos.org/plosntds/" title="Collections.plos.org | PLoS NTDs Collections">Collections</a></li>
</ul>

<h2>About</h2>
 <ul>
    <@s.url action="information" namespace="/static" includeParams="none" id="infoURL"/>
    <@s.url action="scope" namespace="/static" includeParams="none" id="scopeURL"/>
    <@s.url action="edboard" namespace="/static" includeParams="none" id="edboardURL"/>
    <@s.url action="eic" namespace="/static" includeParams="none" id="eicURL"/>
    <@s.url action="license" namespace="/static" includeParams="none" id="licenseURL"/>
    <@s.url action="contact" namespace="/static" includeParams="none" id="contactURL"/>
	
    <li><@s.a href="${infoURL}" title="PLoS NTDs | Journal Information">Journal Information</@s.a></li>
	<li><@s.a href="${scopeURL}" title="PLoS NTDs | Journal Scope">Journal Scope</@s.a></li>
    <li><@s.a href="${edboardURL}" title="PLoS NTDs | Editorial Board">Editorial Board</@s.a></li>
    <li><@s.a href="${eicURL}" title="PLoS NTDs | Editor-in-Chief">Editor-in-Chief</@s.a></li>
    <li><@s.a href="${licenseURL}" title="PLoS NTDs | Open-Access License">Open-Access License</@s.a></li>
    <li><@s.a href="${contactURL}" title="PLoS NTDs | Contact Us">Contact Us</@s.a></li>
  </ul>
  
 <h2>For Readers</h2>
<ul>
    <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="commentURL"/>
    <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="ratingURL"/>
    <@s.url action="help" namespace="/static" includeParams="none" id="helpURL"/>
    <@s.url action="downloads" namespace="/static" includeParams="none" id="downloadsURL"/>
	
    <li><@s.a href="${commentURL}" title="PLoS NTDs | Guidelines for Notes, Comments, and Corrections">Guidelines for Notes, Comments, and Corrections</@s.a></li>
    <li><@s.a href="${ratingURL}" title="PLoS NTDs | Guidelines for Rating">Guidelines for Rating</@s.a></li>
    <li><@s.a href="${helpURL}" title="PLoS NTDs | Help Using this Site">Help Using This Site</@s.a></li>
	<li><@s.a href="${downloadsURL}" title="PLoS NTDs | Media Downloads">Media Downloads</@s.a></li>
    <li>Site Map</li>
</ul>

<h2>For Authors and Reviewers</h2>
<ul>
	<@s.url action="whypublish" namespace="/static" includeParams="none" id="whypublishURL"/>
	<@s.url action="developing" namespace="/static" includeParams="none" id="developingURL"/>
	<@s.url action="policies" namespace="/static" includeParams="none" id="policiesURL"/>
	<@s.url action="competing" namespace="/static" includeParams="none" id="competingURL"/>
	<@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelinesURL"/>
	<@s.url action="latexGuidelines" namespace="/static" includeParams="none" id="latexURL"/>
	<@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figureGuidelinesURL"/>
	<@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
	<@s.url action="revisedChecklist" namespace="/static" includeParams="none" id="revisedChecklistURL"/>
	<@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewerGuidelinesURL"/>

	<li><@s.a href="${whypublishURL}" title="PLoS NTDs | Why Publish With Us?">Why Publish With Us?</@s.a></li>
	<li><@s.a href="${developingURL}" title="PLoS NTDs | Information for Authors in Developing Countries">Information for Authors in Developing Countries</@s.a></li>
	<li><@s.a href="${policiesURL}" title="PLoS NTDs | Editorial and Publishing Policies">Editorial and Publishing Policies</@s.a></li>
	<ul>
		<li><@s.a href="${competingURL}" title="PLoS NTDs | Competing Interests Policy">Competing Interests Policy</@s.a></li>
	</ul>
	<li><@s.a href="${guidelinesURL}" title="PLoS NTDs | Guidelines for Authors">Author Guidelines</@s.a></li>
	<ul>
		<li><@s.a href="${latexURL}" title="PLoS NTDs | LaTeX Guidelines">LaTeX Guidelines</@s.a></li>
	</ul>
	<li><@s.a href="${figureGuidelinesURL}" title="PLoS NTDs | Guidelines for Figure and Table Preparation">Guidelines for Figure and Table Preparation</@s.a></li>
	<li><@s.a href="${cklistURL}" title="PLoS NTDs | Manuscript Submission Checklist">Submit Your Manuscript</@s.a></li>
	<ul>
		<li><a href="http://ntds.plosjms.org/" title="PLoS NTDs | Online Manuscript Submission and Review System">Submit Manuscript</a></li>
		<li><@s.a href="${revisedChecklistURL}" title="PLoS NTDs | Checklist for Revised Manuscripts">Revised Manuscript Checklist</@s.a></li>
	</ul>
	<li><@s.a href="${reviewerGuidelinesURL}" title="PLoS NTDs | Reviewer Guidelines ">Reviewer Guidelines</@s.a></li>
</ul>
				
<h2>General Links</h2>
<ul>
    <@s.url action="privacy" namespace="/static" includeParams="none" id="privacyURL"/>
    <@s.url action="terms" namespace="/static" includeParams="none" id="termsURL"/>
	
    <li><@s.a href="${privacyURL}" title="PLoS NTDs | Privacy Statement">Privacy Statement</@s.a></li>
    <li><@s.a href="${termsURL}" title="PLoS NTDs | Terms of Use">Terms of Use</@s.a></li>
    <li><a href="http://www.plos.org/advertise/" title="PLoS.org | Advertise">Advertise</a></li>
    <li><a href="http://www.plos.org/journals/embargopolicy.html" title="PLoS.org | Media Inquiries">Media Inquiries</a></li>
    <li><a href="http://www.plos.org/journals/print.html" title="PLoS.org | PLoS in Print">PLoS in Print</a></li>
</ul>

<h2>PLoS Journals</h2>
<ul>
    <li><@s.a href="http://www.plosjournals.org/" title="PLoSJournals.org">All PLoS Journals</@s.a></li>
    <li><@s.a href="http://biology.plosjournals.org/" title="PLoSBiology.org"><em>PLoS Biology</em></@s.a></li>
    <li><@s.a href="http://medicine.plosjournals.org/" title="PLoSMedicine.org"><em>PLoS Medicine</em></@s.a></li>
    <li><@s.a href="http://compbiol.plosjournals.org/" title="PLoSCompBiol.org"><em>PLoS Computational Biology</em></@s.a></li>
    <li><@s.a href="http://genetics.plosjournals.org/" title="PLoSGenetics.org"><em>PLoS Genetics</em></@s.a></li>
    <li><@s.a href="http://pathogens.plosjournals.org/" title="PLoSPathogens.org"><em>PLoS Pathogens</em></@s.a></li>
    <li><@s.a href="http://www.plosone.org/" title="PLoSONE.org"><em>PLoS ONE</em></@s.a></li>
    <li><@s.a href="http://www.plosntds.org/" title="PLoSNTDs.org"><em>PLoS Neglected Tropical Diseases</em></@s.a></li>
</ul>

<h2>PLoS Hubs</h2>
<ul>
    <li><@s.a href="http://clinicaltrials.ploshubs.org" title="PLoS Hub for Clinical Trials">Clinical Trials</@s.a></li>
</ul>

<h2>PLoS.org</h2>
<ul>
    <li><@s.a href="http://www.plos.org/" title="PLoS.org">PLoS.org</@s.a></li>
    <li><@s.a href="http://www.plos.org/oa/index.html" title="PLoS.org | Open Access">Open Access</@s.a></li>
    <li><@s.a href="http://www.plos.org/support/donate.php" title="PLoS.org | Join PLoS">Join PLoS</@s.a></li>
    <li><@s.a href="http://www.plos.org/cms/blog" title="PLoS.org | PLoS Blog">PLoS Blog</@s.a></li>
    <li><@s.a href="http://www.plos.org/connect.html" title="PLoS.org | Stay Connected">Stay Connected</@s.a></li>
</ul>

</div>
<!-- end : main contents -->