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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.List;

import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.OtmException;

/**
 * Mapper for a java class field to rdf triples having a specific predicate.
 *
 * @author Pradeep Krishnan
 */
public interface Mapper {
  public static enum MapperType {PREDICATE, RDFLIST, RDFBAG, RDFSEQ, RDFALT, PREDICATE_MAP};

  /**
   * Get a value from a field of an object.
   *
   * @param o the object
   *
   * @return the list containing the field's values (may be serialized)
   *
   * @throws OtmException if a field's value cannot be retrieved and serialized
   */
  public List get(Object o) throws OtmException;

  /**
   * Set a value for an object field.
   *
   * @param o the object
   * @param vals the list of values to set (may be deserialized)
   *
   * @throws OtmException if a field's value cannot be de-serialized and set
   */
  public void set(Object o, List vals) throws OtmException;

  /**
   * Get the raw object field value.
   *
   * @param o the object
   * @param create whether to create an instance
   *
   * @return the raw field value
   *
   * @throws OtmException if a field's value cannot be retrieved
   */
  public Object getRawValue(Object o, boolean create) throws OtmException;

  /**
   * Set the raw object field value
   *
   * @param o the object
   * @param value the value to set
   *
   * @throws OtmException if a field's value cannot be set
   */
  public void setRawValue(Object o, Object value) throws OtmException;

  /**
   * Gets the get method used.
   *
   * @return the get method or null
   */
  public Method getGetter();

  /**
   * Gets the set method used.
   *
   * @return the set method or null
   */
  public Method getSetter();

  /**
   * Gets the underlying field.
   *
   * @return the filed
   */
  public Field getField();

  /**
   * Gets the name of the field.
   *
   * @return the name
   */
  public String getName();

  /**
   * Gets the type of the field.
   *
   * @return the field type
   */
  public Class getType();

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
   * Gets the rdf:type for an association field.
   *
   * @return the rdf:type or null for un-typed
   */
  public String getRdfType();

  /**
   * Gets the component type of this field.
   *
   * @return component type for arrays; member type for collections; or same as type for simple
   *         fields
   */
  public Class getComponentType();

  /**
   * Gets the rdf predicate uri. All fields other than an 'Id' field must have a uri.
   *
   * @return the rdf predicate uri
   */
  public String getUri();

  /**
   * Gets the serializer used. Note that there won't be any serializer set up for
   * associations.
   *
   * @return the serializer or null
   */
  public Serializer getSerializer();

  /**
   * Tests if the predicate uri represents an inverse.
   *
   * @return
   */
  public boolean hasInverseUri();

  /**
   * Gets the model where this field is persisted.
   *
   * @return the model name or null 
   */
  public String getModel();

  /**
   * Gets the mapper type of this mapper.
   *
   * @return the mapper type
   */
  public MapperType getMapperType();

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
}
