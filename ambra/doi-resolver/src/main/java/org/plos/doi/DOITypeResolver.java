/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.doi;

import java.net.URI;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;

/**
 * Resolver for the rdf:type of a DOI-URI.
 *
 * @author Pradeep Krishnan
 */
public class DOITypeResolver {
  /**
   * Predicate URI for a:annotates
   */
  public static final String ANNOTATES = "http://www.w3.org/2000/10/annotation-ns#annotates";

  /**
   * The default GRAPH  that will be searched.
   */
  public static final String GRAPH = "local:///topazproject#filter:model=ri";
  private static final Log log = LogFactory.getLog(DOITypeResolver.class);

  //
  private final SessionFactory sf;
  private String               graph;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   *
   * @throws OtmException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri) throws OtmException {
    sf                         = new SessionFactoryImpl();
    sf.setTripleStore(new ItqlStore(mulgaraUri));
    sf.preload(Resource.class);
    sf.preload(Annotation.class);
    setGraph(GRAPH);
  }

  /**
   * Sets the uri for the graph where the queries are to be performed.
   *
   * @param graph the graph uri
   */
  public void setGraph(String graph) {
    this.graph = graph;

    ModelConfig mc = sf.getModel("ri");

    if (mc != null)
      sf.removeModel(mc);

    sf.addModel(new ModelConfig("ri", URI.create(graph), null));
  }

  /**
   * Gets the graph where the queries are to be performed
   *
   * @return the graph uri
   */
  public String getGraph() {
    return graph;
  }

  /**
   * Queries the ITQL database and returns all known rdf:type values of a URI.
   *
   * @param doi the doi uri
   *
   * @return returns the set of rdf:type values
   *
   * @throws OtmException if an exception occurred talking to the service
   */
  public Set<String> getRdfTypes(final URI doi) throws OtmException {
    return doInSession(new Action<Set<String>>() {
        public Set<String> run(Session session) throws OtmException {
          Resource r = session.get(Resource.class, doi.toString());

          if (r == null)
            return Collections.emptySet();

          return r.types;
        }
      });
  }

  /**
   * Recursively attempts to find the annotated root (article?) of the given annotation doi.
   * Retruns the doi found that does not annotate another doi.
   *
   * @param doi the annotation doi
   *
   * @return the resource that is annotated by the annotation doi
   *
   * @throws OtmException
   */
  public String getAnnotatedRoot(final String doi) throws OtmException {
    return doInSession(new Action<String>() {
        public String run(Session session) throws OtmException {
          String      id         = doi;
          Annotation  a          = session.get(Annotation.class, id);
          Set<String> loopDetect = new HashSet<String>();

          while (a != null) {
            id = a.id;

            if (loopDetect.contains(id))
              throw new OtmException("Loop detected for " + doi);

            loopDetect.add(id);
            a = a.ann;
          }

          return id;
        }
      });
  }

  /**
   * Run the given action within a session.
   *
   * @param <T>    the return type
   * @param action the action to run
   *
   * @return the value returned by the action
   *
   * @throws OtmException on an error
   */
  protected <T> T doInSession(Action<T> action) throws OtmException {
    Session     session = sf.openSession();
    Transaction tx      = null;

    try {
      tx = session.beginTransaction(true, 0);

      T result = action.run(session);
      tx.commit(); // Flush happens automatically

      return result;
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e; // or display error message
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
     * @return the return value
     *
     * @throws OtmException on an error
     */
    T run(Session session) throws OtmException;
  }

  @Entity(model = "ri")
  public static class Resource {
    @Id
    public String                                        id;
    @Predicate(uri = Rdf.rdf + "type", fetch = FetchType.eager)
    public Set<String>                                   types;
  }

  @Entity(model = "ri")
  public static class Annotation {
    @Id
    public String                                                         id;
    @Predicate(uri = ANNOTATES, fetch = FetchType.eager)
    public Annotation                                                     ann;
  }
}
