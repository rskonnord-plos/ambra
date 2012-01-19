/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.user.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.user.PlosOneUser;
import org.plos.user.service.CategoryBean;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Update action for saving or getting alerts that the user subscribes to.
 */
public abstract class UserAlertsAction extends UserActionSupport {
  private String displayName;
  private String[] monthlyAlerts = new String[]{};
  private String[] weeklyAlerts = new String[]{};
  private final String MONTHLY_ALERT_SUFFIX = "_monthly";
  private final String WEEKLY_ALERT_SUFFIX = "_weekly";
  private static final Log log = LogFactory.getLog(UserAlertsAction.class);

  /**
   * Save the alerts.
   * @return webwork status
   * @throws Exception Exception
   */
  public String saveAlerts() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserToUse();
    if (log.isDebugEnabled()) {
      if (plosOneUser != null) {
        log.debug("plosuser authID = " + plosOneUser.getAuthId());
        log.debug("plosuser email = " + plosOneUser.getEmail());
        log.debug("plosuser userID = " + plosOneUser.getUserId());
      }
    }

    final Collection<String> alertsList = new ArrayList<String>();
    for (final String alert : monthlyAlerts) {
      if (log.isDebugEnabled()) {
        log.debug("found monthly alert: " + alert);
      }
      alertsList.add(alert + MONTHLY_ALERT_SUFFIX);
    }
    for (final String alert : weeklyAlerts) {
      if (log.isDebugEnabled()) {
        log.debug("found weekly alert: " + alert);
      }
      alertsList.add(alert + WEEKLY_ALERT_SUFFIX);
    }

    final String[] alerts = alertsList.toArray(new String[alertsList.size()]);
    plosOneUser.setAlerts(alerts);

    getUserService().setPreferences(plosOneUser);

    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   * @return webwork status
   * @throws Exception Exception
   */
  public String retrieveAlerts() throws Exception {
    final PlosOneUser plosOneUser = getPlosOneUserToUse();
    final Collection<String> monthlyAlertsList = new ArrayList<String>();
    final Collection<String> weeklyAlertsList = new ArrayList<String>();

    final String[] alerts = plosOneUser.getAlerts();

    if (log.isDebugEnabled()) {
      log.debug("plosuser authID = " + plosOneUser.getAuthId());
      log.debug("plosuser email = " + plosOneUser.getEmail());
      log.debug("plosuser userID = " + plosOneUser.getUserId());
    }

    if (null != alerts) {
      for (final String alert : alerts) {
        if (log.isDebugEnabled()) {
          log.debug("Alert: " + alert);
        }
        if (alert.endsWith(MONTHLY_ALERT_SUFFIX)) {
          monthlyAlertsList.add(alert.substring(0, alert.indexOf(MONTHLY_ALERT_SUFFIX)));
        } else if (alert.endsWith(WEEKLY_ALERT_SUFFIX)) {
          weeklyAlertsList.add(alert.substring(0, alert.indexOf(WEEKLY_ALERT_SUFFIX)));
        }
      }
    }

    monthlyAlerts = monthlyAlertsList.toArray(new String[monthlyAlertsList.size()]);
    weeklyAlerts = weeklyAlertsList.toArray(new String[weeklyAlertsList.size()]);

    setDisplayName(plosOneUser.getDisplayName());
    return SUCCESS;
  }

  /**
   * Provides a way to get the PlosOneUser to edit
   * @return the PlosOneUser to edit
   * @throws org.plos.ApplicationException ApplicationException
   */
  protected abstract PlosOneUser getPlosOneUserToUse() throws ApplicationException;

  /**
   * @return categories that have monthly alerts
   */
  public String[] getMonthlyAlerts() {
    return monthlyAlerts;
  }

  /**
   * Set the categories that have monthly alerts
   * @param monthlyAlerts monthlyAlerts
   */
  public void setMonthlyAlerts(final String[] monthlyAlerts) {
    this.monthlyAlerts = monthlyAlerts;
  }

  /**
   * @return weekly alert categories
   */
  public String[] getWeeklyAlerts() {
    return weeklyAlerts;
  }

  /**
   * Set weekly alert categories
   * @param weeklyAlerts weeklyAlerts
   */
  public void setWeeklyAlerts(String[] weeklyAlerts) {
    this.weeklyAlerts = weeklyAlerts;
  }

  /**
   * @return all the category beans
   */
  public Collection<CategoryBean> getCategoryBeans() {
    return getUserService().getCategoryBeans();
  }

  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName The displayName to set.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
