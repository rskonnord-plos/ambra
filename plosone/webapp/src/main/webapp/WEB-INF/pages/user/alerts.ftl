<#function isFound collection value>
  <#list collection as element>
    <#if element = value>
      <#return "true">
    </#if>
  </#list>
  <#return "false">
</#function>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>PLoS ONE : A Peer-Reviewed, Open-Access Journal</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<meta name="keywords" content=""/>

<meta name="description" content="" />

<link rel="shortcut icon" href="images/pone_favicon.ico" type="image/x-icon" />
<link rel="home" title="home" href="http://www.plosone.org"></link>

<link rel="alternate" type="application/rss+xml" title="PLoS Biology: New Articles" href="http://biology.plosjournals.org/perlserv/?request=get-rss&#38;issn=1545-7885&#38;type=new-articles" />
<link rel="alternate" type="application/rss+xml" title="PLoS Biology: Table of Contents" href="http://biology.plosjournals.org/perlserv/?request=get-rss&#38;issn=1545-7885&#38;type=toc-articles" />
<link rel="alternate" type="application/rss+xml" title="PLoS Biology: Top Articles" href="http://biology.plosjournals.org/perlserv/?request=get-rss&#38;issn=1545-7885&#38;type=top-articles" />
			
<style type="text/css" media="all"> @import "css/pone_screen.css";</style>
<style type="text/css" media="all"> @import "css/pone_iepc.css";</style>
<style type="text/css" media="all"> 
@import "css/pone_forms.css";
</style>
<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="/plosbiology/ra.php">
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
<!-- begin : top banner external ad space -->
<div id="topBanner">
	<!-- begin : left banner slot -->
	<div class="left">
	</div>
	<!-- end : left banner slot -->
	<!-- begin : right banner slot -->
	<div class="right">
		<img src="images/temp_pone_banner.gif" />
	</div>
	<!-- end : right banner slot -->
</div>
<!-- end : top banner external ad space -->
<!-- begin : header -->
<div id="hdr">
	<div id="logo" title="PLoS ONE: Open-Access 2.0"><a href="http://www.plosone.org" title="PLoS ONE: Open-Access 2.0"><span>PLoS ONE</span></a></div>
	<!-- begin : user controls -->
	<div id="user">
	<div>
	<p>Logged in as <a href="#" class="icon user">margare18charlimit</a></p>
	<ul>
		<li><a href="#" class="icon nomessage">0 New</a></li>
		<li><a href="#" class="icon preferences">Preferences</a></li>
		<li><a href="#" class="icon logout">Logout</a></li>
	</ul>
	</div>
	</div>
	<!-- end : user controls -->
		<ul id="links">
			<li><a href="#" title="Search PLoS ONE with advanced criteria" class="icon advanced">Advanced Search</a></li>
			<li><a href="#" title="PLoS ONE RSS Feeds" class="icon rss">RSS</a></li>
		</ul>
	<!-- begin : dashboard -->
	<div id="db">
		<form>
		<fieldset>
		<legend>Search PLoS ONE</legend>
			<label for="search">Search</label>
			<div class="wrap"><input type="text" value="Search PLoS ONE..." onfocus="if(this.value=='Search PLoS ONE...')value='';" onblur="if(this.value=='')value='Search PLoS ONE...';" class="searchField" alt="Search PLoS ONE..." /></div>
			<input src="images/pone_searchinput_btn.gif" value="ftsearch" alt="SEARCH" tabindex="3" class="button" type="image" />
		</fieldset>
		</form>
	</div>
	<!-- end : dashboard -->
	<!-- begin : navigation -->
	<ul id="nav">
		<li><a href="#">Home</a></li>
		<li><a href="#" class="drop">Articles<!--[if IE 7]><!--></a><!--<![endif]-->
			<table><tr><td>
				<ul>
					<li><a href="#" title="Browse: Current Issue">Current Issue</a></li>

					<li><a href="#" title="Browse: Past Issues">Past Issues</a></li>
					<li><a href="#" title="Browse: By Publication Date">By Publication Date</a></li>
					<li><a href="#" title="Browse: By Subject">By Subject</a></li>
				</ul>
			</li>
			</td></tr></table><!--[if lte IE 6]></a><![endif]-->
		<li><a href="#" class="drop">Authors</a><!--[if IE 7]><!--></a><!--<![endif]-->
			<table><tr><td>
				<ul>
					<li><a href="#" title="For Authors: Why Publish With Us?">Why Publish With Us?</a></li>

					<li><a href="#" title="For Authors: Editorial Policies">Editorial and Publishing Policies</a></li>
					<li><a href="#" title="For Authors: Author Guidelines">Author Guidelines</a></li>
					<li><a href="#" title="For Authors: Figure Guidelines">Figure Guidelines</a></li>
					<li><a href="#" title="For Authors: Submit Your Paper">Submit Your Paper</a></li>
				</ul>
			</li>
			</td></tr></table><!--[if lte IE 6]></a><![endif]-->
		<li><a href="#">Users</a></li>
		<li><a href="#" class="drop">About<!--[if IE 7]><!--></a><!--<![endif]-->
			<table><tr><td>
				<ul>
					<li><a href="#" title="About: Journal Information">Journal Information</a></li>
					<li><a href="#" title="About: Editorial Board">Editorial Board</a></li>
					<li><a href="#" title="About: Editor-in-Chief">Editor-in-Chief</a></li>
					<li><a href="#" title="About: Media Inquiries">Media Inquiries</a></li>
					<li><a href="#" title="About: PLoS in Print">PLoS in Print</a></li>
				</ul>
			</li>
			</td></tr></table><!--[if lte IE 6]></a><![endif]-->
			<li class="journalnav"><a href="http://www.plos.org" title="Public Library of Science" tabindex="10">PLoS.org</a></li>

			<li class="journalnav"><a href="http://www.plosjournals.org" title="PLoS Journals" tabindex="9" class="drop">PLoS Journals<!--[if IE 7]><!--></a><!--<![endif]-->
			<table><tr><td>
				<ul>
					<li><a href="http://biology.plosjournals.org" title="PLoSBiology.org">PLoS Biology</a></li>
					<li><a href="http://medicine.plosjournals.org" title="PLoSMedicine.org">PLoS Medicine</a></li>
					<li><a href="http://clinicaltrials.plosjournals.org" title="PLoSClinicalTrials.org">PLoS Clinical Trials</a></li>

					<li><a href="http://compbiol.plosjournals.org" title="PLoSCompBiol.org">PLoS Computational Biology</a></li>
					<li><a href="http://genetics.plosjournals.org" title="PLoSGenetics.org">PLoS Genetics</a></li>
					<li><a href="http://pathogens.plosjournals.org" title="PLoSPathogens.org">PLoS Pathogens</a></li>
					<li><a href="http://www.plosntd.org/" title="PLoSNTD.org">PLoS Neglected Tropical Diseases</a></li>
					<li><a href="http://www.plosone.org/" title="PLoSONE.org">PLoS ONE</a></li>
				</ul>
			</li>

			</td></tr></table><!--[if lte IE 6]></a><![endif]-->
		</ul>
	<!-- end : navigation -->
