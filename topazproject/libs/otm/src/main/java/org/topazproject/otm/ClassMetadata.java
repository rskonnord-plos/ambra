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
import java.util.List;
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
  private static final Log          log       = LogFactory.getLog(ClassMetadata.class);
  private Set<String>               types;
  private String                    type;
  private String                    name;
  private String                    model;
  private String                    uriPrefix;
  private Mapper                    idField;
  private Map<String, List<Mapper>> uriMap;
  private Map<String, Mapper>       nameMap;
  private Class                     clazz;
  private Collection<Mapper>        fields;

/**
   * Creates a new ClassMetadata object.
   *
   * @param clazz the class 
   * @param name the entity name for use in queries
   * @param types set of rdf:type values that identify this class
   * @param type the most specific rdf:type that identify this class
   * @param model the graph/model where this class is persisted
   * @param uriPrefix the uri-prefix for constructing predicate-uris for fields from their names 
   * @param idField the mapper for the id field
   * @param fields mappers for all persistable fields (includes embedded class fields)
   */
  public ClassMetadata(Class clazz, String name, String type, Set<String> types, String model,
                       String uriPrefix, Mapper idField, Collection<Mapper> fields)
                throws OtmException {
    this.clazz                                = clazz;
    this.name                                 = name;
    this.type                                 = type;
    this.model                                = model;
    this.uriPrefix                            = uriPrefix;
    this.idField                              = idField;

    this.types                                = Collections.unmodifiableSet(new HashSet<String>(types));
    this.fields                               = Collections.unmodifiableCollection(new ArrayList<Mapper>(fields));

    uriMap                                    = new HashMap<String, List<Mapper>>();
    nameMap                                   = new HashMap<String, Mapper>();

    for (Mapper m : fields) {
      List<Mapper> mappers = uriMap.get(m.getUri());

      if (mappers == null)
        uriMap.put(m.getUri(), mappers = new ArrayList<Mapper>());
      else if (isDuplicateMapping(mappers, m))
        throw new OtmException("Duplicate predicate uri for " + m.getField().toGenericString());

      mappers.add(m);

      if (nameMap.put(m.getName(), m) != null)
        throw new OtmException("Duplicate field name for " + m.getField().toGenericString());
    }

    uriMap    = Collections.unmodifiableMap(uriMap);
    nameMap   = Collections.unmodifiableMap(nameMap);
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
   * Gets the uri-prefix used to build the predicate-uri for all fields in this class without
   * an explicit predicate uri defined.
   *
   * @return the uri prefix
   */
  public String getUriPrefix() {
    return uriPrefix;
  }

  /**
   * Gets the entity name for this ClassMetadata
   *
   * @return the entity name
   */
  public String getName() {
    return name;
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
   * @param type for associations the rdf:type or null
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(String uri, boolean inverse, String type) {
    List<Mapper> mappers = uriMap.get(uri);

    if (mappers == null)
      return null;

    Mapper candidate = null;

    for (Mapper m : mappers) {
      if (m.hasInverseUri() != inverse)
        continue;

      String rt = m.getRdfType();

      if ((type == null) || type.equals(rt))
        return m;

      if ((rt == null) && (candidate == null))
        candidate = m;
    }

    return candidate;
  }

  /**
   * Gets a field mapper by its predicate uri.
   *
   * @param sf the session factory for looking up associations
   * @param uri the predicate uri
   * @param inverse if the mapping is reverse (ie. o,p,s instead of s,p,o))
   * @param typeUris Collection of rdf:Type values
   *
   * @return the mapper or null
   */
  public Mapper getMapperByUri(SessionFactory sf, String uri, boolean inverse,
                               Collection<String> typeUris) {
    if ((typeUris == null) || (typeUris.size() == 0))
      return getMapperByUri(uri, inverse, null);

    if (typeUris.size() == 1)
      return getMapperByUri(uri, inverse, typeUris.iterator().next());

    List<Mapper> mappers = uriMap.get(uri);

    if (mappers == null)
      return null;

    Set<String> uris      = new HashSet(typeUris);
    Mapper      candidate = null;

    for (Mapper m : mappers) {
      if (m.hasInverseUri() != inverse)
        continue;

      String rt = m.getRdfType();

      if ((rt == null) && (candidate == null))
        candidate = m;

      if (uris.contains(rt)) {
        uris.removeAll(sf.getClassMetadata(m.getComponentType()).getTypes());
        candidate = m;
      }
    }

    return candidate;
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

  private static boolean isDuplicateMapping(List<Mapper> mappers, Mapper o) {
    for (Mapper m : mappers) {
      if (m.hasInverseUri() != o.hasInverseUri())
        continue;

      if (m.getRdfType() != null) {
        if (m.getRdfType().equals(o.getRdfType()))
          return true;
      } else if (o.getRdfType() == null)
        return true;
    }

    return false;
  }
}
