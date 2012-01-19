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
		  <p style="position:relative; zoom:1; font-size:0.9em; margin-bottom:10px; padding:3px 10px; border:1px dashed red; background-color:#ffc;"><strong>Note to our readers:</strong> We apologize for the recent slowness and intermittent downtime for this journal website. Please bear with us as we work to improve the website performance and stability. <a href="http://www.plos.org/cms/node/334">More information</a></p>
            <div id="importantStuff" class="block">
             <@s.url id="newNoted01" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001780"/>
              <@s.url id="newNoted02" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001743"/>
              <@s.url id="newNoted03" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001709"/>
              <h2>New and Noted</h2>
               <div class="article section">
                <h3>Recently Published</h3>
                <ul class="articles">
                  <li><@s.a href="${newNoted01}" title="Read Open-Access Article">Small-Bodied Humans from Palau, Micronesia</@s.a></li>
                  <li><@s.a href="${newNoted02}" title="Read Open-Access Article">Hibernation in an Antarctic Fish: On Ice for Winter</@s.a></li>
                  <li><@s.a href="${newNoted03}" title="Read Open-Access Article">SnoRNA Snord116 (Pwcr1/MBII-85) Deletion Causes Growth Deficiency and Hyperphagia in Mice</@s.a></li>
                  <li><a href="${browseDateURL}">Browse all recently published articles</a></li>
                </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section">
              <@s.url id="newNoted04" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001584"/>
              <@s.url id="newNoted05" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001506"/>
               <@s.url id="newNoted06" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001678"/>
               <h3>Community Discussions</h3>
                <ul class="articles">
                  <li><@s.a href="${newNoted04}" title="Read Open-Access Article">Microbial Ecology of Four Coral Atolls in the Northern Line Islands</@s.a></li>
                  <li><@s.a href="${newNoted05}" title="Read Open-Access Article">Prelude to Passion: Limbic Activation by "Unseen" Drug and Sexual Cues</@s.a></li>
                  <li><@s.a href="${newNoted06}" title="Read Open-Access Article">One-Pot, Mix-and-Read Peptide-MHC Tetramers</@s.a></li>
               </ul>
                <div class="clearer">&nbsp;</div>
              </div>
              <div class="article section lastSection">
              <@s.url id="newNoted07" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001664"/>

                <h3>In the News</h3>
                <h4><@s.a href="${newNoted07}" title="Read Open-Access Article">A Specific and Rapid Neural Signature for Parental Instinct</@s.a></h4>
                <p>One of a number of <em>PLoS ONE</em> papers published last week that were covered by the media was an article by Kringelbach and colleagues, in which the authors found that a certain region of the brain was activated on the sight of infants' faces, providing evidence for a neural basis of parental instinct. The article was featured in some of the following stories:</p>
		
		<ul class="articles">
			<li>Daily Telegraph - <@s.a href="http://www.telegraph.co.uk/earth/main.jhtml?view=DETAILS&grid=&xml=/earth/2008/02/27/scibaby127.xml">Babies Faces 'Make Us Want to Care for Them'</@s.a></li>
			<li>Discovery Channel - <@s.a href="http://dsc.discovery.com/news/2008/02/27/babies-human-brain.html">Adult Brains Wired to Go Ga-Ga Over Babies</@s.a></li>
			<li>Reuters - <@s.a href="http://www.reuters.com/article/scienceNews/idUSL2657590420080227">Study Sheds Light on Parental Instinct</@s.a></li>
			<li>Cognitive Daily - <@s.a href="http://scienceblogs.com/cognitivedaily/2008/02/we_respond_differently_to_babi.php">We Respond Differently to Babies' Faces within 150 Milliseconds</@s.a></li>
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
                <a href="${comment}">Add Notes, Comments, and Ratings</a>
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
                      <a href="${freemarker_config.context}/article/feed?category=${category?replace(' ','+')}"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
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
                      <a href="${freemarker_config.context}/article/feed?category=${category?replace(' ','+')}"><img src="${freemarker_config.context}/images/feed-icon-inline.gif" /></a>
                    </li>
		  </#if>
                </#list>
              </ul>
              <div class="clearer">&nbsp;</div>
            </div><!-- end : explore by subject block -->
            </#if>
            
            <div class="other block">
              <h2>Other PLoS Content</h2>
			  
             <div class="section">
                 <h3><a href="http://medicine.plosjournals.org/"><em>PLoS Medicine</em></a></h3>
                <ul class="articles">
                  <li><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.pmed.0050052" title="Read Open-Access Article">Alcohol Intake and Blood Pressure: A Systematic Review Implementing a Mendelian Randomization Approach</a></li>
                </ul>
             </div>

              <div class="section">
                <h3><a href="http://biology.plosjournals.org/"><em>PLoS Biology</em></a></h3>
                <ul class="articles">
                  <li><a href="http://biology.plosjournals.org/perlserv/?request=get-document&doi=10.1371%2Fjournal.pbio.0060045" title="Read Open-Access Article">Assessing Evidence for a Pervasive Alteration in Tropical Tree Communities</a></li>
                </ul>
              </div>

              <div class="section">
                <h3><a href="http://www.plosgenetics.org/"><em>PLoS Genetics</em></a></h3>
                <ul class="articles">
                  <li><a href="http://www.plosgenetics.org/article/info%3Adoi%2F10.1371/journal.pgen.1000010" title="Read Open-Access Article">Identification of the <em>Yellow Skin</em> Gene Reveals a Hybrid Origin of the Domestic Chicken</a></li>
                </ul>
              </div>

            <div class="section lastSection">
                <h3><a href="http://www.plosntds.org/"><em>PLoS Neglected Tropical Diseases</em></a></h3>
                <ul class="articles">
                  <li><a href="http://www.plosntds.org/article/info%3Adoi%2F10.1371%2Fjournal.pntd.0000097" title="Read Open-Access Article">Endomyocardial Fibrosis: Still a Mystery after 60 Years</a></li>
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
            <p>An interactive open-access journal for the communication of all peer-reviewed scientific and medical research. <@s.a href="${info}">More</@s.a></p>
          </div>
          <!-- end : about block -->
          <!-- begin : functionality block -->
         <div class="block">
            <h3>New Functionality</h3>
            <p><a href="http://www.plosone.org/static/commentGuidelines.action#corrections">Corrections</a> - Approved minor and formal corrections will be highlighted.</p>
            <!--<p><a href="http://www.plosone.org/search/advancedSearch.action">Advanced Search</a> - Search by author, subject and date to name but a few.</p>-->
          </div>
          <!-- end : functionality block -->

          <!-- begin : journal club block -->
          <div class="block">
		    <@s.url id="JCcomments" namespace="/annotation" action="getCommentary" target="info:doi/10.1371/journal.pone.0001456"/>
		    
		    <@s.url id="Reviewercomments" namespace="/annotation" action="listThread" inReplyTo="info:doi/10.1371/annotation/3a5ae229-5d24-4352-a4ea-068ce780a7a4" root="info:doi/10.1371/annotation/4519ddf3-4c75-452f-b8df-ebf2c227455d"/>    
			<@s.url id="JCarticle" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001456"/>
			<@s.url id="JCarchive" namespace="/static" action="journalClub" />
            <h3>New Journal Club</h3>
            <p>Read these <@s.a href="${JCcomments}">Journal Club comments</@s.a> and <@s.a href="${Reviewercomments}">Reviewer comments</@s.a> on the <em>PLoS ONE</em> article <@s.a href="${JCarticle}">The Sorcerer II Global Ocean Sampling Expedition: Metagenomic Characterization of Viruses within Aquatic Microbial Samples</@s.a>.</p>
            <p>We wish to thank <@s.a href="http://biology.uoregon.edu/people/bohannan/bohannan.php">Brendan Bohannan</@s.a>, <@s.a href="http://biology.uoregon.edu/people/castenholz/castenholz.php">Richard W. Castenholz</@s.a>, and 
