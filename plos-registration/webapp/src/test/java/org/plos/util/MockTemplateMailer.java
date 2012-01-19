/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.plos.registration.User;
import org.plos.service.RegistrationMailer;

import java.util.Map;

public class MockTemplateMailer extends RegistrationMailer {
  public void mail(final String toEmailAddress, final String subject, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename) {
  }

  public void massMail(final Map<String, Map<String, Object>> emailAddressContextMap, final String subject, final String textTemplateFilename, final String htmlTemplateFilename) {
  }

  public void sendEmailAddressVerificationEmail(final User user) {
  }

  public void sendForgotPasswordVerificationEmail(final User user) {
  }

  public void setFreeMarkerConfigurer(final FreeMarkerConfigurer freeMarkerConfigurer) {
  }

  public void setFromEmailAddress(final String fromEmailAddress) {
  }

  public void setMailSender(final JavaMailSender mailSender) {
  }

  public void setTextTemplateFilename(final String textTemplateFilename) {
  }

  public void setHtmlTemplateFilename(final String htmlTemplateFilename) {
  }

  public void setVerifyEmailMap(final Map<String, String> verifyEmailMap) {
  }

  public Map<String, String> getVerifyEmailMap() {
    return null;
  }

  public void setForgotPasswordVerificationEmailMap(final Map<String, String> forgotPasswordVerificationEmailMap) {
  }

}
