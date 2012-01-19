/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.email;

/**
 * Mailer User. Has properties that are required for emailing to a given user.
 */
public interface MailerUser {
  /**
   * @return the user's email address
   */
  String getEmailAddress();
}
