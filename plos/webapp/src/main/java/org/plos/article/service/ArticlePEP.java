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

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.plos.xacml.AbstractSimplePEP;
import org.plos.xacml.XacmlUtil;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Articles.
 *
 * @author Ronald Tschalär
 */
public class ArticlePEP extends AbstractSimplePEP {

  /** The action that represents an ingest operation in XACML policies. */
  public static final String INGEST_ARTICLE = "articles:ingestArticle";
  /** The action that represents a delete operation in XACML policies. */
  public static final String DELETE_ARTICLE = "articles:deleteArticle";
  /** The action that represents a set-state operation in XACML policies. */
  public static final String SET_ARTICLE_STATE = "articles:setArticleState";
  /** The action that represents a get-object-url operation in XACML policies. */
  public static final String GET_OBJECT_URL = "articles:getObjectURL";
  /** The action that represents a set-representation operation in XACML policies. */
  public static final String SET_REPRESENTATION = "articles:setRepresentation";
  /** The action that represents a set-author-user-ids operation in XACML policies. */
  public static final String SET_AUTHOR_USER_IDS = "articles:setAuthorUserIds";
  /** The action that represents a get-object-info operation in XACML policies. */
  public static final String GET_OBJECT_INFO = "articles:getObjectInfo";
  /** The action that represents a list-secondary-objects operation in XACML policies. */
  public static final String LIST_SEC_OBJECTS = "articles:listSecondaryObjects";
  /** The action that represents checking if we can access a specific article. */
  public static final String READ_META_DATA = "articles:readMetaData";
  /** The id of the attribute containing the URI of the object */
  public static final URI OBJ_ID =
      URI.create("urn:topazproject:names:tc:xacml:1.0:resource:object-uri");

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           INGEST_ARTICLE,
                                                           DELETE_ARTICLE,
                                                           SET_ARTICLE_STATE,
                                                           GET_OBJECT_URL,
                                                           SET_REPRESENTATION,
                                                           SET_AUTHOR_USER_IDS,
                                                           GET_OBJECT_INFO,
                                                           LIST_SEC_OBJECTS,
                                                           READ_META_DATA,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                         };

  static {
    init(ArticlePEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  public ArticlePEP() throws IOException {
    this(getPDP(), XacmlUtil.createSubjAttrs());
  }

  private static final PDP getPDP() throws IOException {
    try {
      return XacmlUtil.lookupPDP("topaz.articles.pdpName");
    } catch (ParsingException pe) {
      throw (IOException) new IOException("Error creating articles-pep").initCause(pe);
    } catch (UnknownIdentifierException uie) {
      throw (IOException) new IOException("Error creating articles-pep").initCause(uie);
    }
  }

  protected ArticlePEP(PDP pdp, Set subjAttrs) {
    super(pdp, subjAttrs);
  }
}
