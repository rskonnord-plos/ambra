/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.rmi.RemoteException;
import javax.activation.DataHandler;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;
import org.topazproject.ws.article.impl.ArticleImpl;
import org.topazproject.ws.article.impl.ArticlePEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

public class ArticleServicePortSoapBindingImpl implements Article, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(ArticleServicePortSoapBindingImpl.class);

  private TopazContext ctx = new WSTopazContext(getClass().getName());
  private Article impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      ArticlePEP pep = new WSArticlePEP((ServletEndpointContext) context);

      ctx.init(context);

      // create the impl
      impl = new ArticleImpl(pep, ctx);
      impl = (Article)ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize ArticleImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  /**
   * @see org.topazproject.ws.article.Article#ingest
   */
  public String ingest(DataHandler zip)
      throws RemoteException, DuplicateArticleIdException, IngestException {
    return impl.ingest(zip);
  }

  /**
   * @see org.topazproject.ws.article.Article#markSuperseded
   */
  public void markSuperseded(String oldArt, String newArt)
      throws RemoteException, NoSuchArticleIdException {
    impl.markSuperseded(oldArt, newArt);
  }

  /**
   * @see org.topazproject.ws.article.Article#delete
   */
  public void delete(String article) throws RemoteException, NoSuchArticleIdException {
    impl.delete(article);
  }

  /**
   * @see org.topazproject.ws.article.Article#setState
   */
  public void setState(String article, int state) throws RemoteException, NoSuchArticleIdException {
    impl.setState(article, state);
  }

  /**
   * @see org.topazproject.ws.article.Article#setAuthorUserIds
   */
  public void setAuthorUserIds(String article, String[] userIds)
      throws NoSuchArticleIdException, RemoteException {
    impl.setAuthorUserIds(article, userIds);
  }

  /**
   * @see org.topazproject.ws.article.Article#getObjectURL
   */
  public String getObjectURL(String obj, String rep)
      throws RemoteException, NoSuchObjectIdException {
    return impl.getObjectURL(obj, rep);
  }

  /**
   * @see org.topazproject.ws.article.Article#setRepresentation
   */
  public void setRepresentation(String obj, String rep, DataHandler content)
      throws NoSuchObjectIdException, RemoteException {
    impl.setRepresentation(obj, rep, content);
  }

  /**
   * @see org.topazproject.ws.article.Article#getObjectInfo
   */
  public ObjectInfo getObjectInfo(String obj) throws NoSuchObjectIdException, RemoteException {
    return impl.getObjectInfo(obj);
  }

  /**
   * @see org.topazproject.ws.article.Article#listSecondaryObjects
   */
  public ObjectInfo[] listSecondaryObjects(String article)
      throws NoSuchArticleIdException, RemoteException {
    return impl.listSecondaryObjects(article);
  }

  /**
   * @see org.topazproject.ws.article.Article#getArticles
   */
  public String getArticles(String startDate, String endDate, String[] categories, String[] authors,
                            int[] states, boolean ascending) throws RemoteException {
    return impl.getArticles(startDate, endDate, categories, authors, states, ascending);
  }

  /**
   * @see org.topazproject.ws.article.Article#getArticleInfos
   */
  public ArticleInfo[] getArticleInfos(String startDate, String endDate,
                                       String[] categories, String[] authors, int[] states,
                                       boolean ascending) throws RemoteException {
    return impl.getArticleInfos(startDate, endDate, categories, authors, states, ascending);
  }

  /**
   * @see org.topazproject.ws.article.Article#getCommentedArticles
   */
  public ObjectInfo[] getCommentedArticles(int maxArticles)
    throws RemoteException {
    return impl.getCommentedArticles(maxArticles);
  }

  private static class WSArticlePEP extends ArticlePEP {
    static {
      init(WSArticlePEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSArticlePEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.articles.pdpName"), 
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
