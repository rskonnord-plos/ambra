/* $HeadURL::                                                                            $
 * $Id$
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
package org.topazproject.ambra.action;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Required;

/**
 * Base class for all actions.
 */
public abstract class BaseActionSupport extends ActionSupport {
  private static final Log           log  = LogFactory.getLog(BaseActionSupport.class);

  protected Configuration configuration;
  private static final String FEED_DEFAULT_NAME = "ambra.services.feed.defaultName";
  private static final String FEED_BASE_PATH = "ambra.services.feed.basePath";
  private static final String FEED_DEFAULT_FILE = "ambra.services.feed.defaultFile";

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
   * @return the name of the rss feed for a page (will be prefixed by the journal name)
   */
  public String getRssName() {
    return configuration.getString(FEED_DEFAULT_NAME, "New Articles");
  }

  /**
   * @return the URL path for the rss feed for a page
   */
  public String getRssPath() {
    return configuration.getString(FEED_BASE_PATH, "/article/") +
        configuration.getString(FEED_DEFAULT_FILE, "feed");
  }

  /**
   * Setter method for configuration. Injected through Spring.
   * 
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Add profane words together into a message.
   * @param profaneWords profaneWords
   * @param fieldName fieldName
   * @param readableFieldName readableFieldName
   */
  protected void addProfaneMessages(final List<String> profaneWords, final String fieldName,
                                    final String readableFieldName) {
    if (!profaneWords.isEmpty()) {
      final String joinedWords = StringUtils.join(profaneWords.toArray(), ", ");
      String  msg;
      if (profaneWords.size() > 1) {
        msg = "these words";
      } else {
        msg = "this word";
      }
      addFieldError(fieldName, "Profanity filter found: " + joinedWords + ". Please remove " +
                               msg + ".");
    }
  }
}
