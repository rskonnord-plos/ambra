/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.ambra.email;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.IOException;
import java.util.Map;

/**
 * A contract for all template based emailers.
 */
public interface TemplateMailer {
  public static final String TO_EMAIL_ADDRESS = "toEmailAddress";
  public static final String USER_NAME_KEY = "name";

  /**
   * Helper method for creating Multiparts from a freemarker template for emailing
   *
   * @param textTemplateFilename the text freemarker template
   * @param htmlTemplateFilename the html freemarker template
   * @param context a {@link java.util.Map} of objects to expose to the template engine
   * @return the multipart content for a new email
   * @throws IOException
   * @throws MessagingException
   */
  public Multipart createContent(String textTemplateFilename, String htmlTemplateFilename,
                                 final Map<String, Object> context) throws IOException, MessagingException;

  /**
   * Send a mail with the content specified
   *
   * @param toEmailAddress the email address where to send the email
   * @param fromEmailAddress fromEmailAddress
   * @param subject subject of the email
   * @param context a {@link java.util.Map} of objects to expose to the template engine
   * @param content the content of the message to send
   */
  void mail(final String toEmailAddress, final String fromEmailAddress, final String subject,
            final Map<String, Object> context, final Multipart content);

  /**
   * Send a mail with both a text and a HTML version.
   * @param toEmailAddress the email address where to send the email
   * @param fromEmailAddress fromEmailAddress
   * @param subject subject of the email
   * @param context a {@link java.util.Map} of objects to expose to the template engine
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  void mail(final String toEmailAddress, final String fromEmailAddress, final String subject,
            final Map<String, Object> context, final String textTemplateFilename,
            final String htmlTemplateFilename);

  /**
   * Mail to multiple email addresses with both a text and a HTML version.
   * Each email comes with it's corresponding context to use for the templates
   * @param emailAddressContextMap email address and it's corresponding context
   * @param subject subject of the email
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  void massMail(final Map<String, Map<String, Object>> emailAddressContextMap, final String subject,
                final String textTemplateFilename, final String htmlTemplateFilename);
}
