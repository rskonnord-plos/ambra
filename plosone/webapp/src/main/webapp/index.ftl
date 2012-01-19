<!-- begin : main content -->
<div id="content">
	<!-- begin : home page wrapper -->
	<div id="wrap">
		<div id="home">
			<!-- begin : layout wrapper -->
			<div class="col">
				<!-- begin : wrapper for cols 1 & 2 -->
				<div id="first" class="col">
					<!-- begin : col 1 -->
					<div class="col first">
					<div class="block mainnav">
					<ul>
						<li><a href="http://www.plos.org/oa/index.html" title="Learn more about Open Access on PLoS.org">Open Access</a></li>
						<li><a href="http://www.plos.org/support/donate.php" title="Join PLoS and our Open Access mission">Join PLoS</a></li>
						<li><a href="/static/checklist.action" title="Find out how to submit to PLoS ONE"><@ww.url action="checklist.action" namespace="/static/" includeParams="none" id="checklist"/>
Submit Today</a></li>
					</ul>
					</div>
						<div class="block partner">
				<h6>Partners</h6>
				<a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
				<a href="http://fedora.info/" title="Fedora.info"><img src="${freemarker_config.context}/images/pone_home_fedora.jpg" alt="Fedora.info"/></a>
				<a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
				<a href="http://www.osafoundation.org" title="Open Source Applications Foundation"><img src="${freemarker_config.context}/images/pone_home_osaf.gif" alt="OSAF"/></a>
				<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.gif" alt="Moore Foundation"/></a>
						</div>					
						<div class="block banner">
							<script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
							<script language='JavaScript' type='text/javascript'>
							<!--
							   if (!document.phpAds_used) 
							   		document.phpAds_used = ',';
							   phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
							   document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
							   document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
							   document.write ("&amp;what=plos_print_01");
							   document.write ("&amp;exclude=" + document.phpAds_used);
							   if (document.referrer)
							      document.write ("&amp;referer=" + escape(document.referrer));
							   document.write ("'><" + "/script>");
							//-->
							</script>
							<noscript><a href='http://ads.plos.org/adclick.php?n=a73373f2' target='_blank'><img src='http://ads.plos.org/adview.php?what=plos_print_01&amp;n=a73373f2' border='0' alt=''></a></noscript>
						</div> 
												   
					</div>			
					
					<div class="col last">		
					<div class="horizontalTabs" style="padding-top: 0; ">
						<ul id="tabsContainer">
						</ul>
						
						<div id="tabPaneSet" class="contentwrap">
						  <#include "article/recentArticles.ftl">
						</div>
					</div>
	
	<div class="block feature">
<h2>New and Noted</h2>
<div>
	<@ww.url namespace="/article" includeParams="none" id="articleURL1" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000093"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL2" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000073"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL3" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000075"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL4" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000045"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL5" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000051"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL6" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000039"/>
	<@ww.url namespace="/article" includeParams="none" id="articleURL7" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000079"/>

<a href="${articleURL1}" title="Read Open-Access Article">Wnt and Hedgehog Are Critical Mediators of Cigarette Smoke-Induced Lung Cancer</a><p>The molecular basis of cigarette-induced lung cancer is poorly understood. This paper shows that genes more normally associated with the patterning of embryos may be involved. This may open new avenues for the development of therapies.</p>

<a href="${articleURL2}" title="Read Open-Access Article">The Syntax and Meaning of Wild Gibbon Songs</a><p>Human languages are subtle. Identical words can mean different things when spoken in a different order. This paper shows that the same is true for gibbons. When threatened they change the structure of their calls, warning individuals within earshot of the danger.</p>

<a href="${articleURL7}" title="Read Open-Access Article">Mesenchymal Stem Cell-Mediated Functional Tooth Regeneration in Swine</a><p>Much discussion surrounds the use of stem cells for regenerating organs as a possible treatment for a diverse spectrum of diseases. This paper explores the potential of human stem cells isolated from dental papillae to regenerate functional teeth.</p>

<h3>Also of Note</h3>
<ul class="articles">
	<li><a href="${articleURL3}" title="Read Open-Access Article">Control of Canalization and Evolvability by Hsp90</a></li>
	<li><a href="${articleURL4}" title="Read Open-Access Article">Predator Mimicry: Metalmark Moths Mimic Their Jumping Spider Predators</a></li>
	<li><a href="${articleURL5}" title="Read Open-Access Article">Physiological Mouse Brain A&#223; levels Are Not Related to the Phosphorylation State of Threonine-668 of Alzheimer's APP</a></li>
	<li><a href="${articleURL6}" title="Read Open-Access Article">A Virtual Reprise of the Stanley Milgram Obedience Experiments</a></li>
