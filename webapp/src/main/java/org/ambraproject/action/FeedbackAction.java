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
package org.ambraproject.action;

import org.ambraproject.models.UserProfile;
import org.ambraproject.service.captcha.CaptchaService;
import org.ambraproject.service.mailer.AmbraMailer;
import org.ambraproject.action.user.UserActionSupport;
import org.apache.commons.collections.EnumerationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Send a feedback fromEmail.
 */
public class FeedbackAction extends UserActionSupport {
  private String page;
  private String fromEmailAddress;
  private String note;
  private String subject;
  private String name;
  private AmbraMailer ambraMailer;
  private CaptchaService captchaService;
  private String captchaHTML;
  private String captchaChallenge;
  private String captchaResponse;

  private String userID;

  public final String FROM_EMAIL_ADDRESS_KEY = "fromEmailAddress";

  /**
   * Render the page with the values passed in
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeRender() throws Exception {
    setUserDetailsFromSession();
    setNewReCaptcha();
    return SUCCESS;
  }

  private void setUserDetailsFromSession() {
    final UserProfile ambraUser = getCurrentUser();
    if (null != ambraUser) {
      name = ambraUser.getDisplayName();
      fromEmailAddress = ambraUser.getEmail();
      userID = String.valueOf(ambraUser.getID());
    }
  }

  /**
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeSend() throws Exception {
    if (!validates()) return INPUT;

    final Map<String, Object> mapFields = new HashMap<String, Object>();
    mapFields.put("page", page);
    mapFields.put("subject", subject);
    mapFields.put("name", name);
    mapFields.put(FROM_EMAIL_ADDRESS_KEY, fromEmailAddress);
    mapFields.put("note", note);
    setUserDetailsFromSession();
    mapFields.put("id", StringUtils.defaultString(userID, "not found"));

    final Map<String, String> attributes = getUserSessionAttributes();
    final List<String> values = new ArrayList<String>();
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      values.add(entry.getKey() + " ---> " + entry.getValue());
    }

    mapFields.put("userInfo", StringUtils.join(values.iterator(), "<br/>\n"));
    ambraMailer.sendFeedback(fromEmailAddress, mapFields);
    return SUCCESS;
  }

  @SuppressWarnings("unchecked")
  public Map<String, String> getUserSessionAttributes() {
    final Map<String, String> headers = new LinkedHashMap<String, String>();
    final HttpServletRequest request = ServletActionContext.getRequest();

    {
      final Enumeration headerNames = request.getHeaderNames();
      while (headerNames.hasMoreElements()) {
        final String headerName = (String) headerNames.nextElement();
        final List<String> headerValues = EnumerationUtils.toList(request.getHeaders(headerName));
        headers.put(headerName, StringUtils.join(headerValues.iterator(), ","));
      }
    }

    headers.put("server-name", request.getServerName() + ":" + request.getServerPort());
    headers.put("remote-addr", request.getRemoteAddr());
    headers.put("local-addr", request.getLocalAddr() + ":" + request.getLocalPort());

    /*
     * Keeping this in case more values get passed from the client other than just the visible form
     * fields
     */
    {
      final Enumeration parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        final String paramName = (String) parameterNames.nextElement();
        final String[] paramValues = request.getParameterValues(paramName);
        headers.put(paramName, StringUtils.join(paramValues, ","));
      }
    }

    return headers;
  }

  private boolean validates() throws Exception {
    boolean isValid = true;
    if (StringUtils.isBlank(subject)) {
      addFieldError("subject", "Subject cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(name)) {
      addFieldError("name", "Name cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(fromEmailAddress)) {
      addFieldError(FROM_EMAIL_ADDRESS_KEY, "E-mail address cannot be empty");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(fromEmailAddress)) {
      addFieldError(FROM_EMAIL_ADDRESS_KEY, "Invalid e-mail address");
      isValid = false;
    }
    if (StringUtils.isBlank(note)) {
      addFieldError("note", "Message cannot be empty");
      isValid = false;
    }

    HttpServletRequest request = ServletActionContext.getRequest();

    if (!captchaService.validateCaptcha(request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
      addFieldError("captcha", "Text verification is incorrect");
      isValid = false;
    }

    if(!isValid) {
      setNewReCaptcha();
    }

    return isValid;
  }

  private void setNewReCaptcha() throws Exception
  {
    captchaHTML = captchaService.getCaptchaHTML();
  }

  /**
   * Setter for ambraMailer.
   * @param ambraMailer Value to set for ambraMailer.
   */
  public void setAmbraMailer(final AmbraMailer ambraMailer) {
    this.ambraMailer = ambraMailer;
  }

  /**
   * Getter for page.
   * @return Value of page.
   */
  public String getPage() {
    return page;
  }

  /**
   * Setter for page.
   * @param page Value to set for page.
   */
  public void setPage(final String page) {
    this.page = page;
  }

  /**
   * Getter for fromEmailAddress.
   * @return Value of fromEmailAddress.
   */
  public String getFromEmailAddress() {
    return fromEmailAddress;
  }

  /**
   * Setter for fromEmailAddress.
   * @param fromEmailAddress Value to set for fromEmailAddress.
   */
  public void setFromEmailAddress(final String fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
  }

  /**
   * Getter for name.
   * @return Value of name.
   */
  public String getName() {
    return name;
  }

  /**
   * Setter for name.
   * @param name Value to set for name.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Getter for note.
   * @return Value of note.
   */
  public String getNote() {
    return note;
  }

  /**
   * Setter for note.
   * @param note Value to set for note.
   */
  public void setNote(final String note) {
    this.note = note;
  }

  /**
   * Getter for subject.
   * @return Value of subject.
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Setter for subject.
   * @param subject Value to set for subject.
   */
  public void setSubject(final String subject) {
    this.subject = subject;
  }

  /**
   * @return Returns the RecaptchaHTML block
   */
  public String getCaptchaHTML()
  {
    return captchaHTML;
  }

  /**
   * This field is defined in the form that the google recaptcha sends us and
   * as best as I can tell, can't be changed.
   *
   * @param recaptcha_challenge_field
   */
  public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
    captchaChallenge = recaptcha_challenge_field;
  }

  /**
   * This field is defined in the form that the google recaptcha sends us and
   * as best as I can tell, can't be changed.
   *
   * @param recaptcha_challenge_field
   */
  public void setRecaptcha_response_field(String recaptcha_response_field) {
    captchaResponse = recaptcha_response_field;
  }

  /**
   * @param captchaService The captchaService to set.
   */
  @Required
  public void setCaptchaService(CaptchaService captchaService) {
    this.captchaService = captchaService;
  }
}
