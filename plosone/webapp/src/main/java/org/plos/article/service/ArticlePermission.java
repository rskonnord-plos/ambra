/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
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
