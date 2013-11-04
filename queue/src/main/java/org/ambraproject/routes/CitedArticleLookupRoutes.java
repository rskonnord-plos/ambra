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

import org.ambraproject.models.Article;
import org.ambraproject.models.CitedArticle;
import org.ambraproject.service.article.ArticleService;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joe Osowski
 *
 * Camel routes for looking up crossref cited articles
 */
public class CitedArticleLookupRoutes extends SpringRouteBuilder {
  /**
   * The key to fetch the value for the authorization ID for the given request in the
   * header
   */
  public static final String HEADER_AUTH_ID = "authId";
  public static final String UPDATE_CITED_ARTICLE_QUEUE = "activemq:plos.updateCitedArticle";
  public static final String UPDATE_CITED_ARTICLE_LICENSE_QUEUE = "activemq:plos.updateCitedArticleLicense";
  public static final String UPDATE_CITED_ARTICLES_QUEUE = "activemq:plos.updatedCitedArticles";

  public static final String HEADER_LICENSE_RESPONSE = "licenseResponse";
  public static final int LICENSE_RETRY = 30000; //Set to retry every 30 seconds

  private static final Logger log = LoggerFactory.getLogger(CitedArticleLookupRoutes.class);

  @Override
  public void configure() throws Exception {
    log.info("Setting up route for looking up cross ref DOIS");

    //Route for updating all the citedArticles for an article
    //Requires articleDoi as the body and authId set on the header
    from(UPDATE_CITED_ARTICLES_QUEUE)
      .to("bean:articleService?method=getArticle(${body}, ${headers." + HEADER_AUTH_ID + "})")
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          //exchange.getContext().getSe
          //All we care about is the citedArticle IDs list
          List<CitedArticle> citedArticles = exchange.getIn().getBody(Article.class).getCitedArticles();
          List<Long> ids = new ArrayList<Long>();

          for (CitedArticle ca : citedArticles) {
            ids.add(ca.getID());
          }

          exchange.getOut().setBody(ids);
        }
      })
      .split().body() //Create a job for each CitedArticle
      .to(UPDATE_CITED_ARTICLE_QUEUE);


    //Route for updating one citedArticle
    //TODO: Make this multi threaded?
    from(UPDATE_CITED_ARTICLE_QUEUE)
      //TODO: Rename this method:
      .to("bean:articleService?method=refreshCitedArticleDOI")
      .choice()
        .when(body().isNotNull())
          .log(LoggingLevel.DEBUG, "DOI Found ${body}")
          .to(UPDATE_CITED_ARTICLE_LICENSE_QUEUE);

    //Request the license data
      //If the job returns a 'success' result
        //Do nothing
      //If the job returns a 'error' result
        //abort and log
      //If the job returns a 'processing' result
        //Pause N time
        //Requeue
    from(UPDATE_CITED_ARTICLE_LICENSE_QUEUE)
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          ArticleService articleService = (ArticleService)getApplicationContext().getBean("articleService");

          String doi = (String)exchange.getIn().getBody();
          String result = articleService.refreshCitedArticleLicense(doi);

          log.debug("Result received: {}, {}", new Object[] { result, doi });

          //COPY DOI and result to outgoing message
          Message message = exchange.getOut();
          message.setHeader(HEADER_LICENSE_RESPONSE, result);
          message.setBody(exchange.getIn().getBody());
        }
      })
      .choice()
        .when(header(HEADER_LICENSE_RESPONSE).isEqualTo(ArticleService.LICENSE_RESPONSE_SUCCESS))
          .log(LoggingLevel.DEBUG, "License for DOI Found ${body}")
        .when(header(HEADER_LICENSE_RESPONSE).isEqualTo(ArticleService.LICENSE_RESPONSE_FAILURE))
          .log(LoggingLevel.DEBUG, "License for ERROR ${body}")
        .when(header(HEADER_LICENSE_RESPONSE).isEqualTo(ArticleService.LICENSE_RESPONSE_PROCESSING))
          //If the result is a "still processing" status, re-queue the job
          .log(LoggingLevel.DEBUG, "DOI still processing")
          //Delay 30 seconds
          .delay(LICENSE_RETRY)
          .asyncDelayed() //TODO: Do I really need this?
          .to(UPDATE_CITED_ARTICLE_LICENSE_QUEUE);
  }
}
