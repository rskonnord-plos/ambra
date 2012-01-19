						<#assign commentedArticles = action.getCommentedOnArticles(5)>
						<#if (((commentedArticles?size)!0) gt 0) >
							<ul class="articles">
								<#list commentedArticles as commented>
								<li>
  								<@ww.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${commented.uri}"/>
									<a href="${articleURL}" title="Read Open Access Article">${commented.title}</a>
								</li>
								</#list>
							</ul>
						</#if>