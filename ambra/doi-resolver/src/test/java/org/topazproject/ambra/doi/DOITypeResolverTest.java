/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.doi;

import java.net.URI;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Test for DOI resolver.
 *
 * @author Pradeep Krishnan
 */
public class DOITypeResolverTest {
  private final URI                storeUri    = URI.create("local:///topazproject");
  private String                   graph       = "local:///topazproject#doiResolverTest";
  private DOITypeResolver          doiResolver;
  private Map<String, Set<String>> types       = new HashMap<String, Set<String>>();
  private Map<String, Set<String>> anns        = new HashMap<String, Set<String>>();

  /**
   * Creates a new DOITypeResolverTest object.
   *
   * @throws OtmException on an error
   */
  public DOITypeResolverTest() throws OtmException {
    Set<String> s = new HashSet<String>();

    for (int i = 0; i < 3; i++) {
      types.put("doi:" + i, s);
      s = new HashSet(s);
      s.add("type:" + (i + 1));
    }

    for (int i = 0; i < 3; i++) {
      s = new HashSet<String>();

      for (int j = 0; j < i; j++)
        s.add("doi:annotation/" + i + "/" + j);

      anns.put("doi:article" + i, s);
    }

    doiResolver = new DOITypeResolver(storeUri, graph);
    SessionFactory factory = new SessionFactoryImpl();
    factory.setTripleStore(new ItqlStore(storeUri));

    GraphConfig mc = new GraphConfig("ri", URI.create(graph), null);
    factory.addGraph(mc);

    Session session = null;
    try {
      session = factory.openSession();

      Transaction tx = session.beginTransaction();
      try {
        session.dropGraph(mc.getId());
        tx.commit();
      } catch (OtmException e) {
        tx.rollback();
      }
      tx = session.beginTransaction();
      session.createGraph(mc.getId());

      try {
        for (String doi : types.keySet())
          for (String type : types.get(doi))
            session.doNativeUpdate("insert <" + doi + "> <rdf:type> <" + type
                                   + "> into <" + graph + ">;");

        for (String doi : anns.keySet()) {
          String prev = doi;

          for (String ann : anns.get(doi)) {
            session.doNativeUpdate("insert <" + ann + "> <" + DOITypeResolver.ANNOTATES + "> <" + prev 
                                   + "> into <" + graph + ">;");
            prev = ann;
          }
        }

        tx.commit();
      } catch (OtmException e) {
        try {
          tx.rollback();
        } catch (OtmException t) {
        }

        throw e;
      }
    } finally {
      if (session != null)
        session.close();
    }
  }

  /**
   * Tests rdf:type resolving.
   */
  @Test
  public void testType() {
    for (String doi : types.keySet())
      assertEquals(types.get(doi), doiResolver.getRdfTypes(URI.create(doi)));
  }

  /**
   * Tests a:annotates resolving.
   */
  @Test
  public void testAnnotated() {
    for (String doi : anns.keySet())
      for (String ann : anns.get(doi))
        assertEquals(doi, doiResolver.getAnnotatedRoot(ann));
  }
}
