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
package org.ambraproject.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle browse by category requests
 *
 * @author Joe Osowski
 */
public class BrowseAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(BrowseAction.class);

  private String category;

  @Override
  public String execute() {
    return SUCCESS;

    //return INPUT on no articles found.
  }

  /**
   * Set the category for the search to perform
   */
  public void setCategory(String category) {
    this.category = category;
  }
}
