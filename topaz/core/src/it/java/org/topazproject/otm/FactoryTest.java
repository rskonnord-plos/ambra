/* $HeadURL::                                                                                     $
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
      initGraphs();
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
