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
          <!-- SWT removed horizontalTabs -->
            <p style="position:relative; zoom:1; font-size:0.9em; margin: 25px 0 0px; padding:3px 10px; border:1px dashed red; background-color:#ffc;"><strong>Note to our readers:</strong> We apologize for the recent slowness and intermittent downtime for this journal website. Please bear with us as we work to improve the website performance and stability. <a href="http://www.plos.org/cms/node/334">More information</a></p>
            <div id="info" class="block">
             <@s.url action="information" namespace="/static" includeParams="none" id="info"/>
              <h2>What is the PLoS Hub for Clinical Trials?</h2>
              <p>The PLoS Hub for Clinical Trials collects PLoS journal articles that relate to clinical trials. The Hub is a site for researchers to share their views and build a dynamic, interactive community.</p>
              <p>Currently, the PLoS Hub for Clinical Trials features articles originally published in <em>PLoS Clinical Trials</em>, along with clinical trials articles from <em>PLoS ONE</em>.</p>
              <p>In the future, this new resource will expand to include articles from all the PLoS titles that publish clinical trials. It will also feature open-access articles from other journals plus user-generated content.</p>
              <p><a href="${info}">Find out more</a> and <a href="${feedbackURL}">tell us what you think</a>.</p>
            </div>
            <!-- end block new -->
            <!-- begin : calls to action blocks -->
            <div id="alerts">
              <a href="${freemarker_config.registrationURL}"><span><strong>Sign up</strong>
              Sign up for clinical trials content alerts by e-mail</span></a>
            </div>
            <div id="rss">
              <@s.url action="rssInfo" namespace="/static" includeParams="none" id="rssinfo"/>
              <a href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}${rssPath}"><span><strong>Subscribe</strong>
              Subscribe to the clinical trials RSS content feed</span></a>
              <a href="${rssinfo}" class="adInfo">What is RSS?</a>
            </div>
            <!-- end : calls to action blocks -->
            <div class="block recent">
              <h2>Recently Added Clinical Trials</h2>
              <ul class="articles">    
              <@s.url id="article1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001779"/>	
              <@s.url id="article2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001665"/>
              <@s.url id="article3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001610"/>
              <@s.url id="article4" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001630"/>
                <li><@s.a href="%{article1}" title="Read Open Access Article">
				Open-Label Comparative Clinical Study of Chlorproguanil−Dapsone Fixed Dose Combination (Lapdap<sup>TM</sup>) Alone or with Three Different Doses of Artesunate for Uncomplicated <em>Plasmodium falciparum</em> Malaria
				</@s.a></li>
                <li><@s.a href="%{article2}" title="Read Open Access Article">
				Broad Clade 2 Cross-Reactive Immunity Induced by an Adjuvanted Clade 1 rH5N1 Pandemic Influenza Vaccine
				</@s.a></li>
 				<li><@s.a href="%{article3}" title="Read Open Access Article">
				Poor Reporting of Scientific Leadership Information in Clinical Trial Registers
				</@s.a></li>
               <li><@s.a href="%{article4}" title="Read Open Access Article">
				Safety and Efficacy of Methylene Blue Combined with Artesunate or Amodiaquine for Uncomplicated Falciparum Malaria: A Randomized Controlled Trial from Burkina Faso
				</@s.a></li>
				      </ul>
            </div><!-- end : block recent -->
            <!-- begin : browse widget block -->
            <div id="browseWidget" class="block">
              <p>Browse Clinical Trials Articles: <a href="${browseSubjectURL}">By Subject</a> or <a href="${browseDateURL}">By Publication Date</a></p>
            </div>
            <!-- end : browse block -->
            <!-- begin : other block -->
            <div class="other block">
              <h2>Highlights From Other Open Access Journals</h2>
              <div class="section">
                <h3><a href="http://www.plosmedicine.org/"><em>PLoS Medicine</em></a></h3>
                <ul class="articles">
                  <li><a href="#" title="Read Open Access Article">Observational Research, Randomised Trials, and Two Views of Medical Science</a></li>
                </ul>
              </div>
              <div class="section">
                <h3><a href="#"><em>BMJ</em></a></h3>
                <ul class="articles">
                  <li><a href="#" title="Read Open Access Article">A legal framework for drug safety</a></li>
                  <li><a href="#" title="Read Open Access Article">Empirical evidence of bias in treatment effect estimates in controlled trials with different interventions and outcomes: meta-epidemiological study</a></li>
                </ul>
              </div>
              <div class="section lastSection">
                <h3><a href="#"><em>Open Medicine</em></a></h3>
                <ul class="articles">
                  <li><a href="#" title="Read Open Access Article">Toward a definition of pharmaceutical innovation</a></li>
                </ul>
              </div>
            </div><!-- end : other block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div class="block">
              <@s.url action="commentGuidelines" anchor="note" namespace="/static" includeParams="none" id="note"/>
              <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="rating"/>
 	              <h3>Join the Community</h3>
                <p><a href="${freemarker_config.registrationURL}" title="Register">Register now</a> and share your views on clinical trials research. Only registrants can add <a href="${note}" title="Guidelines for Notes, Comments, and Corrections">Notes, Comments</a>, and <a href="${rating}" title="Guidelines for Rating">Ratings</a> to articles in the Hub.</p>
            </div>
            <div class="block">
              <@s.url action="checklist" namespace="/static" includeParams="none" id="checklist"/>
              <h3>Submit Your Work</h3>
              <p>PLoS is committed to publishing the results of all clinical trials, regardless of outcome, and making this essential information freely and publicly available. <a href="${checklist}" title="Submission Info">Find out how to submit your work</a>.</p>
            </div><!-- end : block -->
            <!-- begin : advocacy blocks -->
            <div id="adWrap">
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:184&#38;source=PHUBCT&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                 document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
             //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=abd0d95d' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:184&#38;source=PHUBCT&#38;n=abd0d95d' border='0' alt=''></a></noscript>
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
           <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:185&#38;source=PHUBCT&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a56536b2' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:185&#38;source=PHUBCT&#38;n=a56536b2' border='0' alt=''></a></noscript>
           <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:186&#38;source=PHUBCT&#38;block=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a6f9fd36' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:186&#38;source=PHUBCT&#38;n=a6f9fd36' border='0' alt=''></a></noscript>
            </div>
            <!-- end : advocacy blocks -->
            <div class="block">
              <h3>From the Web</h3>
              <p>Encouraging Transparency in Research Reporting: Register now for <a href="#">EQUATOR Network Launch Meeting</a>.</p>
              <p>UK Medicines and Healthcare Products Regulatory Agency <a href="#">concludes investigation</a> into GlaxoSmithKline.</p>
            </div><!-- end : info block -->
          </div><!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner">
            <script language='JavaScript' type='text/javascript' src='http://ads.plos.org/adx.js'></script>
             <script language='JavaScript' type='text/javascript'>
              <!--
                if (!document.phpAds_used) document.phpAds_used = ',';
                phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);
                document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                document.write ("&#38;what=zone:181&#38;source=PHUBCT&#38;block=1&#38;blockcampaign=1");
                document.write ("&#38;exclude=" + document.phpAds_used);
                if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
                document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a595dcde' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:181&#38;source=PHUBCT&#38;n=a595dcde' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div>
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
        <a href="http://www.fedora-commons.org/" title="Fedora-Commons.org"><img src="images/pone_home_fedoracommons.png" alt="Fedora-Commons.org"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="images/pone_home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
        <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="images/pone_home_moore.gif" alt="Moore Foundation"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