</ul>
</div>
</div>




					</div>

					<!-- end : col 2 -->
				</div>
				<!-- end : wrapper for cols 1 & 2 -->
				<!-- begin : wrapper for cols 3 & 4 -->
				<div id="second" class="col">
				
				
					<!-- begin : col 3 -->
					<div class="subcol first">
					
											<#if categoryNames?size gt 0>
					<div class="info block">
<@ww.url action="feedbackCreate.action" namespace="/static" includeParams="none" id="feedback"/>
<@ww.url action="commentGuidelines.action#annotation" namespace="/static" includeParams="none" id="annotation"/>
<@ww.url action="commentGuidelines.action#discussion" namespace="/static" includeParams="none" id="discussion"/>

<h2>What is PLoS ONE?</h2>
<div>
<p>A new way of communicating peer-reviewed science and medicine.</p>
<ul><li><strong><a href="${annotation}" title="Learn how to add an annotation">Annotations</a></strong> – add and share your comments</li>
	<li><strong><a href="${discussion}" title="Learn how to start a discussion">Discussions</a></strong> – join the conversation</li>
	<li><em>More functionality coming soon</em></li>
</ul>
<p><a href="${feedback}" title="Send us your feedback">Your feedback</a> will help us shape PLoS ONE.</p>
</div>
</div>
					
						<div class="subject block">
						<h2>Explore by Subject</h2>
							<dl class="category">

							<#list categoryNames as category>
							  <#assign categoryId = category?replace("\\s|\'","","r")>
							  <#if categoryId?length lt 8>
								<#assign index = categoryId?length>
							  <#else>
  								<#assign index = 8>
							  </#if>
								<dt><a class="expand" id="widget${categoryId}" onclick="return singleExpand(this, '${categoryId}');">${category} (${articlesByCategory[category_index]?size})</a></dt>
								<dd id="${categoryId}">
									<ul class="articles">
										<#list articlesByCategory[category_index] as article>
										<li><a href="article/fetchArticle.action?articleURI=${article.uri?url}" title="Read Open Access Article">${article.title}</a></li>
										</#list>
									</ul>
								</dd>
							</#list>
							</dl>

						</div>
						</#if> 

					</div>
					<!-- end : col 3 -->
					<!-- begin : col 4 -->
					<div class="subcol last">
						<div class="block banner">
							<script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
							<script language='JavaScript' type='text/javascript'>
							<!--
						   if (!document.phpAds_used) 
						   		document.phpAds_used = ',';
						   phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
						   document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
						   document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
						   document.write ("&amp;what=sky1_leadphysician");
						   document.write ("&amp;exclude=" + document.phpAds_used);
						   if (document.referrer)
						      document.write ("&amp;referer=" + escape(document.referrer));
							  document.write ("'><" + "/script>");
								//-->

								</script>
								<noscript><a href='http://ads.plos.org/adclick.php?n=ad35bef4' target='_blank'><img src='http://ads.plos.org/adview.php?what=sky1_leadphysician&amp;n=ad35bef4' border='0' alt=''></a></noscript>
						</div>

					</div>
					<!-- end : col 4 -->
					
				</div>
							
				<!-- end : wrapper for cols 3 & 4 -->
				
				
				<div id="lower">
				
			<div class="col first">
			<div class="block ad">
				<a href="http://www.plos.org/downloads/jbanners.html"><img src="${freemarker_config.context}/images/home/t_hom_mar_10.png" />
				<strong>Love PLoS?</strong>
				<span class="body">Help spread the word, download our banners</span></a>
			</div>
			<div class="block ad">
				<a href="http://www.plos.org/cms/node/40"><img src="${freemarker_config.context}/images/home/t_hom_mar_05.PNG" />

				<strong>Reuse</strong>
				<span class="body">Feel free to be creative with our content</span></a>
			</div>
			
			<div class="block ad">
				<a href="http://www.plos.org/advertise"><img src="${freemarker_config.context}/images/home/t_hom_mar_03.PNG" />
				<strong>Advertise with PLoS</strong>
				<span class="body">New high-profile realty available</span></a>

			</div>	
	
				</div>
					
				</div>
				
			</div>
			<!-- end : layout wrapper -->
		</div>
	</div>
	<!-- end : home page wrapper -->
</div>
<!-- end : main contents -->
