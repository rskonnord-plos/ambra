/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.mapping.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.serializer.Serializer;

/**
 * A wrapping loader that projects fields of an embedded class on to the embedding class. This
 * makes it possible for treating embedded class fields the same way as regular class fields.
 *
 * @author Pradeep Krishnan
 */
class EmbeddedClassMemberFieldBinder implements FieldBinder {
  private EmbeddedClassFieldBinder container;
  private FieldBinder              field;

  /**
   * Creates a new EmbeddedClassMemberFieldBinder object.
   *
   * @param container the mapper for the embedded class field in the embedding class
   * @param field the mapper for a field in the embedded class
   */
  public EmbeddedClassMemberFieldBinder(EmbeddedClassFieldBinder container, FieldBinder field) {
    this.container   = container;
    this.field       = field;
  }

  /** 
   * Get the mapper for the embedded class field in the embedding class
   * 
   * @return the mapper
   */
  public EmbeddedClassFieldBinder getContainer() {
    return container;
  }

  /** 
   * Get the mapper for the field in the embedded class
   * 
   * @return the mapper
   */
  public FieldBinder getFieldBinder() {
    return field;
  }

  /*
   * inherited javadoc
   */
  public Object getRawValue(Object o, boolean create) throws OtmException {
    Object co = container.getRawValue(o, create);

    return (co == null) ? null : field.getRawValue(co, create);
  }

  /*
   * inherited javadoc
   */
  public void setRawValue(Object o, Object value) throws OtmException {
    field.setRawValue(container.getRawValue(o, true), value);
  }

  /*
   * inherited javadoc
   */
  public List get(Object o) throws OtmException {
    Object co = container.getRawValue(o, false);

    return (co == null) ? Collections.emptyList() : field.get(co);
  }

  /*
   * inherited javadoc
   */
  public void set(Object o, List vals) throws OtmException {
    field.set(container.getRawValue(o, true), vals);
  }

  /*
   * inherited javadoc
   */
  public void load(Object instance, List<String> values, 
      Map<String, Set<String>> types, RdfMapper mapper, 
      Session session) throws OtmException {
    load(instance, instance, values, types, mapper, session);
  }

  /*
   * inherited javadoc
   */
  public void load(Object root, Object instance, List<String> values, 
      Map<String, Set<String>> types, RdfMapper mapper, 
      Session session) throws OtmException {
    field.load(root, container.getRawValue(instance, true), values, types, mapper, session);
  }

  /*
   * inherited javadoc
   */
  public Method getGetter() {
    return field.getGetter();
  }

  /*
   * inherited javadoc
   */
  public Method getSetter() {
    return field.getSetter();
  }

  /*
   * inherited javadoc
   */
  public Field getField() {
    return field.getField();
  }

  /*
   * inherited javadoc
   */
  public String getName() {
    return container.getName() + "." + field.getName();
  }

  /*
   * inherited javadoc
   */
  public Class getType() {
    return field.getType();
  }

  /*
   * inherited javadoc
   */
  public Class getComponentType() {
    return field.getComponentType();
  }

  /*
   * inherited javadoc
   */
  public Serializer getSerializer() {
    return field.getSerializer();
  }

  /*
   * inherited javadoc
   */
  public boolean isLoaded(Object o) throws OtmException {
    Object co = container.getRawValue(o, false);
    return (co == null) ? false : field.isLoaded(co);
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "EmbeddedClassMemberFieldBinder[container=" + container + ", field=" + field + "]";
  }
}
