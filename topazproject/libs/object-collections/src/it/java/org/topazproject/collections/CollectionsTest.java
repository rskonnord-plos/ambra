/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.collections;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.stores.ItqlStore;

import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.criterion.Order;

import static org.topazproject.collections.SmartCollection.ALL;
import static org.topazproject.collections.SmartCollection.ANY;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;

public class CollectionsTest {
  private static final Log log = LogFactory.getLog(CollectionsTest.class);
  private SessionFactory factory = new SessionFactory();
  private Session session = null;
  private Transaction tx = null;

  @Entity(type = Rdf.topaz + "Article", model = "coltest")
  public static class Article {
    public Article() {}
    public Article(String id, String author, String category) {
      this.id = URI.create(id);
      this.author = author;
      this.category = category;
    }

    @Id
    public URI id;
    @Predicate(uri = Rdf.topaz + "author")
    public String author;
    @Predicate(uri = Rdf.topaz + "category")
    public String category;
  }

  @BeforeClass
  public void setUpFactory() throws OtmException {
    factory.setTripleStore(
      new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")));

    ModelConfig coltest =
      new ModelConfig("coltest", URI.create("local:///topazproject#coltest"), null);
    factory.addModel(coltest);

    try {
      factory.getTripleStore().dropModel(coltest);
    } catch (Throwable t) {
      log.debug("Failed to drop model 'coltest'", t);
    }

    factory.getTripleStore().createModel(coltest);

    session = factory.openSession();
    tx = session.beginTransaction();

    factory.preload(Article.class);
    session.saveOrUpdate(new Article("art:1", "Einstein", "atom"));
    session.saveOrUpdate(new Article("art:2", "Einstein", "dancing"));
    session.saveOrUpdate(new Article("art:3", "Oppenheimer", "physics"));
    session.saveOrUpdate(new Article("art:4", "Oppenheimer", "dancing"));
    session.saveOrUpdate(new Article("art:5", "Bush", "iraq"));
    session.saveOrUpdate(new Article("art:6", "Clinton", "monica"));
    session.saveOrUpdate(new Article("art:7", "Hala", "dancing"));
    session.saveOrUpdate(new Article("art:8", "Shawn", "cooking"));
  }

  @AfterClass
  public void tearDownTx() throws OtmException {
    if (tx == null) {
      session.close();
      return;
    }

    try {
      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }
    }

    session.close();
  }

  /**
   * Simple collection of 3 articles
   */
  @Test
  public void simpleTest() throws OtmException {
    SimpleCollection<Article> articles = new SimpleCollection<Article>(session, Article.class);
    articles.addIds(new String[] { "art:1", "art:5", "art:7" });
    assert articles.size() == 3;
  }

  /**
   * SmartCollection:
   *    ((author is "Einstein") or (author is "Oppenheimer") and (category is "dancing")
   */
  @Test
  public void smartTest() throws OtmException {
    SmartCollection<Article> c = new SmartCollection<Article>(session, Article.class, ALL);
    SmartCollection<Article> a = new SmartCollection<Article>(session, Article.class, ANY);

    Criterion einstein    = Restrictions.eq("author", "Einstein");
    Criterion oppenheimer = Restrictions.eq("author", "Oppenheimer");
    Criterion dancing     = Restrictions.eq("category", "dancing");

    a.add(einstein).add(oppenheimer); // ANY = or
    c.add(a).add(dancing); // ALL = and
    assert c.size() == 2;
  }


  /**
   * SmartCollection that contains a SimpleCollection:
   *   (category is "dancing") or ("art:2", "art:5")
   */
  @Test
  public void smartContainingSimpleTest() throws OtmException {
    SimpleCollection<Article> articles = new SimpleCollection<Article>(session, Article.class);
    articles.addIds(new String[] { "art:2", "art:5" });

    SmartCollection<Article> c = new SmartCollection<Article>(session, Article.class, ANY);
    Criterion dancing = Restrictions.eq("category", "dancing");

    c.add(dancing).add(articles);
//    c.getCriteria().addOrder(Order.asc("id")); // can't run order on id field at the moment
    assert c.size() == 4;

    ArrayList<String> artArray = new ArrayList<String>();
    for (Article a: c)
      artArray.add(a.id.toString());

    String[] artsDesired = new String[] { "art:2", "art:4", "art:5", "art:7" };
    String[] artsFound = artArray.toArray(new String[0]);
    Arrays.sort(artsFound); // TODO: Just add orderBy clause to c.getCriteria()
    assert Arrays.equals(artsDesired, artsFound);
  }
}
