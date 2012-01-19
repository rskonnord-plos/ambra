/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.mapping.Mapper;

/**
 * Meta information for mapping a class to a set of triples.
 *
 * @author Pradeep Krishnan
 */
public class ClassMetadata {
  private static final Log    log        = LogFactory.getLog(ClassMetadata.class);
  private Set<String>         types;
  private String              type;
  private String              model;
  private String              ns;
  private Mapper              idField;
  private Map<String, Mapper> fieldMap;
  private Map<String, Mapper> inverseMap;
  private Map<String, Mapper> nameMap;
  private Class               clazz;
  private Collection<Mapper>  fields;

/**
   * Creates a new ClassMetadata object.
   *
   * @param clazz the class 
   * @param types set of rdf:type values that identify this class
   * @param type the most specific rdf:type that identify this class
   * @param model the graph/model where this class is persisted
   * @param ns the default namespace for fields/predicates
   * @param idField the mapper for the id field
   * @param fields mappers for all persistable fields (includes embedded class fields)
   */
  public ClassMetadata(Class clazz, String type, Set<String> types, String model, String ns,
                       Mapper idField, Collection<Mapper> fields)
                throws OtmException {
    this.clazz                           = clazz;
    this.type                            = type;
    this.model                           = model;
    this.ns                              = ns;
    this.idField                         = idField;

    this.types                           = Collections.unmodifiableSet(new HashSet<String>(types));
    this.fields                          = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));

    fieldMap                             = new HashMap<String, Mapper>();
    inverseMap                           = new HashMap<String, Mapper>();
    nameMap                              = new HashMap<String, Mapper>();

    for (Mapper m : fields) {
      Map<String, Mapper> map = m.hasInverseUri() ? inverseMap : fieldMap;

      if (map.put(m.getUri(), m) != null)
        throw new OtmException("Duplicate Rdf uri for " + m.getField().toGenericString());

      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name for " + m.getField().toGenericString());
    }

    fieldMap     = Collections.unmodifiableMap(fieldMap);
    inverseMap   = Collections.unmodifiableMap(inverseMap);
    nameMap      = Collections.unmodifiableMap(nameMap);
  }

  /**
   * Gets the class that this meta info pertains to.
   *
   * @return the class
   */
  public Class getSourceClass() {
    return clazz;
  }

  /**
   * Gets the graph/model where this class is persisted.
   *
   * @return the model identifier
   */
  public String getModel() {
    return model;
  }

  /**
   * Gets the default namespace for all fields/predicates in this class.
   *
   * @return the namespace uri
   */
  public String getNs() {
    return ns;
  }

  /**
   * Gets the most specific rdf:type that describes this class.
   *
   * @return the rdf:type uri or null
   */
  public String getType() {
    return type;
  }

  /**
   * Gets the set of rdf:type values that describe this class.
   *
   * @return set of rdf:type uri values or an empty set
   */
  public Set<String> getTypes() {
    return types;
  }

  /**
   * Gets the persistable fields of this class. Includes embedded class fields.
   *
   * @return collection of field mappers.
   */
  public Collection<Mapper> getFields() {
    return fields;
  }

  /**
   * Gets the mapper for the id/subject-uri field.
   *
   * @return the id field or null for embeddable classes
   */
  public Mapper getIdField() {
    return idField;
  }

  /**
   * Gets a field mapper by its predicate uri.
   *
   * @param uri the predicate uri
   * @param inverse if the mapping is reverse (ie. o,p,s instead of s,p,o))
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(String uri, boolean inverse) {
    return inverse ? inverseMap.get(uri) : fieldMap.get(uri);
  }

  /**
   * Gets a field mapper by its field name.
   *
   * @param name the field name.
   *
   * @return the mapper or null
   */
  public Mapper getMapperByName(String name) {
    return nameMap.get(name);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return clazz.toString();
  }

  /**
   * Tests if this meta-data is for an entity class. Entity classes have an id field, and
   * graph/model
   *
   * @return true for entity type
   */
  public boolean isEntity() {
    return (idField != null) && (model != null);
  }
}
