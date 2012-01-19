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

import java.net.URI;
import java.net.URL;

import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.id.IdentifierGenerator;

/**
 * A convenient base class for all mappers.
 *
 * @author Pradeep krishnan
 */
public abstract class AbstractMapper implements Mapper {
  private Serializer          serializer;
  private Method              getter;
  private Method              setter;
  private Field               field;
  private String              name;
  private String              uri;
  private Class               type;
  private Class               componentType;
  private boolean             inverse;
  private String              model;
  private String              dataType;
  private String              rdfType;
  private MapperType          mapperType;
  private boolean             entityOwned;
  private IdentifierGenerator generator;

  /**
   * Creates a new AbstractMapper object.
   *
   * @param uri the rdf predicate
   * @param field the java class field
   * @param getter the field get method or null
   * @param setter the field set method or null
   * @param serializer the serializer or null
   * @param componentType of arrays and collections or type of functional properties
   * @param dataType of literals or null for un-typed
   * @param rdfType of associations or null for un-typed
   * @param inverse if this field is persisted with an inverse predicate
   * @param model  the model where this field is persisted
   * @param mapperType the mapper type of this field
   * @param entityOwned if the triples for this field is owned by the containing entity
   * @param generator if there is a generator for this field
   */
  public AbstractMapper(String uri, Field field, Method getter, Method setter,
                        Serializer serializer, Class componentType, String dataType,
                        String rdfType, boolean inverse, String model, MapperType mapperType,
                        boolean entityOwned, IdentifierGenerator generator) {
    this.uri             = uri;
    this.field           = field;
    this.getter          = getter;
    this.setter          = setter;
    this.serializer      = serializer;
    this.name            = field.getName();
    this.type            = field.getType();
    this.componentType   = componentType;
    this.dataType        = dataType;
    this.rdfType         = rdfType;
    this.inverse         = inverse;
    this.model           = model;
    this.mapperType      = mapperType;
    this.entityOwned     = entityOwned;
    this.generator       = generator;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    Object value;

    try {
      if (getter != null)
        value = getter.invoke(o);
      else
        value = field.get(o);

      if ((value == null) && create) {
        value = field.getType().newInstance();
        setRawValue(o, value);
      }
    } catch (Exception e) {
      throw new OtmException("Failed to get a value from '" + field.toGenericString() + "'", e);
    }

    return value;
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    try {
      if (setter != null)
        setter.invoke(o, value);
      else
        field.set(o, value);
    } catch (Exception e) {
      if ((value == null) && type.isPrimitive())
        setRawValue(o, 0);
      else
        throw new OtmException("Failed to set a value for '" + field.toGenericString() + "'", e);
    }
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
  public Method getGetter() {
    return getter;
  }

  /*
   * inherited javadoc
   */
  public Method getSetter() {
    return setter;
  }

  /*
   * inherited javadoc
   */
  public Field getField() {
    return field;
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return name;
  }

  /*
   * inherited javadoc
   */
  public Class getType() {
    return type;
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    Class clazz = getComponentType();

    return URI.class.isAssignableFrom(clazz) || URL.class.isAssignableFrom(clazz)
            || (getSerializer() == null)
            || (String.class.isAssignableFrom(clazz) && hasInverseUri())
            || (Rdf.xsd + "anyURI").equals(getDataType());
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
  public String getRdfType() {
    return rdfType;
  }

  /*
   * inherited javadoc
   */
  public Class getComponentType() {
    return componentType;
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return serializer;
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
  public MapperType getMapperType() {
    return mapperType;
  }

  /*
   * inherited javadoc
   */
  public boolean isEntityOwned() {
    return entityOwned;
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object serialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.serialize(o) : o;
    } catch (Exception e) {
      throw new OtmException("Serialization error", e);
    }
  }

  /**
   * Run a value through the serializer. If no serializer is defined, the value is returned
   * as is.
   *
   * @param o the object to serialize.
   *
   * @return the returned value
   *
   * @throws OtmException on an error in serialize
   */
  protected Object deserialize(Object o) throws OtmException {
    try {
      return (serializer != null) ? serializer.deserialize((String) o) : o;
    } catch (Exception e) {
      throw new OtmException("Deserialization error on " + toString(), e);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public IdentifierGenerator getGenerator() {
    return generator;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName() + "[field=" + name + ", pred=" + uri + ", type="
           + ((type != null) ? type.getName() : "-null-") + ", componentType="
           + ((componentType != null) ? componentType.getName() : "-null-") + ", dataType="
           + dataType + ", rdfType=" + rdfType + ", mapperType=" + mapperType + ", inverse="
           + inverse + ", serializer=" + serializer + ", generator="
           + ((generator != null) ? generator.getClass() : "-null-") + "]";
  }
}
