/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.net.URI;

import java.util.List;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.NoRdfType;
import org.topazproject.otm.samples.PrivateAnnotation;
import org.topazproject.otm.samples.PublicAnnotation;
import org.topazproject.otm.stores.ItqlStore;

import org.topazproject.otm.owl.OwlClass;
import org.topazproject.otm.owl.ObjectProperty;
import org.topazproject.otm.owl.OwlHelper;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class OwlTest {
  private static final Log log = LogFactory.getLog(OwlTest.class);

  private static final String VIN = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#";
  private static final String VINModel = "otmtest";
  private static final URI VINModelUri = URI.create("local:///topazproject#" + VINModel);

  private SessionFactory factory = new SessionFactory();
  private SessionFactory metaFactory = new SessionFactory();

  private static void clearModel(SessionFactory fac, ModelConfig model) throws OtmException {
    try {
      fac.getTripleStore().dropModel(model);
    } catch (Throwable t) {
      log.debug("Failed to drop model '" + model.getId() + "'", t);
    }

    fac.getTripleStore().createModel(model);
  }

  @BeforeClass
  public void setUpFactory() throws OtmException {
    factory.setTripleStore(
      new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")));

    ModelConfig otm = new ModelConfig(VINModel, VINModelUri, null);
    factory.addModel(otm);
    ModelConfig ri = new ModelConfig("ri", URI.create("local:///topazproject#otmtest-ri"), null);
    factory.addModel(ri);

    clearModel(factory, otm);
    clearModel(factory, ri);

    factory.preload(Article.class);
    factory.preload(PublicAnnotation.class);
    factory.preload(PrivateAnnotation.class);
    factory.preload(NoRdfType.class);
  }

  @BeforeClass
  public void setUpMetaFactory() throws OtmException {
    metaFactory.setTripleStore(
      new ItqlStore(URI.create("http://localhost:9091/mulgara-service/services/ItqlBeanService")));
    ModelConfig meta =
      new ModelConfig("metadata", URI.create("local:///topazproject#metadata"), null);
    metaFactory.addModel(meta);

    clearModel(metaFactory, meta);

    metaFactory.preload(OwlClass.class);
    metaFactory.preload(ObjectProperty.class);
  }

  @Test
  public void wineTest() throws OtmException {
    // Test raw OWL classes

    Session session = metaFactory.openSession();
    Transaction tx = session.beginTransaction();

    ClassMetadata cm = metaFactory.getClassMetadata(OwlClass.class);
    OwlClass pa = new OwlClass();
    pa.setOwlClass(URI.create(VIN + "ProductionArea"));
    session.saveOrUpdate(pa);

    OwlClass c = new OwlClass();
    c.setOwlClass(URI.create(VIN + "Country"));
//    c.setSuperClasses(new URI[] { URI.create(VIN + "ProductionArea") } );
    c.setSuperClasses(Arrays.asList(new URI[] { URI.create(VIN + "ProductionArea") } ));
    session.saveOrUpdate(c);

    List<String> ids = session.getIds(Arrays.asList(new Object[] { c } ) );
    OwlClass cc = session.get(OwlClass.class, "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#Country");

    tx.commit();
    session.close();

    session = metaFactory.openSession();
    tx = session.beginTransaction();
    c = session.get(OwlClass.class, VIN + "Country");
    assert c.getSuperClasses().get(0).equals(URI.create(VIN + "ProductionArea"));
    tx.commit();
    session.close();
  }

  @Test
  public void helperTest() throws OtmException {
    OwlHelper.addFactory(factory, metaFactory.getModel("metadata"));
  }
}
