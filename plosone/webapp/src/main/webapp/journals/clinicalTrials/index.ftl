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
              <h2><img class="badgenew" src="images/pone_home_new_badge.gif" alt="NEW!" />What is the PLoS Hub for Clinical Trials?</h2>
              <p>Launched in August 2007, the PLoS Hub for Clinical Trials collects PLoS journal articles that relate to clinical trials. The Hub is a destination site for researchers to share their views and build a dynamic, interactive community.</p>
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
                <@s.url id="article0000000" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000000"/>
                <@s.url id="article0000007" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000007"/>
                <@s.url id="article0000008" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000008"/>
                <@s.url id="article0000012" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000012"/>

                <li><@s.a href="%{article0000012}" title="Read Open Access Article">The Effectiveness of Contact Tracing in Emerging Epidemics</@s.a></li>
                <li><@s.a href="%{article0000008}" title="Read Open Access Article">Molecular Adaptation during Adaptive Radiation in the Hawaiian Endemic Genus <em>Schiedea</em></@s.a></li>
                <li><@s.a href="%{article0000000}" title="Read Open Access Article">PLoS ONE Sandbox: A Place to Learn and Play</@s.a></li>
                <li><@s.a href="%{article0000007}" title="Read Open Access Article">A Single Basis for Developmental Buffering of <em>Drosophila</em> Wing Shape</@s.a></li>
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
               document.write ("&#38;what=zone:184&#38;source=PHUBCT&#38;block=1&#38;blockcampaign=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                 document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
             //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=abd0d95d' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:184&#38;source=PHUBCT&#38;n=abd0d95d' border='0' alt=''></a></noscript>
            <script language='JavaScript' type='text/javascript'>
            <!--
               if (!document.phpAds_used) document.phpAds_used = ',';
               phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

               document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
               document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
               document.write ("&#38;what=zone:185&#38;source=PHUBCT&#38;block=1&#38;blockcampaign=1");
               document.write ("&#38;exclude=" + document.phpAds_used);
               if (document.referrer)
                  document.write ("&#38;referer=" + escape(document.referrer));
               document.write ("'><" + "/script>");
            //-->
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a56536b2' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:185&#38;source=PHUBCT&#38;n=a56536b2' border='0' alt=''></a></noscript>
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a6f9fd36' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:186&#38;source=PHUBCT&#38;n=a6f9fd36' border='0' alt=''></a></noscript>
            <!-- end : advocacy blocks -->
            <div class="info block">
              <h3>Highlights From Other Open Access Journals</h3>
              <p>Links to relevant content that is not currently in the Hub. We will be adding content in coming months.</p>
              <dl class="category">
                <dt><em>PLoS Medicine</em></dt>
                <dd><a href="http://medicine.plosjournals.org/perlserv/?request=get-document&doi=10.1371/journal.pmed.0040201">Liver Fluke Induces Cholangiocarcinoma</a></dd>
                <dt><em>Trials</em> (BioMed Central)</dt>
                <dd><a href="http://www.trialsjournal.com/content/8/1/18">Telecare motivational interviewing for diabetes patient education and support: a randomised controlled trial based in primary care comparing nurse and peer supporter delivery</a></dd>
              </dl>
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
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a595dcde' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:181&#38;source=PHUBCT&#38;n=a595dcde' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->
        <div id="lower">&nbsp;</div>
      </div><!-- end : col -->
      <div class="partner">
        <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
        <a href="http://fedora.info/" title="Fedora.info"><img src="images/pone_home_fedora.jpg" alt="Fedora.info"/></a>
        <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="images/pone_home_mulgara.gif" alt="Mulgara.org"/></a>
        <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
        <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="images/pone_home_moore.gif" alt="Moore Foundation"/></a>
      </div><!-- end : block partners -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
