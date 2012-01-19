<#if parameters.validate?default(false) == true>
	<script src="${base}/struts/xhtml/validation.js"></script>
	<#if parameters.onsubmit?exists>
		${tag.addParameter('onsubmit', "${parameters.onsubmit}; customOnsubmit(); return validateForm_${parameters.id}();")}
	<#else>
		${tag.addParameter('onsubmit', "customOnsubmit(); return validateForm_${parameters.id}();")}
	</#if>
</#if>
<#if !(fieldErrors.size() == 0)>
  <p class="required">Please correct the errors below. </p>
</#if>        
