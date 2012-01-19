/* $HeadURL::                                                                                      $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plosone.it;

import java.net.URI;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plosone.it.jwebunit.PlosOneTestContext;
import org.plosone.it.jwebunit.PlosOneWebTester;
import org.plosone.it.pages.AbstractPage;

import org.plos.models.Aggregation;
import org.plos.models.Annotation;
import org.plos.models.Annotea;
import org.plos.models.Article;
import org.plos.models.AuthenticationId;
import org.plos.models.Category;
import org.plos.models.Citation;
import org.plos.models.Comment;
import org.plos.models.DublinCore;
import org.plos.models.EditorialBoard;
import org.plos.models.FoafPerson;
import org.plos.models.Issue;
import org.plos.models.Journal;
import org.plos.models.License;
import org.plos.models.ObjectInfo;
import org.plos.models.PLoS;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.RatingSummaryContent;
import org.plos.models.Reply;
import org.plos.models.ReplyThread;
import org.plos.models.Trackback;
import org.plos.models.TrackbackContent;
import org.plos.models.UserAccount;
import org.plos.models.UserPreference;
import org.plos.models.UserPreferences;
import org.plos.models.UserProfile;
import org.plos.models.UserRole;

import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
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

import com.gargoylesoftware.htmlunit.BrowserVersion;

import net.sourceforge.jwebunit.util.TestingEngineRegistry;

/**
 * A mostly read-only access to the otm persistence store to cross
 * check the info displayed by the plosone webapp. Note that sessions
 * are short-lived so that objects are not cached at all.
 *
 * @author Pradeep Krishnan
 */
public class PlosOneDAO {
  private static final Log log     = LogFactory.getLog(PlosOneDAO.class);
  private SessionFactory   factory = new SessionFactory();

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
  public PlosOneDAO() throws OtmException {
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

    URI storeUri = URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService");
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
