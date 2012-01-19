<!-- begin : main content -->
<div id="content">
  <!-- begin : home page wrapper -->
  <div id="wrap">
    <div id="home">
      <!-- begin : layout wrapper -->
      <div class="col">
        <!-- begin : wrapper for cols 1 & 2 -->
        <div id="first" class="col">
        <!-- SWT removed col 1 -->
        <!-- begin : col 2 -->
          <div class="col last">
            <div id="importantStuff" class="block">
              <h2>New and Noted</h2>
              <@s.url id="newNoted01" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001341"/>
              <@s.url id="newNoted02" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001318"/>
              <@s.url id="newNoted03" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001324"/>
              <@s.url id="newNoted04" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001083"/>
              <@s.url id="newNoted05" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001268"/>
              <@s.url id="newNoted06" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001247"/>
              <@s.url id="newNoted07" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001326"/>
              <div class="article section">
                <h3>Recently Published</h3>
                <ul class="articles">
                  <li><@s.a href="${newNoted01}" title="Read Open-Access Article">Light Variability Illuminates Niche-Partitioning among Marine Picocyanobacteria</@s.a></li>
                  <li><@s.a href="${newNoted02}" title="Read Open-Access Article">Full-Exon Resequencing Reveals Toll-Like Receptor Variants Contribute to Human Susceptibility to Tuberculosis Disease</@s.a></li>
                  <li><@s.a href="${newNoted03}" title="Read Open-Access Article">Canine Population Structure: Assessment and Impact of Intra-Breed Stratification on SNP-Based Association Studies</@s.a></li>
                  <li><a href="${browseDateURL}">Browse all recently published articles</a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
                <h3>Community Discussions</h3>
                <ul class="articles">
                  <li><@s.a href="${newNoted04}" title="Read Open-Access Article">Analysis of the Trajectory of <em>Drosophila melanogaster</em> in a Circular Open Field Arena</@s.a></li>
                  <li><@s.a href="${newNoted05}" title="Read Open-Access Article">Repeated Exposure to Media Violence Is Associated with Diminished Response in an Inhibitory Frontolimbic Network</@s.a></li>
                  <li><@s.a href="${newNoted06}" title="Read Open-Access Article">Brain Responses to Violet, Blue, and Green Monochromatic Light Exposures in Humans: Prominent Role of Blue Light and the Brainstem</@s.a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
                <h3>In the News</h3>
                <h4><a href="/article/info%3Adoi%2F10.1371%2Fjournal.pone.0001431;jsessionid=663B0A0E2C0C2A81D9FF08EB2B6FC33F" title="Read Open-Access Article">Climate Influence on Deep Sea Populations</a></h4>
                <p>The progressive depletion of world fisheries is an important socio-economic issue but a study by <a href="#">Joan B. Company</a> and colleagues describes how shrimp populations in the Mediterranean remain constant, thanks to deep-water currents. The coverage of the paper included:</p>
                <ul class="articles refs">
                  <li>Science: <a href="http://sciencenow.sciencemag.org/cgi/content/full/2008/116/2">Out of Disaster, Shrimp Are Reborn </a></li>
                  <li>Journal Watch: <a href="http://journalwatch.conservationmagazine.org/2008/01/16/heavy-water-saves-shrimp/">Heavy Water Saves Shrimp</a></li>
                  <li>Science Daily: <a href="http://www.sciencedaily.com/releases/2008/01/080116080316.htm">Climate Influences Deep Sea Populations</a></li>
                  <li>Deep-Sea News: <a href="http://scienceblogs.com/deepseanews/2008/01/deep_shrimp_fishery_swept_away.php">Deep Shrimp Fishery Swept Away by Currents</a></li>
                </ul>

                <div class="clearer">&nbsp;</div>
              </div>
            </div>
            <!-- end : block -->
            <!-- begin : calls to action blocks -->
            <div class="ctaWrap">
              <div id="cta1">
                <strong>Publish with PLoS</strong>
                <a href="${checklist}">We want to publish your work</a>
              </div>
              <div id="cta2">
                <strong>Have Your Say</strong>
                <a href="${comment}">Add ratings and discussions</a>
              </div>
              <div class="clearer">&nbsp;</div>
            </div>
            <!-- end : calls to action blocks -->
            
            <#if categoryInfos?size gt 0>
	    
            <#assign colSize = (categoryInfos?size / 2) + 0.5>
	    
            <!-- begin : explore by subject block -->
            <div class="explore block">
              <h2>Explore by Subject</h2>
              <p>(#) indicates the number of articles published in each subject category.</p>
              <ul>
                <#list categoryInfos?keys as category>
		  <#if (category_index + 1) lte colSize>
		  <#assign categoryId = category?replace("\\s|\'","","r")>
                    <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                    <li>
                      <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                      <a href="${freemarker_config.context}/article/feed?category=${category}"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                    </li>
		  </#if>
                </#list>
              </ul>
              <ul>
                <#list categoryInfos?keys as category>
		  <#if (category_index + 1) gt colSize>
                    <#assign categoryId = category?replace("\\s|\'","","r")>
                    <@s.url id="browseURL" action="browse" namespace="/article" catName="${category}" includeParams="none"/>
                    <li>
                      <a id="widget${categoryId}" href="${browseURL}">${category} (${categoryInfos[category]})</a>&nbsp;
                      <a href="${freemarker_config.context}/rss/${category?replace(' ','')?replace("'",'')}.xml"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                    </li>
		  </#if>
                </#list>
              </ul>
              <div class="clearer">&nbsp;</div>
            </div><!-- end : explore by subject block -->
            </#if>
            
            <div class="other block">
              <h2>Other PLoS Content</h2>
              <@s.url id="other01" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pgen.0030226"/>
              <@s.url id="other02" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pntd.0000087"/>
              <@s.url id="other03" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pcbi.0030230"/>
              <@s.url id="other04" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pbio.0050324"/>
              <div class="section">
                <h3><a href="http://genetics.plosjournals.org"><em>PLoS Genetics</em></a></h3>
                <ul class="articles">
                  <li><@s.a href="${other01}" title="Read Open-Access Article">Chromosome Structuring Limits Genome Plasticity in <em>Escherichia coli</em></@s.a></li>
                </ul>
              </div>
              <div class="section">
                <h3><a href="http://www.plosntds.org"><em>PLoS Neglected Tropical Diseases</em></a></h3>
                <ul class="articles">
                  <li><@s.a href="${other02}" title="Read Open-Access Article">Risk Factors for Tungiasis in Nigeria: Identification of Targets for Effective Intervention</@s.a></li>
                </ul>
               </div>
               <div class="section">
                <h3><a href="http://compbiol.plosjournals.org"><em>PLoS Computational Biology</em></a></h3>
                <ul class="articles">
                  <li><@s.a href="${other03}" title="Read Open-Access Article">Using Likelihood-Free Inference to Compare Evolutionary Dynamics of the Protein Networks of <em>H. pylori</em> and <em>P. falciparum</em></@s.a></li>
                </ul>
               </div>
               <div class="section lastSection">
                 <h3><a href="http://biology.plosjournals.org"><em>PLoS Biology</em></a></h3>
                <ul class="articles">
                  <li><@s.a href="${other04}" title="Read Open-Access Article">Mapping Meiotic Single-Strand DNA Reveals a New Landscape of DNA Double-Strand Breaks in <em>Saccharomyces cerevisiae</em></@s.a></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
          <!-- begin : about block -->
          <div class="block">
            <h3>What is <em>PLoS ONE</em>?</h3>
            <p>An interactive open-access journal for the communication of all peer-reviewed scientific and medical research. <a href="${info}">More</a></p>
          </div>
          <!-- end : about block -->
          <!-- begin : block -->
          <div class="block">
            <h3><em>PLoS ONE</em> is 1 Year Old</h3>
            <p>We're marking <a href="#">the occasion</a> with this home page makeover that will help you find relevant content more quickly and encourage dialogue on articles.</li></p>
          </div>
          <!-- end : block -->
          <!-- begin : block -->
          <div class="block">
            <h3>Author Survey Results</h3>
            <p>Thanks to the 2,000+ authors who responded to our survey&#8212;97.2% say they will publish with <em>PLoS ONE</em> again.</p>
            <p>Congratulations to our iPod Shuffle winners: <a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0000374" title="Read Open Access Article">Marion Coolen</a>, <a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0000993" title="Read Open Access Article">Thomas J. Baiga</a>, and <a href="http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0000277" title="Read Open Access Article">B&#233;n&#233;dicte Lafay</a>.</p>
          </div>
          <!-- end : block -->
          <!-- begin : journal club block -->
          <div class="block">
            <h3>Journal Club</h3>
            <p>New one coming soon, in the meantime, <a href="${journalClub}">visit our archive</a>.</p>
            <p>Want to get involved? <a href="${feedbackURL}">Nominate your lab</a> and get your team and your work some free publicity.</p>
          </div>
          <!-- end : journal club block -->
          <!-- begin : advocacy blocks -->
          <div id="adWrap">
          <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
          <script language='JavaScript' type='text/javascript'>
          <!--
             if (!document.phpAds_used) document.phpAds_used = ',';
             phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
             document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
             document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
             document.write ("&amp;what=zone:160&amp;source=ONE&amp;target=_top&amp;block=1");
             document.write ("&amp;exclude=" + document.phpAds_used);
             if (document.referrer)
                document.write ("&amp;referer=" + escape(document.referrer));
             document.write ("'><" + "/script>");
          //-->
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=a1ec113d' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:195&amp;source=NTD&amp;n=a1ec113d' border='0' alt=''></a></noscript>
          <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
          <script language='JavaScript' type='text/javascript'>
          <!--
             if (!document.phpAds_used) document.phpAds_used = ',';
             phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
             document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
             document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
             document.write ("&amp;what=zone:161&amp;source=ONE&amp;target=_top&amp;block=1");
             document.write ("&amp;exclude=" + document.phpAds_used);
             if (document.referrer)
                document.write ("&amp;referer=" + escape(document.referrer));
             document.write ("'><" + "/script>");
          //-->
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=ace5c997' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:196&amp;source=NTD&amp;n=ace5c997' border='0' alt=''></a></noscript>
          <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
          <script language='JavaScript' type='text/javascript'>
          <!--
             if (!document.phpAds_used) document.phpAds_used = ',';
             phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
             document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
             document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
             document.write ("&amp;what=zone:162&amp;source=ONE&amp;target=_top&amp;block=1");
             document.write ("&amp;exclude=" + document.phpAds_used);
             if (document.referrer)
                document.write ("&amp;referer=" + escape(document.referrer));
             document.write ("'><" + "/script>");
          //-->
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=aec547bc' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:197&amp;source=NTD&amp;n=aec547bc' border='0' alt=''></a></noscript>
          </div>
          <!-- end : advocacy blocks -->
            <!-- begin : stay-connected block -->
            <div id="connect" class="block">
              <h3>Stay Connected</h3>
              <ul>
                  <li><img src="images/icon_alerts_small.gif" alt="email alerts icon" /><a href="http://www.plosone.org/user/secure/editPrefsAlerts.action?tabId=alerts"><strong>E-mail Alerts</strong></a><br />Sign up for alerts by e-mail</li>
                  <li><img src="images/icon_rss_small.gif" alt="rss icon" /><@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/><a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><strong>RSS</strong></a> (<a href="${rssinfo}">What is RSS?</a>)<br />Subscribe to content feed</li>
                  <li><img src="images/icon_join.gif" alt="join PLoS icon" /><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show Your Support"><strong>Join PLoS</strong></a><br />Support the open-access movement!</li>
              </ul>
            </div>
            <!-- end : stay-connected block -->
            <!-- begin : blog block -->
            <div id="blog" class="block">
              <h3>From the PLoS Blog</h3>
              <p>Read the <a href="http://www.plos.org/cms/blog" title="PLoS Blog">PLoS Blog</a> <a href="http://feeds.feedburner.com/plos/Blog"><img alt="RSS" src="images/feed-icon-inline.gif" /></a> and contribute your views on scientific research and open-access publishing.</p>
              <ul class="articles">
                <li><a href="http://www.plos.org/cms/node/297">Children's medicines matter</a></li>
                <li><a href="http://www.plos.org/cms/node/296">Accept no imitations, unless you're learning to speak</a></li>
                <li><a href="http://www.plos.org/cms/node/295">Zotero Translator for PLoS Articles</a></li>
              </ul>
            </div>
            <!-- end : blog block -->
          </div><!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner"><!--skyscraper-->
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
              if (!document.phpAds_used) document.phpAds_used = ',';
              phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
              document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
              document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
              document.write ("&amp;what=zone:36&amp;source=ONE&amp;target=_top&amp;block=1&amp;blockcampaign=1");
              document.write ("&amp;exclude=" + document.phpAds_used);
              if (document.referrer)
                document.write ("&amp;referer=" + escape(document.referrer));
              document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a93f3323' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:192&amp;source=NTD&amp;n=a93f3323' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div> <!-- displays lower background image -->
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.fedora-commons.org" title="Fedora-Commons.org"><img src="${freemarker_config.context}/images/pone_home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
				<a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.gif" alt="Moore Foundation"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/pone_home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
				<a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
