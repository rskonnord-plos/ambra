/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.ArrayMapper;
import org.topazproject.otm.mapping.CollectionMapper;
import org.topazproject.otm.mapping.EmbeddedClassMapper;
import org.topazproject.otm.mapping.EmbeddedClassFieldMapper;
import org.topazproject.otm.mapping.FunctionalMapper;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.Serializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Describes a field in a class.
 */
public class FieldDef {
  private static final Log    log    = LogFactory.getLog(FieldDef.class)
  private static final String xsdURI = "http://www.w3.org/2001/XMLSchema#";
  private static final String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  /** the field name */
  String   name
  /** the rdf predicate to store this under; if not specified, defaults to uriPrefix + name  */
  String   pred
  /** the model to store the statements in */
  String   model
  /**
   * the rdf type. If null, this is an untyped literal; if it's an xsd type, this is a typed
   * literal; else this is a class type, and interpreted as uriPrefix + type if not absolute
   */
  String   type
  /**
   * the property type. If maxCard &gt; 1 this is the component type. If null this is defaulted
   * from the rdf type.
   */
  Class    javaType
  /** true if this is the id-field */
  boolean  isId        = false
  /** max cardinality; use -1 for unbound */
  int      maxCard     = 1
  /** true if the predicate is an "inverse" predicate */
  boolean  inverse     = false
  /** true if the value should be unique among all values in all instances */
  boolean  unique      = false
  /** true if this field should be not be persisted (or loaded) */
  boolean  isTransient = false
  /** if maxCard &gt; 1, the field's type - must be one of 'Set', 'List', or 'Array' */
  String   colType
  /**
   * if maxCard &gt; 1, how the collection is stored in rdf - must be one of 'Predicate',
   * 'RdfList', 'RdfSeq', 'RdfBag', or 'RdfAlt'.
   */
  String   colMapping
  /** if false this field is never saved, only loaded */
  boolean  owned       = true
  /**
   * true if the class instance this field points to should have it's fields stored as though they
   * were part of this instance instead of as a separate instance. Only valid for class types.
   */
  boolean  embedded    = false

  protected ClassDef classType          // the class-def if this has a class type
  protected Map      classAttrs = [:]   // save class attributes for later
  protected def      defaultValue

  /**
   * This explicit constructor is so we can "split" the attributes into the per-class and
   * the per-field ones. Needed because we don't know whether this is a nested class definition
   * or not at this time.
   */
  FieldDef(Map attrs) {
    attrs.each{ k, v ->
      boolean inCls = ClassDef.class.declaredFields.any{it.name == k}
      if (inCls)
        classAttrs[k] = v
      if (!inCls || FieldDef.class.declaredFields.any{it.name == k})
        setProperty(k, v)
    }
  }

  def call(def f) {
    defaultValue = f
  }

  /**
   * Resolve any defaults
   */
  protected init(RdfBuilder rdf, ClassDef clsDef, def classDefsByType) {
    // fix up predicate if needed
    if (!pred && !isId && !embedded)
      pred = name;
    if (pred && !pred.toURI().isAbsolute()) {
      if (!clsDef.uriPrefix)
        throw new OtmException("field '${name}' has non-absolute predicate '${pred}' but no " +
                               "uri-prefix has been configured")
      pred = clsDef.uriPrefix + pred
    } else
      pred = rdf.expandAliases(pred)

    // fix up type if needed
    if (type && !type.toURI().isAbsolute()) {
      if (!clsDef.uriPrefix)
        throw new OtmException("field '${name}' has type '${type}' which is not an absolute uri, " +
                               "but no uri-prefix has been configured")
      type = clsDef.uriPrefix + type
    } else
      type = rdf.expandAliases(type)

    // and resolve any references to classes defined earlier
    if (!classType)
      classType = classDefsByType[type]
    if (classType)
      type = classType.type

    // validity checks
    if (embedded && (maxCard < 0 || maxCard > 1))
      throw new OtmException("embedded fields must have max-cardinality 1; " +
                             "class='${clsDef.className}', field='${name}', maxCard=${maxCard}")
    if (embedded && !classType)
      throw new OtmException("only class types may be embedded; class='${clsDef.className}', " +
                             "field='${name}', type=${type}")
    if (embedded && pred)
      log.warn "predicate ignored for embedded field; class='${clsDef.className}', " +
               "field='${name}', pred=${pred}"
  }