</div>
<!-- end : header -->
<!-- begin : main content wrapper -->
<div id="content">
<h2>Alerts</h2>
	<p><strong>instruction Title   Text.</strong> General Instructions- Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.</p>
	<p>Field marked with an <span class="required">*</span> are required. </p>

  <@s.form action="saveUserAlerts" namespace="/user/secure" method="post" cssClass="pone-form" method="post" title="Alert Form">

  <fieldset>
		<legend>Choose your alerts  </legend>
		<ol>
			<li>Check back soon for more PLoS One alerts</li>
      <@s.textfield name="alertEmailAddress" label="Email address for alerts" required="true"/>
      <#list categoryBeans as category>
        <li>
          <ol>
            <li class="alerts-title">${category.name}</li>
            <li>
              <#if category.weeklyAvailable>
                <label for="${category.key}">
              <@s.checkbox name="weeklyAlerts" fieldValue="${category.key}" value="${isFound(weeklyAlerts, category.key)}"/>
                Weekly </label>
              </#if>
            </li>

            <li>
              <#if category.monthlyAvailable>
                <label for="${category.key}">
              <@s.checkbox name="monthlyAlerts" fieldValue="${category.key}" value="${isFound(monthlyAlerts, category.key)}"/>
                  Monthly </label>
              <#else>
              </#if>
            </li>
          </ol>
        </li>
      </#list>
		</ol>
		<br clear="all" />
			<input type="submit" name="cancel" id="cancel" value="Cancel" tabindex="199">
      <@s.submit name="Save" tabindex="200"/>
	</fieldset>
  </@s.form>

</div>
<!-- end : main content wrapper -->
</div>
<!-- end : container -->
<div id="ftr">
	<p><span>All journal content, except where otherwise noted, is licensed under a <a href="http://creativecommons.org/licenses/by/2.5/" title="Creative Commons Attribution License 2.5">Creative Commons Attribution License</a>.</span></p>
	<ul>
		<li><a href="http://redesign-dev.plos.org/plosbiology/privacy.php" title="PLoS ONE Statement">Privacy Statement</a></li>
		<li><a href="http://redesign-dev.plos.org/plosbiology/terms.php" title="PLoS ONE Terms of Use">Terms of Use</a></li>
		<li><a href="http://www.plos.org/advertise/" title="Advertise With PLoS">Advertise</a></li>
		<li><a href="http://redesign-dev.plos.org/plosbiology/sitemap.php" title="PLoS Biology Site Map">Site Map</a></li>
		<li><a href="http://www.plos.org" title="PLoS.org">PLoS.org</a></li>
	</ul>
</div>
</body>
</html>