/* $HeadURL:: $
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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
package org.topazproject.ambra.doi;

import java.net.URI;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.FetchType;
import org.topazproject.otm.GraphConfig;
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
  private static final Log log = LogFactory.getLog(DOITypeResolver.class);

  /**
   * Predicate URI for a:annotates
   */
  public static final String ANNOTATES = "http://www.w3.org/2000/10/annotation-ns#annotates";

  /**
   * The default GRAPH  that will be searched.
   */
  public static final String GRAPH = "local:///topazproject#filter:graph=ri";

  private final SessionFactory sf;

  /**
   * Creates a new DOITypeResolver object.
   *
   * @param mulgaraUri the mulgara service uri
   * @param graph      the graph to use
   * @throws OtmException if an error occurred talking to the web-service
   */
  public DOITypeResolver(URI mulgaraUri, String graph) throws OtmException {
    sf = new SessionFactoryImpl();
    sf.setTripleStore(new ItqlStore(mulgaraUri));
    sf.preload(Resource.class);
    sf.preload(Annotation.class);
    if (graph == null)
      graph = GRAPH;
    sf.addGraph(new GraphConfig("ri", URI.create(graph), null));
    sf.validate();
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

          return r.getTypes();
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
            id = a.getId();

            if (loopDetect.contains(id))
              throw new OtmException("Loop detected for " + doi);

            loopDetect.add(id);
            a = a.getAnn();
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
     * @return the return value
     *
     * @throws OtmException on an error
     */
    T run(Session session) throws OtmException;
  }

  @Entity(graph = "ri")
  public static class Resource {
    private String      id;
    private Set<String> types;

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
      return id;
    }

    /**
     * Set id.
     *
     * @param id the value to set.
     */
    @Id
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Get types.
     *
     * @return types as Set of String.
     */
    public Set<String> getTypes() {
      return types;
    }

    /**
     * Set types.
     *
     * @param types the value to set.
     */
    @Predicate(uri = Rdf.rdf + "type", fetch = FetchType.eager)
    public void setTypes(Set<String> types) {
        this.types = types;
    }
  }

  @Entity(graph = "ri")
  public static class Annotation {
    private String     id;
    private Annotation ann;

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
      return id;
    }

    /**
     * Set id.
     *
     * @param id the value to set.
     */
    @Id
    public void setId(String id) {
      this.id = id;
    }

    /**
     * Get ann.
     *
     * @return ann as Annotation.
     */
    public Annotation getAnn() {
      return ann;
    }

    /**
     * Set ann.
     *
     * @param ann the value to set.
     */
    @Predicate(uri = ANNOTATES, fetch = FetchType.eager)
    public void setAnn(Annotation ann) {
      this.ann = ann;
    }
  }
}
