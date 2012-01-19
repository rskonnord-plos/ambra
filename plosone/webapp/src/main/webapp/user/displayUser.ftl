<#assign defaultValue = "<em>No answer</em>">
<#if pou.surnames?has_content>
	<#assign surnames = pou.surnames>
<#else>
	<#assign surnames = defaultValue>
</#if>
<#if pou.city?has_content>
	<#assign city = pou.city>
<#else>
	<#assign city = defaultValue>
</#if>
<#if pou.country?has_content>
	<#assign country = pou.country>
<#else>
	<#assign country = defaultValue>
</#if>
<#if pou.postalAddress?has_content>
	<#assign postalAddress = pou.postalAddress>
<#else>
	<#assign postalAddress = defaultValue>
</#if>
<#if pou.title?has_content>
	<#assign title = pou.title>
<#else>
	<#assign title = defaultValue>
</#if>
<#if pou.organizationName?has_content>
	<#assign orgName = pou.organizationName>
<#else>
	<#assign orgName = defaultValue>
</#if>
<#if pou.organizationType?has_content>
	<#assign orgType = pou.organizationType>
<#else>
	<#assign orgType = defaultValue>
</#if>
<#if pou.positionType?has_content>
	<#assign positionType = pou.positionType>
<#else>
	<#assign positionType = defaultValue>
</#if>

<#if pou.biographyText?has_content>
	<#assign bio = pou.biographyText>
<#else>
	<#assign bio = defaultValue>
</#if>
<#if pou.researchAreasText?has_content>
	<#assign research = pou.researchAreasText>
<#else>
	<#assign research = defaultValue>
</#if>
<#if pou.interestsText?has_content>
	<#assign interests = pou.interestsText>
<#else>
	<#assign interests = defaultValue>
</#if>
<#if pou.homePage?has_content>
	<#assign homePage = pou.homePage>
<#else>
	<#assign homePage = defaultValue>
</#if>
<#if pou.weblog?has_content>
	<#assign weblog = pou.weblog>
<#else>
	<#assign weblog= defaultValue>
</#if>



<div id="content" class="profile">

<img src="${freemarker_config.context}/images/pone_avatar.png" />
<h1>${pou.displayName}</h1>

<ol>
<li><span class="heading">Full Name</span><span class="text">${pou.givenNames} ${surnames}</span></li>

<li><span class="heading">Location</span><span class="text">${city}, ${country}</span></li>

<li><span class="heading">Address</span><span class="text">${postalAddress}</span></li>

<li><span class="heading">Organization Type</span><span class="text">${orgType}</span></li>
<li><span class="heading">Organization Name</span><span class="text">${orgName}</span></li>
<li><span class="heading">Title</span><span class="text">${title}</span></li>
<li><span class="heading">Position Type</span><span class="text">${positionType}</span></li>

<li><span class="heading">About Me</span><span class="text">${bio}</span></li>

<li><span class="heading">Research Areas</span><span class="text">${research}</span></li>

<li><span class="heading">Interests</span><span class="text">${interests}</span></li>

<li><span class="heading">Home Page</span><span class="text">${homePage}</span></li>

<li><span class="heading">Weblog</span><span class="text">${weblog}</span></li>
</ol>

</div>
