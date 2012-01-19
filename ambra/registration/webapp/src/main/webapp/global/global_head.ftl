<#--
  $HeadURL::                                                                            $
  $Id$
  
  Copyright (c) 2007-2008 by Topaz, Inc.
  http://topazproject.org
  
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<title>Ambra</title>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon" />

<#include "../css/global_css.ftl">

<script type="text/javascript" src="javascript/prototype.js"></script>	
<script type="text/javascript" src="javascript/browserDetection.js"></script>	
<script type="text/javascript" src="javascript/config_default.js"></script>
<script type="text/javascript" src="javascript/dojo/dojo.js"></script>
<script type="text/javascript" src="javascript/topaz/topaz.js"></script>
<script type="text/javascript">  
  dojo.registerModulePath("topaz", "../topaz");
  dojo.require("topaz.topaz");
  dojo.require("dojo.html.*");
  dojo.require("dojo.io.*");
  dojo.require("dojo.event.*");
</script>
<script type="text/javascript" src="javascript/topaz/formUtil.js"></script>   
<script type="text/javascript" src="javascript/topaz/domUtil.js"></script>
<script type="text/javascript" src="javascript/init_global.js"></script>  
<script type="text/javascript" src="javascript/init_navigation.js"></script>  

<!--
<rdf:RDF xmlns="http://web.resource.org/cc/"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
<Work rdf:about="">
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
