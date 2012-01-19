/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

package org.topazproject.ambra.search2.service;

/**
 * Service class for (re)indexing articles
 * @author Dragisa Krsmanovic
 */
public interface ArticleIndexingService {

  /**
   * Send one articles for re-indexing.
   *
   * @param articleId Article ID
   * @throws Exception if operation fails
   */
  public void indexArticle(String articleId) throws Exception;

  /**
   * Start asynchronous process that will index all articles.
   *
   * @throws Exception if operation fails
   */
  public void startIndexingAllArticles() throws Exception;

  /**
   * Send all articles for re-indexing (slow). This method is invoked asynchronously after
   * startIndexingAllArticles() is called.
   *
   * @return Confirmation email body
   * @throws Exception if operation fails
   */
  public String indexAllArticles() throws Exception;

}
