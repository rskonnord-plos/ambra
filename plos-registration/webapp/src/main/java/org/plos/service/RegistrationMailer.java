/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.email.impl.FreemarkerTemplateMailer;
import org.plos.registration.User;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

public class RegistrationMailer extends FreemarkerTemplateMailer {
  private Map<String, String> verifyEmailMap;
  private Map<String, String> forgotPasswordVerificationEmailMap;
  private static final Log log = LogFactory.getLog(RegistrationMailer.class);

  
  /**
   * Setter for verifyEmailMap.
   * @param verifyEmailMap verifyEmailMap
   */
  public void setVerifyEmailMap(final Map<String, String> verifyEmailMap) {
    this.verifyEmailMap = Collections.unmodifiableMap(verifyEmailMap);
  }

  /**
   * Setter for forgotPasswordVerificationEmailMap.
   * @param forgotPasswordVerificationEmailMap Value to set for forgotPasswordVerificationEmailMap.
   */
  public void setForgotPasswordVerificationEmailMap(final Map<String, String> forgotPasswordVerificationEmailMap) {
    this.forgotPasswordVerificationEmailMap = forgotPasswordVerificationEmailMap;
  }

  /**
   * Send a email address verification email to the user
   * @param user user
   */
  public void sendEmailAddressVerificationEmail(final User user) {
    final Map<String, Object> newMapFields = new HashMap<String, Object>();
    newMapFields.putAll(verifyEmailMap);
    newMapFields.put("user", user);
    newMapFields.put("name", getFromEmailName());
    if (log.isDebugEnabled()) {
      log.debug("sending email address verification for " + ((user != null) ? user.getLoginName() : null));
    }
    sendEmail(user.getLoginName(), newMapFields);
  }

  /**
   * Send a forgot password verification email to the user
   * @param user user
   */
  public void sendForgotPasswordVerificationEmail(final User user) {
    final Map<String, Object> newMapFields = new HashMap<String, Object>();
    newMapFields.putAll(forgotPasswordVerificationEmailMap);
    newMapFields.put("user", user);
    newMapFields.put("name", getFromEmailName());
    if (log.isDebugEnabled()) {
      log.debug("sending forgot password email for " + ((user != null) ? user.getLoginName() : null));
    }
    sendEmail(user.getLoginName(), newMapFields);
  }
}
