/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.article.service;

/**
 * The XACML PEP for Articles.
 * Copied from org.topazproject.ws.article.impl.ArticlePEP;
 */
public interface ArticlePermission {
  /** The action that represents an ingest operation in XACML policies. */
  String INGEST = "articles:ingestArticle";

  /** The action that represents a delete operation in XACML policies. */
  String DELETE = "articles:deleteArticle";

  /** The action that represents a set-state operation in XACML policies. */
  String SET_STATE = "articles:setArticleState";

  /** The action that represents a get-object-url operation in XACML policies. */
  String GET_OBJECT_URL = "articles:getObjectURL";

  /** The action that represents checking if we can access a specific article. */
  String READ_META_DATA = "articles:readMetaData";
}
