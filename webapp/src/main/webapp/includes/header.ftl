<#include "/includes/global_variables.ftl">
<!DOCTYPE html>
<html lang="en">
  <head>
    <title>${pgTitle}</title>

    <#include "/includes/css.ftl">

    <script>document.documentElement.className += ' js';</script>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9" />

    <link rel="shortcut icon" href="${freemarker_config.context}/images/favicon.ico" type="image/x-icon" />

    <!--chartbeat -->
    <script type="text/javascript">var _sf_startpt=(new Date()).getTime()</script>

    <link rel="home" title="home" href="${homeURL}" />
    <link rel="alternate" type="application/rss+xml"
          title="${freemarker_config.getArticleTitlePrefix(journalContext)} New Articles"
          href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}/article/feed" />

    <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}" />
    <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}" />
  </head>
  <body>
  <!--body and html tags gets closed in global_footer.ftl-->
    <div id="page-wrap">
