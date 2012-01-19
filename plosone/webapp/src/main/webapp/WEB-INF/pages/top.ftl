<html>
	<head>
	<title>${freemarker_config.getTitle(templateFile)}</title>

	<#list freemarker_config.getCss(templateFile) as x>
      <link rel="stylesheet" type="text/css" media="screen" href="${x}" />
  </#list> 

	<#list freemarker_config.getJavaScript(templateFile) as x>
     <script language="javascript" type="text/javascript" src="${x}"></script>
  </#list> 

	</head>
	<body>

	