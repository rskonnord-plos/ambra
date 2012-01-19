/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.mulgara.resolver.stringcompare;

import java.net.URI;

import org.apache.log4j.Logger;

import org.mulgara.query.rdf.URIReferenceImpl;
import org.mulgara.resolver.spi.InitializerException;
import org.mulgara.resolver.spi.Resolver;
import org.mulgara.resolver.spi.ResolverFactory;
import org.mulgara.resolver.spi.ResolverFactoryException;
import org.mulgara.resolver.spi.ResolverFactoryInitializer;
import org.mulgara.resolver.spi.ResolverSession;

/** 
 * The factory for {@link StringCompareResolver StringCompareResolver}s.
 * 
 * @author Ronald Tschalär
 */
public class StringCompareResolverFactory implements ResolverFactory {
  private static final Logger logger = Logger.getLogger(StringCompareResolverFactory.class);

  /** the model type we handle */
  public static final URI MODEL_TYPE = URI.create("http://topazproject.org/models#StringCompare");
  /** the ignore-case property we handle */
  public static final URI EQ_IGNORE_CASE =
      URI.create("http://rdf.topazproject.org/RDF/equalsIgnoreCase");

  /** The preallocated local node representing the <code>topaz:equalsIgnoreCase</code> property. */
  private long equalsIgnoreCaseNode;

  /** The preallocated local node representing models representing all non-blank nodes.  */
  private long modelType;

  /** 
   * Create a new string-compare-resolver-factory instance. 
   * 
   * @param initializer the factory initializer
   * @return the new string-compare-resolver-factory instance
   */
  public static ResolverFactory newInstance(ResolverFactoryInitializer initializer)
      throws InitializerException {
    return new StringCompareResolverFactory(initializer);
  }

  /**
   * Instantiate a {@link StringCompareResolverFactory StringCompareResolverFactory}.
   */
  private StringCompareResolverFactory(ResolverFactoryInitializer initializer)
      throws InitializerException {
    // Validate "resolverFactoryInitializer" parameter
    if (initializer == null)
      throw new IllegalArgumentException("Null 'resolverFactoryInitializer' parameter");

    // Claim the filter model type
    initializer.addModelType(MODEL_TYPE, this);

    // set up our data
    equalsIgnoreCaseNode = initializer.preallocate(new URIReferenceImpl(EQ_IGNORE_CASE));
    modelType            = initializer.preallocate(new URIReferenceImpl(MODEL_TYPE));
  }

  public void close() {
  }

  public void delete() {
  }

  /**
   * Obtain a String Compare resolver.
   */
  public Resolver newResolver(boolean canWrite, ResolverSession resolverSession,
                              Resolver systemResolver)
      throws ResolverFactoryException {
    return new StringCompareResolver(resolverSession, equalsIgnoreCaseNode, MODEL_TYPE);
  }
}
