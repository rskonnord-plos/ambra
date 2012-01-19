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

import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import java.util.Calendar;
import java.util.Collection;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.mail.EmailException;

import org.topazproject.configuration.ConfigurationStore;
import org.topazproject.feed.ArticleFeed;

/**
 * Collection of helpers we use in AlertsImpl.
 *
 * @author Eric Brown
 */
abstract class AlertsHelper {
  private static final Configuration CONF  = ConfigurationStore.getInstance().getConfiguration();
  
  private static final String TRANSFORM_RESOURCE = CONF.getString("topaz.alerts.xsl", "/email.xsl");
  private static final String SMTP_HOSTNAME = CONF.getString("topaz.alerts.relayhost", "localhost");
  private static final int    SMTP_PORT = CONF.getInt("topaz.alerts.relayport", 2525);

  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  private static final Log log = LogFactory.getLog(AlertsImpl.class);

  private static Transformer transformer;

  static {
    try {
      TransformerFactory factory = TransformerFactory.newInstance();
      StreamSource source = new StreamSource(
        AlertsHelper.class.getResourceAsStream(TRANSFORM_RESOURCE));
      transformer = factory.newTransformer(source);
    } catch (TransformerConfigurationException tce) {
      log.warn("Unable to create alerts XSL transformer", tce);
    }
  }

  /**
   * Get YYYY-MM-DD formatted timestamp from Calendar instance.
   */
  static String getTimestamp(Calendar c) {
    return sdf.format(c.getTime());
  }

  /**
   * Roll a Calendar instance days forward or backward.
   */
  static void rollCalendar(Calendar c, int amount) {
    c.setTimeInMillis(c.getTimeInMillis() + amount * 86400000L);
  }

  /**
   * Roll a timestamp string days forward or backward.
   */
  static String rollTimestamp(String stamp, int amount) {
    assert stamp != null;
    try {
      Calendar c = Calendar.getInstance();
      c.setTime(sdf.parse(stamp));
      rollCalendar(c, amount);
      return sdf.format(c.getTime());
    } catch (ParseException pe) {
      log.warn("Unexpected problem parsing timestamp: " + stamp, pe);
      return stamp;
    }
  }

  /**
   * Determine if a date is between two other dates.
   *
   * @param date is the date to test
   * @param startDate is the date to start searching from. If empty, start from begining of time
   * @param endDate is the date to search until. If empty, search until prsent date
   * @return true if the date is between startDate and endDate inclusive.
   */
  static boolean isDateInRange(String date, String startDate, String endDate) {
    if (startDate != null && startDate.length() > 0 && date.compareTo(startDate) < 0)
      return false;
    if (endDate != null && endDate.length() > 0 && date.compareTo(endDate) > 0)
      return false;
    return true;
  }

  /**
   * Transform xml intended for RSS feed into the body of an email.
   *
   */
  static Email getEmail(Collection articles) throws AlertsGenerationException, EmailException {
    String xml = ArticleFeed.buildXml(articles);
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream(250);
      transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(os));

      SimpleEmail email = new SimpleEmail();
      email.setMsg(os.toString("UTF-8"));

      return email;
    } catch (TransformerException te) {
      log.info("Articles xml: " + xml);
      throw new AlertsGenerationException("Unable to generate email", te);
    } catch (UnsupportedEncodingException uee) {
      throw new AlertsGenerationException("Unable to convert email text to UTF-8", uee);
    }
  }

  /**
   * Send an email message.
   */
  static void sendEmail(Email email) throws EmailException {
    email.setHostName(SMTP_HOSTNAME);
    email.setSmtpPort(SMTP_PORT);
    email.send();
  }
}
