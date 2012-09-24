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

package org.ambraproject.service.trackback;

import org.ambraproject.models.Article;
import org.ambraproject.models.Trackback;
import org.ambraproject.util.UriUtil;
import org.ambraproject.views.LinkbackView;
import org.apache.commons.configuration.Configuration;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alex Kudlick Date: 5/5/11
 *         <p/>
 *         org.ambraproject.annotation.service
 */
public class TrackbackServiceImpl extends LinkbackServiceImpl implements TrackbackService {
  private static final Logger log = LoggerFactory.getLogger(TrackbackServiceImpl.class);

  private Configuration configuration;

  @Required
  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  protected Configuration getConfiguration() {
    return configuration;
  }

  @Override
  @Transactional(readOnly = true)
  @SuppressWarnings("unchecked")
  public List<LinkbackView> getTrackbacks(final Date startDate, final Date endDate,
                                          final int maxResults, final String journal) {

    /***
     * There may be a more efficient way to do this other than querying the database twice, at some point in time
     * we might improve how hibernate does the object mappings
     *
     * This execute returns trackbackIDs, article DOIs and titles, which are needed to construction the trackbackView
     * object
     */
    Map<Long, String[]> results = (Map<Long, String[]>) hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        /**
         * We have to do this with SQL because of how the mappings are currently defined
         * And hence, there is no way to unit test this
         */

        StringBuilder sqlQuery = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>(3);

        sqlQuery.append("select track.trackbackID, art.doi, art.title ");
        sqlQuery.append("from trackback track ");
        sqlQuery.append("join article art on art.articleID = track.articleID ");
        sqlQuery.append("join journal j on art.eIssn = j.eIssn ");
        sqlQuery.append("where j.journalKey = :journal ");
        params.put("journal", journal);

        if (startDate != null) {
          sqlQuery.append(" and track.created > :startDate");
          params.put("startDate", startDate);
        }

        if (endDate != null) {
          sqlQuery.append(" and track.created < :endDate");
          params.put("endDate", endDate);
        }

        sqlQuery.append(" order by track.created desc");

        SQLQuery query = session.createSQLQuery(sqlQuery.toString());
        query.setProperties(params);

        if (maxResults > 0) {
          query.setMaxResults(maxResults);
        }

        List<Object[]> tempResults = query.list();
        Map<Long, String[]> results = new HashMap<Long, String[]>(tempResults.size());

        for (Object[] obj : tempResults) {
          //This forces this method to return Long values and not BigInteger
          results.put((((Number) obj[0]).longValue()), new String[]{(String) obj[1], (String) obj[2]});
        }

        return results;
      }
    });

    //The previous query puts annotationID and doi into the map. annotationID is key
    //I do this to avoid extra doi lookups later in the code.

    if (results.size() > 0) {
      DetachedCriteria criteria = DetachedCriteria.forClass(org.ambraproject.models.Trackback.class)
          .add(Restrictions.in("ID", results.keySet()))
          .addOrder(Order.desc("created"));

      List<org.ambraproject.models.Trackback> trackbacks = hibernateTemplate.findByCriteria(criteria);
      List<LinkbackView> views = new ArrayList<LinkbackView>(trackbacks.size());

      for (org.ambraproject.models.Trackback track : trackbacks) {
        String articleDoi = results.get(track.getID())[0];
        String articleTitle = results.get(track.getID())[1];
        views.add(new LinkbackView(track, articleDoi, articleTitle));
      }

      return views;
    } else {
      return new ArrayList<LinkbackView>();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional
  public Long createTrackback(String articleDoi, String url, String title, String blogName, String excerpt) throws DuplicateTrackbackException {
    if (articleDoi == null) {
      throw new IllegalArgumentException("No DOI specified");
    } else if (url == null || title == null || excerpt == null || blogName == null) {
      throw new IllegalArgumentException("URL, title, excerpt, and blog name must be provided");
    }

    Long articleId;
    try {
      articleId = (Long) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", articleDoi))
              .setProjection(Projections.id()), 0, 1
      ).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("DOI: " + articleDoi + " didn't correspond to an article");
    }

    List<Long> existingTrackbacks = hibernateTemplate.findByCriteria(
        DetachedCriteria.forClass(Trackback.class)
            .add(Restrictions.eq("articleID", articleId))
            .add(Restrictions.eq("url", url))
            .setProjection(Projections.id())
    );
    if (existingTrackbacks.size() > 0) {
      throw new DuplicateTrackbackException(articleDoi, url);
    } else {
      log.debug("Creating trackback for article: {}; url: {}", articleDoi, url);
      Trackback trackback = new Trackback();
      trackback.setArticleID(articleId);
      trackback.setTitle(title);
      trackback.setBlogName(blogName);
      trackback.setUrl(url);
      trackback.setExcerpt(excerpt);
      return (Long) hibernateTemplate.save(trackback);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean blogLinksToArticle(String blogUrl, String doi) throws IOException {
    final String articleDoi = UriUtil.decodeUtf8(doi);
    //look up the url for the journal in which the article was published
    String eIssn;
    try {
      eIssn = (String) hibernateTemplate.findByCriteria(
          DetachedCriteria.forClass(Article.class)
              .add(Restrictions.eq("doi", articleDoi))
              .setProjection(Projections.property("eIssn")),
          0, 1).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Doi " + articleDoi + " didn't correspond to an article");
    }
    String journalName = fetchJournalName(hibernateTemplate, eIssn);

    final InboundLinkTranslator linkTranslator = InboundLinkTranslator.forJournal(journalName, configuration);
    LinkValidator linkValidator = new LinkValidator() {
      @Override
      public boolean isValid(URL link) {
        return articleDoi.equals(linkTranslator.getDoi(link));
      }
    };
    BlogLinkDigest blogLinkDigest = examineBlogPage(new URL(blogUrl), linkValidator);
    return blogLinkDigest.getLink() != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true)
  public List<LinkbackView> getTrackbacksForArticle(String articleDoi) {
    return getLinkbacksForArticle(Trackback.class, articleDoi);
  }

  @Override
  @Transactional(readOnly = true)
  public int countTrackbacksForArticle(String articleDoi) {
    return countLinkbacksForArticle(Trackback.class, articleDoi);
  }

}
