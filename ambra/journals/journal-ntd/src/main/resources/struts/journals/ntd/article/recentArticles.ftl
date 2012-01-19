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
<!-- Recent Research -->
<ul class="articles">
	<@s.url id="art1URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000033"/>	
	<li><a href="${art1URL}" title="Read Open Access Article">
	Comparing Models for Early Warning Systems of Neglected Tropical Diseases
	</a></li>
	
	<@s.url id="art2URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000085"/>
	<li><a href="${art2URL}" title="Read Open Access Article">
	Epidemiology and Clinical Features of Patients with Visceral Leishmaniasis Treated by an MSF Clinic in Bakol Region, Somalia, 2004-2006
	</a></li>
	
	<@s.url id="art3URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000002"/>		
	<li><a href="${art3URL}" title="Read Open Access Article">
	Development of Highly Organized Lymphoid Structures in Buruli Ulcer Lesions after Treatment with Rifampicin and Streptomycin
	</a></li>
	
  <@s.url id="art4URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000075"/>			
	<li><a href="${art4URL}" title="Read Open Access Article">
	Occurrence of <em>Strongyloides stercoralis</em> in Yunnan Province, China, and Comparison of Diagnostic Methods
	</a></li>
		
	<!-- Do not edit below this comment -->
	<@s.url action="browse" namespace="/article" field="date" includeParams="none" id="browseDateURL"/>
	<li class="more"><a href="${browseDateURL}">Browse all recently published articles</a></li>
</ul>
						
