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

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.ApplicationException;
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
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.user.AmbraUser;
import org.topazproject.ambra.xacml.AbstractSimplePEP;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Query;
import org.topazproject.otm.query.Results;
import org.topazproject.otm.Interceptor.Updates;
import org.topazproject.otm.Session.FlushMode;

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
  private static final BeanUtilsBean beanUtils;

  static {
    ALL_ANNOTATION_CLASSES.add(ArticleAnnotation.class);
    beanUtils = new BeanUtilsBean();
  }

  @Required
  public void setAnnotationsPdp(PDP pdp) {
    pep  = new AnnotationsPEP(pdp);
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
   * @param annotationClass the class of annotation
   * @param mimeType mimeType of the annotation body
   * @param target target of this annotation
   * @param context context the context within the target that this applies to
   * @param olderAnnotation olderAnnotation that the new one will supersede
   * @param title title of this annotation
   * @param body body of this annotation
   * @param ciStatement competing interest statement of this annotation
   * @param isPublic to set up public permissions
   *
   * @param user logged in user
   * @return a the new annotation id
   *
   * @throws Exception on an error
   */
  private String createAnnotation(Class<? extends ArticleAnnotation> annotationClass,
                                  final String mimeType, final String target,
                                  final String context, final String olderAnnotation,
                                  final String title, final String body, final String ciStatement,
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
    blob.setCIStatement(ciStatement);
    blob.setBody(body.getBytes(getEncodingCharset()));

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

    if (a != null) {
      String pp[];
      if (a.getBody() != null) {
        pp = new String[] { a.getBody().getId()};
      } else {
        pp = new String[] {};
      }

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
  private List<String> lookupAnnotations(final String target)
      throws OtmException {
    // This flush is so that our own query cache reflects change.
    // TODO : implement query caching in OTM and let it manage this cached query
    if (session.getFlushMode().implies(FlushMode.always))
      session.flush();

    // lock @ Article level
    final Object lock = (FetchArticleService.ARTICLE_LOCK + target).intern();

    return articleAnnotationCache.get(ANNOTATED_KEY + target, -1,
        new Cache.SynchronizedLookup<List<String>, OtmException>(lock) {
          @Override
          public List<String> lookup() throws OtmException {
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

  // TODO: merge with getFeedAnnotationIds method
  @SuppressWarnings("unchecked")
  private List<String> loadAnnotations(final String target, final String mediator,
      final int state, final Set<Class<?extends ArticleAnnotation>> classTypes)
    throws OtmException {

    List<String> annotationIds = new ArrayList<String>();

    for (Class<? extends ArticleAnnotation> anClass : classTypes) {

      Results results = buildQuery(target, mediator, state, anClass).execute();

      while (results.next())
        annotationIds.add(results.getURI(0).toString());
    }

    if (log.isDebugEnabled()) {
      log.debug("retrieved " + annotationIds.size() + " annotations for target: " + target);
    }

    return annotationIds;
  }

  private Query buildQuery(String target, String mediator, int state, Class<? extends ArticleAnnotation> anClass) {
    StringBuilder queryString = new StringBuilder();
    queryString.append("select an.id from ");
    queryString.append(anClass.getSimpleName());
    queryString.append(" an where ");

    boolean multipleConditions = false;

    if (target != null) {
      queryString.append("an.annotates = :target");
      multipleConditions = true;
    }

    if (mediator != null) {
      if (multipleConditions)
        queryString.append(" and ");
      queryString.append("an.mediator = :mediator");
      multipleConditions = true;
    }

    if (state != -1) {
      if (multipleConditions)
        queryString.append(" and ");
      if (state == 0) {
        queryString.append("an.state != 0");
      } else {
        queryString.append("an.state = :state");
      }
    }

    queryString.append(';');

    Query query = session.createQuery(queryString.toString());
    if (target != null) {
      query.setParameter("target", target);
    }

    if (mediator != null) {
      query.setParameter("mediator", mediator);
    }

    if (state > 0) {
      query.setParameter("state", state);
    }
    return query;
  }

  /**
   * Get a list of all annotation Ids satifying the given criteria. All caching is done
   * at the object level by the session.
   *
   * @param startDate  search for annotation after start date.
   * @param endDate    is the date to search until. If null, search until present date
   * @param annotTypes List of annotation types
   * @param maxResults the maximum number of results to return, or 0 for no limit
   *
   * @return the (possibly empty) list of article ids.
   *
   * @throws ParseException   if any of the dates or query could not be parsed
   * @throws URISyntaxException  if an element of annotType cannot be parsed as a URI
   */
  @Transactional(readOnly = true)
  public List<String> getFeedAnnotationIds(Date startDate, Date endDate, Set<String> annotTypes,
                                       int maxResults)
  throws ParseException, URISyntaxException {

    // create the query, applying parameters
    Query annotationQuery = getFeedAnnotationQuery(startDate, endDate, annotTypes, maxResults);

    Results annotationResults = annotationQuery.execute();
    List<String> res = new ArrayList<String>();

    while (annotationResults.next()) {
      URI id = annotationResults.getURI(0);
      res.add(id.toString());
    }

    return res;
  }


  /**
   * Get a list of all reply Ids satifying the given criteria. All caching is done
   * at the object level by the session.
   *
   * @param startDate  search for replies after start date.
   * @param endDate    is the date to search until. If null, search until present date
   * @param annotTypes List of annotation types
   * @param maxResults the maximum number of results to return, or 0 for no limit
   *
   * @return the (possibly empty) list of article ids.
   *
   * @throws ParseException   if any of the dates or query could not be parsed
   * @throws URISyntaxException  if an element of annotType cannot be parsed as a URI
   */
  @Transactional(readOnly = true)
  public List<String> getReplyIds(Date startDate, Date endDate, Set<String> annotTypes, int maxResults)
  throws ParseException, URISyntaxException {

    // create the query, applying parameters
    Query query = getReplyQuery(startDate, endDate, annotTypes, maxResults);

    Results results = query.execute();
    List<String> res = new ArrayList<String>();

    while (results.next()) {
      URI id = results.getURI(0);
      URI annotationId = results.getURI(2);
      try {
        // apply access-controls
        pep.checkAccess(AnnotationsPEP.LIST_ANNOTATIONS, annotationId);
        res.add(id.toString());
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering reply " + id + " from Annotation list due to PEP SecurityException", se);
      }
    }

    return res;
  }

  private Query getReplyQuery(Date startDate, Date endDate, Set<String> annotTypes,
                              int maxResults)
      throws URISyntaxException {

    String queryString = createReplyQueryString(startDate, endDate, annotTypes, maxResults);
    Query query = session.createQuery(queryString);

    bindCommonParams(startDate, endDate, annotTypes, query);
    return query;
  }

  private Query getFeedAnnotationQuery(Date startDate, Date endDate, Set<String> annotTypes,
                                   int maxResults)
      throws URISyntaxException {
    String queryString = createFeedAnnotationQueryString(startDate, endDate, annotTypes, maxResults);
    Query query = session.createQuery(queryString);

    bindCommonParams(startDate, endDate, annotTypes, query);
    return query;
  }

  private void bindCommonParams(Date startDate, Date endDate, Set<String> annotTypes, Query query)
      throws URISyntaxException {

    if (startDate != null)
      query.setParameter("sd",  startDate);
    if (endDate != null)
      query.setParameter("ed",  endDate);

    if (annotTypes != null) {
      int i = 0;
      for (String type : annotTypes) {
        query.setUri("type" + i++, URI.create(type));
      }
    }
  }

  private String createFeedAnnotationQueryString(Date startDate, Date endDate, Set<String> annotTypes,
                                             int maxResults) {

    StringBuilder qry = new StringBuilder();

    qry.append("select a.id id, cr from Annotation a, Article ar ")
       .append("where a.annotates = ar and cr := a.created ")
       .append("and (a.<rdf:type> = <").append(Annotation.RDF_TYPE).append("> ")
       .append("minus (a.<rdf:type> = <").append(Annotation.RDF_TYPE).append("> ")
       .append("and a.<rdf:type> = <").append(RatingSummary.RDF_TYPE).append(">)) ") ;

    appendCommonCriteria(startDate, endDate, annotTypes, maxResults, qry);

    qry.append(';');

    return qry.toString();
  }

  // For now only fetches all Replys
  private String createReplyQueryString(Date startDate, Date endDate, Set<String> annotTypes,
                                        int maxResults) {

    StringBuilder qry = new StringBuilder();

    qry.append("select r.id id, cr, a.id annid from Reply r, ArticleAnnotation a, Article ar ")
       .append("where cr := r.created and r.root = a and a.annotates = ar ");

    appendCommonCriteria(startDate, endDate, annotTypes, maxResults, qry);

    qry.append(';');

    return qry.toString();
  }

  private void appendCommonCriteria(Date startDate, Date endDate, Set<String> annotTypes,
                                    int maxResults, StringBuilder qry) {
    // apply date constraints
    if (startDate != null)
      qry.append("and ge(cr, :sd) ");
    if (endDate != null)
      qry.append("and le(cr, :ed) ");


    // match all types
    if (annotTypes != null && annotTypes.size() > 0) {
      qry.append("and (");
      for (int i = 0; i < annotTypes.size(); i++)
        qry.append("a.<rdf:type> = :type").append(i).append(" or ");
      qry.setLength(qry.length() - 4);
      qry.append(") ");
    }
    // add ordering and limit
    qry.append("order by cr desc, id asc");

    if (maxResults > 0)
      qry.append(" limit ").append(maxResults);
  }

  /**
   * Get all article annotations specified by the list of annotation ids.
   *
   * @param annotIds a list of annotations Ids to retrieve.
   *
   * @return the (possibly empty) list of annotations.
   *
   */
  // TODO: this method can be declared private
  @Transactional(readOnly = true)
  public List<ArticleAnnotation> getArticleAnnotations(List<String> annotIds) {
    List<ArticleAnnotation> annotations = new ArrayList<ArticleAnnotation>();

    for (String id : annotIds)  {
      try {
        annotations.add(getArticleAnnotation(id));
      } catch (IllegalArgumentException iae) {
        if (log.isDebugEnabled())
          log.debug("Ignored illegale annotation id:" + id);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }
    return annotations;
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
  public List<Annotation> getAnnotations(List<String> annotIds) {
    List<Annotation> annotations = new ArrayList<Annotation>();

    for (String id : annotIds)  {
      try {
        annotations.add(getAnnotation(id));
      } catch (IllegalArgumentException iae) {
        if (log.isDebugEnabled())
          log.debug("Ignored illegale annotation id:" + id);
      } catch (SecurityException se) {
        if (log.isDebugEnabled())
          log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", se);
      }
    }
    return annotations;
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

    List<ArticleAnnotation> allAnnotations = getArticleAnnotations(lookupAnnotations(target));
    List<ArticleAnnotation> filtered = filterAnnotations(allAnnotations, annotationClassTypes);

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
  public ArticleAnnotation getArticleAnnotation(final String annotationId)
    throws OtmException, SecurityException, IllegalArgumentException {
    pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, URI.create(annotationId));
    // comes from object-cache.
    ArticleAnnotation   a = session.get(ArticleAnnotation.class, annotationId);
    if (a == null)
      throw new IllegalArgumentException("invalid annotation id: " + annotationId);

    return a;
  }

  /**
   * Loads the annotation with the given id.
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
  public Annotation getAnnotation(final String annotationId)
    throws OtmException, SecurityException, IllegalArgumentException {
    pep.checkAccess(AnnotationsPEP.GET_ANNOTATION_INFO, URI.create(annotationId));
    // comes from object-cache.
    Annotation a = session.get(Annotation.class, annotationId);
    if (a == null)
      throw new IllegalArgumentException("invalid annotation id: " + annotationId);

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
    List<ArticleAnnotation> annotations = getArticleAnnotations(
        loadAnnotations(null, mediator, state, ALL_ANNOTATION_CLASSES));
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

    if (newAnnotationClassType.isInstance(srcAnnotation)) {
      if (log.isDebugEnabled())
        log.debug("Cannot convert " + srcAnnotationId + " to "
            + newAnnotationClassType.getSimpleName()
            + ". It is already " + srcAnnotation.getClass().getSimpleName());
      return srcAnnotationId;
    }

    // Trying to catch #1108
    if (srcAnnotation.getBody() == null)
      throw new ApplicationException("Trying to copy annotation " + srcAnnotationId +
          " that has NULL body");

    ArticleAnnotation newAn = newAnnotationClassType.newInstance();
    synchronized (beanUtils) {
      beanUtils.copyProperties(newAn, srcAnnotation);
    }

    srcAnnotation.setBody(null);
    session.delete(srcAnnotation);
    session.flush();

    // Trying to catch #1108
    if (newAn.getBody() == null)
      throw new ApplicationException("Trying to save annotation " + newAn.getId().toString()
          + " with NULL body");

    if (session.get(ArticleAnnotation.class, srcAnnotationId) != null)
      throw new ApplicationException("Delete didn't really delete " + srcAnnotationId 
          + ". It could happen if this object is reachable via associations thru other objects.");

    String newId = session.saveOrUpdate(newAn);
    session.flush();

    // Trying to catch #1108
    ArticleAnnotation newAnnotation = session.get(ArticleAnnotation.class, newId);
    if (newAnnotation.getBody() == null)
      throw new ApplicationException("Annotation " + newId + " saved with NULL body");

    return newId; // Should return value equal to srcAnnotationId
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
   * @param ciStatement competing interesting statement
   * @param isPublic isPublic
   * @param user logged in user
   * @throws Exception on an error
   * @return unique identifier for the newly created annotation
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String createComment(final String target, final String context,
                              final String olderAnnotation, final String title,
                              final String mimeType, final String body, final String ciStatement,
                              final boolean isPublic, AmbraUser user) throws Exception {

    if (log.isDebugEnabled()) {
      log.debug("creating Comment for target: " + target + "; context: " + context +
                "; supercedes: " + olderAnnotation + "; title: " + title + "; mimeType: " +
                mimeType + "; body: " + body + "; ciStatment: " + ciStatement + "; isPublic: " + isPublic);
    }

    String annotationId = createAnnotation(Comment.class, mimeType, target, context,
                                           olderAnnotation, title, body, ciStatement, true, user);

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
    //TODO: Is assigning null to the competing interest statement OK here?
    return createComment(target, null, null, null, mimeType, flagBody, null, true, user);
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
