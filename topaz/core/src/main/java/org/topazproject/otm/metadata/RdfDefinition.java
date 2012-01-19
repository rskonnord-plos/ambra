/* $HeadURL::                                                                            $
 * $Rdf: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.metadata;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * The definition for an Rdf property.
 *
 * @author Pradeep Krishnan
 */
public class RdfDefinition extends PropertyDefinition {
  private final String              uri;
  private final boolean             inverse;
  private final boolean             objectProperty;
  private final String              model;
  private final String              dataType;
  private final CollectionType      colType;
  private final boolean             entityOwned;
  private final boolean             predicateMap;
  private final CascadeType[]       cascade;
  private final FetchType           fetchType;
  private final String              associatedEntity;
  private final IdentifierGenerator generator;

  /**
   * Creates a new RdfDefinition object.
   *
   * @param name   The name of this definition.
   * @param uri the rdf predicate
   * @param dataType of literals or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model  the model where this field is persisted
   * @param colType the collection type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   * @param cascade cascade options for this field
   * @param fetchType fetch type for associations. Must be null otherwise
   * @param associatedEntity the entity name for associations
   * @param objectProperty if this is an object property 
   */
  public RdfDefinition(String name, String uri, String dataType, boolean inverse, String model,
                       CollectionType colType, boolean entityOwned, IdentifierGenerator generator,
                       CascadeType[] cascade, FetchType fetchType, String associatedEntity,
                       boolean objectProperty) {
    super(name);
    this.uri                = uri;
    this.dataType           = dataType;
    this.inverse            = inverse;
    this.model              = model;
    this.colType            = colType;
    this.entityOwned        = entityOwned;
    this.generator          = generator;
    this.cascade            = cascade;
    this.fetchType          = fetchType;
    this.associatedEntity   = associatedEntity;
    this.predicateMap       = false;
    this.objectProperty     = objectProperty;
  }

  /**
   * Creates a new RdfDefinition object for a predicate-map.
   *
   * @param name   The name of this definition.
   * @param model  the model where this field is persisted
   */
  public RdfDefinition(String name, String model) {
    super(name);
    this.uri                = null;
    this.dataType           = null;
    this.inverse            = false;
    this.model              = model;
    this.colType            = null;
    this.entityOwned        = true;
    this.generator          = null;
    this.cascade            = null;
    this.fetchType          = null;
    this.associatedEntity   = null;
    this.predicateMap       = true;
    this.objectProperty     = false;
  }

  /*
   * inherited javadoc
   */
  public String getUri() {
    return uri;
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    return objectProperty;
  }

  /*
   * inherited javadoc
   */
  public String getDataType() {
    return dataType;
  }

  /*
   * inherited javadoc
   */
  public boolean isAssociation() {
    return associatedEntity != null;
  }

  /*
   * inherited javadoc
   */
  public boolean isPredicateMap() {
    return predicateMap;
  }

  /*
   * inherited javadoc
   */
  public boolean hasInverseUri() {
    return inverse;
  }

  /*
   * inherited javadoc
   */
  public String getModel() {
    return model;
  }

  /*
   * inherited javadoc
   */
  public CollectionType getColType() {
    return colType;
  }

  /*
   * inherited javadoc
   */
  public boolean isEntityOwned() {
    return entityOwned;
  }

  /*
   * inherited javadoc
   */
  public CascadeType[] getCascade() {
    return cascade;
  }

  /*
   * inherited javadoc
   */
  public boolean isCascadable(CascadeType op) {
    for (CascadeType ct : cascade)
      if (ct.implies(op))
        return true;

    return false;
  }

  /*
   * inherited javadoc
   */
  public FetchType getFetchType() {
    return fetchType;
  }

  /*
   * inherited javadoc
   */
  public IdentifierGenerator getGenerator() {
    return generator;
  }

  /*
   * inherited javadoc
   */
  public String getAssociatedEntity() {
    return associatedEntity;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[pred=" + uri + ", dataType=" + dataType + ", colType="
           + colType + ", inverse=" + inverse + ", fetchType=" + fetchType + ", cascade=" + cascade
           + ", generator=" + ((generator != null) ? generator.getClass() : "-null-")
           + ", association=" + associatedEntity + ", objectProperty=" + objectProperty
           + ", model=" + model + ",entityOwned=" + entityOwned + "]";
  }
}
