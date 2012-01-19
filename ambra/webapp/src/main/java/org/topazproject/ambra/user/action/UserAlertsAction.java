/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.ambra.user.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.user.service.UserAlert;

/**
 * Update action for saving or getting alerts that the user subscribes to.
 */
@SuppressWarnings("serial")
public abstract class UserAlertsAction extends UserActionSupport {

  private static final Log log = LogFactory.getLog(UserAlertsAction.class);

  /**
   * The list of available {@link UserAlert}s as prescribed in the configuration. May be empty.
   */
  private static final List<UserAlert> userAlerts = new ArrayList<UserAlert>();

  /**
   * @return All available user alerts
   */
  public static Collection<UserAlert> getUserAlerts() {
    return userAlerts;
  }

  /**
   * Stubs optional user alert data data which may be specified in the configuration. <br>
   *
   * Config FORMAT EXAMPLE:<br>
   *
   * <pre>
   * &lt;userAlerts&gt;
   *         &lt;categories&gt;
   *           &lt;category key=&quot;biology&quot;&gt;PLoS Biology&lt;/category&gt;
   *           &lt;category key=&quot;computational_biology&quot;&gt;PLoS Computational Biology&lt;/category&gt;
   *           &lt;category key=&quot;clinical_trials&quot;&gt;PLoS Hub for Clinical Trials&lt;/category&gt;
   *           &lt;category key=&quot;genetics&quot;&gt;PLoS Genetics&lt;/category&gt;
   *           &lt;category key=&quot;medicine&quot;&gt;PLoS Medicine&lt;/category&gt;
   *           &lt;category key=&quot;pathogens&quot;&gt;PLoS Pathogens&lt;/category&gt;
   *           &lt;category key=&quot;plosntds&quot;&gt;PLoS Neglected Tropical Diseases&lt;/category&gt;
   *           &lt;category key=&quot;plosone&quot;&gt;PLoS ONE&lt;/category&gt;
   *         &lt;/categories&gt;
   *         &lt;monthly&gt;biology, clinical_trials, computational_biology, genetics, medicine, pathogens, plosntds&lt;/monthly&gt;
   *         &lt;weekly&gt;biology, clinical_trials, computational_biology, genetics, medicine, pathogens, plosntds, plosone&lt;/weekly&gt;
   *       &lt;/userAlerts&gt;
   * </pre>
   */
  @SuppressWarnings("unchecked")
  private static void initUserAlertData() {

    final Map<String, String> categoryNames = new HashMap<String, String>();

    ConfigurationStore config = ConfigurationStore.getInstance();
    HierarchicalConfiguration hc = (HierarchicalConfiguration) config.getConfiguration();
    List<HierarchicalConfiguration> categories = hc
        .configurationsAt("ambra.userAlerts.categories.category");
    for (HierarchicalConfiguration c : categories) {
      String key = c.getString("[@key]");
      String value = c.getString("");
      categoryNames.put(key, value);
    }

    final String[] weeklyCategories = hc.getStringArray("ambra.userAlerts.weekly");
    final String[] monthlyCategories = hc.getStringArray("ambra.userAlerts.monthly");

    final Set<Map.Entry<String, String>> categoryNamesSet = categoryNames.entrySet();

    for (final Map.Entry<String, String> category : categoryNamesSet) {
      final String key = category.getKey();
      boolean weeklyCategoryKey = false;
      boolean monthlyCategoryKey = false;
      if (ArrayUtils.contains(weeklyCategories, key)) {
        weeklyCategoryKey = true;
      }
      if (ArrayUtils.contains(monthlyCategories, key)) {
        monthlyCategoryKey = true;
      }
      userAlerts
          .add(new UserAlert(key, category.getValue(), weeklyCategoryKey, monthlyCategoryKey));
    }
  }

  static {
    initUserAlertData();
  }

  private String displayName;
  private String[] monthlyAlerts = new String[]{};
  private String[] weeklyAlerts = new String[]{};
  private final String MONTHLY_ALERT_SUFFIX = "_monthly";
  private final String WEEKLY_ALERT_SUFFIX = "_weekly";

  /**
   * Save the alerts.
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String saveAlerts() throws Exception {
    final AmbraUser ambraUser = getAmbraUserToUse();
    if (ambraUser == null) {
      throw new ServletException("Unable to resolve ambra user");
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
    ambraUser.setAlerts(alerts);

    getUserService().setPreferences(ambraUser);

    return SUCCESS;
  }

  /**
   * Retrieve the alerts for the logged in user
   * @return webwork status
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String retrieveAlerts() throws Exception {
    final AmbraUser ambraUser = getAmbraUserToUse();
    if (ambraUser == null) {
      throw new ServletException("Unable to resolve ambra user");
    }

    final Collection<String> monthlyAlertsList = new ArrayList<String>();
    final Collection<String> weeklyAlertsList = new ArrayList<String>();

    final String[] alerts = ambraUser.getAlerts();

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

    setDisplayName(ambraUser.getDisplayName());
    return SUCCESS;
  }

  /**
   * Provides a way to get the AmbraUser to edit
   * @return the AmbraUser to edit
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  protected abstract AmbraUser getAmbraUserToUse() throws ApplicationException;

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
