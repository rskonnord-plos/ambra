/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.mapping.EmbeddedClassMapper;
import org.topazproject.otm.mapping.Mapper.MapperType;

/** 
 * This describes the type of an expression. It can be an untyped literal, a typed literal,
 * a URI, or class, or an embedded class.
 * 
 * @author Ronald Tschalär
 */
class ExprType {
  /** valid expression types */
  public enum Type { CLASS, EMB_CLASS, URI, UNTYPED_LIT, TYPED_LIT };

  private final Type                      type;
  private final ClassMetadata             meta;
  private final List<EmbeddedClassMapper> embFields;
  private final String                    datatype;
  private final MapperType                colType;

  private ExprType(Type type, ClassMetadata meta, List<EmbeddedClassMapper> embFields,
                   String datatype, MapperType colType) {
    this.type      = type;
    this.meta      = meta;
    this.embFields = embFields;
    this.datatype  = datatype;
    this.colType   = (colType != null) ? colType : MapperType.PREDICATE;
  }

  /** 
   * Create a new expression type representing a class. 
   * 
   * @param meta    the class metadata
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   * @throws NullPointerException if <var>meta</var> is null
   */
  public static ExprType classType(ClassMetadata meta, MapperType colType) {
    if (meta == null)
      throw new NullPointerException("class metadata may not be null");
    return new ExprType(Type.CLASS, meta, null, null, colType);
  }

  /** 
   * Create a new expression type representing an embedded class. 
   * 
   * @param meta  the container class' metadata
   * @param field the field name pointing to the embedded class
   * @return the new type
   * @throws NullPointerException if <var>meta</var> or <var>field</var> is null
   */
  public static ExprType embeddedClassType(ClassMetadata meta, EmbeddedClassMapper field) {
    if (meta == null)
      throw new NullPointerException("class metadata may not be null");
    if (field == null)
      throw new NullPointerException("class field may not be null");

    List<EmbeddedClassMapper> fields = new ArrayList<EmbeddedClassMapper>();
    fields.add(field);
    return new ExprType(Type.EMB_CLASS, meta, fields, null, null);
  }

  /** 
   * Create a new expression type representing a URI. 
   * 
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   */
  public static ExprType uriType(MapperType colType) {
    return new ExprType(Type.URI, null, null, null, colType);
  }

  /** 
   * Create a new expression type representing an untyped literal. 
   * 
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   */
  public static ExprType literalType(MapperType colType) {
    return new ExprType(Type.UNTYPED_LIT, null, null, null, colType);
  }

  /** 
   * Create a new expression type representing a typed literal. 
   * 
   * @param datatype the literal's datatype
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   * @throws NullPointerException if <var>datatype</var> is null
   */
  public static ExprType literalType(String datatype, MapperType colType) {
    if (datatype == null)
      throw new NullPointerException("datatype may not be null for a typed literal");
    return new ExprType(Type.TYPED_LIT, null, null, datatype, colType);
  }

  /**
   * Get the expression type.
   *
   * @return the expression type
   */
  public Type getType()
  {
      return type;
  }

  /**
   * Get the class metadata.
   *
   * @return the class metadata if this is a class; null otherwise
   */
  public ClassMetadata getMeta() {
    return meta;
  }

  /**
   * Get the mappers describing the fields containing the (nested) embedded classes.
   *
   * @return the mappers if this is an embedded class; null otherwise
   */
  public List<EmbeddedClassMapper> getEmbeddedFields() {
    return embFields;
  }

  /**
   * Get the datatype.
   *
   * @return datatype if this is a typed literal; null otherwise
   */
  public String getDataType()
  {
      return datatype;
  }

  /**
   * Get the collection type.
   *
   * @return the collection type
   */
  public MapperType getCollectionType()
  {
      return colType;
  }

  /** 
   * Get the class of this type. For non class or embedded-class types this returns null.
   * 
   * @return the class, or null if not a class type
   */
  public Class getExprClass() {
    switch (type) {
      case CLASS:
        return meta.getSourceClass();

      case EMB_CLASS:
        return embFields.get(embFields.size() - 1).getType();

      default:
        return null;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ExprType))
      return false;

    ExprType o = (ExprType) other;

    if (type != o.type)
      return false;
    if (colType != o.colType)
      return false;

    switch (type) {
      case CLASS:
        return meta == o.meta || meta.equals(o.meta);
      case EMB_CLASS:
        return meta == o.meta || meta.equals(o.meta) &&
               embFields == o.embFields || embFields.equals(o.embFields);
      case TYPED_LIT:
        return datatype.equals(o.datatype);
      default:
        return true;
    }
  }

  @Override
  public String toString() {
    return type + (type == Type.CLASS ? "[" + meta.getSourceClass().getName() + colType() + "]" :
                   type == Type.EMB_CLASS ? "[" + meta.getSourceClass().getName() + 
                                                  embFieldNames() + colType() + "]" :
                   type == Type.TYPED_LIT ? "[" + datatype + colType() + "]" :
                   (colType != MapperType.PREDICATE) ? "[" + colType + "]" : "");
  }

  private String embFieldNames() {
    StringBuilder res = new StringBuilder();
    for (EmbeddedClassMapper m : embFields)
      res.append('.').append(m.getName());
    return res.toString();
  }

  private String colType() {
    return (colType != MapperType.PREDICATE) ? ", " + colType : "";
  }
}
