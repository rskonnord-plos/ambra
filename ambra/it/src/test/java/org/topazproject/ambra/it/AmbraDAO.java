/* $HeadURL::                                                                                    $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.ambra.it;

import java.net.URI;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.topazproject.ambra.models.Aggregation;
import org.topazproject.ambra.models.Annotation;
import org.topazproject.ambra.models.Annotea;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.AuthenticationId;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.Comment;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.EditorialBoard;
import org.topazproject.ambra.models.FoafPerson;
import org.topazproject.ambra.models.Issue;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.models.License;
import org.topazproject.ambra.models.ObjectInfo;
import org.topazproject.ambra.models.PLoS;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.models.RatingContent;
import org.topazproject.ambra.models.RatingSummary;
import org.topazproject.ambra.models.RatingSummaryContent;
import org.topazproject.ambra.models.Reply;
import org.topazproject.ambra.models.ReplyThread;
import org.topazproject.ambra.models.Trackback;
import org.topazproject.ambra.models.TrackbackContent;
import org.topazproject.ambra.models.UserAccount;
import org.topazproject.ambra.models.UserPreference;
import org.topazproject.ambra.models.UserPreferences;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.ambra.models.UserRole;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.criterion.Disjunction;
import org.topazproject.otm.criterion.EQCriterion;
import org.topazproject.otm.criterion.ExistsCriterion;
import org.topazproject.otm.criterion.GECriterion;
import org.topazproject.otm.criterion.GTCriterion;
import org.topazproject.otm.criterion.LECriterion;
import org.topazproject.otm.criterion.LTCriterion;
import org.topazproject.otm.criterion.MinusCriterion;
import org.topazproject.otm.criterion.NECriterion;
import org.topazproject.otm.criterion.NotCriterion;
import org.topazproject.otm.criterion.NotExistsCriterion;
import org.topazproject.otm.criterion.Order;
import org.topazproject.otm.criterion.Parameter;
import org.topazproject.otm.criterion.TransCriterion;
import org.topazproject.otm.criterion.WalkCriterion;
import org.topazproject.otm.stores.ItqlStore;



/**
 * A mostly read-only access to the otm persistence store to cross
 * check the info displayed by the plosone webapp. Note that sessions
 * are short-lived so that objects are not cached at all.
 *
 * @author Pradeep Krishnan
 */
public class AmbraDAO {
  private static final Log log     = LogFactory.getLog(AmbraDAO.class);
  private SessionFactory   factory = new SessionFactoryImpl();

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public AmbraDAO() throws OtmException {
    initFactory();
  }

  /**
   * DOCUMENT ME!
   *
   * @param doi DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Article getArticle(final String doi) {
    return doInSession(new Action<Article>() {
        public Article run(Session session) throws OtmException {
          return session.get(Article.class, doi);
        }
      });
  }

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected void initFactory() throws OtmException {
    log.info("initializing otm session factory ...");

    String[] models =
      new String[] { "ri", "users", "grants", "revokes", "preferences", "profiles", "criteria" };

    for (String model : models)
      factory.addModel(new ModelConfig(model,
                                       URI.create("local:///topazproject#filter:model=" + model),
                                       URI.create("http://topazproject.org/models#filter")));

    URI storeUri = URI.create("rmi://localhost/topazproject");
    factory.setTripleStore(new ItqlStore(storeUri));

    Class[] classes =
      new Class[] {
                    Annotation.class, Annotea.class, Article.class, AuthenticationId.class,
                    Category.class, Citation.class, Comment.class, DublinCore.class,
                    FoafPerson.class, ObjectInfo.class, PLoS.class, Rating.class,
                    RatingContent.class, RatingSummary.class, RatingSummaryContent.class,
                    License.class, Reply.class, ReplyThread.class, UserAccount.class,
                    UserPreference.class, UserPreferences.class, UserProfile.class, UserRole.class,
                    Journal.class, Issue.class, Aggregation.class, EditorialBoard.class,
                    Trackback.class, TrackbackContent.class, // criteria classes
      Conjunction.class, Criterion.class, DetachedCriteria.class, Disjunction.class,
                    EQCriterion.class, ExistsCriterion.class, GECriterion.class, GTCriterion.class,
                    LECriterion.class, LTCriterion.class, MinusCriterion.class, NECriterion.class,
                    NotCriterion.class, NotExistsCriterion.class, Order.class, Parameter.class,
                    TransCriterion.class, WalkCriterion.class,
      };

    for (Class c : classes)
      factory.preload(c);
  }

  /**
   * Run the given action within a session.
   *
   * @param action the action to run
   *
   * @return the value returned by the action
   *
   * @throws OtmException DOCUMENT ME!
   */
  protected <T> T doInSession(Action<T> action) throws OtmException {
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction();

      T ret = action.run(session);
      tx.commit(); // Flush happens automatically

      return ret;
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e;
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }

/**
   * The interface actions must implement.
   */
  protected static interface Action<T> {
    /**
     * This is run within the context of a session.
     *
     * @param session the current transaction
     *
     * @return DOCUMENT ME!
     *
     * @throws OtmException DOCUMENT ME!
     */
    T run(Session session) throws OtmException;
  }
}
