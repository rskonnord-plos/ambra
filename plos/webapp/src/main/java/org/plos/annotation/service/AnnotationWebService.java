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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import net.sf.ehcache.Ehcache;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.article.service.FetchArticleService;
import static org.plos.annotation.service.WebAnnotation.FLAG_MASK;
import static org.plos.annotation.service.WebAnnotation.PUBLIC_MASK;

import org.plos.models.Annotation;
import org.plos.models.ArticleAnnotation;
import org.plos.models.Comment;
import org.plos.models.Correction;
import org.plos.models.FormalCorrection;
import org.plos.models.MinorCorrection;

import org.plos.permission.service.PermissionWebService;

import org.plos.util.CacheAdminHelper;
import org.plos.user.PlosOneUser;
import org.topazproject.otm.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class AnnotationWebService extends BaseAnnotationService {
  public static final String ANNOTATION_KEY = "ArticleAnnotationCache-Annotation-";
  private static final Log          log                      =
    LogFactory.getLog(AnnotationWebService.class);
  private AnnotationsPEP            pep;
  private FedoraHelper              fedora;
  private Session                   session;
  private PermissionWebService      permissionsWebService;
  private FetchArticleService fetchArticleService;
  private Ehcache articleAnnotationCache;
  protected static final Set<Class<? extends Annotation>> ALL_ANNOTATION_CLASSES = new HashSet<Class<? extends Annotation>>();
  static { 
    ALL_ANNOTATION_CLASSES.add(Comment.class);
    ALL_ANNOTATION_CLASSES.add(Correction.class);
    // ALL_ANNOTATION_CLASSES.add(FormalCorrection.class);
    // ALL_ANNOTATION_CLASSES.add(MinorCorrection.class);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   * @throws URISyntaxException DOCUMENT ME!
   * @throws ServiceException DOCUMENT ME!
   */
  public AnnotationWebService() throws IOException, URISyntaxException, ServiceException {
    try {
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
   * Create a Flag Annotation
   * @param mimeType
   * @param target
   * @param body
   * @param reasonCode
   * @return
   * @throws RemoteException
   * @throws UnsupportedEncodingException
   */
  public String createFlagAnnotation(final String mimeType,
      final String target, final String body, String reasonCode)
      throws RemoteException, UnsupportedEncodingException {
    // TODO - eventually this should create a different type of annotation class and not call this method...
    return createAnnotation(mimeType, target, null, null, null, body);
  }
  
  /**
   * Create a Comment annotation.
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
    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String user      = PlosOneUser.getCurrentUser().getUserId();
    String bodyUri   = fedora.createBody(contentType, body.getBytes(getEncodingCharset()), 
        "Annotation", "Annotation Body");

    if (log.isDebugEnabled())
      log.debug("created fedora object " + bodyUri + " for annotation ");

    final Comment a = new Comment();

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
      permissionsWebService.propagatePermissions(newId, new String[] { bodyUri });
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

    fetchArticleService.removeFromArticleCache(new String[] {target});
    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }

    return newId;
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
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws UnsupportedOperationException DOCUMENT ME!
   */
  public String createArticleAnnotation(Class<ArticleAnnotation> annotationClass, final String mimeType, final String target, final String context,
                                 final String olderAnnotation, final String title, final String body)
                          throws RemoteException, UnsupportedEncodingException, InstantiationException, IllegalAccessException {
    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String user      = PlosOneUser.getCurrentUser().getUserId();
    String bodyUri   = fedora.createBody(contentType, body.getBytes(getEncodingCharset()), 
        "Annotation", "Annotation Body");

    if (log.isDebugEnabled())
      log.debug("created fedora object " + bodyUri + " for correction annotation ");

    final ArticleAnnotation annotation = (ArticleAnnotation) annotationClass.newInstance();
    
    annotation.setMediator(getApplicationId());
    // a.setType(getDefaultType());
    annotation.setAnnotates(URI.create(target));
    annotation.setContext(context);
    annotation.setTitle(title);

    if (isAnonymous())
      annotation.setAnonymousCreator(user);
    else
      annotation.setCreator(user);

    annotation.setBody(URI.create(bodyUri));
    annotation.setCreated(new Date());

    String newId =
      TransactionHelper.doInTx(session,
                               new TransactionHelper.Action<String>() {
          public String run(Transaction tx) {
            return tx.getSession().saveOrUpdate(annotation);
          }
        });

    if (log.isDebugEnabled())
      log.debug("created annotaion " + newId + " for " + target + " annotated by " + bodyUri);

    boolean propagated = false;

    try {
      permissionsWebService.propagatePermissions(newId, new String[] { bodyUri });
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

    fetchArticleService.removeFromArticleCache(new String[] {target});
    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }

    return newId;
  }
  
  
  
  /**
   * Unflag an annotation.
   *
   * @param annotationId annotationId
   *
   * @throws RemoteException RemoteException
   */
  public void unflagAnnotation(final String annotationId)
      throws RemoteException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    TransactionHelper.doInTx(session,
        new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationId);
            a.setState(a.getState() & ~FLAG_MASK);
            tx.getSession().saveOrUpdate(a);
            return null;
          }
        });
  }

  /**
   * Delete an annotation subtree and not just mark it.
   * 
   * @param annotationId
   *          annotationId
   * @param deletePreceding
   *          deletePreceding
   * 
   * @throws RemoteException
   *           RemoteException
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
      fetchArticleService.removeFromArticleCache(new String[] {a.getAnnotates().toString()});
      if (a instanceof ArticleAnnotation) {
        ArticleAnnotation aa = (ArticleAnnotation)a;
        fedora.purgeObjects(new String[] { FedoraHelper.uri2PID(aa.getBody().toString()) });
      }
    }
  }

  public List<Annotation> listAnnotationsForTarget(String target) {
    return listAnnotationsForTarget(target, null);
  }
  
  /**
   * Retrieve all Annotation instances that annotate the given target DOI. If annotationClassTypes is null, then all 
   * annotation types are retrieved. If annotationClassTypes is not null, only the Annotation class types in the 
   * annotationClassTypes Set are returned. 
   * 
   * @param target
   * @param annotationClassTypes
   * @return
   */
  public List<Annotation> listAnnotationsForTarget(final String target, final Set<Class<? extends Annotation>> annotationClassTypes) {
    final Object lock = (FetchArticleService.ARTICLE_LOCK + target).intern(); // lock @ Article level
    List<Annotation> allAnnotations = CacheAdminHelper
        .getFromCache(articleAnnotationCache, ANNOTATION_KEY + target,
            -1, lock, "annotation list",
            new CacheAdminHelper.EhcacheUpdater<List<Annotation>>() {
              public List<Annotation> lookup() {
                return listAnnotations(target, getApplicationId(), -1, ALL_ANNOTATION_CLASSES);
              }
            });
    
    // TODO: Since we cache the set of all annotations, we can't query and cache a limited set of annotations
    // at this time, so we have to filter out the types here and query for all types above when populating the cache
    if (annotationClassTypes != null) {
      List<Annotation> filteredAnnotations = new ArrayList<Annotation>();
      for (Annotation a : allAnnotations) {
        for (Class classType : annotationClassTypes) {
          if (classType.isInstance(a)) {
            filteredAnnotations.add(a);
            break;
          }
        }
      }
      return filteredAnnotations;
    }
    
    return allAnnotations;
  }
  
  private List<Annotation> listAnnotations(final String target, 
      final String mediator, final int state, final Set<Class<? extends Annotation>> classTypes) {
    if (target != null) {
      pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, URI.create(target));
    } else {
      pep.checkAccess(pep.LIST_ANNOTATIONS_IN_STATE, pep.ANY_RESOURCE);
    }

    List<Annotation> allAnnotations = TransactionHelper.doInTx(
        session,
        new TransactionHelper.Action<List<Annotation>>() {
          public List<Annotation> run(Transaction tx) {
            
            List<Annotation> combinedAnnotations = new ArrayList<Annotation>();
            
            for (Class anClass : classTypes) {
              Criteria c = tx.getSession().createCriteria(anClass);
              setRestrictions(c, target, mediator, state);
              combinedAnnotations.addAll((List<Annotation>)c.list());
            }

            return combinedAnnotations;
          }
        });

    if (log.isDebugEnabled()) {
      log.debug("retrieved annotation list from TOPAZ for target: " + target);
    }

    return allAnnotations;
  }
  
  /**
   * Helper method to set restrictions on the criteria used in listAnnotations()
   */
  private static void setRestrictions(Criteria c, final String target,
      final String mediator, final int state) {
    if (mediator != null) {
      c.add(Restrictions.eq("mediator", mediator));
    }
    if (state != -1) {
      if (state == 0) {
        c.add(Restrictions.ne("state", "0"));
      } else {
        c.add(Restrictions.eq("state", "" + state));
      }
    }
    if (target != null) {
      c.add(Restrictions.eq("annotates", target));
    }
  }
  
  /**
   * See listAnnotations(target, annotationClassTypes). This method returns the result of that 
   * method that with annoationClassTypes set to null. 
   * 
   * @param target
   * @return
   * @throws RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target) throws RemoteException {
    return listAnnotations(target, null);
  }
  
  /**
   * Retrieve all AnnotationInfo instances that annotate the given target DOI. If annotationClassTypes is null, then all 
   * annotation types are retrieved. If annotationClassTypes is not null, only the Annotation class types in the 
   * annotationClassTypes Set are returned. 
   * 
   * Each Class in annotationClassTypes should extend Annotation. E.G. Comment.class or FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   *
   * @return a list of annotations
   *
   * @throws RemoteException RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target, Set<Class<? extends Annotation>> annotationClassTypes) throws RemoteException {
    
    List<Annotation> allAnnotations = listAnnotationsForTarget(target, annotationClassTypes);

// List<Annotation> pepFilteredComments   = allArticleAnnotations;
// // Note that the PubApp does not currently support private annotations so this logic is not necessary
// // It is also not going to be possible to cache annotated article XML ones we do support it since the article
// // XML could appear differently to each user. 
//    if (false) { // xxx: no point here because of the cache logic above
//      pepFilteredComments = new ArrayList(allArticleAnnotations);
//
//      for (ArticleAnnotation a : allArticleAnnotations) {
//        try {
//          pep.checkAccess(pep.GET_ANNOTATION_INFO, a.getId());
//        } catch (Throwable t) {
//          if (log.isDebugEnabled())
//            log.debug("no permission for viewing annotation " + a.getId()
//                      + " and therefore removed from list");
//
//          pepFilteredComments.remove(a);
//        }
//      }
//    }
    
    AnnotationInfo[] annotations = new AnnotationInfo[allAnnotations.size()];
    int i = 0;

    // TODO: Flags should not extend Comment (or Annotation). When this is fixed, they should not be stored in this cache! 
    for (Annotation a : allAnnotations) {
      if (a instanceof ArticleAnnotation) {
        annotations[i++] = new AnnotationInfo((ArticleAnnotation)a, fedora);
      } else {
        log.error("Code error! Annotation found in annotation cache that does not " +
        		"implement ArticleAnnotation. (id=" + a.getId() + 
            "). This type of annotation should be stored in a different cache." );
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

    if (!(a instanceof ArticleAnnotation)) {
      throw new IllegalArgumentException("Annotation (id="+annotationId+") not instanceof ArticleAnnotation.");
    }
    
    return new AnnotationInfo((ArticleAnnotation)a, fedora);
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   *
   * @throws RemoteException RemoteException
   */
  public void setPublic(final String annotationDoi) throws RemoteException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationDoi));

    TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationDoi);
            a.setState(a.getState() | PUBLIC_MASK);
            tx.getSession().saveOrUpdate(a);

            return null;
          }
        });
  }

  /**
   * Set the annotation as flagged.
   *
   * @param annotationId annotationId
   *
   * @throws RemoteException RemoteException
   */
  public void setFlagged(final String annotationId) throws RemoteException {
    pep.checkAccess(pep.SET_ANNOTATION_STATE, URI.create(annotationId));

    TransactionHelper.doInTx(session, new TransactionHelper.Action<Void>() {
          public Void run(Transaction tx) {
            Annotation a = tx.getSession().get(Annotation.class, annotationId);
            a.setState(a.getState() | FLAG_MASK);
            tx.getSession().saveOrUpdate(a);

            return null;
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

    List<Annotation> annotationList = listAnnotations(null, mediator, state, ALL_ANNOTATION_CLASSES);
    AnnotationInfo[] annotations = new AnnotationInfo[annotationList.size()];

    int i = 0;
    for (Annotation a : annotationList) {
      if (a instanceof ArticleAnnotation) {
        annotations[i++] = new AnnotationInfo((ArticleAnnotation) a, fedora);
      } else {
        log.error("Code error! Annotation found in annotation cache that does not "
                + "implement ArticleAnnotation. (id=" + a.getId()
                + "). This type of annotation should be stored in a different cache.");
      }
    }
    return annotations;
  }


  /**
   * Replaces the Annotation with DOI targetId with a new Annotation of type newAnnotationClassType. Converting requires
   * that a new class type be used, so the old annotation properties are copied over to the new type. All annotations that
   * referenced the old annotation are updated to reference the new annotation. The old annotation is deleted. 
   * 
   * The given newAnnotationClassType should implement the interface ArticleAnnotation. Known annotation classes that implement 
   * this interface are Comment, FormalCorrection, MinorCorrection 
   * 
   * @param srcAnnotationId - the DOI of the annotation to convert
   * @param newAnnotationClassType - the Class of the new annotation type. Should implement ArticleAnnotation
   * @return
   * @throws Exception
   */
  public String convertArticleAnnotationToType(final String srcAnnotationId, final Class newAnnotationClassType) 
  throws Exception {
    ArticleAnnotation newAnnotation = TransactionHelper.doInTxE(session,
        new TransactionHelper.ActionE<ArticleAnnotation, Exception>() {
          public ArticleAnnotation run(Transaction tx) throws Exception {
            Annotation srcAnnotation = tx.getSession().get(Annotation.class, srcAnnotationId);
            if (srcAnnotation == null) {
              log.error("Annotation was null for id: " + srcAnnotationId);
              return null;
            }
            
            if (!(srcAnnotation instanceof ArticleAnnotation)) {
              log.error("Cannot convert Annotation to correction for id: " + srcAnnotationId);
              return null;
            }
            
            ArticleAnnotation oldAn = (ArticleAnnotation) srcAnnotation;
            ArticleAnnotation newAn = (ArticleAnnotation)newAnnotationClassType.newInstance();
            
            BeanUtils.copyProperties(newAn, oldAn);
            newAn.setId(null); // this should not have been copied (original ArticleAnnotation interface did not have a setId() method...but somehow it is! Reset to null. 

            pep.checkAccess(pep.CREATE_ANNOTATION, oldAn.getAnnotates());
            
            String newId = tx.getSession().saveOrUpdate(newAn);
            URI newIdUri = new URI(newId);
            tx.getSession().flush();
            permissionsWebService.propagatePermissions(newId, new String[] { newAn.getBody().toString() });
            
            // Find all Annotations that refer to the old Annotation and update their target 'annotates' 
            // property to point to the new Annotation. 
            List<Annotation> refAns = listAnnotationsForTarget(oldAn.getId().toString());
            for (Annotation refAn : refAns) {
              refAn.setAnnotates(newIdUri);
              tx.getSession().saveOrUpdate(refAn);
            }
            tx.getSession().flush();
            
            // Delete the original annotation from mulgara. Note, we don't call deleteAnnotation() here
            // since that also removes the body of the article from fedora and we are referencing that from 
            // the new annotation. 
            tx.getSession().delete(oldAn);
            
            return newAn;
          }
        });

    // We must clear the annotated article from the article cache 
    String target = newAnnotation.getAnnotates().toString();
    fetchArticleService.removeFromArticleCache(new String[] {target});
    if (log.isDebugEnabled()) {
      log.debug("removed " + target + " from articleAnnotationCache");
    }
    
    return newAnnotation.getId().toString();
  }
  
  
  /**
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *   to use.
   */
  @Required
  public void setArticleAnnotationCache(Ehcache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
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
   * Set the fetchArticleService.
   * 
   * @param fetchArticleService To use.
   */
  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * Set the PermissionWebService
   *
   * @param permissionWebService permissionWebService
   */
  @Required
  public void setPermissionWebService(final PermissionWebService permissionWebService) {
    this.permissionsWebService = permissionWebService;
  }
}
