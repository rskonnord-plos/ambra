<!-- begin : main content -->
<div id="content" class="static">
  <h1>Site Map</h1>

  <h2>Home</h2>
  <ul>
    <@s.url action="home" namespace="/" includeParams="none" id="homeURL"/>
    <@s.url action="rssFeeds" namespace="/static" includeParams="none" id="rssFeedURL"/>
    <@s.url action="releaseNotes" namespace="/static" includeParams="none" id="releaseURL"/>
    <@s.url namespace="/user/secure" includeParams="none" id="loginURL" action="secureRedirect" goTo="${thisPage}"/>
    <li><@s.a href="${homeURL}" title="PLoS Hub | Home page">Home page</@s.a></li>
    <li><@s.a href="${rssFeedURL}" title="PLoS Hub | RSS Feeds">PLoS RSS Feeds</@s.a></li>
    <#if Session?exists && Session[freemarker_config.userAttributeKey]?exists>
      <li><a id="loginLogoutLink" href="${logoutURL}" title="Logout">Logout</a></li>
    <#else>
      <li><a id="loginLogoutLink" href="${loginURL}" title="PLoS ONE | Account Login">Login</a></li>
    </#if>
    <li><@s.a href="${freemarker_config.registrationURL}" title="PLoS Hub | Create a New Account">Create Account</@s.a></li>
    <li><@s.a href="${feedbackURL}" title="PLoS Hub | Send Us Your Feedback">Send Us Feedback</@s.a></li>
    <li><@s.a href="${releaseURL}" title="PLoS Hub | Release Notes">Release Notes</@s.a></li>
  </ul>

  <h2>Browse Articles</h2>
  <ul>
    <@s.url action="browse" field="date" namespace="/article" includeParams="none" id="browseDateURL"/>
    <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubURL"/>
    <li><@s.a href="${browseDateURL}" title="PLoS Hub | Browse by Publication Date">By Publication Date</@s.a></li>
    <li><@s.a href="${browseSubURL}" title="PLoS Hub | Browse by Subject">By Subject</@s.a></li>
  </ul>

  <h2>About</h2>
  <ul>
    <@s.url action="information" namespace="/static" includeParams="none" id="infoURL"/>
    <@s.url action="license" namespace="/static" includeParams="none" id="licenseURL"/>
    <@s.url action="faq" namespace="/static" includeParams="none" id="faqURL"/>
    <@s.url action="contact" namespace="/static" includeParams="none" id="contactURL"/>
    <li><@s.a href="${infoURL}" title="PLoS Hub | Information">Information</@s.a></li>
    <li><@s.a href="${licenseURL}" title="PLoS Hub | License">Open-Access License</@s.a></li>
    <li><@s.a href="${faqURL}" title="PLoS Hub | FAQ">FAQ</@s.a></li>
    <li><@s.a href="${contactURL}" title="PLoS Hub | Contact">Contact Us</@s.a></li>
  </ul>

  <h2>For Readers</h2>
  <ul>
    <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="commentURL"/>
    <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="ratingURL"/>
    <@s.url action="help" namespace="/static" includeParams="none" id="helpURL"/>
    <li><@s.a href="${commentURL}" title="PLoS Hub | Guidelines for Notes, Comments, and Corrections">Guidelines for Notes, Comments, and Corrections</@s.a></li>
    <li><@s.a href="${ratingURL}" title="PLoS Hub | Guidelines for Rating">Guidelines for Rating</@s.a></li>
    <li><@s.a href="${helpURL}" title="PLoS Hub | Help Using this Site">Help Using This Site</@s.a></li>
    <li>Site Map</li>
  </ul>

  <h2>For Authors</h2>
  <ul>
    <@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
    <li><@s.a href="${cklistURL}" title="PLoS Hub | Submit Your Paper">Submit Your Paper</@s.a>
  </ul>

  <h2>General Links</h2>
  <ul>
    <@s.url action="privacy" namespace="/static" includeParams="none" id="privacyURL"/>
    <@s.url action="terms" namespace="/static" includeParams="none" id="termsURL"/>
    <li><@s.a href="${privacyURL}" title="PLoS Hub | Privacy Statement">Privacy Statement</@s.a></li>
    <li><@s.a href="${termsURL}" title="PLoS Hub | Terms of Use">Terms of Use</@s.a></li>
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
    <li><@s.a href="${homeURL}" title="PLoS Hub - Clinical Trials">Clinical Trials</@s.a></li>
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