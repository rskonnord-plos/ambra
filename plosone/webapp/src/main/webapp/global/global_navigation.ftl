  <ul id="nav">
    <li><a href="${homeURL}" tabindex="101">Home</a></li>
    <@s.url action="browse" namespace="/static" includeParams="none" id="browseURL"/>
    <@s.url action="browse" namespace="/article" includeParams="none" id="browseSubjectURL"/>
    <@s.url action="browse" namespace="/article" field="date" includeParams="none" id="browseDateURL"/>
    <li><a href="${browseURL}" tabindex="102">Browse Articles</a>
        <ul>
          <li><a href="${browseDateURL}">By Publication Date</a></li>
          <li><a href="${browseSubjectURL}">By Subject</a></li>
        </ul>
    </li>
    <@s.url action="about" namespace="/static" includeParams="none" id="about"/>
    <li><a href="${about}" tabindex="103">About</a>
        <ul>
        <@s.url action="information" namespace="/static" includeParams="none" id="info"/>
        <@s.url action="edboard" namespace="/static" includeParams="none" id="edboard"/>
        <@s.url action="license" namespace="/static" includeParams="none" id="license"/>
          <@s.url action="contact" namespace="/static" includeParams="none" id="contact"/>
          <li><a href="${info}">Journal Information</a></li>
          <li><a href="${edboard}">Editorial Board</a></li>
          <li><a href="${license}">Open-Access License</a></li>
          <li><a href="${contact}">Contact Us</a></li>
        </ul>
      </li>
    <@s.url action="users" namespace="/static" includeParams="none" id="users"/>
    <li><a href="${users}" tabindex="104">For Readers</a>
        <ul>
        <@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="comment"/>
        <@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="rating"/>
        <@s.url action="help" namespace="/static" includeParams="none" id="help"/>
        <@s.url action="sitemap" namespace="/static" includeParams="none" id="site"/>
          <li><a href="${comment}">Commenting Guidelines</a></li>
          <li><a href="${rating}">Rating Guidelines</a></li>
          <li><a href="${help}">Help Using this Site</a></li>
          <li><a href="${site}">Site Map</a></li>
        </ul>
      </li>
    <@s.url action="authors" namespace="/static" includeParams="none" id="authors"/>
    <li><a href="${authors}" tabindex="105">For Authors and Reviewers</a>
        <ul>
        <@s.url action="whypublish" namespace="/static" includeParams="none" id="why"/>
        <@s.url action="policies" namespace="/static" includeParams="none" id="policies"/>
        <@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelines"/>
        <@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figure"/>
        <@s.url action="checklist" namespace="/static" includeParams="none" id="checklist"/>
        <@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewer"/>
          <li><a href="${why}">Why Publish With Us?</a></li>
          <li><a href="${policies}">Editorial and Publishing Policies</a></li>
          <li><a href="${guidelines}">Author Guidelines</a></li>
          <li><a href="${figure}">Figure Guidelines</a></li>
          <li><a href="${checklist}">Submit Your Paper</a></li>
          <li><a href="${reviewer}">Reviewer Guidelines</a></li>
        </ul>
      </li>
      <li class="journalnav"><a href="http://www.plos.org" title="Public Library of Science" tabindex="110" class="drop">PLoS.org</a>
        <ul>
          <li><a href="http://www.plos.org/oa/index.html" title="Open Access Statement">Open Access</a></li>
          <li><a href="http://www.plos.org/support/donate.php" title="Join PLoS: Show Your Support">Join PLoS</a></li>
          <li><a href="http://www.plos.org/cms/blog" title="PLoS Blog">PLoS Blog</a></li>
          <li><a href="http://www.plos.org/connect.html" title="PLoS.org | Stay Connected">Stay Connected</a></li>
        </ul>
      </li>
			<#assign hubUrl = freemarker_config.getJournalUrl("PLoSClinicalTrials")/>
      <li class="journalnav"><a href="${hubUrl}" tabindex="109">Hubs</a>
        <ul>
          <li><a href="${hubUrl}" title="PLoS Hub for Clinical Trials">Clinical Trials</a></li>
        </ul>
      </li>
      <li class="journalnav"><a href="http://www.plosjournals.org" tabindex="108">Journals</a>
        <ul>
          <li><a href="http://biology.plosjournals.org" title="PLoSBiology.org">PLoS Biology</a></li>
          <li><a href="http://medicine.plosjournals.org" title="PLoSMedicine.org">PLoS Medicine</a></li>
          <li><a href="http://compbiol.plosjournals.org" title="PLoSCompBiol.org">PLoS Computational Biology</a></li>
          <li><a href="http://genetics.plosjournals.org" title="PLoSGenetics.org">PLoS Genetics</a></li>
          <li><a href="http://pathogens.plosjournals.org" title="PLoSPathogens.org">PLoS Pathogens</a></li>
          <li><a href="http://www.plosone.org/" title="PLoSONE.org">PLoS ONE</a></li>
          <li><a href="http://www.plosntds.org/" title="PLoSNTDs.org">PLoS Neglected Tropical Diseases</a></li>
        </ul>
      </li>
    </ul>
