
/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.amendment;

import org.ambraproject.views.ArticleAmendment;

import java.util.List;

/**
 * A service used to retrieve an article's amendments from the database
 */
public interface AmendmentService {

  /**
   * This method returns the article's amendments of type eoc and/or retraction
   * @param doi
   * @return
   */
  public List<ArticleAmendment> fetchAmendmentsFromAmbra(String doi);

  void setAmbraServer(String ambraServer);
}
