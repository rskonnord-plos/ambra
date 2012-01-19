<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- begin : main content -->
<div id="content" class="static">
  <h1>Release Notes</h1>
  <ul>
  	<li><a href="#features">Features Updates</a></li>
  	<li><a href="#credits">Site Credits</a></li>
  </ul>
  
  <a name="features"></a>
  <h2>Features Updates</h2>
  <p>
    This site is published using a new Open Source publishing framework being developed by 
    <a href="http://www.topazproject.org/" title="Topaz | Open Source software 
    applications development project">Topaz</a>. This site is updated regularly as new features 
    become available. This page summarizes features and known bugs in each updated version.
  </p>
  
    <h2>RC 0.9 rc1 beta</h2>
<p>Released July 16, 2008</p>
<h3>Features implemented in RC 0.9 rc1</h3>
<ul>
<li>Pre-ingest script (process_sip) for images transforms.</li>
<li>Re-architected the cache code.</li>
<li>Moved transactions to read-only queries where possible.</li>
<li>Optimized slideshow retrieval/display.</li>
<li>Optimized many actions (e.g. FetchArticleAction, GetAverageRatingsAction, etc.).</li>
<li>Lots of Ambra code improvements.</li>
<li>Upgraded to Dojo 1.1.</li>
<li>Upgraded to Mulgara 1.2 (multi-core!).</li>
<li>Upgraded to Lucene 2.3.</li>
<li>Upgraded to ehCache 1.4.1.</li>
<li>Many Ambra configuration changes for ease of installation.</li>
</ul>

<h3>Features fixed in RC 0.9 rc1</h3>
<ul>
<li>Safari bugs fixed; ratings, drop-down menus.</li>
<li>Ingest fixes; ingest doesn't change .mov to .qt; ingest handles .swf files.</li>
<li>Advanced search by past month &amp; past three months working.</li>
<li>Search results with correct journal context.</li>
<li>XSL fixes.</li> 
</ul>
  
	<h2>RC 0.8.2.1 beta</h2>
	<p>Released February 26, 2008</p>
	<h3>Features Implemented in RC 0.8.2.1</h3>
	<ul>
		<li>Minor corrections on articles</li>
		<li>Formal corrections on articles</li>
		<li>"View All" corrections page</li>
		<li>Admin interface for converting notes to corrections</li>
		<li>Citations for formal corrections</li>
		<li>Advanced search:</li>
		<ul>
			<li>Search by author name</li>
			<li>Search article text </li>
			<li>Search by date published</li>
			<li>Search by subject category</li>
		</ul>
		<li>Support for note DOIs in the DOI resolver</li>
		<li>Dynamic "Most Viewed" tab for journal home pages</li>
	</ul>

	<h3>Features fixed in RC 0.8.2.1</h3>

	<ul>
		<li>Nomenclature of annotations/discussions changed to notes/comments</li>
		<li>Dynamic Table of Contents with article caching</li>
		<li>Dynamic Journal Archive with article caching</li>
		<li>Re-index of past published articles for advanced search</li>
		<li>Slideshow accommodates small thumbnail issue images</li>
		<li>Overall rating correctly calculates front matter articles</li>
		<li>Order articles in the Table of Contents and RSS feeds ordered by date published and then by article DOI</li>
		<li>Correct ordering of issues in Table of Contents</li>
		<li>Ingest correctly identifies article XML file when multiple XML files are found in the article zip file</li>
		<li>Correctly display front matter ratings when a rating is deleted</li>
		<li>Table of Contents displays "Author Summary" links for research articles</li>
	</ul>


  <h2>RC 0.8.2 beta</h2>
  <p>Released January 11, 2008</p>
  <h3>Features implemented in RC 0.8.2</h3>
  <ul>
	<li>Fixes to Browse by Issue (Table of Contents) to add more article types and next/previous links</li>
	<li>A dynamic Browse by Journal (Journal Archive) page</li>
	<li>A single rating field for all front matter articles (e.g. Editorial, Opinion)</li>
	<li>Affiliation footnote links are displayed for group authors</li>
	<li>Minor FreeMarker and CSS fixes</li>
	<li>Updates to the volume and issue object models</li>
	<li>Migration from OSCache to ehcache for faster performance</li>
	<li>Administrative interface for viewing and releasing caches</li>
  </ul>
  
  <h2>RC 0.8.1 beta</h2>
  <p>Released October 25, 2007</p>
  <h3>Features implemented in RC 0.8.1</h3>
  <ul>
	<li>Display links to related articles on Article and Browse pages</li>
	<li>Administrative interface for collecting Articles into Issue and Issues into Volumes</li>
	<li>Administrative interface for attaching Issue Image article to Issues, and setting the current Issue</li>
  	<li>Browse by Issue (Table of Contents) page</li>
  </ul>

  <h2>RC 0.8 beta</h2>
  <p>Released September 20, 2007</p>
  <h3>Features implemented in RC 0.8:</h3>
  <ul>
  	<li>Enable multiple journals using a single repository</li>
	<li>Skins for multiple journals</li>
	<li>Filter search results by journal using OTM</li>
	<li>Trackback links for articles</li>
	<li>Citation download for the published article</li>
	<li>A fix to a rating display issue in IE7</li>
	<li>Allow multiple e-mail addresses for "E-mail this Article"</li>
	<li>Administrative modifications to annotations</li>
	<li>Administrative interface for multiple journals</li>
	<li>Migration to the Struts 2 web application framework</li>
  </ul>

  <h2>RC 0.7 beta</h2>
  <p>Released July 9, 2007</p>
  <h3>Features implemented in RC 0.7:</h3>
  <ul>
    <li>Ratings feature</li>
    <li>Architecture overhaul</li>
    <li>Removal of a layer of web services and increased performance</li>
    <li>Object Triple Mapping (OTM) which speeds retrieval of objects</li>
    <li>Object Query Language (OQL) that is similar to HQL, an object-oriented query language from Hibernate</li>
    <li>Atom syndication format</li>
    <li>A REST based interface which will allow developers/users to directly fetch articles and annotations through READ/GET operations</li>
  </ul>

  <h2>RC 0.6 beta</h2>
  <p>Released April 20, 2007</p>
  <h3>Features implemented in RC 0.6:</h3>
  <ul>
    <li>Browse features</li>
    <li>Full Safari support</li>
    <li>Cross section and cross paragraph annotations now work correctly</li>
    <li>Image cache is properly flushed when an article is deleted via the admin console</li>
    <li>Fix issue where jsession id was appended to search button and javascript file requests</li>
    <li>Resend address verification e-mail feature</li>
    <li>Removed alerts e-mail address</li>
    <li>After submitting profile for a new user, you get directed to alerts tab</li>
    <li>Users can change their e-mail address now</li>
    <li>New templates for the profile pages are available for customization</li>
    <li>New organization types on profile page</li>
    <li>Article title and journal title is in title of page now</li>
    <li>Fixed RDF header in Ambra Journal pages</li>
  </ul>
  
  <a name="credits"></a>
  <h2>Site Credits</h2>
	<p>Many of the icons are adapted from the <a href="http://www.famfamfam.com/lab/icons/silk/">Silk Icon</a> set, courtesy FamFamFam.</p>
  
</div>
<!-- end : main contents -->
