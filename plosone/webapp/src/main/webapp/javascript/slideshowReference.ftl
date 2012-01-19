<#list secondaryObjects as image>
  <@s.url id="imageUrl" namespace="/article" includeParams="none"  action="fetchObject" uri="${image.uri}"/>
  <@s.url id="imageAttachUrl" namespace="/article" includeParams="none"  action="fetchObjectAttachment" uri="${image.uri}"/>
  <@s.url id="imageLargeUrl" namespace="/article" includeParams="none"  action="showImageLarge" uri="${image.uri}"/>
	slideshow[${image_index}] = {imageUri: '${imageUrl?js_string}', imageLargeUri: '${imageLargeUrl}', imageAttachUri: '${imageAttachUrl?js_string}',
	                title: '<strong>${image.title?js_string}.</strong> ${image.transformedCaptionTitle?js_string}',
	                titlePlain: '${image.title?js_string} ${image.plainCaptionTitle?js_string}',
	                description: '${image.transformedDescription?js_string}'};
</#list>