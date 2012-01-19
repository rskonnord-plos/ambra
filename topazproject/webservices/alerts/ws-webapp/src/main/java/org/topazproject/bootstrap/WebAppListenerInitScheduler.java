/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.bootstrap;

import java.util.List;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationMap;
import org.topazproject.configuration.ConfigurationStore;

import org.quartz.impl.StdSchedulerFactory;
import org.quartz.Scheduler;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;
import org.quartz.CronTrigger;
import org.quartz.SchedulerException;

/**
 * A listener class for web-apps to initialize things at startup.
 *
 * This class will call other ServletContextListeners configured.
 *
 * @author Eric Brown
 */
public class WebAppListenerInitScheduler implements ServletContextListener {
  private static Log log = LogFactory.getLog(WebAppListenerInitScheduler.class);
  private static Scheduler scheduler = null;

  /**
   * Shutdown the scheduler.
   *
   * @param event the destryed event
   */
  public void contextDestroyed(ServletContextEvent event) {
    try {
      scheduler.shutdown();
      log.info("Shutdown scheduler");
    } catch (SchedulerException se) {
      log.warn("Problem shutting down scheduler", se);
    }
  }

  /**
   * Initialize an L{org.quarts.Scheduler} object.
   *
   * Configuration information is read from topaz configuration as:
   * <pre>
   *   <topaz>
   *     <scheduler>
   *       <config>
   *         <property>key1=value1</property>
   *         <property>key2=value2</property>
   *       ...
   * </pre>
   *
   * Job information is also read from topaz configuration as:
   * <pre>
   *   <topaz>
   *     <scheduler>
   *       <jobs>
   *         <job name="jobname" group="jobgroup">
   *           <class>org.topazproject...</class>
   *           <cron>0 0 12 ? * SUN</cron>
   *           ...
   * </pre>
   *
   * @param event destroyed event
   */
  public synchronized void contextInitialized(ServletContextEvent event) {
    try {
      Configuration conf = ConfigurationStore.getInstance().getConfiguration();
      
      if (scheduler != null) {
        log.info("Scheduler already initialized.");
      } else {
        // Start scheduler
        Properties quartzConf = conf.getProperties("topaz.scheduler.config.property");
        scheduler = new StdSchedulerFactory(quartzConf).getScheduler();
        scheduler.start();
        log.info("Initialized scheduler");
      }

      // Lookup Jobs
      conf = conf.subset("topaz.scheduler.jobs");
      List classList = conf.getList("job.class");
      log.info("Scheduling " + classList.size() + " jobs");
      for (int i = 0; i < classList.size(); i++) {
        try {
          Configuration job   = conf.subset("job(" + i + ")");
          String name         = job.getString("[@name]", "default" + i);
          String group        = job.getString("[@group]", "default");
          String className    = job.getString("class");
          String cronExpr     = job.getString("cron");
          JobDataMap dataMap  = new JobDataMap(new ConfigurationMap(job.subset("datamap")));
        
          CronTrigger trigger = new CronTrigger(name, group, cronExpr);
          JobDetail jobDetail = new JobDetail(name, group, Class.forName(className));
          jobDetail.setJobDataMap(dataMap);
          scheduler.scheduleJob(jobDetail, trigger);
          log.info("Scheduled job " + name + " (" + className + ") " + cronExpr);
        } catch (SchedulerException se) {
          log.warn("Unable to schedule " + classList.get(i), se);
        }
      }
    } catch (Exception e) {
      log.warn("Unable to start scheduler", e);
    }
  }
}
