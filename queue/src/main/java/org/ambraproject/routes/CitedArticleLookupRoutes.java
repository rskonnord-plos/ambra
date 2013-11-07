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
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.cottagelabs.CottageLabsLicenseService;
import org.ambraproject.service.cottagelabs.json.Identifier;
import org.ambraproject.service.cottagelabs.json.Processing;
import org.ambraproject.service.cottagelabs.json.Response;
import org.ambraproject.service.cottagelabs.json.Error;
import org.ambraproject.service.cottagelabs.json.Result;
import org.apache.camel.Exchange;
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
  public static final String UPDATE_CITED_ARTICLE_QUEUE = "activemq:plos.updateArticleCites";
  public static final String UPDATE_CITED_ARTICLE = "seda:plos.updatedCitedArticles";
  public static final String UPDATE_CITED_ARTICLE_LICENSE = "seda:plos.updateCitedArticleLicense";
  public static final String UPDATE_CITED_ARTICLE_LICENSE_RETRY = "seda:plos.updateCitedArticleLicense.retry";

  //public static final int LICENSE_RETRY_DELAY = 300000; //Set to retry every 5 minutes
  public static final int LICENSE_RETRY_DELAY = 15000; //Set to retry every 15 seconds
  public static final int QUEUE_CONSUMERS = 10; //Set to run 10 consumers in parallel

  private static final Logger log = LoggerFactory.getLogger(CitedArticleLookupRoutes.class);

  @Override
  public void configure() throws Exception {
    log.info("Setting up route for looking up cross ref DOIS");

    //Route for updating all the citedArticle DOIs for an article
    //Requires articleDoi as the body and authId set on the header
    from(UPDATE_CITED_ARTICLE_QUEUE)
      //Immediately pass all messages off to a route with multiple consumers
      .to(UPDATE_CITED_ARTICLE);

    from(UPDATE_CITED_ARTICLE_QUEUE + "?concurrentConsumers=" + QUEUE_CONSUMERS)
      //Refresh all cited article DOIs for a given articleID
      .to("bean:articleService?method=refreshArticleCiteDOIs(${body}, ${headers." + HEADER_AUTH_ID + "})")
      .to(UPDATE_CITED_ARTICLE_LICENSE);

    from(UPDATE_CITED_ARTICLE_LICENSE + "?concurrentConsumers=" + QUEUE_CONSUMERS)
      .process(new Processor() {
        /**
         * Request the license data
         *  For the items that return a 'success' result
         *    Store
         *  For the items that return a 'error' result
         *    abort and log
         *  For the items that are still in a 'processing' state
         *    Pause N time
         *    Requeue
         *
         * @param exchange
         *
         * @throws Exception
         */
        @Override
        public void process(Exchange exchange) throws Exception {
          ArticleService articleService = (ArticleService)getApplicationContext().getBean("articleService");
          CottageLabsLicenseService clService = (CottageLabsLicenseService)getApplicationContext().getBean("cottageLabsLicenseService");

          String articleDoi = (String)exchange.getIn().getBody();
          String authID = (String)exchange.getIn().getHeader(HEADER_AUTH_ID);
          Article article = articleService.getArticle(articleDoi, authID);

          List<String> DOIs = new ArrayList<String>();

          for(int a = 0; a < article.getCitedArticles().size(); a++) {
            String doi = article.getCitedArticles().get(a).getDoi();

            if(doi != null) {
              DOIs.add(doi);
            }
          }

          Response response = clService.findLicenses(DOIs.toArray(new String[DOIs.size()]));
          String[] retryDOIs = processCottageLabsResponse(response);

          exchange.getOut().setBody(retryDOIs);
        }
      })
      .choice()
        .when(body().isNotNull())
          //If the header value was not null, there are still DOIs to retry
          .delay(LICENSE_RETRY_DELAY)
          .to(UPDATE_CITED_ARTICLE_LICENSE_RETRY);

    from(UPDATE_CITED_ARTICLE_LICENSE_RETRY + "?concurrentConsumers=" + QUEUE_CONSUMERS)
      .process(new Processor() {
        @Override
        public void process(Exchange exchange) throws Exception {
          String[] DOIs = (String[])exchange.getIn().getBody();

          CottageLabsLicenseService clService = (CottageLabsLicenseService)getApplicationContext().getBean("cottageLabsLicenseService");

          Response response = clService.findLicenses(DOIs);
          String[] retryDOIs = processCottageLabsResponse(response);

          exchange.getOut().setBody(retryDOIs);
        }
      })
      .choice()
        .when(body().isNotNull())
          //If the header value was not null, there are still DOIs to retry
          .delay(LICENSE_RETRY_DELAY)
          .to(UPDATE_CITED_ARTICLE_LICENSE_RETRY);
  }

  /**
   * Process the response from cottage labs
   *
   * @param response the marshalled response from the cottage labs API
   *
   * @return an array of DOIS that are still in process or null if there are none
   */
  private String[] processCottageLabsResponse(Response response) {
    //Log errors
    logErrors(response.getErrors());

    //Store results
    storeResults(response.getResults());

    //Get results that are still in process
    String[] retryDOIs = getDOISinProcess(response.getProcessing());

    //COPY results to outgoing message
    if(retryDOIs != null && retryDOIs.length > 0) {
      return retryDOIs;
    } else {
      return null;
    }
  }

  /**
   * Store results from cottagelabs
   * @param results
   */
  private void storeResults(List<Result> results) {
    for(Result result : results) {
      log.debug("Received CottageLabs License for {}, \"{}\"", new Object[] {
        result.getIdentifier().get(0).getId(),
        result.getLicense().get(0).getTitle()
      });

      //TODO: Store this someplace
    }
  }

  /**
   * log any errors to the log
   *
   * @param errors
   */
  private static void logErrors(List<Error> errors) {
    for(Error error : errors) {
      log.error("Error received from cottage labs for DOI: {}, Error \"{}\"", new Object[] {
        error.getIdentifer(),
        error.getError()
      });
    }
  }

  /**
   * For the given response, get all the DOIS that are still in process
   *
   * @param processing the marshalled response from the cottage labs API
   *
   * @return
   */
  private static String[] getDOISinProcess(List<Processing> processing) {
    String[] retryDOIs = new String[processing.size()];
    for(int a = 0; a < processing.size(); a++) {
      retryDOIs[a] = processing.get(a).getIdentifier().getId();
    }

    log.info("CottageLabs DOIS still in process {}", retryDOIs.length);

    if(log.isDebugEnabled()) {
      for(String doi : retryDOIs) {
        log.debug("CottageLab DOI Still in process: {}", doi);
      }
    }

    return retryDOIs;
  }
}

