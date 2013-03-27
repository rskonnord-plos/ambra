/*
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.routes;

import org.ambraproject.search.SavedSearchRetriever;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created with IntelliJ IDEA. User: stumu Date: 9/20/12 Time: 5:03 PM To change this template use File | Settings |
 * File Templates.
 */
public class SavedSearchEmailRoutes extends SpringRouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(SavedSearchEmailRoutes.class);
  private String weeklyCron;
  private String monthlyCron;

  public static final String SEARCH_ALERTS_QUEUE = "activemq:ambra.searchAlerts";

  @Override
  public void configure() throws Exception {

    //Weekly alert emails
    log.info("Setting Route for sending 'Weekly' saved search emails");

    from("quartz:ambra/savedsearch/weeklyemail?cron=" + weeklyCron)
      .setBody(constant(SavedSearchRetriever.AlertType.WEEKLY))
      .to(SEARCH_ALERTS_QUEUE);

    //Monthly alert emails
    log.info("Setting Route for sending 'Monthly' saved search emails");

    from("quartz:ambra/savedsearch/monthlyemail?cron=" + monthlyCron)
      .setBody(constant(SavedSearchRetriever.AlertType.MONTHLY))
      .to(SEARCH_ALERTS_QUEUE);

    from(SEARCH_ALERTS_QUEUE)
      .split().method("savedSearchRetriever", "retrieveSearchAlerts")
      .to("seda:runInParallel");

    //Hard coding this for 15 threads, can increase later if needed
    from("seda:runInParallel?concurrentConsumers=15")
      .to("bean:savedSearchRunner?method=runSavedSearch")
      .to("bean:savedSearchSender");
  }

  @Required
  public void setWeeklyCron(String weeklyCron) {
    this.weeklyCron = weeklyCron;
  }

  @Required
  public void setMonthlyCron(String monthlyCron) {
    this.monthlyCron = monthlyCron;
  }
}
