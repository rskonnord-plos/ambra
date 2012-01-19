/* $HeadURL::                                                                            $
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
package org.topazproject.sample1;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.TripleStore;

import org.topazproject.mulgara.itql.DefaultItqlClientFactory;

import org.topazproject.otm.impl.SessionFactoryImpl;

import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.otm.stores.SimpleBlobStore;

/**
 * Very simple sample program to define a subset of the FOAF profile in an object and to be able to
 * store and retrieve it.
 *
 * @author Amit Kapoor
 */
@Entity(types = {"foaf:Person", "foaf:agent"}, graph = "users")
@UriPrefix("foaf:")
public class Foaf {
  private URI     id;
  private String  name;
  private String  nick;
  private URL     homePage;

  /**
   * Blank constructor needed by Topaz
   */
  public Foaf() {
  }

  /**
   * Create FOAF with the specified identifier
   *
   * @param id the identifier for the FOAF resource
   */
  public Foaf (URI id) {
    setId(id);
  }

  /**
   * Create the FOAF resource of type foaf:Person with the passed name, nick and home-page
   *
   * @param id       the FOAF identifier
   * @param name     the name of the FOAF resource
   * @param nick     the nick for the FOAF resource (mandatory)
   * @param homePage the home-page for the FOAF resource (can be null)
   */
  public Foaf(String id, String name, String nick, String homePage) throws MalformedURLException {
    setId(URI.create(id));
    setName(name);
    setNick(nick);
    setHomePage(new URL(homePage));
  }

  /**
   * @return the resource identifier
   */
  public URI getId() {
    return id;
  }

  /**
   * Set the resource identifier
   *
   * @param id the resource identifier
   */
  @Id @GeneratedValue(uriPrefix = "users/")
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * @return the name for the FOAF resource
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the FOAF resource
   *
   * @param name the name for the FOAF resource
   */
  @Predicate
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the nick for the FOAF resource
   */
  public String getNick() {
    return nick;
  }

  /**
   * Set the nick of the FOAF resource
   *
   * @param nick the nick for the FOAF resource
   */
  @Predicate
  public void setNick(String nick) {
    if (nick == null) {
      throw new NullPointerException("nick for FOAF resource cannot be null.");
    }

    this.nick = nick;
  }

  /**
   * @return the home-page for the FOAF resource
   */
  public URL getHomePage() {
    return homePage;
  }

  /**
   * Set the home-page of the FOAF resource
   *
   * @param homePage the homePage for the FOAF resource
   */
  @Predicate
  public void setHomePage(URL homePage) {
    this.homePage = homePage;
  }

  /**
   * Main entry point
   */
  public static void main(String[] args) {
    try {
      SessionFactory factory = new SessionFactoryImpl();

      // Initialize the triple store
      DefaultItqlClientFactory tqlFactory = new DefaultItqlClientFactory();
      tqlFactory.setDbDir("target/triple-db");
      TripleStore tripleStore = new ItqlStore(URI.create("local:///topazproject"), tqlFactory);
      factory.setTripleStore(tripleStore);

      // Initialize the blob store
      SimpleBlobStore blobStore = new SimpleBlobStore("target/blob-db");
      factory.setBlobStore(blobStore);

      // Pre-load all the Topaz annotated classes
      factory.preloadFromClasspath();
      factory.validate();
      initGraphs(factory);

      // Store and retrieve an object
      Session s = factory.openSession();
      Transaction txn = null;
      try {
        txn = s.beginTransaction();

        s.saveOrUpdate(new Foaf("http://www.topazproject.org/foaf/1", "John Doe", "john",
                                "http://john.doe.com/"));

        Foaf john = s.get(Foaf.class, "http://www.topazproject.org/foaf/1");
        System.out.println("\n Person name: " + john.getName() + ", nick: " + john.getNick() + "\n");

        txn.commit();
      } catch (OtmException exp) {
        try {
          if (txn != null)
            txn.rollback();
        } catch (OtmException re) {
          System.out.println("rollback failed: " + re);
        }

        throw exp;
      } finally {
        try {
          s.close();
        } catch (OtmException ce) {
          System.out.println("close failed: " + ce);
        }
      }
    } catch (Exception e) {
      System.out.println("Error: " + e);
    }
  }

  /**
   * Initialize the graphs
   *
   * @param factory the Topaz factory.
   */
  public static void initGraphs(SessionFactory factory) throws OtmException {
    Session s = null;
    Transaction txn = null;
    try {
      s = factory.openSession();
      for (GraphConfig graph : factory.listGraphs()) {
        txn = s.beginTransaction();
        try {
          s.dropGraph(graph.getId());
          txn.commit();
        } catch (Throwable t) {
          System.out.println("Failed to drop graph '" + graph.getId() + "' " + t);
          txn.rollback();
        }
        txn = null;
        txn = s.beginTransaction();
        s.createGraph(graph.getId());
        txn.commit();
      }
    } catch (OtmException e) {
      if (txn != null)
        txn.rollback();
      throw e;
    } finally {
      if (s != null)
        s.close();
    }
  }
}
