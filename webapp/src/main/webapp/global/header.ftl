<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:foaf="http://xmlns.com/foaf/0.1/"
      xmlns:dc="http://purl.org/dc/terms/"
      xmlns:doi="http://dx.doi.org/"
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:xsd="http://www.w3.org/2001/XMLSchema-datatypes#"
      lang="en" xml:lang="en"
      itemscope itemtype="http://schema.org/Article">
<head>
  <title>${pgTitle}</title>
<#include "/includes/css.ftl">

  <!--chartbeat -->
  <script type="text/javascript">var _sf_startpt = (new Date()).getTime()</script>
  <script>document.documentElement.className += ' js';</script>

  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7; IE=EmulateIE9"/>
  <meta name="description" content="${freemarker_config.getMetaDescription(journalContext)}"/>
  <meta name="keywords" content="${freemarker_config.getMetaKeywords(journalContext)}"/>
  <meta name="almHost" content="${freemarker_config.get("almHost")}"/>
  <meta name="solrHost" content="${freemarker_config.get("solrHost")}"/>
  <meta name="solrApiKey" content="${freemarker_config.get("solrApiKey")}"/>
  <meta name="pubGetHost" content="${freemarker_config.pubGetURL}"/>
<#include "../includes/article_meta_tags.ftl">
<#include "banner_macro.ftl"/>
  <link rel="shortcut icon" href="${freemarker_config.context}/images/favicon.ico" type="image/x-icon"/>
  <link rel="home" title="home" href="${homeURL}"/>
  <link rel="alternate" type="application/rss+xml"
        title="${freemarker_config.getArticleTitlePrefix(journalContext)} New Articles"
        href="${Request[freemarker_config.journalContextAttributeKey].baseUrl}/article/feed"/>
</head>
<body>
<#import "../includes/global_body.ftl" as global>
<div id="page-wrap">

  <div id="topbanner" class="cf">
  <#include "../includes/home_topbanner.ftl">
  </div>

  <div id="pagehdr-wrap">
    <div id="pagehdr">

      <div id="user" class="nav">
        <ul>
        <#if Session["AMBRA_USER"]?exists>
          <@s.url id="editProfileURL" includeParams="none" namespace="/user/secure" action="editProfile" tabId="preferences"/>
          <li><a href="${editProfileURL}" title="Edit your account profile and alert settings">preferences</a></li>
          <@s.url id="logoutURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${freemarker_config.casLogoutURL}?service=${Request[freemarker_config.journalContextAttributeKey].baseUrl}/logout.action"/>
          <li class="btn-style"><a href="${logoutURL}">sign out</a></li>
        <#else>
          <li><a href="${freemarker_config.registrationURL}">create account</a></li>
          <@s.url id="loginURL" includeParams="none" namespace="/user/secure" action="secureRedirect" goTo="${global.thisPage}"/>
          <li class="btn-style"><a
            href="${freemarker_config.context}/user/secure/secureRedirect.action?goTo=${global.thisPage}">sign in</a>
          </li>
        </#if>
        </ul>
      </div>

      <div class="logo">
        <a href="${homeURL}"><img src="/images/logo.png" alt="${freemarker_config.getDisplayName(journalContext)}"></a>
      </div>

    <#include "/includes/common_navbar.ftl">

    </div>
    <!-- pagehdr-->
  </div>
  <!-- pagehdr-wrap -->

  <!--body and html tags gets closed in global_footer.ftl-->
