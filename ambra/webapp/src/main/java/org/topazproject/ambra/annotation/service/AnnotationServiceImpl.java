/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */

package org.topazproject.ambra.annotation.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts2.ServletActionContext;
import org.hibernate.*;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.annotation.FlagUtil;
import org.topazproject.ambra.models.*;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.ambra.user.AmbraUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;

/**
 * @author Alex Kudlick Date: 4/29/11
 *         <p/>
 *         org.topazproject.ambra.annotation.service
 */
public class AnnotationServiceImpl extends BaseAnnotationServiceImpl implements AnnotationService {
  private static final Logger log = LoggerFactory.getLogger(AnnotationServiceImpl.class);
  /**
   * The PEP is just used to check permissions
   */

  protected static final Set<Class<? extends ArticleAnnotation>> ALL_ANNOTATION_CLASSES = new HashSet<Class<? extends ArticleAnnotation>>();
  private PermissionsService permissionsService;

  static {
    ALL_ANNOTATION_CLASSES.add(ArticleAnnotation.class);
  }

  @Required
  public void setPermissionsService(PermissionsService ps) {
    permissionsService = ps;
  }

  @Override
  public Set<Class<? extends ArticleAnnotation>> getAllAnnotationClasses() {
    return ALL_ANNOTATION_CLASSES;
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public void deleteAnnotation(String annotationId, String authId) throws SecurityException {
    permissionsService.checkRole(PermissionsService.ADMIN_ROLE, authId);
    //We need to call getAnnotation() because we don't know the class to which the id corresponds
    Annotation annotation = getAnnotation(annotationId);
    hibernateTemplate.delete(annotation);
//    hibernateTemplate.evict(annotation);
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<String> getFeedAnnotationIds(final Date startDate, final Date endDate,
      final Set<String> annotTypes, final int maxResults, final String journal) throws ParseException, URISyntaxException {
    return (List<String>)hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        StringBuilder sb = new StringBuilder();
        sb.append("select " +
            "a.annotationUri " +
            "from " +
            "Annotation a " +
            "join Article ar on a.annotates = ar.articleUri " +
            "join Journal j on ar.eIssn = j.eIssn " +
            "where j.journalKey = :journal");

        if(startDate != null){
          sb.append(" and a.creationDate > :startDate");
        }

        if(endDate != null){
          sb.append(" and a.creationDate < :endDate");
        }

        if(annotTypes != null){
          sb.append(" and a.type in :types");
        } else {
          sb.append(" and a.type != '" + RatingSummary.RDF_TYPE + "'");
        }

        sb.append(" order by a.creationDate desc");

        Query q = session.createSQLQuery(sb.toString());
        q.setParameter("journal", journal);

        if(startDate != null){
          q.setParameter("startDate", startDate);
        }

        if (endDate != null){
          q.setParameter("endDate", endDate);
        }

        if(annotTypes != null){
          q.setParameterList("types", annotTypes);
        }

        if (maxResults > 0) {
          q.setMaxResults(maxResults);
        }

        List results = q.list();
        //This gets around the problem of Hibernate returning URI ids
        List<String> ids = new ArrayList<String>(results.size());
        for (Object o : results) {
          String id = (String) o;
          // apply access-controls
          try {
            ids.add(id);
          } catch (SecurityException se) {
            if (log.isDebugEnabled())
              log.debug("Filtering reply " + id + " from Annotation list due to PEP SecurityException", se);
          }
        }

        return ids;
      }
    });
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<String> getReplyIds(final Date startDate, final Date endDate,
      final Set<String> annotTypes, final int maxResults, final String journal) throws ParseException, URISyntaxException {
    //We want all replies between the dates whose "root" property is an article annotation of one of the given types
    //Query for "roots"

    return (List<String>)hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

       StringBuilder sb = new StringBuilder();
        sb.append("select " +
            "r.replyUri " +
            "from " +
            "Reply r " +
            "join Annotation a on a.annotationUri = r.root " +
            "join Article ar on a.annotates = ar.articleUri " +
            "join Journal j on ar.eIssn = j.eIssn " +
            "where j.journalKey = :journal");

        if(startDate!=null){
          sb.append(" and a.creationDate > :startDate");
        }

        if(endDate!=null){
          sb.append(" and a.creationDate < :endDate");
        }

        if(annotTypes!=null){
            sb.append(" and a.type = :type");
        }

        sb.append(" order by a.creationDate desc");

        Query q = session.createSQLQuery(sb.toString());
        q.setParameter("journal", journal);

        if(startDate!=null){
          q.setParameter("startDate", startDate);
        }

        if (endDate!=null){
          q.setParameter("endDate", endDate);
        }

        if(annotTypes!=null){
          for (String type : annotTypes) {
            q.setParameter("type", type);
          }
        }

        if (maxResults > 0) {
          q.setMaxResults(maxResults);
        }

        List<Object> results = q.list();

        List<String> ids = new ArrayList<String>(results.size());
        for (Object row : results) {
          try {
            ids.add(row.toString());
          } catch (SecurityException se) {
            if (log.isDebugEnabled())
              log.debug("Filtering reply " + row + " from Annotation list due to PEP SecurityException", se);
          }
        }
        return ids;
      }
    });
  }

  @Override
  @Transactional(readOnly = true)
  public List<Annotation> getAnnotations(List<String> annotIds) {
    List<Annotation> annotations = new ArrayList<Annotation>(annotIds.size());
    for (String id : annotIds) {
      try {
        annotations.add(getAnnotation(id));
      } catch (SecurityException e) {
        log.debug("Filtering URI " + id + " from Article list due to PEP SecurityException", e);
      } catch (IllegalArgumentException e) {
        log.debug("Ignored illegal annotation id:" + id);
      }
    }
    return annotations;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public ArticleAnnotation[] listAnnotations(final String target,
      final Set<Class<? extends ArticleAnnotation>> annotationClassTypes) throws SecurityException {
    return (ArticleAnnotation[])hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List<ArticleAnnotation> annotations = new ArrayList<ArticleAnnotation>();

        if(annotationClassTypes == null || annotationClassTypes.size() == 0) {
          annotations.addAll(session
            .createCriteria(ArticleAnnotation.class)
            .add(Restrictions.eq("annotates", URI.create(target)))
            .addOrder(Order.asc("created"))
            .list());
        } else {
          for (Class<? extends ArticleAnnotation> clazz : annotationClassTypes) {
            annotations.addAll(session
              .createCriteria(clazz)
              .add(Restrictions.eq("annotates", URI.create(target)))
              .list());
          }
        }

        return annotations.toArray(new ArticleAnnotation[annotations.size()]);
      }
    });
  }


  @Override
  @Transactional(readOnly = true)
  public ArticleAnnotation getArticleAnnotation(String annotationId) throws SecurityException, IllegalArgumentException {
    Annotation annotation = getAnnotation(annotationId);
    if (!(annotation instanceof ArticleAnnotation)) {
      throw new IllegalArgumentException("Annotation corresponding to id: "
          + annotationId + " wasn't an article annotation");
    }
    return (ArticleAnnotation) annotation;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public Annotation getAnnotation(final String annotationId) throws SecurityException, IllegalArgumentException {
    //We have to create a criteria instead of calling session.get()
    // because we don't know what class the id corresponds to

    return (Annotation)hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Annotation annotation;

        try {
          annotation = (Annotation) session.createCriteria(Annotation.class)
              .add(Restrictions.eq("id", URI.create(annotationId)))
              .list().get(0);
        } catch (IndexOutOfBoundsException e) {
          //This means the list was empty
          throw new IllegalArgumentException("No annotation for specified id: " + annotationId);
        }
        return annotation;
      }
    });
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public void updateBodyAndContext(String id, String body, String context, String authId) throws SecurityException, IllegalArgumentException, UnsupportedEncodingException {
    permissionsService.checkLogin(authId);

    ArticleAnnotation annotation = getArticleAnnotation(id);
    if (annotation.getBody() == null) {
      annotation.setBody(new AnnotationBlob());
    }
    annotation.getBody().setBody(body.getBytes(getEncodingCharset()));
    annotation.setContext(context);
    hibernateTemplate.update(annotation);
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public ArticleAnnotation[] listAnnotations(final String mediator, final int state) throws SecurityException {
    return (ArticleAnnotation[])hibernateTemplate.execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        List results = session.createCriteria(ArticleAnnotation.class)
            .add(Restrictions.eq("mediator", mediator))
            .add(Restrictions.eq("state", state))
            .list();

        return results.toArray(new ArticleAnnotation[results.size()]);
      }
    });
  }


  @Override
  @Transactional(rollbackFor = Throwable.class)
  @SuppressWarnings("unchecked")
  public String convertAnnotationToType(final String srcAnnotationId, Class<? extends ArticleAnnotation> newAnnotationClassType) throws Exception {
    final String newClassName = newAnnotationClassType.getSimpleName();
    final String newType = (String) newAnnotationClassType.getMethod("getType").invoke(newAnnotationClassType.newInstance());
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        session.doWork(new Work() {
          @Override
          public void execute(Connection connection) throws SQLException {
            Statement statement = connection.createStatement();
            statement.executeUpdate(
                "update Annotation set class = '" + newClassName + "'," +
                    "type = '" +  newType  + "'" +
                    " where annotationUri = '" + srcAnnotationId + "';");
            statement.close();
          }
        });
        return null;
      }
    });

    Annotation newAnnotation = getAnnotation(srcAnnotationId);

    if (newAnnotation instanceof FormalCorrection) {
      HibernateAnnotationUtil.createDefaultCitation((FormalCorrection)newAnnotation, getArticleCitation((ArticleAnnotation) newAnnotation), hibernateTemplate);
	  } else if (newAnnotation instanceof Retraction) {
      HibernateAnnotationUtil.createDefaultCitation((Retraction)newAnnotation, getArticleCitation((ArticleAnnotation) newAnnotation), hibernateTemplate);
 	 	}
    
    return srcAnnotationId;
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public String createAnnotation(Class<? extends ArticleAnnotation> annotationClass, String mimeType, String target,
                                 String context, String olderAnnotation, String title, String body, String ciStatement,
                                 boolean isPublic, AmbraUser user) throws Exception {

    final String contentType = getContentType(mimeType);
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

    String newId = hibernateTemplate.save(annotation).toString();
    log.info("Created Annotation of type: " + annotationClass.getSimpleName() + " with generated id: " + newId);
    return newId;
  }

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public String createComment(String target, String context, String olderAnnotation, String title, String mimeType,
                              String body, String ciStatement, boolean isPublic, AmbraUser user) throws Exception {
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

  @Override
  @Transactional(rollbackFor = Throwable.class)
  public String createFlag(String target, String reasonCode, String body, String mimeType, AmbraUser user) throws Exception {
    final String flagBody = FlagUtil.createFlagBody(reasonCode, body);
    return createComment(target, null, null, null, mimeType, flagBody, null, true, user);
  }

  private Citation getArticleCitation(ArticleAnnotation annotation) {
    Article article = (Article) hibernateTemplate.get(Article.class, annotation.getAnnotates());

    if (article == null){
      throw new IllegalArgumentException("Article " + annotation.getAnnotates() + " not found");
    }
    return article.getDublinCore().getBibliographicCitation();
  }
}
