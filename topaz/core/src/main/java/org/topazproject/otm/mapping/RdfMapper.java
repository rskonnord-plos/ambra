/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * Mapper for a property that is persisted in a TripleStore.
 *
 * @author Pradeep Krishnan
 */
public interface RdfMapper extends Mapper {
  /**
   * Checks if the type is an rdf resource and not a literal.
   *
   * @return true if this field is persisted as a uri
   */
  public boolean typeIsUri();

  /**
   * Gets the dataType for a literal field.
   *
   * @return the dataType or null for un-typed literal
   */
  public String getDataType();

  /**
   * Checks if the type is an association and not a serialized literal/URI. When a field is
   * not an association, the node is considered a leaf node in the rdf graph.
   *
   * @return true if this field is
   */
  public boolean isAssociation();

  /**
   * Tests if this is a wild-card (no predicate-uri) mapping.
   *
   * @return true if no predicate-uri
   */
  public boolean isPredicateMap();

  /**
   * Gets the rdf predicate uri.
   *
   * @return the rdf predicate uri or null for predicate maps.
   */
  public String getUri();

  /**
   * Tests if the predicate uri represents an inverse.
   *
   * @return true if the predicate uri points towards us rather than away
   */
  public boolean hasInverseUri();

  /**
   * Gets the model where this field is persisted.
   *
   * @return the model name or null
   */
  public String getModel();

  /**
   * Gets the Collection type of this mapper.
   *
   * @return the collection type
   */
  public CollectionType getColType();

  /**
   * Tests if the triples for this field are owned by the containing entity.
   *
   * @return true if owned,
   */
  public boolean isEntityOwned();

  /**
   * Get the generator for this field
   *
   * @return the generator to use for this field (or null if there isn't one)
   */
  public IdentifierGenerator getGenerator();

  /**
   * Get the cascading options for this field.
   *
   * @return the cascading options.
   */
  public CascadeType[] getCascade();

  /**
   * Tests if an operation is cascaded for this field
   *
   * @param op the operation to test
   *
   * @return true if the operation is cascadable
   */
  public boolean isCascadable(CascadeType op);

  /**
   * Get the fetch options for this field. Only applicable for associations.
   *
   * @return the FetchType option
   */
  public FetchType getFetchType();

  /**
   * For associations, the name of the associated entity.
   *
   * @return the name of the associated entity or null if this is not an association mapping
   */
  public String getAssociatedEntity();
}
