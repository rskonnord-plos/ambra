						<#assign numArticles = recentArticles?size>
						<#if (numArticles > 0)>
							<#assign randomIndices = action.randomNumbers(5, numArticles)>
							<ul class="articles">
							  <#list randomIndices as random>
									<#assign article = recentArticles[random]>
									<#if random_index % 2 == 0>
								<li class="even">
									<#else>
								<li>
									</#if>
									<@ww.url id="articleURL" includeParams="none" namespace="/article" action="fetchArticle" articleURI="${article.uri}"/>
									<a href="${articleURL}" title="Read Open Access Article">${article.title}</a>
								</li>
								</#list>
							</ul>
						</#if>
