<#assign pgTitleOrig = freemarker_config.getTitle(templateFile, journalContext)>
<#assign pgTitle = pgTitleOrig>

<#if pgTitleOrig = "CODE_ARTICLE_TITLE" && articleInfoX??> <#--to get article title in w/o a new template for now-->
  <#assign pgTitle = freemarker_config.getArticleTitlePrefix(journalContext) + " " + articleInfoX.unformattedTitle>
</#if>

<@s.url includeParams="get" includeContext="true" encode="false" id="pgURL"/>
<#assign rdfPgURL = pgURL?replace("&amp;", "&")>

<@s.url action="home" namespace="/" includeParams="none" includeContext="true" id="homeURL"/>
<@s.url action="authors" namespace="/static" includeParams="none" id="authorsURL"/>
<@s.url action="policies" namespace="/static" includeParams="none" id="policiesURL"/>
<@s.url action="competing" namespace="/static" includeParams="none" id="competingURL"/>
<@s.url action="guidelines" namespace="/static" includeParams="none" id="guidelinesURL"/>
<@s.url action="latexGuidelines" namespace="/static" includeParams="none" id="latexURL"/>
<@s.url action="figureGuidelines" namespace="/static" includeParams="none" id="figureGuidelinesURL"/>
<@s.url action="checklist" namespace="/static" includeParams="none" id="cklistURL"/>
<@s.url action="reviewerGuidelines" namespace="/static" includeParams="none" id="reviewerGuidelinesURL"/>
<@s.url action="eic" namespace="/static" includeParams="none" id="eicURL"/>
<@s.url action="about" namespace="/static" includeParams="none" id="aboutURL"/>
<@s.url action="sitemap" namespace="/static" includeParams="none" id="siteMapURL"/>
<@s.url action="contact" namespace="/static" includeParams="none" id="contactURL"/>
<@s.url action="information" namespace="/static" includeParams="none" id="informationURL"/>
<@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="commentURL"/>
<@s.url action="ratingGuidelines" namespace="/static" includeParams="none" id="ratingURL"/>
<@s.url action="help" namespace="/static" includeParams="none" id="helpURL"/>
<@s.url action="feedback" namespace="/feedback" includeParams="none" id="feedbackURL"/>
<@s.url action="aeGuidelines" namespace="/static" includeParams="none" id="aeGuidelinesURL"/>
<@s.url action="submissionInstructions" namespace="/static" includeParams="none" id="manuscriptURL"/>
<@s.url action="publication" namespace="/static" includeParams="none" id="publicationURL"/>
<@s.url action="editorial" namespace="/static" includeParams="none" id="editorialURL"/>
<@s.url action="supportingInformation" namespace="/static" includeParams="none" id="supportingInformationURL"/>
<@s.url action="edboard" namespace="/static" includeParams="none" id="edboardURL"/>
<@s.url action="section_editors" namespace="/static" includeParams="none" id="sectionEditorsURL"/>
<@s.url action="peerReviewers" namespace="/static" includeParams="none" id="peerReviewersURL"/>
<@s.url action="almInfo" namespace="/static" includeParams="none" id="almInfoURL"/>
<@s.url action="license" namespace="/static" includeParams="none" id="licenseURL"/>
<@s.url action="downloads" namespace="/static" includeParams="none" id="downloadsURL"/>
<@s.url action="journalClub" namespace="/static" includeParams="none" id="journalClubURL"/>
<@s.url action="browse" namespace="/article/browse" includeParams="none" id="browseURL"/>
<@s.url action="issue" namespace="/article/browse" includeParams="none" id="browseIssueURL"/>
<@s.url action="volume" namespace="/article/browse" includeParams="none" id="browseVolumeURL"/>
<@s.url action="pmedCollections" namespace="/static" includeParams="none" id="pmedCollectionsURL"/>
<@s.url action="benefits" namespace="/static" includeParams="none" id="benefitsURL"/>
<@s.url action="faq" namespace="/static" includeParams="none" id="faqURL"/>
<@s.url action="studentforum" namespace="/static" includeParams="none" id="studentforumURL"/>
<@s.url action="ghostwriting" namespace="/static" includeParams="none" id="ghostwritingURL"/>
<@s.url action="developing" namespace="/static" includeParams="none" id="developingURL"/>
<@s.url action="whypublish" namespace="/static" includeParams="none" id="whypublishURL"/>
<@s.url action="scope" namespace="/static" includeParams="none" id="scopeURL"/>
<@s.url action="privacy" namespace="/static" includeParams="none" id="privacyURL"/>
<@s.url action="terms" namespace="/static" includeParams="none" id="termsURL"/>
<@s.url action="releaseNotes" namespace="/static" includeParams="none" id="releaseNotesURL"/>
<@s.url action="hppcallforpapers" namespace="/static" includeParams="none" id="callForPapersURL"/>
<@s.url action="publish" namespace="/static" includeParams="none" id="publishURL"/>

<#assign nowString>${.now?string("yyyy-MM-dd")}T23:59:59Z</#assign>
<#-- 30 days ago -->
<#assign then = "${(.now?long - 2628000000)?number_to_datetime}" />
<#assign thenString>${then?datetime?string("yyyy-MM-dd")}T00:00:00Z</#assign>
<@s.url action="advancedSearch" namespace="/search" startPage="0" sort="Date, newest first"
  filterJournals="${currentJournal}"
  filterStartDate="${thenString}"
  filterEndDate="${nowString}"
  unformattedQuery="*:*"
  includeParams="none" id="recentArticlesURL"/>
