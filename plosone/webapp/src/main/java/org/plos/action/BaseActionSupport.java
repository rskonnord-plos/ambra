/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.action;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.views.util.UrlHelper;
import com.opensymphony.xwork.ActionSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Base class for all actions.
 */
public abstract class BaseActionSupport extends ActionSupport {
  private static final Log log = LogFactory.getLog(BaseActionSupport.class);
  /**
   * This overrides the deprecated super inplementation and returns an empty implementation as we
   * want to avoid JSON serializing the deprecated implementation when it tries to serialize
   * an Action when the result type is ajaxJSON.
   *
   * @return a empty list
   */
  @Override
  public Collection getErrorMessages() {
      return Collections.EMPTY_LIST;
  }

  /**
   * This overrides the deprecated super inplementation and returns an empty implementation as we
   * want to avoid JSON serializing the deprecated implementation when it tries to serialize
   * an Action when the result type is ajaxJSON.
   *
   * @return a empty map
   */
  @Override
  public Map getErrors() {
      return Collections.EMPTY_MAP;
  }

  /**
   * Return the number of fields with errors.
   * @return number of fields with errors
   */
  public int getNumFieldErrors() {
    return getFieldErrors().size();
  }

  /**
   * Return the url for this request, which could be used to redirect the user to it in case of failure  
   * @return the start url for this request
   */
  public String getStartingUrl() {
    final HttpServletRequest servletRequest = getRequest();
    final StringBuilder urlBuilder = new StringBuilder();
    final String method = servletRequest.getMethod();
    if (method.equals("POST")) {
      final String referer = servletRequest.getHeader("referer");
      urlBuilder.append(
              StringUtils.isBlank(referer)? "/"
                                          :referer);
    } else {
      urlBuilder.append(UrlHelper.buildUrl(null, servletRequest, getResponse(), null, null, false, true));
      final String queryString = servletRequest.getQueryString();
      if (null != queryString) {
        urlBuilder.append("?")
                  .append(queryString);
      }
    }
    final String url = urlBuilder.toString();
    log.debug("Url for redirection (on getting proxy invalidation!?) = " + url);    
    return url;
  }

  private HttpServletResponse getResponse() {
    return ServletActionContext.getResponse();
  }

  private HttpServletRequest getRequest() {
    return ServletActionContext.getRequest();
  }
}
