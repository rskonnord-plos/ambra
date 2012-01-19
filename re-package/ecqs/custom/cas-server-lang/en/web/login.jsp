<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

<head>
<title>PLoS Journals : A Peer-Reviewed, Open-Access Journal</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="shortcut icon" href="images/pjou_favicon.ico" type="image/x-icon" />

<link rel="home" title="home" href="http://www.plosjournals.org"></link>

<!-- global_css.ftl -->

<style type="text/css" media="all"> @import "css/pjou_screen.css";</style>
<!--style type="text/css" media="all"> @import "css/pjou_iepc.css";</style-->
<style type="text/css" media="all"> @import "css/pjou_forms.css";</style>

<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="http://register.plos.org">
   <license rdf:resource="http://creativecommons.org/licenses/by/2.5/" />
</Work>
<License rdf:about="http://creativecommons.org/licenses/by/2.5/">
   <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
   <permits rdf:resource="http://web.resource.org/cc/Distribution" />
   <requires rdf:resource="http://web.resource.org/cc/Notice" />
   <requires rdf:resource="http://web.resource.org/cc/Attribution" />
   <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
</License>
</rdf:RDF>
-->
</head>
<body>
<!-- begin : container -->
<div id="container">
	
<!-- begin : header -->
<div id="hdr">
	<div id="logo"><a href="http://www.plosjournals.org" title="PLoS Journals"><span>PLoS Journals</span></a></div>
	<div id="tagline"><span>A peer-reviewed, open-access journal published by the Public Library of Science</span></div>
</div>
<!-- end : header -->

<!-- begin : navigation -->
<ul id="nav">
	<li class="none"><a href="http://www.plosjournals.org" title="PLoS Journals Home Page" tabindex="101">Home</a></li>
	<li class="none"><a href="http://www.plosone.org/profile" title="My Profile" tabindex="102">My Profile</a></li>
	<li class="journalnav"><a href="http://www.plos.org" title="Public Library of Science" class="drop" tabindex="110">PLoS.org</a>
	<ul>
			<li><a href="http://www.plos.org/oa/index.html" title="Open Access Statement">Open Access</a></li>
			<li><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show your Support">Join PLoS</a></li>
			<li><a href="http://www.plos.org/cms/blog" title="PLoS Blogs">PLoS Blog</a></li>
		</ul>
	</li>
	<li class="journalnav"><a href="http://www.plosjournals.org" title="PLoS Journals" tabindex="109" class="drop">PLoS Journals</a>
	<ul>
			<li><a href="http://medicine.plosjournals.org" title="PLoSMedicine.org">PLoS Medicine</a></li>
			<li><a href="http://compbiol.plosjournals.org" title="PLoSCompBiol.org">PLoS Computational Biology</a></li>
			<li><a href="http://genetics.plosjournals.org" title="PLoSGenetics.org">PLoS Genetics</a></li>
			<li><a href="http://pathogens.plosjournals.org" title="PLoSPathogens.org">PLoS Pathogens</a></li>
			<li><a href="http://clinicaltrials.plosjournals.org" title="PLoSClinicalTrials.org">PLoS Clinical Trials</a></li>
			<li><a href="http://www.plosone.org/" title="PLoSONE.org">PLoS ONE</a></li>
			<li><a href="http://www.plosntds.org/" title="PLoSNTDs.org">PLoS Neglected Tropical Diseases</a></li>
		</ul>
	</li>
</ul>	
<!-- end : navigation -->

<div id="content">
   <h1>Login to Your PLoS Journals Account</h1>
   <p>Fields marked with <span class="required">*</span> are required.</p>
   <form method="post" name="login_form" class="pone-form">
        <fieldset>
          <legend>Login</legend>
          <% boolean loginError = (request.getAttribute("edu.yale.its.tp.cas.badUsernameOrPassword") != null);%>
            <input type="hidden" name="lt" value="<%= request.getAttribute("edu.yale.its.tp.cas.lt") %>" />
            <ol>
                <% if (loginError) { %>
                <li class="form-error"><p class="required">Please correct the error below.</p></li>
                <% } else if (request.getAttribute("edu.yale.its.tp.cas.service") == null) { %>
                <li><em>You may login now in order to access protected services later.</em></li>
                <% } else if (request.getAttribute("edu.yale.its.tp.cas.badLoginTicket") != null) { %>
                <li><em>Bad Login Ticket: Please check to make sure you are coming from a PLoS site.</em></li>
                <% } else { %>                        
                <!-- <em>You have requested access to a site that requires authentication.</em> -->               
                <% } %>

              <li<%=(loginError?" class=form-error":"")%>>
                <label for="username">E-mail</label>
                <input type="text" name="username" tabindex="1"/>
		<%=loginError?" Please enter a valid e-mail address and password":""%>
              </li>
              <li>
                <label for="password">Password</label>
                <input type="password" name="password" tabindex="2"/>
              </li>
            </ol>
              <div class="btnwrap">
                 <input type="submit" name="login" value="Login" tabindex="3"/>
              </div>
          </form>
        </fieldset>

        <ul>
          <li><a href="http://journals.plos.org/help.php#account">Help</a></li>
          <li><a href="/plos-registration/register.action" tabindex="12">Register for a New Account</a></li>
          <li><a href="/plos-registration/forgotPassword.action" title="Click here if you forgot your password" tabindex="11">Forgotten Password?</a></li>
	  <li><a href="/plos-registration/resendRegistration.action" title="Click here if you need to confirm your e-mail address" tabindex="11">Resend e-mail address confirmation</a></li>
	</ul>
      </div>
</div>
<!-- end : container -->	

<!-- begin : footer -->
<div id="ftr">
<ul>
<li><a href="http://journals.plos.org/privacy.php" title="PLoS Privacy Statement" tabindex="501">Privacy Statement</a></li>
<li><a href="http://journals.plos.org/terms.php" title="PLoS Terms of Use" tabindex="502">Terms of Use</a></li>
<li><a href="http://www.plos.org/advertise/" title="Advertise with PLoS" tabindex="503">Advertise</a></li>
<li><a href="http://journals.plos.org/help.php" title="Help Using this Site" tabindex="504">Help</a></li>
<li><a href="http://www.plos.org" title="PLoS.org" tabindex="504">PLoS.org</a></li>
</ul>

</div>
<!-- end : footer -->
<script src="https://ssl.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-338393-1";
_udn = "plos.org";
urchinTracker();
</script>
</body>
</html>
