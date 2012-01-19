<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<#include "head.ftl">
</head>
<body>
<!-- begin : container -->
<div id="container">
	
	<!-- begin : top banner external ad space -->
	<div id="topBanner">
	<#include "topbanner.ftl">
	</div>
	<!-- end : top banner external ad space -->
	
	<#if Session.PLOS_ONE_USER?exists>
	<!-- begin : header -->
	<div id="hdr">
  <#else>	
	<!-- begin : header -->
	<div id="hdr" class="login">
	</#if>
	
	<#include "header.ftl">
	</div>
	<!-- end : header -->