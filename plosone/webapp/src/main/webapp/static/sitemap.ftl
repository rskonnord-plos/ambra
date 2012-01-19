
<!-- begin : main content -->
<div id="content" class="static">
<h1><em>PLoS ONE</em> Site Map</h1>

<h2>Home</h2>
<ul>
	<li><a href="/" title="PLoS ONE | Home page">Home page</a></li>
	<li><a href="http://www.plos.org/oa/index.html" title="PLoS.org | Open Access">Open Access</a></li>
	<li><a href="rssFeeds.action" title="PLoS ONE | RSS Feeds">PLoS RSS Feeds</a></li>
	
	<@s.url namespace="/" includeParams="none" id="feedbackURL" action="feedbackCreate" page="${thisPageURL?url}"/>
	<@s.url namespace="/user/secure" includeParams="none" id="loginURL" action="secureRedirect" goTo="${thisPage}"/>
	<li><a href="${feedbackURL}" title="PLoS ONE | Send Us Your Feedback">Send Us Feedback</a></li>
	<li><a href="${freemarker_config.registrationURL}" title="PLoS ONE | Create a New Account">Create Account</a></li>
	<li><a href="${loginURL}" title="PLoS ONE | Account Login">Login</a></li>	
</ul>

<h2>About</h2>

  <ul>
    <li><a href="information.action" title="PLoS ONE | Journal Information">Journal Information</a></li>
    <li><a href="edboard.action" title="PLoS ONE | Advisory and Editorial Boards">Advisory and Editorial Boards</a></li>
	<li><a href="license.action" title="PLoS ONE | License">License</a></li>
  </ul>

<h2>For Users</h2>
<ul>
	<!-- <li><a href="faq.action" title="PLoS ONE | Frequently Asked Questions">Frequently Asked Questions</a></li> -->
    <li><a href="commentGuidelines.action" title="PLoS ONE | Commenting Guidelines">Commenting Guidelines</a></li>
    <li><a href="help.action" title="PLoS ONE | Help Using this Site">Help Using This Site</a></li>
    <li><a href="sitemap.action" title="PLoS ONE | Site Map">Site Map</a></li>
    <li><a href="contact.action" title="PLoS ONE | Contact">Contact Us</a></li>

</ul>

<h2>For Authors and Reviewers</h2>
  <ul>
    <li><a href="whypublish.action" title="PLoS ONE | Why Publish?">Why Publish in <em>PLoS ONE</em>?</a></li>
    <li><a href="policies.action" title="PLoS ONE | Editorial and Publishing Policies">Editorial and Publishing Policies</a>
    <ul>
		<li><a href="competing.action" title="PLoS ONE | Competing Interests Policy">Competing Interests Policy</a></li>

	</ul>
	</li>
    <li><a href="guidelines.action" title="PLoS ONE | Author Guidelines">Author Guidelines</a></li>
    <li><a href="figureGuidelines.action" title="PLoS ONE | Figure Guidelines">Figure Guidelines</a></li>
    <li><a href="checklist.action" title="PLoS ONE | Submission Checklist">Submit Your Paper - Submission Checklist</a>
    <ul>
		<li><a href="http://one.plosjms.org/" title="PLoS ONE | Online Manuscript Submission and Review System">Submit Manuscript</a></li>

	</ul>
	</li>
    <li><a href="reviewerGuidelines.action" title="PLoS ONE | Reviewer Guidelines">Reviewer Guidelines</a></li>
  </ul>

<h2>General Links</h2>
<ul>
    <li><a href="privacy.action" title="PLoS ONE | Privacy Statement">Privacy Statement</a></li>
	<li><a href="terms.action" title="PLoS ONE | Terms of Use">Terms of Use</a></li>
	<li><a href="http://www.plos.org/advertise/" title="PLoS.org | Advertise">Advertise</a></li>
    <li><a href="http://www.plos.org/journals/embargopolicy.html" title="PLoS.org | Media Inquiries">Media Inquiries</a></li>
	<li><a href="http://www.plos.org/journals/print.html" title="PLoS.org | PLoS in Print">PLoS in Print</a></li>
</ul>

<h2>PLoS Journals</h2>
 <ul>
  <li><a href="http://www.plosjournals.org/" title="PLoSJournals.org">All PLoS Journals</a></li>
  <li><a href="http://biology.plosjournals.org/" title="PLoSBiology.org"><em>PLoS Biology</em></a></li>
  <li><a href="http://medicine.plosjournals.org/" title="PLoSMedicine.org"><em>PLoS Medicine</em></a></li>
  <li><a href="http://compbiol.plosjournals.org/" title="PLoSCompBiol.org"><em>PLoS Computational Biology</em></a></li>

  <li><a href="http://genetics.plosjournals.org/" title="PLoSGenetics.org"><em>PLoS Genetics</em></a></li>
  <li><a href="http://pathogens.plosjournals.org/" title="PLoSPathogens.org"><em>PLoS Pathogens</em></a></li>
  <li><a href="http://clinicaltrials.plosjournals.org/" title="PLoSClinicalTrials.org"><em>PLoS Clinical Trials</em></a></li>
  <li><a href="http://www.plosone.org/" title="PLoSONE.org"><em>PLoS ONE</em></a></li>
  <li><a href="http://www.plosntds.org/" title="PLoSNTDs.org"><em>PLoS Neglected Tropical Diseases</em></a></li>
 </ul>

<h2>PLoS.org</h2>
<ul>
  <li><a href="http://www.plos.org/" title="PLoS.org">PLoS.org</a></li>
</ul>


</div>
<!-- end : main contents -->
