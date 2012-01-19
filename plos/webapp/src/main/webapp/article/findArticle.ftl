<!-- begin : main contents wrapper -->
<div id="content" class="static">

<#if Parameters.title?exists>
	<#assign title = Parameters.title>
<#else>
	<#assign title = "">
</#if>

<#if Parameters.author?exists>
	<#assign author = Parameters.author>
<#else>
	<#assign author= "">
</#if>

		<h1>Find this article online</h1>
		<h2>${title}</h2>
		<p>The article may exist at:</p>
		<ul>
		<li><a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=PubMed&cmd=Search&doptcmdl=Citation&defaultField=Title+Word&term=${author}%5Bauthor%5D+AND+${title?html}"
		 onclick="window.open(this.href, 'PLoSFindArticle','');return false;" title="Go to article in PubMed" class="ncbi icon">PubMed/NCBI</a></li>
		<li><a href="http://scholar.google.com/scholar?hl=en&safe=off&q=author%3A${author}+%22${title}%22"
				 onclick="window.open(this.href, 'PLoSFindArticle','');return false;" title="Go to article in Google Scholar" class="google icon">Google Scholar</a></li>	
		</ul>
		<a href="#" onClick="history.back();return false;" class="article icon">Back to article</a>
</div>