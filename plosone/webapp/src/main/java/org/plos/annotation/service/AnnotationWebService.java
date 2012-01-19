/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URI;
import java.net.URISyntaxException;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.plos.annotation.service.Annotation.DELETE_MASK;
import static org.plos.annotation.service.Annotation.FLAG_MASK;
import static org.plos.annotation.service.Annotation.PUBLIC_MASK;

import org.plos.models.Annotation;

import org.plos.permission.service.PermissionWebService;

import org.plos.util.FileUtils;
import org.plos.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class AnnotationWebService extends BaseAnnotationService {
  private GeneralCacheAdministrator articleCacheAdministrator;
  private static final String       CACHE_KEY_ANNOTATION     = "ANNOTATION";
  private static final Log          log                      =
    LogFactory.getLog(AnnotationWebService.class);
  private AnnotationsPEP            pep;
  private FedoraHelper              fedora;
  private Session                   session;
  private PermissionWebService      permissions;
  private static final String       PLOSONE_0_6_DEFAULT_TYPE =
    "http://www.w3.org/2000/10/annotationType#Annotation";

  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   * @throws URISyntaxException DOCUMENT ME!
   * @throws ServiceException DOCUMENT ME!
   */
  public void init() throws IOException, URISyntaxException, ServiceException {
    try {
      if (pep == null)
        pep = new AnnotationsPEP();
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      IOException ioe = new IOException("Failed to create PEP");
      ioe.initCause(e);
      throw ioe;
    }

    if (fedora == null)
      fedora = new FedoraHelper();
  }

  /**
   * Create an annotation.
   *
   * @param mimeType mimeType
   * @param target target
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param body body
   *
   * @return a the new annotation id
   *
   * @throws RemoteException RemoteException
   * @throws UnsupportedEncodingException UnsupportedEncodingException
   * @throws UnsupportedOperationException DOCUMENT ME!
   */
  public String createAnnotation(final String mimeType, final String target, final String context,
                                 final String olderAnnotation, final String title, final String body)
                          throws RemoteException, UnsupportedEncodingException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String bodyUri;
    String user;

    try {
      fedora.getContext().activate();
      user      = fedora.getContext().getUserName();
      bodyUri   = fedora.createBody(contentType, body.getBytes(getEncodingCharset()), "Annotation",
                                    "Annotation Body");
    } finally {
      fedora.getContext().passivate();
    }

    if (log.isDebugEnabled())
      log.debug("created fedora object " + bodyUri + " for annotation ");

    final Annotation a = new Annotation();

    a.setMediator(getApplicationId());
    a.setType(getDefaultType());
    a.setAnnotates(URI.create(target));
    a.setContext(context);
    a.setTitle(title);

    if (isAnonymous())
      a.setAnonymousCreator(user);
    else
      a.setCreator(user);

    a.setBody(URI.create(bodyUri));
    a.setCreated(new Date());

    String newId =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<String>() {
          public String run(Transaction tx) {
            return tx.getSession().saveOrUpdate(a);
          }
        });

    if (log.isDebugEnabled())
      log.debug("created annotaion " + newId + " for " + target + " annotated by " + bodyUri);

    boolean propagated = false;

    try {
      permissions.propagatePermissions(newId, new String[] { bodyUri });
      propagated = true;

      if (log.isDebugEnabled())
        log.debug("propagated permissions for annotaion " + newId + " to " + bodyUri);
    } finally {
      if (!propagated) {
        if (log.isDebugEnabled())
          log.debug("failed to propagate permissions for annotaion " + newId + " to " + bodyUri);

        try {
          deleteAnnotation(newId);
        } catch (Throwable t) {
          if (log.isDebugEnabled())
            log.debug("failed to delete partially created annotation " + newId, t);
        }
      }
    }

    articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(target));

    if (log.isDebugEnabled()) {
      log.debug("flushing cache group: " + FileUtils.escapeURIAsPath(target));
    }

    return newId;
  }

  /**
   * Unflag an annotation
   *
   * @param annotationId annotationId
   *
   * @throws RemoteException RemoteException
   */
  public void unflagAnnotation(final String annotationId)
                        throws RemoteException {
    setPublic(annotationId);
  }

  /**
   * Delete an annotation subtree and not just mark it.
   *
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   *
   * @throws RemoteException RemoteException
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding)
                               throws RemoteException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(annotationId));
    deleteAnnotation(annotationId);
  }

  /**
   * Mark an annotation as deleted.
   *
   * @param annotationId annotationId
   *
   * @throws RemoteException RemoteException
   */
  public void deletePublicAnnotation(final String annotationId)
                              throws RemoteException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(annotationId));
    deleteAnnotation(annotationId);
  }

  /**
   * Mark the given flag as deleted.
   *
   * @param flagId flagId
   *
   * @throws RemoteException RemoteException
   */
  public void deleteFlag(final String flagId) throws RemoteException {
    pep.checkAccess(pep.DELETE_ANNOTATION, URI.create(flagId));
    deleteAnnotation(flagId);
  }

  private void deleteAnnotation(final String annotationId)
                         throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    Annotation a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Annotation>() {
          public Annotation run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationId);

            if (a != null)
              tx.getSession().delete(a);

            return a;
          }
        });

    if (a != null) {
      articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(a.getAnnotates()));

      try {
        fedora.getContext().activate();
        fedora.purgeObjects(new String[] { fedora.uri2PID(a.getBody().toString()) });
      } finally {
        fedora.getContext().passivate();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param target target
   *
   * @return a list of annotations
   *
   * @throws RemoteException RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target)
                                   throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.LIST_ANNOTATIONS, URI.create(target));

    AnnotationInfo[] annotations = null;

    try {
      //for now, since all annotations are public, don't have to cache based on userID
      annotations = (AnnotationInfo[]) articleCacheAdministrator.getFromCache(target
                                                                              + CACHE_KEY_ANNOTATION);

      if (log.isDebugEnabled()) {
        log.debug("retrieved annotation list from cache for target: " + target);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;

      try {
        List<Annotation> all =
          TransactionHelper.doInTx(session,
                                   new TransactionHelper.Action<List<Annotation>>() {
              public List<Annotation> run(Transaction tx) {
                // xxx: See trac #357. The previous default was incorrect. 
                // Support both the old and the new to be compatible. (till data migration)
                return tx.getSession().createCriteria(Annotation.class)
                          .add(Restrictions.eq("mediator", getApplicationId()))
                          .add(Restrictions.eq("annotates", target))
                          .add(Restrictions.disjunction()
                                            .add(Restrictions.eq("type", getDefaultType()))
                                            .add(Restrictions.eq("type", PLOSONE_0_6_DEFAULT_TYPE)))
                          .list();
              }
            });

        List<Annotation> l   = all;

        if (false) { // xxx: no point here because of the cache logic above
          l = new ArrayList(all);

          for (Annotation a : all) {
            try {
              pep.checkAccess(pep.GET_ANNOTATION_INFO, a.getId());
            } catch (Throwable t) {
              if (log.isDebugEnabled())
                log.debug("no permission for viewing annotation " + a.getId()
                          + " and therefore removed from list");

              l.remove(a);
            }
          }
        }

        annotations = new AnnotationInfo[l.size()];

        int i = 0;

        for (Annotation a : l)
          annotations[i++] = new AnnotationInfo(a, fedora);

        //use grouping for future when annotations can be private
        articleCacheAdministrator.putInCache(target + CACHE_KEY_ANNOTATION, annotations,
                                             new String[] { FileUtils.escapeURIAsPath(target) });
        updated = true;

        if (log.isDebugEnabled()) {
          log.debug("retrieved annotation list from TOPAZ for target: " + target);
        }
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(target + CACHE_KEY_ANNOTATION);
      }
    }

    return annotations;
  }

  /**
   * DOCUMENT ME!
   *
   * @param annotationId annotationId
   *
   * @return an annotation
   *
   * @throws RemoteException RemoteException
   * @throws IllegalArgumentException DOCUMENT ME!
   */
  public AnnotationInfo getAnnotation(final String annotationId)
                               throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.GET_ANNOTATION_INFO, URI.create(annotationId));

    Annotation a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Annotation>() {
          public Annotation run(Transaction tx) {
            return tx.getSession().get(Annotation.class, annotationId);
          }
        });

    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + annotationId);

    return new AnnotationInfo(a, fedora);
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   *
   * @throws RemoteException RemoteException
   */
  public void setPublic(final String annotationDoi) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationDoi));

    Annotation a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Annotation>() {
          public Annotation run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationDoi);
            a.setState(PUBLIC_MASK);
            tx.getSession().saveOrUpdate(a);

            return a;
          }
        });
  }

  /**
   * Set the annotation as flagged
   *
   * @param annotationId annotationId
   *
   * @throws RemoteException RemoteException
   */
  public void setFlagged(final String annotationId) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    Annotation a =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<Annotation>() {
          public Annotation run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationId);
            a.setState(PUBLIC_MASK | FLAG_MASK);
            tx.getSession().saveOrUpdate(a);

            return a;
          }
        });
  }

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *        administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws RemoteException if some error occurred
   */
  public AnnotationInfo[] listAnnotations(final String mediator, final int state)
                                   throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();

    pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, pep.ANY_RESOURCE);

    List<Annotation> l           =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<List<Annotation>>() {
          public List<Annotation> run(Transaction tx) {
            Criteria c           = tx.getSession().createCriteria(Annotation.class);

            if (mediator != null)
              c.add(Restrictions.eq("mediator", mediator));

            if (state == 0)
              c.add(Restrictions.ne("state", "0"));
            else
              c.add(Restrictions.eq("state", "" + state));

            return c.list();
          }
        });

    AnnotationInfo[] annotations = new AnnotationInfo[l.size()];

    int              i           = 0;

    for (Annotation a : l)
      annotations[i++] = new AnnotationInfo(a, fedora);

    return annotations;
  }

  /**
   * DOCUMENT ME!
   *
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }

  /**
   * DOCUMENT ME!
   *
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }

  /**
   * Set the OTM session. Called by spring's bean wiring.
   *
   * @param session the otm session
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the PermissionWebService
   *
   * @param permissionWebService permissionWebService
   */
  @Required
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissions = permissionWebService;
  }
}
