<#if Request[freemarker_config.journalContextAttributeKey]?exists>
	<#assign journalContext = Request[freemarker_config.journalContextAttributeKey].journal>
<#else>
	<#assign journalContext = "">
</#if>
<title>PLoS Journals : A Peer-Reviewed, Open-Access Journal</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="shortcut icon" href="${freemarker_config.context}/images/pjou_favicon.ico" type="image/x-icon" />

<link rel="home" title="home" href="http://www.plosjournals.org"></link>

<#include "css.ftl">
<#include "../javascript/global_js.ftl">

<meta name="keywords" content="PLoS, Public Library of Science, Open Access, Open-Access, Science, Medicine, Biology, Research, Peer-review, Inclusive, Interdisciplinary, Ante-disciplinary, Physics, Chemistry, Engineering" />

<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="http://www.plosone.org">
   <license rdf:resource="http://creativecommons.org/licenses/by/2.5/" />
</Work>
<License rdf:about="http://creativecommons.org/licenses/by/2.5/">
   <permits rdf:resource="http://web.resource.org/cc/Reproduction" />
   <permits rdf:resource="http://web.resource.org/cc/Distribution" />
   <requires rdf:resource="http://web.resource.org/cc/Notice" />
   <requires rdf:resource="http://web.resource.org/cc/Attribution" />
   <permits rdf:resource="http://web.resource.org/cc/DerivativeWorks" />
</License>
</rdf:RDF>
-->
