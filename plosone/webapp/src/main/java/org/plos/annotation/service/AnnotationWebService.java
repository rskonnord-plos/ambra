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

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.plos.annotation.service.Annotation.DELETE_MASK;
import static org.plos.annotation.service.Annotation.FLAG_MASK;
import static org.plos.annotation.service.Annotation.PUBLIC_MASK;

import org.plos.ApplicationException;
import org.plos.util.FileUtils;
import org.topazproject.authentication.ProtectedService;
import org.topazproject.ws.annotation.AnnotationClientFactory;
import org.topazproject.ws.annotation.AnnotationInfo;
import org.topazproject.ws.annotation.Annotations;
import org.topazproject.ws.annotation.NoSuchAnnotationIdException;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class AnnotationWebService extends BaseAnnotationService {
  private Annotations annotationService;
  private GeneralCacheAdministrator articleCacheAdministrator;
  private static final String CACHE_KEY_ANNOTATION = "ANNOTATION";
  private static final Log log = LogFactory.getLog(AnnotationWebService.class);
  
  public void init() throws IOException, URISyntaxException, ServiceException {
    final ProtectedService protectedService = getProtectedService();
    annotationService = AnnotationClientFactory.create(protectedService);
  }

  /**
   * Create an annotation.
   * @param mimeType mimeType
   * @param target target
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param body body
   * @return a the new annotation id
   * @throws java.io.UnsupportedEncodingException UnsupportedEncodingException
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public String createAnnotation(final String mimeType, final String target, final String context, final String olderAnnotation, final String title, final String body) throws RemoteException, NoSuchAnnotationIdException, UnsupportedEncodingException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    final String contentType = getContentType(mimeType);
    //Ensure that it is null if the olderAnnotation is empty
    final String earlierAnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;
    final String newId = annotationService.createAnnotation(getApplicationId(), getDefaultType(), target, context, earlierAnotation, false, title, contentType, body.getBytes(getEncodingCharset()));
    articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(target));
    if (log.isDebugEnabled()){
      log.debug("flushing cache group: " + FileUtils.escapeURIAsPath(target));
    }
    return newId;
  }

  /**
   * Unflag an annotation
   * @param annotationId annotationId
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void unflagAnnotation(final String annotationId) throws RemoteException, NoSuchAnnotationIdException {
    setPublic(annotationId);
  }

  /**
   * Delete an annotation subtree and not just mark it.
   * @param annotationId annotationId
   * @param deletePreceding deletePreceding
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void deletePrivateAnnotation(final String annotationId, final boolean deletePreceding) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    AnnotationInfo ai = getAnnotation(annotationId);
    annotationService.deleteAnnotation(annotationId, deletePreceding);
    articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(ai.getAnnotates()));
  }

  /**
   * Mark an annotation as deleted.
   * @param annotationId annotationId
   * @throws java.rmi.RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @see org.topazproject.ws.annotation.Annotations#deleteAnnotation(String, boolean)
   */
  public void deletePublicAnnotation(final String annotationId) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    //annotationService.setAnnotationState(annotationId, PUBLIC_MASK | DELETE_MASK);  
    AnnotationInfo ai = getAnnotation(annotationId);
    annotationService.deleteAnnotation(annotationId, true);  
    articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(ai.getAnnotates()));
  }

  
  /**
   * Mark the given flag as deleted.
   * @param flagId flagId
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @throws java.rmi.RemoteException RemoteException
   */
  public void deleteFlag(final String flagId) throws NoSuchAnnotationIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    //annotationService.setAnnotationState(flagId, DELETE_MASK);
    AnnotationInfo ai = getAnnotation(flagId);
    annotationService.deleteAnnotation(flagId, true);
    articleCacheAdministrator.flushGroup(FileUtils.escapeURIAsPath(ai.getAnnotates()));
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#listAnnotations(String, String, String)
   * @param target target
   * @return a list of annotations
   * @throws java.rmi.RemoteException RemoteException
   */
  public AnnotationInfo[] listAnnotations(final String target) throws RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    AnnotationInfo[] annotations = null;
    try {
      //for now, since all annotations are public, don't have to cache based on userID
      annotations = (AnnotationInfo[])articleCacheAdministrator.getFromCache(target + CACHE_KEY_ANNOTATION); 
      if (log.isDebugEnabled()) {
        log.debug("retrieved annotation list from cache for target: " + target);
      }
    } catch (NeedsRefreshException nre) {
      boolean updated = false;
      try {
        //use grouping for future when annotations can be private
        annotations = annotationService.listAnnotations(getApplicationId(), target, getDefaultType());
        articleCacheAdministrator.putInCache(target + CACHE_KEY_ANNOTATION, 
                                   annotations, new String[]{FileUtils.escapeURIAsPath(target)});
        updated = true;
        if (log.isDebugEnabled()) {
          log.debug("retrieved annotation list from TOPAZ for target: " + target);
        }        
      } catch (RemoteException e) {
        log.error ("Could not retrieve all annotations for target: " + target);
        throw (e);
      } finally {
        if (!updated)
          articleCacheAdministrator.cancelUpdate(target + CACHE_KEY_ANNOTATION);
      }
    }
    return annotations;
  }

  /**
   * @see org.topazproject.ws.annotation.Annotations#getAnnotationInfo(String)
   * @param annotationId annotationId
   * @return an annotation
   * @throws RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public AnnotationInfo getAnnotation(final String annotationId) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    return annotationService.getAnnotationInfo(annotationId);
  }

  /**
   * Set the annotation as public.
   * @param annotationDoi annotationDoi
   * @throws RemoteException RemoteException
   * @throws org.topazproject.ws.annotation.NoSuchAnnotationIdException NoSuchAnnotationIdException
   */
  public void setPublic(final String annotationDoi) throws RemoteException, NoSuchAnnotationIdException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(annotationDoi, PUBLIC_MASK);
  }

  /**
   * Set the annotation as flagged
   * @param annotationId annotationId
   * @throws NoSuchAnnotationIdException NoSuchAnnotationIdException
   * @throws RemoteException RemoteException
   */
  public void setFlagged(final String annotationId) throws NoSuchAnnotationIdException, RemoteException {
    ensureInitGetsCalledWithUsersSessionAttributes();
    annotationService.setAnnotationState(annotationId, PUBLIC_MASK | FLAG_MASK);
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
  public AnnotationInfo[] listAnnotations(String mediator, int state)
                                   throws RemoteException {
	    ensureInitGetsCalledWithUsersSessionAttributes();
	    return annotationService.listAnnotations(mediator, state);	  
  }

  /**
   * @return Returns the articleCacheAdministrator.
   */
  public GeneralCacheAdministrator getArticleCacheAdministrator() {
    return articleCacheAdministrator;
  }

  /**
   * @param articleCacheAdministrator The articleCacheAdministrator to set.
   */
  public void setArticleCacheAdministrator(GeneralCacheAdministrator articleCacheAdministrator) {
    this.articleCacheAdministrator = articleCacheAdministrator;
  }

}
