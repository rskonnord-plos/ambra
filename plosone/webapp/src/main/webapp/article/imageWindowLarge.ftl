<a href="#" class="return" onclick="history.back(-1);return false;">Return to slideshow</a>
<#if Parameters.uri?exists>
   <#assign imageURI = Parameters.uri>
<#else>
   <#assign imageURI = "">
</#if>

	<@ww.url id="imageUrl" includeParams="none"  action="fetchObject" uri="${imageURI}"/>

			<img src="${imageUrl}&representation=PNG_L" title="Larger Image" class="large" id="figureImg" />

