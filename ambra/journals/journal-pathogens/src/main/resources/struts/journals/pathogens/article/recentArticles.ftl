<#--
  $HeadURL:: http://gandalf/svn/head/topaz/core/src/main/java/org/topazproject/otm/Abst#$
  $Id: AbstractConnection.java 4807 2008-02-27 11:06:12Z ronald $
  
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
	Role of ABO Secretor Status in Mucosal Innate Immunity and <em>H. pylori Infection</em>
	</a></li>
	
	<@s.url id="art2URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000085"/>
	<li><a href="${art2URL}" title="Read Open Access Article">
	Progression of <em>Plasmodium berghei</em> through Anopheles stephensi Is Density-Dependent
	</a></li>
	
	<@s.url id="art3URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000002"/>		
	<li><a href="${art3URL}" title="Read Open Access Article">
	Interfering Residues Narrow the Spectrum of MLV Restriction by Human TRIM5
	</a></li>
	
  <@s.url id="art4URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000075"/>			
	<li><a href="${art4URL}" title="Read Open Access Article">
	Convergent Use of RhoGAP Toxins by Eukaryotic Parasites and Bacterial Pathogens
	</a></li>
		
	<!-- Do not edit below this comment -->
	<@s.url action="browse" namespace="/article" field="date" includeParams="none" id="browseDateURL"/>
	<li class="more"><a href="${browseDateURL}">Browse all recently published articles</a></li>
</ul>
						
