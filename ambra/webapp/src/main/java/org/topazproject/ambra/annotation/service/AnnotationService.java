/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.annotation.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.Constants;
import org.topazproject.ambra.annotation.FlagUtil;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.AnnotationBlob;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.ArticleAnnotation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.xacml.AbstractSimplePEP;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session.FlushMode;
import org.topazproject.otm.criterion.Restrictions;

import com.sun.xacml.PDP;

/**
 * Wrapper over annotation(not the same as reply) web service
 */
public class AnnotationService extends BaseAnnotationService {

  protected static final Set<Class<?extends ArticleAnnotation>> ALL_ANNOTATION_CLASSES =
    new HashSet<Class<?extends ArticleAnnotation>>();

  public  static final String ANNOTATED_KEY = "ArticleAnnotationCache-Annotation-";
  private static final Log    log  = LogFactory.getLog(AnnotationService.class);
  private              AnnotationsPEP pep;
  private              Cache          articleAnnotationCache;
  private              Invalidator    invalidator;

  static {
    ALL_ANNOTATION_CLASSES.add(ArticleAnnotation.class);
  }

  @Required
  public void setAnnotationsPdp(PDP pdp) {
    pep  = new AnnotationsPEP(pdp);
  }

  /**
   * Create an annotation.
   *
   * @param annotationClass the class of annotation
   * @param mimeType mimeType of the annotation body
   * @param target target of this annotation
   * @param context context the context within the target that this applies to
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title of this annotation
   * @param body body of this annotation
   * @param isPublic to set up public permissions
   *
   * @param user
   * @return a the new annotation id
   *
   * @throws Exception on an error
   */
  private String createAnnotation(Class<? extends ArticleAnnotation> annotationClass,
                                  final String mimeType, final String target,
                                  final String context, final String olderAnnotation,
                                  final String title, final String body,
                                  boolean isPublic, AmbraUser user)
                     throws Exception {

    pep.checkAccess(AnnotationsPEP.CREATE_ANNOTATION, URI.create(target));

    final String contentType = getContentType(mimeType);

    // Ensure that it is null if the olderAnnotation is empty
    final String earlierAnnotation = StringUtils.isEmpty(olderAnnotation) ? null : olderAnnotation;

    if (earlierAnnotation != null)
      throw new UnsupportedOperationException("supersedes is not supported");

    String userId = user.getUserId();
    AnnotationBlob blob = new AnnotationBlob(contentType);
    final ArticleAnnotation annotation = annotationClass.newInstance();

    annotation.setMediator(getApplicationId());
    annotation.setAnnotates(URI.create(target));
    annotation.setContext(context);
    annotation.setTitle(title);
    annotation.setCreator(userId);
    annotation.setBody(blob);
    annotation.setCreated(new Date());

    String newId = session.saveOrUpdate(annotation);
    // now that the blob is created by OTM, write to it
    blob.getBody().writeAll(body.getBytes(getEncodingCharset()));

    if (log.isDebugEnabled())
      log.debug("created annotaion " + newId + " for " + target + " with body in blob " +
                blob.getId());

    permissionsService.propagatePermissions(newId, new String[] { blob.getId() });

    if (isPublic)
      setPublicPermissions(newId);

    return newId;
  }

  /**
   * Delete an annotation.
   *
   * @param annotationId annotationId
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void deleteAnnotation(final String annotationId) throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.DELETE_ANNOTATION, URI.create(annotationId));
    ArticleAnnotation a = session.get(ArticleAnnotation.class, annotationId);

    if ((a != null) && (a.getBody() != null)) {
      String pp[]     = new String[] { a.getBody().getId() };
      session.delete(a);
      permissionsService.cancelPropagatePermissions(annotationId, pp);
      cancelPublicPermissions(annotationId);
    }
  }

  /**
   * Retrieve all Annotation instances that annotate the given target DOI.
   *
   * @param target the target
   *
   * @return list of annotations
   *
   * @throws OtmException on an error
   */
  private List<ArticleAnnotation> lookupAnnotations(final String target)
      throws OtmException {
    // This flush is so that our own query cache reflects change.
    // TODO : implement query caching in OTM and let it manage this cached query
    if (session.getFlushMode().implies(FlushMode.always))
      session.flush();

    // lock @ Article level
    final Object lock = (FetchArticleService.ARTICLE_LOCK + target).intern();

    return articleAnnotationCache.get(ANNOTATED_KEY + target, -1,
          new Cache.SynchronizedLookup<List<ArticleAnnotation>, OtmException>(lock) {
          @Override
          public List<ArticleAnnotation> lookup() throws OtmException {
            return loadAnnotations(target, getApplicationId(), -1, ALL_ANNOTATION_CLASSES);
          }
        });
  }

