/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.context.ThreadLocalSessionContext;
import org.topazproject.otm.samples.Article;
import org.topazproject.otm.samples.Reply;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Tests for OtmSessionFactory.
 *
 * @author Pradeep Krishnan
 */
public class FactoryTest extends AbstractOtmTest {
  private static final Log log = LogFactory.getLog(FactoryTest.class);

  @BeforeClass
  public void setUp() throws OtmException {
    try {
      initFactory();
      initModels();
    } catch (OtmException e) {
      log.error("OtmException in setup", e);
      throw e;
    } catch (RuntimeException e) {
      log.error("Exception in setup", e);
      throw e;
    } catch (Error e) {
      log.error("Error in setup", e);
      throw e;
    }
  }

  @Test
  public void testGetByEntityName() {
    log.info("Testing look-up by entity name ...");
    assertNotNull(factory.getClassMetadata("Article"));
    assertEquals(factory.getClassMetadata(Article.class.getName()),
                             factory.getClassMetadata("Article"));

    assertNotNull(factory.getClassMetadata("Reply"));
    assertEquals(factory.getClassMetadata(Reply.class.getName()),
                                     factory.getClassMetadata("Reply"));
  }

  @Test
  public void testThreadLocalSessionContext() throws OtmException {
    log.info("Testing thread-local session context ...");
    Session s1 = null;
    Session s2 = null;

    try {
      factory.setCurrentSessionContext(new ThreadLocalSessionContext(factory));

      s1 = factory.getCurrentSession();
      assertNotNull(s1);
      assertTrue(s1 == factory.getCurrentSession());
      s1.close();

      s2 = factory.getCurrentSession();
      assertNotNull(s2);
      assertTrue(s2 == factory.getCurrentSession());
      s2.close();
      assertTrue(s1 != s2);
      s1   = null;
      s2   = null;
    } finally {
      try {
        factory.setCurrentSessionContext(null);
      } catch (Throwable t) {
        log.warn("removing current session context failed", t);
      }

      try {
        if (s1 != null)
          s1.close();
      } catch (Throwable t) {
        log.warn("close failed", t);
      }

      try {
        if (s2 != null)
          s2.close();
      } catch (Throwable t) {
        log.warn("close failed", t);
      }
    }
  }

}