  /**
   * Generate a mapper for this field.
   */
  protected List toMapper(RdfBuilder rdf, Class cls, IdentifierGenerator idGen) {
    Field  f   = cls.getDeclaredField(name)
    Method get = cls.getMethod('get' + RdfBuilder.capitalize(name))
    Method set = cls.getMethod('set' + RdfBuilder.capitalize(name), f.getType())
    Mapper.MapperType mt = getMapperType(rdf)

    String dtype = (type?.startsWith(xsdURI) && type != xsdURI + 'anyURI') ? type : null

    // generate the mapper(s)
    List m;
    if (maxCard == 1 && embedded) {
      def container = new EmbeddedClassMapper(f, get, set)
      m = classType.toClass().fields.collect{ new EmbeddedClassFieldMapper(container, it) }
    } else if (maxCard == 1) {
      Serializer ser = rdf.sessFactory.getSerializerFactory().getSerializer(f.getType(), dtype)
      String rt = (ser == null) ? getRdfType(rdf, f.getType()) : null;
      m = [new FunctionalMapper(pred, f, get, set, ser, dtype, rt, inverse, model, mt, owned, idGen)]
    } else {
      String     collType = colType ? colType : rdf.defColType
      Class      compType = toJavaClass(getBaseJavaType(), rdf);
      Serializer ser      = rdf.sessFactory.getSerializerFactory().getSerializer(compType, dtype)
      String rt = (ser == null) ? getRdfType(rdf, compType) : null;
      if (collType.toLowerCase() == 'array')
        m = [new ArrayMapper(pred, f, get, set, ser, compType, dtype, rt, inverse, model, mt, owned,
                             idGen)]
      else
        m = [new CollectionMapper(pred, f, get, set, ser, compType, dtype, rt, inverse, model, mt,
                                  owned, idGen)]
    }

    // done
    if (log.debugEnabled)
      log.debug "created mapper(s) for field '${name}' in class '${cls.name}': ${m}"

    return m;
  }

  protected String getJavaType(RdfBuilder rdf) {
    if (maxCard > 1 || maxCard < 0) {
      String ct = colType ? colType : rdf.defColType
      switch (ct?.toLowerCase()) {
        case null:
        case 'list':
          return 'List'
        case 'set':
          return 'Set'
        case 'array':
          return getBaseJavaType() + '[]'
        default:
          throw new OtmException("Unknown collection type '${ct}' - must be one of 'List', " + 
                                 "'Set', or 'Array'");
      }
    } else
      return getBaseJavaType();
  }

  protected String getBaseJavaType() {
    if (javaType)
      return javaType.name;
    else if (classType)
      return classType.className
    else
      return xsdToJava()
  }

  private String xsdToJava() {
    switch (type) {
      case null:
        return 'String'
      case xsdURI + 'string':
        return 'String'
      case rdfURI + 'XMLLiteral':
        return 'String'
      case xsdURI + 'anyURI':
        return 'URI'
      case xsdURI + 'boolean':
        return 'boolean'
      case xsdURI + 'byte':
        return 'byte'
      case xsdURI + 'short':
        return 'short'
      case xsdURI + 'int':
        return 'int'
      case xsdURI + 'long':
        return 'long'
      case xsdURI + 'float':
        return 'float'
      case xsdURI + 'double':
        return 'double'
      case xsdURI + 'dateTime':
        return 'Date'
      case xsdURI + 'date':
        return 'Date'
      case xsdURI + 'time':
        return 'Date'
      default:
        throw new OtmException("Unsupported xsd type '${type}' - must be one of " +
                               "xsd:string, rdf:XMLLiteral, xsd:anyURI, xsd:boolean, xsd:byte, " +
                               "xsd:short, xsd:int, xsd:long, xsd:float, xsd:double, xsd:date, " +
                               "xsd:time, xsd:dateTime")
    }
  }

  private Class toJavaClass(String name, RdfBuilder rdf) {
    switch (name) {
      case 'String':
        return String.class
      case 'URI':
        return URI.class
      case 'Date':
        return Date.class
      case 'boolean':
        return Boolean.TYPE
      case 'byte':
        return Byte.TYPE
      case 'char':
        return Character.TYPE
      case 'short':
        return Short.TYPE
      case 'int':
        return Integer.TYPE
      case 'long':
        return Long.TYPE
      case 'float':
        return Float.TYPE
      case 'double':
        return Double.TYPE
      default:
        return ClassDef.gcl.loadClass(name)
    }
  }

  private Mapper.MapperType getMapperType(RdfBuilder rdf) {
    String cm = colMapping ? colMapping : rdf.defColMapping
    switch (cm?.toLowerCase()) {
      case null:
      case 'predicate':
        return Mapper.MapperType.PREDICATE
      case 'rdflist':
        return Mapper.MapperType.RDFLIST
      case 'rdfbag':
        return Mapper.MapperType.RDFBAG
      case 'rdfseq':
        return Mapper.MapperType.RDFSEQ
      case 'rdfalt':
        return Mapper.MapperType.RDFALT
      default:
        throw new OtmException("Unknown collection-mapping type '${cm}' - must be one of " +
                               "'Predicate', 'RdfList', 'RdfBag', 'RdfSeq', or 'RdfAlt'");
    }
  }

  private String getRdfType(RdfBuilder rdf, Class clazz) {
    ClassMetadata cm = rdf.sessFactory.getClassMetadata(clazz);
    if (cm == null) {
      try { rdf.sessFactory.preload(clazz)} catch (Throwable t) {}
      cm = rdf.sessFactory.getClassMetadata(clazz);
    }
    return (cm != null) ? cm.getType() : null;
  }
}