  private List<ArticleAnnotation> filterAnnotations(List<ArticleAnnotation> allAnnotations,
      Set<Class<?extends ArticleAnnotation>> annotationClassTypes) {

    if ((annotationClassTypes == null) || (annotationClassTypes.size() == 0) ||
        ALL_ANNOTATION_CLASSES.equals(annotationClassTypes))
      return allAnnotations;

    List<ArticleAnnotation> filteredAnnotations = new
      ArrayList<ArticleAnnotation>(allAnnotations.size());

    for (ArticleAnnotation a : allAnnotations) {
      for (Class<? extends ArticleAnnotation> classType : annotationClassTypes) {
        if (classType.isInstance(a)) {
          filteredAnnotations.add(a);

          break;
        }
      }
    }

    return filteredAnnotations;
  }

  @SuppressWarnings("unchecked")
  private List<ArticleAnnotation> loadAnnotations(final String target, final String mediator,
      final int state, final Set<Class<?extends ArticleAnnotation>> classTypes)
    throws OtmException {
    List<ArticleAnnotation> combinedAnnotations = new ArrayList<ArticleAnnotation>();

    for (Class<? extends ArticleAnnotation> anClass : classTypes) {
      Criteria c = session.createCriteria(anClass);
      setRestrictions(c, target, mediator, state);
      combinedAnnotations.addAll(c.list());
    }

    if (log.isDebugEnabled()) {
      log.debug("retrieved annotation list from TOPAZ for target: " + target);
    }

    return combinedAnnotations;
  }

  /**
   * Get all annotations satisfying the criteria.
   *
   * @param articleId  limits the list to annotations from a particular article.
   * @param startDate  is the date to start searching from. If null, start from begining of time.
   *                   Can be iso8601 formatted or string representation of Date object.
   * @param endDate    is the date to search until. If null, search until present date
   * @param mediator   the mediator of the annotation.
   * @param annotType  a filter list of rdf types for the annotations.
   * @param states     array of states to filter on
   * @param ascending  controls the sort order (by date).
   * @param maxResults the maximum number of results to return, or 0 for no limit
   *
   * @return the (possibly empty) list of articles.
   *
   * @throws ParseException if any of the dates or query could not be parsed
   * @throws URISyntaxException if an element of annotType cannot be parsed as a URI
   */
  @Transactional(readOnly = true)
  public List<ArticleAnnotation> getAnnotations(String articleId, Date startDate, Date endDate,
                                                String mediator, List<String> annotType,
                                                int[] states, boolean ascending, int maxResults)
  throws ParseException, URISyntaxException {
    List<String> annotationIds = getAnnotationIds(articleId, startDate, endDate, mediator,
                                                  annotType, states, ascending, maxResults);

    return getAnnotations(annotationIds);
  }

