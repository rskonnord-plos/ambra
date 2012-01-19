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
            <div id="new" class="block">
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
                <@s.url id="article1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001018"/>
                <@s.url id="article2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0001023"/>
                <@s.url id="article3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000991"/>
                <@s.url id="article4" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000919"/>

               <li><@s.a href="%{article1}" title="Read Open Access Article">
				A Randomized Placebo-Controlled Phase Ia Malaria Vaccine Trial of Two Virosome-Formulated Synthetic Peptides in Healthy Adult Volunteers
				</@s.a></li>
                <li><@s.a href="%{article2}" title="Read Open Access Article">
				Primaquine Clears Submicroscopic <em>Plasmodium falciparum</em> Gametocytes that Persist after Treatment with Sulphadoxine-Pyrimethamine and Artesunate
				</@s.a></li>
                <li><@s.a href="%{article3}" title="Read Open Access Article">
				Creatine Monohydrate and Conjugated Linoleic Acid Improve Strength and Body Composition Following Resistance Exercise in Older Adults
				</@s.a></li>
                <li><@s.a href="%{article4}" title="Read Open Access Article">
				Naturopathic Care For Chronic Low Back Pain: A Randomized Trial
				</@s.a></li>
               </ul>
            </div><!-- end : block recent -->
            <!-- begin : browse widget block -->
            <div id="browseWidget" class="block">
              <p>Browse Clinical Trials Articles: <a href="${browseSubjectURL}">By Subject</a> or <a href="${browseDateURL}">By Publication Date</a></p>
            </div>
            <!-- end : browse block -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->
        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div class="info block">
              <@s.url action="commentGuidelines" anchor="annotation" namespace="/static" includeParams="none" id="annotation"/>
              <@s.url action="commentGuidelines" anchor="discussion" namespace="/static" includeParams="none" id="discussion"/>
              <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="rating"/>
              <@s.url action="checklist" namespace="/static" includeParams="none" id="checklist"/>
              <h3>Join the Community</h3>
              <p><a href="${freemarker_config.registrationURL}" title="Register">Register now</a> and share your views on clinical trials research. Only registrants can <a href="${rating}" title="Rating Guidelines">rate</a>, <a href="${discussion}" title="Learn how to start a discussion">discuss</a> and <a href="${annotation}" title="Learn how to add an annotation">annotate</a> articles in the Hub.</p>
              <h3>Submit Your Work</h3>
              <p>PLoS is committed to publishing the results of all clinical trials, regardless of outcome, and making this essential information freely and publicly available. <a href="${checklist}" title="Submission Info">Find out how to submit your work</a>.</p>
            </div><!-- end : info block -->
            <!-- begin : advocacy blocks -->
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=abd0d95d' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:184&#38;source=PHUBCT&#38;n=abd0d95d' border='0' alt=''/></a></noscript>
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a56536b2' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:185&#38;source=PHUBCT&#38;n=a56536b2' border='0' alt=''/></a></noscript>
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a6f9fd36' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:186&#38;source=PHUBCT&#38;n=a6f9fd36' border='0' alt=''/></a></noscript>
            <!-- end : advocacy blocks -->
            <!-- start : info block -->
            <div class="info block">
              <h3>Highlights From Other Open Access Journals</h3>
              <p>Links to relevant content that is not currently in the Hub. We will be adding content in coming months.</p>
              <dl class="category">
                <dt><em>PLoS Medicine</em></dt>
                <dd><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050022">Market Failure and the Poverty of New Drugs in Maternal Health</a></dd>
                <dd><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050021">Incorporating Molecular Tools into Early-Stage Clinical Trials</a></dd>	
                <dd><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050020">CONSORT for Reporting Randomized Controlled Trials in Journal and Conference Abstracts: Explanation and Elaboration</a></dd>
                 <dd><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0050008">Antitumor Activity of Rapamycin in a Phase I Trial for Patients with Recurrent PTEN-Deficient Glioblastoma</a></dd>
              </dl>
              <h3>From the Web</h3>
              <p><a href="http://www.wma.net/e/ethicsunit/helsinki.htm" title="WMA.net | Ethics Unit: Declaration of Helsinki">World Medical Association undertakes review of Declaration of Helsinki</a></p>
            </div><!-- end : info block -->
           </div><!-- end : subcol first -->
          <!-- end : col 3 -->
          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner">
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
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a595dcde' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:181&#38;source=PHUBCT&#38;n=a595dcde' border='0' alt=''/></a></noscript>
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
