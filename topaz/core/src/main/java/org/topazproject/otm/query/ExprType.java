/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.mapping.EmbeddedMapper;

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
  private final List<EmbeddedMapper>      embFields;
  private final String                    datatype;
  private final CollectionType            colType;

  private ExprType(Type type, ClassMetadata meta, List<EmbeddedMapper> embFields,
                   String datatype, CollectionType colType) {
    this.type      = type;
    this.meta      = meta;
    this.embFields = embFields;
    this.datatype  = datatype;
    this.colType   = (colType != null) ? colType : CollectionType.PREDICATE;
  }

  /** 
   * Create a new expression type representing a class. 
   * 
   * @param meta    the class metadata
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   * @throws NullPointerException if <var>meta</var> is null
   */
  public static ExprType classType(ClassMetadata meta, CollectionType colType) {
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
  public static ExprType embeddedClassType(ClassMetadata meta, EmbeddedMapper field) {
    if (meta == null)
      throw new NullPointerException("class metadata may not be null");
    if (field == null)
      throw new NullPointerException("class field may not be null");

    List<EmbeddedMapper> fields = new ArrayList<EmbeddedMapper>();
    fields.add(field);
    return new ExprType(Type.EMB_CLASS, meta, fields, null, null);
  }

  /** 
   * Create a new expression type representing a URI. 
   * 
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   */
  public static ExprType uriType(CollectionType colType) {
    return new ExprType(Type.URI, null, null, null, colType);
  }

  /** 
   * Create a new expression type representing an untyped literal. 
   * 
   * @param colType the collection type; if null it defaults to PREDICATE
   * @return the new type
   */
  public static ExprType literalType(CollectionType colType) {
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
  public static ExprType literalType(String datatype, CollectionType colType) {
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
  public List<EmbeddedMapper> getEmbeddedFields() {
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
  public CollectionType getCollectionType()
  {
      return colType;
  }

  /** 
   * Get the class of this type. For non class or embedded-class types this returns null.
   *
   * @return the class metadata, or null if not a class type
   */
  public ClassMetadata getExprClass() {
    switch (type) {
      case CLASS:
        return meta;

      case EMB_CLASS:
        return embFields.get(embFields.size() - 1).getEmbeddedClass();

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
    return type + (type == Type.CLASS ? "[" + meta.getName() + colType() + "]" :
                   type == Type.EMB_CLASS ? "[" + meta.getName() + 
                                                  embFieldNames() + colType() + "]" :
                   type == Type.TYPED_LIT ? "[" + datatype + colType() + "]" :
                   (colType != CollectionType.PREDICATE) ? "[" + colType + "]" : "");
  }

  private String embFieldNames() {
    StringBuilder res = new StringBuilder();
    for (EmbeddedMapper m : embFields)
      res.append('.').append(m.getName());
    return res.toString();
  }

  private String colType() {
    return (colType != CollectionType.PREDICATE) ? ", " + colType : "";
  }
}
