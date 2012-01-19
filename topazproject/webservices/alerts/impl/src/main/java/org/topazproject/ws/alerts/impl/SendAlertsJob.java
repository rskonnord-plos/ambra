/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.alerts.impl;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.configuration.Configuration;
import org.topazproject.configuration.ConfigurationStore;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendAlertsJob implements Job {
  private static Log log = LogFactory.getLog(SendAlertsJob.class);
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    log.info("Sending all alerts");

    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    String mulgaraUriString = conf.getString("topaz.services.itql.uri");
    if (mulgaraUriString == null) {
      log.warn("Unable to find mulgara in config");
      return;
    }

    try {
      URI mulgaraUri = URI.create(mulgaraUriString);
      AlertsImpl impl = new AlertsImpl(mulgaraUri);
      impl.sendAllAlerts();
    } catch (Exception e) {
      log.warn("Error sending alerts", e);
    }
    
    log.info("Done sending all alerts");
  }
}
