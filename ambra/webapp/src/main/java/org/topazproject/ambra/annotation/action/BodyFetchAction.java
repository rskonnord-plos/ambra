/* $HeadURL::                                                                            $
 * $Id:BodyFetchAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.topazproject.ambra.annotation.action;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.util.TextUtils;


/**
 * Get the content/text/body of the annotation or reply.
 * Useful when we want to change the content before presenting it to the web layer.
 */
public class BodyFetchAction extends AnnotationActionSupport {
  private String body;
  private String bodyURL;

  private static final Log log = LogFactory.getLog(BodyFetchAction.class);

  @Transactional(readOnly = true)
  public String execute() throws Exception {
    try {
      final String bodyContent = getAnnotationService().getBody(bodyURL);
      //htmlEncoded so that any dangerous scripting is rendered safely to viewers of the annotation.
      body = TextUtils.escapeAndHyperlink(bodyContent);
    } catch (final ApplicationException e) {
      log.error("Could not retrieve body for URL: " + bodyURL, e);
      addActionError("Getting the annotation body failed with error message: " + e.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  public String getBody() {
    return body;
  }

  /**
   * @param bodyURL the url to fetch the body.
   */
  public void setBodyUrl(final String bodyURL) {
    this.bodyURL = bodyURL;
  }

  @RequiredStringValidator(message = "Url of the body cannot be empty")
  public String getBodyURL() {
    return bodyURL;
  }
}
