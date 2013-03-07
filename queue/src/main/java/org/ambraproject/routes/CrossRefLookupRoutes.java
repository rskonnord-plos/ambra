/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.routes;

import org.ambraproject.models.CitedArticle;
import org.ambraproject.views.article.ArticleInfo;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Joe Osowski
 *
 * Camel routes for looking up crossref cited articles
 */
public class CrossRefLookupRoutes extends SpringRouteBuilder {
  private static final Logger log = LoggerFactory.getLogger(CrossRefLookupRoutes.class);

  @Override
  public void configure() throws Exception {
    log.info("Setting up route for looking up cross ref DOIS");

    //Route for updating all the citedArticles for an article
    //Requires articleDoi as the body and authId set on the header
    from("direct:updatedCitedArticles")
      .beanRef("articleService", "getArticleInfo(${body}, ${headers.authId})")
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          //All we care about is the citedArticleCollection
          exchange.getOut().setBody(
            exchange.getIn().getBody(ArticleInfo.class).getCitedArticles());
        }
      })
      //Create a job for each CitedArticle
      .split().body()
      .to("direct:updateCitedArticle");

    //Route for updating one citedArticle
    from("direct:updateCitedArticle")
      .beanRef("articleService", "refreshCitedArticle");
  }
}