  /**
   * Get a list of all annotation Ids satifying the given criteria. All caching is done
   * at the object level by the session.
   *
   * @param targetId  limits annotations to a particlualr target id.
   * @param startDate  search for annotation after start date.
   * @param endDate    is the date to search until. If null, search until present date
   * @param mediator   annotation mediator
   * @param annotType  a filter list of rdf types for the annotations.
   * @param states     the list of annotation states to search for (all states if null or empty)
   * @param ascending  controls the sort order (by date).
   * @param maxResults the maximum number of results to return, or 0 for no limit
   *
   * @return the (possibly empty) list of article ids.
   *
   * @throws ParseException   if any of the dates or query could not be parsed
   * @throws URISyntaxException  if an element of annotType cannot be parsed as a URI
   */
  @Transactional(readOnly = true)
  public List<String> getAnnotationIds(String targetId, Date startDate, Date endDate,
                                       String mediator, List<String> annotType, int[] states,
                                       boolean ascending, int maxResults)
  throws ParseException, URISyntaxException {
    StringBuilder qry = new StringBuilder();

    if (targetId != null) {
      qry.append("select a.id id, cr from Annotation a ");
      qry.append("where cr := a.created and ");
      qry.append("a.annotates = :targ and ");
    } else {
      qry.append("select a.id id, cr from ArticleAnnotation a, Article ar ");
      qry.append("where a.annotates = ar and cr := a.created and ");
    }

    if (mediator != null)
      qry.append("a.mediator = :med and ");

    // apply date constraints
    if (startDate != null)
      qry.append("ge(cr, :sd) and ");
    if (endDate != null)
      qry.append("le(cr, :ed) and ");

    // match all states
    if (states != null && states.length > 0) {
      qry.append("(");
      for (int idx = 0; idx < states.length; idx++)
        qry.append("art.state = :st").append(idx).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ");
    }

    // match all types
    if (annotType != null && annotType.size() > 0) {
      qry.append("(");
      for (int idx = 0; idx < annotType.size(); idx++)
        qry.append("a.<rdf:type> = :type").append(idx).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ");
    }

    // trim off trailing 'and'
    if (qry.indexOf(" and ", qry.length() - 5) > 0)
      qry.setLength(qry.length() - 4);

    // add ordering and limit
    qry.append("order by cr ").append(ascending ? "asc" : "desc").append(", id asc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);

    qry.append(";");

    // create the query, applying parameters
    Query q = session.createQuery(qry.toString());

    if (targetId != null)
      q.setParameter("targ", targetId);
    if (mediator != null)
      q.setParameter("med", mediator);
    if (startDate != null)
      q.setParameter("sd",  startDate);
    if (endDate != null)
      q.setParameter("ed",  endDate);
    for (int idx = 0; states != null && idx < states.length; idx++)
      q.setParameter("st" + idx, states[idx]);
    for (int idx = 0; annotType != null && idx < annotType.size(); idx++)
      q.setUri("type" + idx, new URI(annotType.get(idx)));

    Results r = q.execute();
    // apply access-controls
    List<String> res = new ArrayList<String>();
    while (r.next()) {
      URI id = r.getURI(0);
      try {
        pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, id);
        res.add(id.toString());
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }
    return res;
  }

  /**
   * Get all annotations specified by the list of annotation ids.
   *
   * @param annotIds a list of annotations Ids to retrieve.
   *
   * @return the (possibly empty) list of annotations.
   *
   */
  @Transactional(readOnly = true)
  public List<ArticleAnnotation> getAnnotations(List<String> annotIds) {
    List<ArticleAnnotation> annotationList = new ArrayList<ArticleAnnotation>();

    for (String id : annotIds)  {
      try {
        pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, URI.create(id));
        ArticleAnnotation a = session.get(ArticleAnnotation.class, id);

        if (a != null)
          annotationList.add(a);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }
    return annotationList;
  }

  /**
   * Helper method to set restrictions on the criteria used in listAnnotations()
   *
   * @param c the criteria
   * @param target the target that is being annotated
   * @param mediator the mediator that does the annotation on the user's behalf
   * @param state the state to filter by (0 for all non-zero, -1 to not restrict by state)
   */
  private static void setRestrictions(Criteria c, final String target, final String mediator,
                                      final int state) {
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
   * Retrieve all Annotation instances that annotate the given target DOI. If annotationClassTypes
   * is null, then all annotation types are retrieved. If annotationClassTypes is not null, only the
   * Annotation class types in the annotationClassTypes Set are returned. Each Class in
   * annotationClassTypes should extend Annotation. E.G.  Comment.class or FormalCorrection.class
   *
   * @param target target doi that the listed annotations annotate
   * @param annotationClassTypes a set of Annotation class types to filter the results
   *
   * @return a list of annotations
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation[] listAnnotations(final String target,
      Set<Class<?extends ArticleAnnotation>> annotationClassTypes)
    throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, URI.create(target));


    List<ArticleAnnotation> filtered = new ArrayList<ArticleAnnotation>();

    for (ArticleAnnotation a : filterAnnotations(lookupAnnotations(target), annotationClassTypes)) {
      try {
        pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, a.getId());
        filtered.add(a);
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("no permission for viewing annotation " + a.getId() +
                    " and therefore removed from list");
      }
    }

