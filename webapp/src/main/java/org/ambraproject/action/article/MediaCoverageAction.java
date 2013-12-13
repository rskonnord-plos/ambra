/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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

package org.ambraproject.action.article;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.captcha.CaptchaService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles media coverage link form submission
 */
public class MediaCoverageAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(MediaCoverageAction.class);

  // TODO better value?
  private static final int MAX_LENGTH = 1000;

  private CaptchaService captchaService;

  private String captchaChallenge;
  private String captchaResponse;

  private String uri;
  private String link;
  private String comment;
  private String name;
  private String email;

  @Override
  public String execute() throws Exception {

    String status = ERROR;

    if (validateInput() == false) {
      addActionError("Invalid values have been submitted.");
      return status;
    }

    HttpClient httpClient = new DefaultHttpClient();

    String linkComment = this.name + ", " + this.email + "\n" + this.comment;

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("doi", this.uri.replaceFirst("info:doi/", "")));
    params.add(new BasicNameValuePair("link", this.link));
    params.add(new BasicNameValuePair("comment", linkComment));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = configuration.getString("ambra.services.mediaCoverage.url", null);

    if (mediaCurationUrl != null) {
      HttpPost httpPost = new HttpPost(mediaCurationUrl);
      try {
        httpPost.setEntity(entity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        // check for status code
        if (statusCode == HttpStatus.SC_CREATED) {
          status = SUCCESS;
        }

      } catch(Exception e) {
        log.error("Failed to submit the link to media curation app", e);
        addActionError("There was an error while submitting the media coverage link.");
      } finally {
        httpPost.releaseConnection();
      }
    }

    return status;

  }

  /**
   * Validate the input from the form
   * @return true if everything is ok
   */
  private boolean validateInput() {
    // TODO handle data better

    boolean isValid = true;

    UrlValidator urlValidator = new UrlValidator();

    if (StringUtils.isBlank(link)) {
      addFieldError("link", "This field is required.");
      isValid = false;
    } else if (!urlValidator.isValid(link)) {
      addFieldError("link", "Invalid Media link URL");
    }

    if (StringUtils.isBlank(name)) {
      addFieldError("name", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(email)) {
      addFieldError("email", "This field is required.");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(email)) {
      addFieldError("email", "Invalid e-mail address");
      isValid = false;
    }

    HttpServletRequest request = ServletActionContext.getRequest();

    if (!captchaService.validateCaptcha(request.getRemoteAddr(), captchaChallenge, captchaResponse)) {
      addFieldError("captcha", "Verification is incorrect. Please try again.");
      isValid = false;
    }

    if (isValid) {
      this.link = this.link.substring(0, Math.min(this.link.length(), MAX_LENGTH));
      this.name = this.name.substring(0, Math.min(this.name.length(), MAX_LENGTH));
      this.email = this.email.substring(0, Math.min(this.email.length(), MAX_LENGTH));

      if (!StringUtils.isBlank(comment)) {
        this.comment = this.comment.substring(0, Math.min(this.comment.length(), MAX_LENGTH));
      }
    }

    return isValid;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setRecaptcha_challenge_field(String recaptcha_challenge_field) {
    captchaChallenge = recaptcha_challenge_field;
  }

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
