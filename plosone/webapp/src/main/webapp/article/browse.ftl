<#macro pagination>
	<#assign totalResults = articleList?size>
	<#assign totalPages = (totalResults/pageSize)?int>
	<#if totalResults % pageSize != 0>
		<#assign totalPages = totalPages + 1>
	</#if>
  <#if (totalPages gt 1) >
		<div class="resultsTab">
  
    <#if (startPage gt 0)>
     	<@ww.url id="prevPageURL" action="browse" namespace="/article" startPage="${startPage - 1}" pageSize="${pageSize}" includeParams="get"/>
      <@ww.a href="%{prevPageURL}">&lt; Prev</@ww.a> |
    </#if>
    <#list 1..totalPages as pageNumber>
      <#if (startPage == (pageNumber-1))>
      	<strong>${pageNumber}</strong>
      <#else>
      	<@ww.url id="browsePageURL" action="browse" namespace="/article" startPage="${pageNumber - 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
      	<@ww.a href="%{browsePageURL}">${pageNumber}</@ww.a>
      </#if>
      <#if pageNumber != totalPages>|</#if>
    </#list>
    <#if (startPage lt totalPages - 1 )>
     	<@ww.url id="nextPageURL" action="browse" namespace="/article" startPage="${startPage + 1}" pageSize="${pageSize}" field="${field}" includeParams="get"/>
       <@ww.a href="%{nextPageURL}">Next &gt;</@ww.a> 
    </#if>
		</div> <!-- results tab-->
    
  </#if>
</#macro>

<div id="content" class="browse static">
	<!-- begin : banner -->
	<div id="bannerRight">
		<script language='JavaScript' type='text/javascript'src='http://ads.plos.org/adx.js'></script>
		<script language='JavaScript' type='text/javascript'>
		<!--
		  if (!document.phpAds_used) document.phpAds_used = ',';
		  phpAds_random = new String (Math.random()); 
		  phpAds_random = phpAds_random.substring(2,11);

		  document.write ("<" + "script language='JavaScript'	type='text/javascript' src='");
		  document.write ("http://ads.plos.org/adjs.php?n=" + phpAds_random);
		  document.write ("&amp;what=zone:177&amp;source=ONE&amp;withText=1&amp;block=1");
		  document.write ("&amp;exclude=" + document.phpAds_used);
		  if (document.referrer)
		    document.write ("&amp;referer=" + escape(document.referrer));
		  document.write ("'><" + "/script>");
		//-->
		</script>
		<noscript>
			<a href='http://ads.plos.org/adclick.php?n=a98abd23' target='_blank'><img src='http://ads.plos.org/adview.php?what=zone:177&amp;source=ONE&amp;n=a98abd23' border='0' alt='' /></a>
		</noscript>
	</div>
	<!-- end : banner -->
	<h1>Browse Articles</h1>
	<#if field == "date">
		<#include "browseNavDate.ftl">
	<#else>
		<#include "browseNavSubject.ftl">
	</#if>

	<#assign totalResults = articleList?size>

	<#assign startIndex = totalResults - startPage * pageSize - 1>
	<#assign endIndex = startIndex - pageSize + 1>
	<#if endIndex lt 0>
		<#assign endIndex = 0 >
	</#if>

	<#assign startPgIndex = startPage * pageSize>
	<#assign endPgIndex = startPgIndex + pageSize - 1>
	<#if endPgIndex gte totalResults>
		<#assign endPgIndex = totalResults - 1 >
	</#if>

	<div id="search-results">	<p><strong>${startPgIndex + 1} - ${endPgIndex + 1}</strong> of <strong>${totalResults}</strong> article<#if totalResults != 1>s</#if> published ${infoText}.</p>
		<@pagination />
		<ul>

		  <#list startIndex .. endIndex as idx>
				<#assign art = articleList[idx]>
				<li>
					<span class="date">Published ${art.articleDate?string("dd MMM yyyy")}</span>
          <@ww.url id="fetchArticleURL" action="fetchArticle" namespace="/article" articleURI="${art.uri}" includeParams="none"/>
					<span class="article"><@ww.a href="%{fetchArticleURL}" title="Read Open Access Article">${art.title}</@ww.a></span>
					<span class="authors">
						<#list art.authors as auth><#if auth_index gt 0>, </#if>${auth}</#list>
					</span>
				</li>
			</#list>
		</ul>
		<@pagination />
	</div> <!-- search results -->
</div> <!--content-->
