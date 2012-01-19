<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

<head>
<title>PLoS Journals : A Peer-Reviewed, Open-Access Journal</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<meta name="keywords" content="clinical trials, clinical trial, clinical trials journal, trials journal, medical publishing, plos, public library of science, open access, open-access, science, medical research, clinical research, trial results, human studies, experimental treatment, disease, drug, drugs, device, devices, vaccine, vaccines, phase I, phase II, phase III, phase IV, clinical trials database, clinical trials databank"/>
<meta name="description" content="PLoS ONE: an inclusive, peer-reviewed, open access resource from the PUBLIC LIBRARY OF SCIENCE. Reports of well-performed scientific studies from all disciplines freely available to the whole World. " />

<link rel="alternate" type="application/rss+xml" title="PLoS Journals: New Articles" href="/?request=get-rss&issn=&type=new-articles" />
<link rel="alternate" type="application/rss+xml" title="PLoS Journals: Table of Contents" href="/?request=get-rss&issn=&type=toc-articles" />
<link rel="alternate" type="application/rss+xml" title="PLoS Journals: Top Articles" href="/?request=get-rss&issn=&type=top-articles" />

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
<Work rdf:about="http://www.plosone.org">
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
	<li class="none"><a href="http://www.plosjournals.org" title="PLoS Journals Home">Home</a></li>
	<li class="none"><a href="http://www.plosone.org/profile" title="My E-mail Alert Settings">My Profile</a></li>
	<li class="none"><a href="http://www.plosjournals.org/perlserv/?request=advanced-search">Search the Journals</a></li>
	<li class="none"><a href="http://www.plos.org" title="PLoS.org">PLoS.org</a></li>
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
          <li><a href="/plos-registration/forgotPassword.action" title="Click here if you forgot your password" tabindex="11">Forgotten Password?</a></li>
          <li><a href="/plos-registration/register.action" tabindex="12"><strong>Register for a New Account</strong></a></li>
          <li><a href="http://www.plosone.org/static/help.action#account">Help</a></li>
        <% if (request.getAttribute("edu.yale.its.tp.cas.badUsernameOrPassword") != null) { %>
	  <li><a href="/plos-registration/resendRegistration.action" title="Click here if you need to confirm your e-mail address" tabindex="11">Resend e-mail address confirmation</a></li>
	<% } %>
	</ul>
      </div>
</div>
<!-- end : container -->	

<!-- begin : footer -->
<div id="ftr">
<ul>
<li><a href="http://www.plos.org/privacy.html" title="PLoS Privacy Statement" tabindex="51">Privacy Statement</a></li>
<li><a href="http://www.plos.org/terms.html" title="PLoS Terms of Use" tabindex="52">Terms of Use</a></li>
<li><a href="http://www.plos.org/advertise/" title="Advertise with PLoS" tabindex="53">Advertise</a></li>
<li><a href="http://www.plos.org" title="PLoS.org" tabindex="54">PLoS.org</a></li>
</ul>
<ul class="journals">
<li><a href="http://www.plosbiology.org" title="PLoS Biology" tabindex="101">PLoS Biology</a></li>
<li><a href="http://www.plosmedicine.org" title="PLoS Medicine" tabindex="102">PLoS Medicine</a></li>
<li><a href="http://www.ploscompbiol.org" title="PLoS Medicine" tabindex="103">PLoS Computational Biology</a></li>
<li><a href="http://www.plosgenetics.org" title="PLoS Genetics" tabindex="104">PLoS Genetics</a></li>
<li><a href="http://www.plospathogens.org" title="PLoS Pathogens" tabindex="105">PLoS Pathogens</a></li>
<li><a href="http://www.plosclinicaltrials.org" title="PLoS Clinical Trials" tabindex="106">PLoS Clinical Trials</a></li>
<li><a href="http://www.plosone.org" title="PLoS ONE" tabindex="107">PLoS ONE</a></li>
<li><a href="http://www.plosntds.org" title="PLoS Neglected Tropical Diseases" tabindex="108"style="border: none;">PLoS Neglected Tropical Diseases</a></li>
</ul>

</div>
<!-- end : footer -->
</body>
</html>
