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

import java.net.URI;

import java.util.Map;
import java.util.HashMap;

import org.topazproject.otm.query.Results;
import org.topazproject.otm.samples.Annotation;
import org.topazproject.otm.samples.Annotea;
import org.topazproject.otm.samples.PublicAnnotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.testng.AssertJUnit.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for Native Queries.
 *
 * @author Pradeep Krishnan
 */
public class NativeQueryTest extends AbstractOtmTest {
  private static final Log log = LogFactory.getLog(NativeQueryTest.class);

  /**
   * DOCUMENT ME!
   *
   * @throws OtmException DOCUMENT ME!
   */
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

  /**
   * DOCUMENT ME!
   */
  @Test
  public void testNativeQuery() throws OtmException {
    log.info("Testing native query ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          String model = factory.getClassMetadata(PublicAnnotation.class).getModel();
          model = factory.getModel(model).getUri().toString();

          session.doNativeUpdate("delete select $s $p $o from <" + model +
                                 "> where $s $p $o from <" + model + ">;");

          URI        id1 = URI.create("http://localhost/annotation/1");
          URI        id2 = URI.create("http://localhost/annotation/2");
          Annotation a1  = new PublicAnnotation(id1);
          Annotation a2  = new PublicAnnotation(id2);

          a1.setAnnotates(URI.create("foo:1"));
          a2.setAnnotates(URI.create("foo:1"));

          a1.setCreator("aa");
          a2.setCreator("bb");

          a1.setSupersededBy(a2);
          a2.setSupersedes(a1);

          a1.setTitle("foo");

          session.saveOrUpdate(a1);
          session.saveOrUpdate(a2);

          Results r  = session.doNativeQuery("select $s $p $o from <" + model + "> where $s $p $o;");
          Map     m1 = new HashMap();
          Map     m2 = new HashMap();

          while (r.next()) {
            URI    s = r.getURI(0);
            URI    p = r.getURI(1);
            String o = r.getString(2);

            if (s.equals(id1))
              m1.put(p, o);
            else if (s.equals(id2))
              m2.put(p, o);
            else
              fail("Unknown subject-id");
          }

          r.close();

          assertEquals("foo:1", m1.get(URI.create(Annotea.NS + "annotates")));
          assertEquals("foo:1", m2.get(URI.create(Annotea.NS + "annotates")));
          assertEquals("aa", m1.get(URI.create(Rdf.dc + "creator")));
          assertEquals("bb", m2.get(URI.create(Rdf.dc + "creator")));
          assertEquals("foo", m1.get(URI.create(Rdf.dc + "title")));
          assertNull(m2.get(URI.create(Rdf.dc + "title")));
        }
      });
  }

  @Test
  public void testNativeQuery2() throws OtmException {
    log.info("Testing native update and query ...");

    doInSession(new Action() {
        public void run(Session session) throws OtmException {
          String model = factory.getModel("ri").getUri().toString();

          session.doNativeUpdate("delete select $s $p $o from <" + model +
                                 "> where $s $p $o from <" + model + ">;");

          session.doNativeUpdate("insert <s:1> <p:1> '1' " +
            "<s:2> <p:2> $bn1 $bn1 <p:3> '3'^^<xsd:int> " +
            "<s:4> <p:4> <l:4> " +
            "into <" + model + ">;");

          Results r = session.doNativeQuery(
                              "select $s $p $o from <" + model + "> where $s $p $o order by $p;");

          assertEquals(3, r.getVariables().length);
          assertEquals("s", r.getVariables()[0]);
          assertEquals("p", r.getVariables()[1]);
          assertEquals("o", r.getVariables()[2]);

          int cnt = 0;

          // res 1
          assertTrue(r.next());
          assertEquals(cnt++, r.getRowNumber());

          assertEquals(Results.Type.URI, r.getType("s"));
          assertEquals(Results.Type.URI, r.getType("p"));
          assertEquals(Results.Type.LITERAL, r.getType("o"));

          assertEquals(URI.create("s:1"), r.getURI("s"));
          assertEquals(URI.create("p:1"), r.getURI("p"));
          assertEquals("1", r.getLiteral("o").getValue());
          assertNull(r.getLiteral("o").getDatatype());

          // res 2
          assertTrue(r.next());
          assertEquals(cnt++, r.getRowNumber());

          assertEquals(Results.Type.URI, r.getType("s"));
          assertEquals(Results.Type.URI, r.getType("p"));
          assertEquals(Results.Type.BLANK_NODE, r.getType("o"));

          assertEquals(URI.create("s:2"), r.getURI("s"));
          assertEquals(URI.create("p:2"), r.getURI("p"));
          String bn = (String) r.get("o");

          // res 3
          assertTrue(r.next());
          assertEquals(cnt++, r.getRowNumber());
          assertEquals(Results.Type.BLANK_NODE, r.getType("s"));
          assertEquals(Results.Type.URI, r.getType("p"));
          assertEquals(Results.Type.LITERAL, r.getType("o"));

          assertEquals(bn, r.get("s"));
          assertEquals(URI.create("p:3"), r.getURI("p"));
          assertEquals("3", r.getLiteral("o").getValue());
          assertEquals(URI.create(Rdf.xsd + "int"), r.getLiteral("o").getDatatype());

          // res 4
          assertTrue(r.next());
          assertEquals(cnt++, r.getRowNumber());
          assertEquals(Results.Type.URI, r.getType("s"));
          assertEquals(Results.Type.URI, r.getType("p"));
          assertEquals(Results.Type.URI, r.getType("o"));

          assertEquals(URI.create("s:4"), r.getURI("s"));
          assertEquals(URI.create("p:4"), r.getURI("p"));
          assertEquals(URI.create("l:4"), r.getURI("o"));

          // done
          assertFalse(r.next());
          assertEquals(4, cnt);

          r.close();
        }
      });
  }
}
