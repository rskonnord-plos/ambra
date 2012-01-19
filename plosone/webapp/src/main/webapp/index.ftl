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
                <@s.url action="checklist" namespace="/static" includeParams="none" id="checklist"/>
                <li><a href="${checklist}" title="Find out how to submit to PLoS ONE">Submit Today</a></li>
              </ul>
            </div><!-- end : block mainnav -->
            <div class="block partner">
              <h6>Partners</h6>
              <a href="http://www.unitedlayer.com/" title="UnitedLayer, LLC"><img src="${freemarker_config.context}/images/pone_home_unitedlayer.gif" alt="UnitedLayer, LLC"/></a>
              <a href="http://fedora.info/" title="Fedora.info"><img src="${freemarker_config.context}/images/pone_home_fedora.jpg" alt="Fedora.info"/></a>
              <a href="http://www.mulgara.org/" title="Mulgara.org"><img src="${freemarker_config.context}/images/pone_home_mulgara.png" alt="Mulgara.org"/></a>
              <a href="http://www.sciencecommons.org/" title="Science Commons"><img src="${freemarker_config.context}/images/pone_home_sciencecommons.gif"  alt="Science Commons"/></a>
              <a href="http://www.osafoundation.org" title="Open Source Applications Foundation"><img src="${freemarker_config.context}/images/pone_home_osaf.gif" alt="OSAF"/></a>
              <a href="http://www.moore.org" title="Gorden and Betty Moore Foundation"><img src="${freemarker_config.context}/images/pone_home_moore.gif" alt="Moore Foundation"/></a>
            </div><!-- end : block partner -->
            <div class="block banner">
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&amp;what=zone:38&amp;source=ONE&amp;target=_top&amp;block=1");
                 document.write ("&amp;exclude=" + document.phpAds_used);
                 if (document.referrer)
                  document.write ("&amp;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a50d73a8' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:38&amp;source=ONE&amp;n=a50d73a8' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
            <div class="block banner">
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&amp;what=zone:37&amp;source=ONE&amp;target=_top&amp;block=1");
                 document.write ("&amp;exclude=" + document.phpAds_used);
                 if (document.referrer)
                  document.write ("&amp;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=a8d63154' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:37&amp;source=ONE&amp;n=a8d63154' border='0' alt=''></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : col first -->
          <!-- begin : col 2 -->
          <div class="col last">
            <div class="horizontalTabs" style="padding-top: 0; ">
              <ul id="tabsContainer">
              </ul>

              <div id="tabPaneSet" class="contentwrap">
                  <#include "article/recentArticles.ftl">
              </div>
            </div><!-- end : horizontalTabs -->

            <div class="block feature">

              <h2>New and Noted</h2>
              <@s.url id="newNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000443"/>
              <@s.url id="newNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000444"/>
              <@s.url id="newNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000451"/>                            

              <@s.url id="alsoNoted1" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000446"/>
              <@s.url id="alsoNoted2" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000450"/>
              <@s.url id="alsoNoted3" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000447"/>                            
              <@s.url id="alsoNoted4" namespace="/article" action="fetchArticle" articleURI="info:doi/10.1371/journal.pone.0000445"/>
              
              <div>
                <@s.a href="${newNoted1}" title="Read Open-Access Article">Order in Spontaneous Behavior</@s.a>
                <p>Animals are usually considered to behave as complex automata, responding predictably to external stimuli. This study suggests otherwise, showing that even the humble fruit fly can behave spontaneously. The flight paths of flies in a completely featureless environment were neither random nor predictable, but followed a complicated fractal pattern generated within the fly's brain.</p>

                <@s.a href="${newNoted2}" title="Read Open-Access Article">Economic Inequality Predicts Biodiversity Loss</@s.a>
                <p>Human activity is causing high rates of biodiversity loss, but how socioeconomics feeds into this decline is poorly understood. This research explores how economic inequality, the gap between a country's rich and its poor, is related to biodiversity loss. Looking at both countries and individual U.S. states, it shows that inequality and increased biodiversity loss go hand-in-hand.</p>

                <@s.a href="${newNoted3}" title="Read Open-Access Article">Immunity against <em>Ixodes scapularis</em> Salivary Proteins Expressed within 24 Hours of Attachment Thwarts Tick Feeding and Impairs <em>Borrelia</em> Transmission</@s.a>
                <p>In North America, the black-legged tick, <em>Ixodes scapularis</em>, spreads several human diseases. This study shows that, in guinea pigs, immunity against proteins that ticks produce in the first day after their attachment inhibited the ticks' subsequent feeding. It also reduced transmission of the Lyme disease agent <em>Borrelia burgdorferi</em>, suggesting new strategies for the control tick-borne diseases.</p>

                <h3>Also of Note</h3>
                <ul class="articles">
                  <li><@s.a href="${alsoNoted1}" title="Read Open-Access Article">Calculation of the Free Energy and Cooperativity of Protein Folding</@s.a></li>
                  <li><@s.a href="${alsoNoted2}" title="Read Open-Access Article">Conditional Expression of <em>Wnt4</em> during Chondrogenesis Leads to Dwarfism in Mice</@s.a></li>
                  <li><@s.a href="${alsoNoted3}" title="Read Open-Access Article">Genetic Evidence for a Link Between Glycolysis and DNA Replication</@s.a></li>
                  <li><@s.a href="${alsoNoted4}" title="Read Open-Access Article">A Model of Late Long-Term Potentiation Simulates Aspects of Memory Maintenance</@s.a></li>
                </ul>
              </div>
            </div>

             <!-- end block feature -->
          </div><!-- end : col last -->
        </div><!-- end : wrapper for cols 1 & 2 -->

        <!-- begin : wrapper for cols 3 & 4 -->
        <div id="second" class="col">
          <!-- begin : col 3 -->
          <div class="subcol first">
            <div class="info block">
              <@s.url action="commentGuidelines" anchor="annotation" namespace="/static" includeParams="none" id="annotation"/>
              <@s.url action="commentGuidelines" anchor="discussion" namespace="/static" includeParams="none" id="discussion"/>

              <h2>What is PLoS ONE?</h2>
              <div>
                <p>A new way of communicating peer-reviewed science and medicine.</p>
                <ul>
                  <@s.url id="sandbox" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pone.0000000"/>
                  <li>Try out <a href="${annotation}" title="Learn how to add an annotation">annotations</a> and <a href="${discussion}" title="Learn how to start a discussion">discussions</a> in the <a href="${sandbox}" title="PLoS ONE Sandbox: A Play to Learn and Play"><em>PLoS ONE</em> Sandbox</a>.</li>
                  <li><a href="${browseDateURL}" title="PLoS ONE | Browse by Publication Date">Browse articles by Publication Date</a> and try our improved <a href="/article/browse.action" title="PLoS ONE | Browse by Subject">Browse by Subject</a>.</li>
                  <li><a href="${browseSubjectURL}" title="PLoS ONE | RSS Feeds">RSS Feeds</a> by Subject</li>
                </ul>
                <p><em>More functionality coming soon</em></p>
                <p><a href="${feedbackURL}" title="Send us your feedback">Your feedback</a> will help us shape <em>PLoS ONE</em>.</p>
                <h3>Journal Clubs - New Discussion</h3>
                <p><em>PLoS ONE</em> regularly sends out papers in advance of publication to interested labs to debate them and post their comments online. </p>
                <#assign jClubArticleDOI="info:doi/10.1371/journal.pone.0000343"/>
                <@s.url id="jClubArticle" namespace="/article" action="fetchArticle" articleURI="${jClubArticleDOI}"/>
                <@s.url id="jClubComment" namespace="/annotation" action="getCommentary" target="${jClubArticleDOI}"/>
                <ul>
                  <li><@s.a href="${jClubArticle}" title="Onset Rivalry: Brief Presentation Isolates an Early Independent Phase of Perceptual Competition">This recent paper</a> was discussed by the <a href="${freemarker_config.context}/user/showUser.action?userId=info:doi/10.1371/account/39755" title="Potsdam University Eye-Movement Group">Potsdam University Eye-Movement Group</@s.a>.</li>
                  <li>Add your own comments to <a href="${jClubComment}" title="Annotations and Discussions Re: Onset Rivalry: Brief Presentation Isolates an Early Independent Phase of Perceptual Competition">the discussion</a>.</li>
                  <li><a href="${feedbackURL}" title="Send us your feedback">Nominate your lab</a> for a future journal club.</li>
                </ul>
              </div>
            </div><!-- end : info block -->

            <#if categoryNames?size gt 0>
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
              <@s.url id="browseURL" action="browse" namespace="/article"  catId="${category_index}" includeParams="none"/>
              <dt>
                <a class="expand" id="widget${categoryId}" href="${browseURL}">${category} (${articlesByCategory[category_index]?size})</a>&nbsp;
                <a href="${freemarker_config.context}/rss/${category?replace(' ','')?replace("'",'')}.xml"><img src="${freemarker_config.context}/images/feed-icon-12x12.gif" /></a>
              </dt>
              </#list>
              </dl>
            </div><!-- end : subject block -->
            </#if>
          </div><!-- end : subcol first -->

          <!-- begin : col 4 -->
          <div class="subcol last">
            <div class="block banner">
              <script language='JavaScript' type='text/javascript'>
              <!--
                 if (!document.phpAds_used) document.phpAds_used = ',';
                 phpAds_random = new String (Math.random()); phpAds_random = phpAds_random.substring(2,11);

                 document.write ("<" + "script language='JavaScript' type='text/javascript' src='");
                 document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
                 document.write ("&amp;what=zone:36&amp;source=ONE&amp;target=_top&amp;block=1");
                 document.write ("&amp;exclude=" + document.phpAds_used);
                 if (document.referrer)
                    document.write ("&amp;referer=" + escape(document.referrer));
                 document.write ("'><" + "/script>");
              //-->
              </script><noscript><a href='http://ads.plos.org/adclick.php?n=ad8f367e' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:36&amp;source=ONE&amp;n=ad8f367e' border='0' alt='' /></a></noscript>
            </div><!-- end : block banner -->
          </div><!-- end : subcol last -->
        </div><!-- end : wrapper for cols 3 & 4 -->

        <div id="lower">
          <div class="col first">
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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a58966f8' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:160&amp;source=ONE&amp;n=a58966f8' border='0' alt='' /></a></noscript>

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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a67dfba7' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:161&amp;source=ONE&amp;n=a67dfba7' border='0' alt='' /></a></noscript>

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
            </script><noscript><a href='http://ads.plos.org/adclick.php?n=a08b8fff' target='_top'><img src='http://ads.plos.org/adview.php?what=zone:162&amp;source=ONE&amp;n=a08b8fff' border='0' alt='' /></a></noscript>
          </div><!-- end : col first -->
        </div><!-- end : lower -->
      </div><!-- end : col -->
    </div><!-- end : home -->
  </div><!-- end : wrap -->
</div><!-- end : content -->
