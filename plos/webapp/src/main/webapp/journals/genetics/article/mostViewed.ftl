<!-- Featured Comments -->
<ul class="articles">
	<@s.url id="art1URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000000"/>	
	<li><a href="${art1URL}" title="Read Open Access Article">
	This is the mostViewed FTL!
	</a></li>
	
	<@s.url id="art2URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000085"/>
	<li><a href="${art2URL}" title="Read Open Access Article">
	Exerci eu Enim, Imputo Indoles Commodo Valde, Comis Verto
	</a></li>
	
	<@s.url id="art3URL" namespace="/article" action="fetchArticle" includeParams="none" articleURI="info:doi/10.1371/journal.pntd.0000002"/>		
	<li><a href="${art3URL}" title="Read Open Access Article">
	Exerci eu Enim, Imputo Indoles Commodo Valde, Comis Verto
	</a></li>
	
	<!-- Do not edit below this comment -->
	<@s.url action="commentGuidelines" namespace="/static" includeParams="none" id="comment"/>
	<li class="more">Learn how to <a href="${comment}">add comments and start discussions</a> on articles.</li>
</ul>
