<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#--This is the journal-agnostic header file included in generic.ftl-->
<html lang="en">
<head>
<#include "/includes/global_variables.ftl">
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
  <meta name="solrApiKey" content="${freemarker_config.get("solrApiKey")}" />
  <meta name="almAPIKey" content="${freemarker_config.get("almAPIKey")}" />

  <link rel="shortcut icon" href="${freemarker_config.context}/images/favicon.ico" type="image/x-icon"/>
  <link rel="home" title="home" href="${homeURL}"/>
</head>
<body>
<#import "/includes/global_body.ftl" as global>
<div id="page-wrap">
  <div id="pagehdr-wrap">
    <div id="pagehdr">

      <div id="user" class="nav">
        <ul>
          <li><a href="/">Ambra</a></li>
        <#if Session["AMBRA_USER"]?exists>
          <@s.url id="editProfileURL" includeParams="none" namespace="/user/secure" action="editProfile"/>
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

      <div class="logo-generic">
        <a href="${homeURL}"><img src="/images/ambra.png" alt="${freemarker_config.getDisplayName(journalContext)}"></a>
      </div>

    </div>
  </div>