    return filtered.toArray(new ArticleAnnotation[filtered.size()]);
  }

  /**
   * Loads the article annotation with the given id.
   *
   * @param annotationId annotationId
   *
   * @return an annotation
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   * @throws IllegalArgumentException if an annotation with this id does not exist
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation getAnnotation(final String annotationId)
    throws OtmException, SecurityException, IllegalArgumentException {
    pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, URI.create(annotationId));
    // comes from object-cache.
    ArticleAnnotation   a = session.get(ArticleAnnotation.class, annotationId);
    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + annotationId);

    return a;
  }

  /**
   * Set the annotation context.
   *
   * @param id the annotation id
   * @param context the context to set
   *
   * @throws OtmException on an error
   * @throws SecurityException if a security policy prevented this operation
   * @throws IllegalArgumentException if an annotation with this id does not exist
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void updateContext(String id, String context)
    throws OtmException, SecurityException, IllegalArgumentException {

    pep.checkAccess(AnnotationsPEP.UPDATE_ANNOTATION, URI.create(id));

    Annotation<?> a = session.get(Annotation.class, id);
    if (a == null)
      throw new IllegalArgumentException("invalid annoation id: " + id);

    a.setContext(context);
  }

  /**
   * List the set of annotations in a specific administrative state.
   *
   * @param mediator if present only those annotations that match this mediator are returned
   * @param state the state to filter the list of annotations by or 0 to return annotations in any
   *              administrative state
   *
   * @return an array of annotation metadata; if no matching annotations are found, an empty array
   *         is returned
   *
   * @throws OtmException if some error occurred
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(readOnly = true)
  public ArticleAnnotation[] listAnnotations(final String mediator, final int state)
  throws OtmException, SecurityException {
    pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS_IN_STATE, AbstractSimplePEP.ANY_RESOURCE);
    List<ArticleAnnotation> annotations =
      loadAnnotations(null, mediator, state, ALL_ANNOTATION_CLASSES);

    return annotations.toArray(new ArticleAnnotation[annotations.size()]);
  }

  /**
   * Replaces the Annotation (indicated by the <code>srcAnnotationId</code> DOI)
   * with a new Annotation of type <code>newAnnotationClassType</code>.
   * Convertion requires that a new class type be used, so the old annotation
   * properties are copied over to the new type, even though the DOI remains the same.
   * <p/>
   * The given newAnnotationClassType should implement the interface ArticleAnnotation.
   * Known annotation classes that implement this interface are Comment, FormalCorrection,
   * MinorCorrection, and Retraction.
   *
   * @param srcAnnotationId the DOI of the annotation to convert
   * @param newAnnotationClassType the Class of the new annotation type.
   * Should implement ArticleAnnotation
   *
   * @return the id of the annotation, identical to <code>srcAnnotationId</code>
   *
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String convertAnnotationToType(final String srcAnnotationId,
      final Class<? extends ArticleAnnotation> newAnnotationClassType) throws Exception {

    ArticleAnnotation srcAnnotation = session.get(ArticleAnnotation.class, srcAnnotationId);
    if (srcAnnotation == null) {
      log.error("Annotation was null for id: " + srcAnnotationId);
      return null;
    }

    ArticleAnnotation newAn = newAnnotationClassType.newInstance();
    BeanUtils.copyProperties(newAn, srcAnnotation);

    srcAnnotation.setBody(null);
    session.delete(srcAnnotation);
    session.flush();

    return session.saveOrUpdate(newAn); // Should return value equal to srcAnnotationId
  }

  /**
   * Sets the article-annotation cache.
   *
   * @param articleAnnotationCache The Article(transformed)/ArticleInfo/Annotation/Citation cache
   *                               to use.
   */
  @Required
  public void setArticleAnnotationCache(Cache articleAnnotationCache) {
    this.articleAnnotationCache = articleAnnotationCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      articleAnnotationCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * Create an annotation.
   *
   * @param target target that an annotation is being created for
   * @param context context
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title
   * @param mimeType mimeType
   * @param body body
   * @param isPublic isPublic
   * @param user
   * @throws Exception on an error
   * @return unique identifier for the newly created annotation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createComment(final String target, final String context,
                              final String olderAnnotation, final String title,
                              final String mimeType, final String body,
                              final boolean isPublic, AmbraUser user) throws Exception {

    if (log.isDebugEnabled()) {
      log.debug("creating Comment for target: " + target + "; context: " + context +
                "; supercedes: " + olderAnnotation + "; title: " + title + "; mimeType: " +
                mimeType + "; body: " + body + "; isPublic: " + isPublic);
    }

    String annotationId = createAnnotation(Comment.class, mimeType, target, context,
                                           olderAnnotation, title, body, true, user);

    if (log.isDebugEnabled()) {
      log.debug("Comment created with ID: " + annotationId + " for user: " + user +
                " for IP: " + ServletActionContext.getRequest().getRemoteAddr());
    }

    return annotationId;
  }

  /**
   * Set the annotation as public.
   *
   * @param annotationDoi annotationDoi
   *
   * @throws OtmException if some error occurred
   * @throws SecurityException if a security policy prevented this operation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void setPublicPermissions(final String annotationDoi)
  throws OtmException, SecurityException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    permissionsService.grant(annotationDoi,
                             new String[] { AnnotationsPEP.GET_ANNOTATION_INFO}, everyone);

    permissionsService.revoke(annotationDoi,
                              new String[] { AnnotationsPEP.DELETE_ANNOTATION,
                                             AnnotationsPEP.SUPERSEDE },
                              everyone);
  }

  @Transactional(rollbackFor = { Throwable.class })
  public void cancelPublicPermissions(final String annotationDoi)
                throws OtmException, SecurityException {
    final String[] everyone = new String[]{Constants.Permission.ALL_PRINCIPALS};
    permissionsService.cancelGrants(annotationDoi,
                                    new String[] { AnnotationsPEP.GET_ANNOTATION_INFO }, everyone);

    permissionsService.cancelRevokes(annotationDoi,
                                     new String[] { AnnotationsPEP.DELETE_ANNOTATION,
                                                    AnnotationsPEP.SUPERSEDE },
                                     everyone);
  }

  /**
   * Create a flag against an annotation or a reply
   *
   * @param target target that a flag is being created for
   * @param reasonCode reasonCode
   * @param body body
   * @param mimeType mimeType
   * @param user Logged in user
   * @return unique identifier for the newly created flag
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createFlag(final String target, final String reasonCode,
                           final String body, final String mimeType, AmbraUser user) throws Exception {
    final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
    return createComment(target, null, null, null, mimeType, flagBody, true, user);
  }

  private class Invalidator extends AbstractObjectListener {
    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object o,
                              Updates updates) {
      handleEvent(id, o, updates, false);
    }

    @Override
    public void objectRemoved(Session session, ClassMetadata cm, String id, Object o) {
      handleEvent(id, o, null, true);
    }

    private void handleEvent(String id, Object o, Updates updates, boolean removed) {
      if ((o instanceof Article) && removed) {
        if (log.isDebugEnabled())
          log.debug("Invalidating annotation list for the article that was deleted.");
        articleAnnotationCache.remove(ANNOTATED_KEY + id);
      } else if (o instanceof ArticleAnnotation) {
        if (log.isDebugEnabled())
          log.debug("ArticleAnnotation changed/deleted. Invalidating annotation list " +
              " for the target this was annotating or is about to annotate.");
        articleAnnotationCache.remove(ANNOTATED_KEY +
                                      ((ArticleAnnotation)o).getAnnotates().toString());
        if ((updates != null) && updates.isChanged("annotates")) {
           List<String> v = updates.getOldValue("annotates");
           if (v.size() == 1)
             articleAnnotationCache.remove(ANNOTATED_KEY + v.get(0));
        }
        if (removed)
          articleAnnotationCache.remove(ANNOTATED_KEY + id);
      }
    }
  }
}
