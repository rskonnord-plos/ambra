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
      <#assign jourAnchor = "<a href=\"" + freemarker_config.getJournalUrl(jour.key) + "/faq.action\">"/>    
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
      <p id="articleType">Research Article</p>
    </div>
    <#if publisher != "">
      <div id="publisher"><p>${publisher}</p></div>
    </#if>
    <@s.property value="transformedArticle" escape="false"/>
  </div>

