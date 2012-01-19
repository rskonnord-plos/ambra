/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos.email;

import java.util.Map;

/**
 * A contract for all template based emailers.
 */
public interface TemplateMailer {

  /**
   * Send a mail with both a text and a HTML version.
   * @param toEmailAddress          the email address where to send the email
   * @param fromEmailAddress fromEmailAddress
   * @param subject subject of the email
   * @param context        a {@link java.util.Map} of objects to expose to the template engine
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  void mail(final String toEmailAddress, final String fromEmailAddress, final String subject, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename);

  /**
   * Mail to multiple email addresses with both a text and a HTML version.
   * Each email comes with it's corresponding context to use for the templates
   * @param emailAddressContextMap email address and it's corresponding context
   * @param subject subject of the email
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  void massMail(final Map<String, Map<String, Object>> emailAddressContextMap, final String subject, final String textTemplateFilename, final String htmlTemplateFilename);
}