<@s.a href="http://biology.uoregon.edu/people/green/">Jessica Green</@s.a> from the <@s.a href="http://ceeb.uoregon.edu/">Center for Ecology and Evolutionary Biology at University of Oregon</@s.a> for running this journal club.</p>

            <p>This new article has been added to <em>PLoS Biology's</em> <@s.a href="http://collections.plos.org/plosbiology/gos-2007.php">Oceanic Metagenomics Collection</@s.a>, originally published in March last year.</p>
            
            <p>Also, it is never too late to add your thoughts to the <@s.a href="${JCarchive}">previous Journal Clubs</@s.a>.</p>
			<p>Want to get involved? <@s.a href="${feedbackURL}">Nominate your lab</@s.a> and get your team and your work some free publicity.</p>
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
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=a1ec113d' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:160&amp;source=ONE&amp;n=a1ec113d' border='0' alt=''></a></noscript>
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
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=ace5c997' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:161&amp;source=ONE&amp;n=ace5c997' border='0' alt=''></a></noscript>
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
          </script><noscript><a href='http://ads.plos.org/adclick.php?n=aec547bc' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:162&amp;source=ONE&amp;n=aec547bc' border='0' alt=''></a></noscript>
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
                  <#include "/article/plosBlog.ftl">
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a93f3323' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:36&amp;source=ONE&amp;n=a93f3323' border='0' alt=''></a></noscript>
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