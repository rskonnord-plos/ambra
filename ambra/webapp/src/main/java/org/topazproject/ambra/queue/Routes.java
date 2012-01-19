/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.topazproject.ambra.queue;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import static org.apache.camel.builder.xml.XPathBuilder.xpath;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.camel.LoggingLevel;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Dragisa Krsmanovic
 */
public class Routes extends SpringRouteBuilder {

  private static final Logger log = LoggerFactory.getLogger(Routes.class);

  private Configuration configuration;
  private long redeliveryInterval = 600l; // 10 minutes

  /**
   * <p>
   * Setter method for configuration. Injected through Spring.
   * </p>
   * <p>
   * Response queues are obtained from configuration file.
   * Beans that consume response queue are named <target_lowercase>ResponseConsumer and should already
   * be defined in Spring context. For examle for PMC, consumer bean is named "pmcResponseConsumer".
   * </p>
   * <p>
   * In addition to normal route, two routes for testing are configuret for each target:
   * <ol>
   *    <li>direct:test<target>Ok - loopback route that always returs success.</li>
   *    <li>direct:test<target>Fail - loopback route that always returns failuer.</li>
   * </ol>
   * </p>
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Redelivery interval. In case an error is thrown during processing of incomming message, how long do we wait
   * to retry processing.
   * @param redeliveryInterval Interval in seconds. Default is 600 (10 minutes).
   */
  public void setRedeliveryInterval(long redeliveryInterval) {
    this.redeliveryInterval = redeliveryInterval;
  }

  @SuppressWarnings("unchecked")
  public void configure() throws Exception {
    
    List<HierarchicalConfiguration> syndications = ((HierarchicalConfiguration) configuration)
        .configurationsAt("ambra.services.syndications.syndication");

    if (syndications != null) {

      onException(Exception.class)
          .handled(true)
          .retryAttemptedLogLevel(LoggingLevel.WARN)
          .retriesExhaustedLogLevel(LoggingLevel.ERROR)
          .maximumRedeliveries(-1) // redeliver forever
          .maximumRedeliveryDelay(redeliveryInterval * 1000l)
          .redeliverDelay(redeliveryInterval * 1000l);

      for (HierarchicalConfiguration syndication : syndications) {
        String target = syndication.getString("[@target]");
        String name = syndication.getString("name");
        String responseQueue = syndication.getString("responseQueue", null);
        log.info("Creating routes for " + name);

        String beanName = target.toLowerCase() + "ResponseConsumer";

        if (responseQueue != null) {
          log.info("Setting consumer for response queue " + responseQueue + " to " + beanName);
          from(responseQueue)
              .transacted()
              .to("bean:" + beanName);
        }

        // Test queue that always returns success
        String testOkQueue = "seda:test" + target + "Ok";
        log.info("Creating success test route to " + testOkQueue);
        from(testOkQueue)
            .to("log:org.topazproject.ambra.queue." + target + "TestMessageSent?level=INFO" +
                "&showBodyType=false" +
                "&showBody=true" +
                "&showExchangeId=true" +
                "&multiline=true")
            .setBody(xpath("/ambraMessage/doi", String.class))
            .setBody(body().append(constant("|OK")))
            .delay(5000l) // 5 sec delay
            .to("bean:"+beanName);

        // Test queue that always returns failure
        String testFailQueue = "seda:test" + target + "Fail";
        log.info("Creating failure test route to " + testFailQueue);
        from(testFailQueue)
            .to("log:org.topazproject.ambra.queue." + target + "TestMessageSent?level=INFO" +
                "&showBodyType=false" +
                "&showBody=true" +
                "&showExchangeId=true" +
                "&multiline=true")
            .setBody(xpath("/ambraMessage/doi", String.class))
            .setBody(body().append(constant("|FAILED|This is a test error")))
            .delay(5000l) // 5 sec delay
            .to("bean:"+beanName);
      }


    }
  }
}
