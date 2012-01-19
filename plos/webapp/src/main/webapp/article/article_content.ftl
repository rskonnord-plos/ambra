<#assign publisher=""/>
<#if Request[freemarker_config.journalContextAttributeKey]?exists>
  <#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
  <#assign journalContext = "">
</#if>
<#list journalList as jour>
  <#-- Special Case -->
  <#if (journalList?size == 1) && (jour.key == journalContext)>
    <#if jour.key == "PLoSClinicalTrials">
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "/static/faq.action\">"/>    
      <#assign publisher="Published in <em>" + jourAnchor + "PLoS Clinical Trials</a></em>" />
    </#if>
  <#else>
    <#if (articleInfo.EIssn = jour.EIssn) && (jour.key != journalContext) >
      <#assign publisher = "Published in <em><a href=\"" + freemarker_config.getJournalUrl(jour.key) +
                           "\">"+ jour.dublinCore.title + "</a></em>" />
      <#break/>
    <#else>
      <#if jour.key != journalContext> 
        <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "\">"/>
        <#if publisher?length gt 0>
          <#assign publisher = publisher + ", " + jourAnchor + jour.dublinCore.title + "</a>" />
        <#else>
          <#assign publisher = publisher + "Featured in " + jourAnchor + jour.dublinCore.title + "</a>" />
        </#if>
      </#if>
    </#if>
  </#if>
</#list>
  <div id="researchArticle" class="content">
    <a id="top" name="top" toc="top" title="Top"></a>
    <@s.url id="thisPageURL" includeParams="get" includeContext="true" encode="false"/>
    <@s.url id="feedbackURL" includeParams="none" namespace="/" action="feedbackCreate" page="${thisPageURL?url}"/>
    <div class="beta">We are still in beta! Help us make the site better and
      <a href="${feedbackURL}" title="Submit your feedback">report bugs</a>.
    </div>
    <div id="contentHeader">
      <p>Open Access</p>
      <p id="articleType">${articleType.heading}</p>
    </div>
    <#if publisher != "">
      <div id="publisher"><p>${publisher}</p></div>
    </#if>
    <div id="fch" class="fch" style="display:none;">
	  <p class="fch"><strong> Formal Correction:</strong> This article has been <em>formally corrected</em> to address the following errors.</p>
	  <ol id="fclist" class="fclist"></ol>
    </div>
	<div id="articleMenu"> 
		<ul> 
			<@s.url id="articleArticleRepXML"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}"> 
				<@s.param name="representation" value="%{'XML'}"/> 
			</@s.url> 
			<li><a href="${articleArticleRepXML}" class="xml" title="Download XML">Download Article XML</a></li> 
			<@s.url id="articleArticleRepPDF"  namespace="/article" action="fetchObjectAttachment" includeParams="none" uri="${articleURI}"> 
				<@s.param name="representation" value="%{'PDF'}"/> 
			</@s.url> 
			<li><a href="${articleArticleRepPDF}" class="pdf" title="Download PDF">Download Article PDF</a></li> 
			<@s.url id="articleCitationURL"  namespace="/article" action="citationList" includeParams="none" articleURI="${articleURI}" /> 
				<li><@s.a href="%{articleCitationURL}"  cssClass="citation" title="Download Citation">Download Citation</@s.a></li> 
			<@s.url id="emailArticleURL" namespace="/article" action="emailArticle" articleURI="${articleURI}"/> 
			<li><@s.a href="%{emailArticleURL}"  cssClass="email" title="E-mail This Article to a Friend or Colleague">E-mail this Article</@s.a></li> 
			<li><a href="http://www.plos.org/journals/print.html" title="Order reprinted versions of this article" class="reprints icon">Order Reprints</a></li> 
			<li><a href="#" onclick="window.print();return false;" class="print last" title="Print this article">Print this Article</a></li> 
		</ul> 
	</div> 
    <@s.property value="transformedArticle" escape="false"/>
  </div>
