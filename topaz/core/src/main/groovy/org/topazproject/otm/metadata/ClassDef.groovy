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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.RdfMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This holds the class information while the definitions are being parsed, and generates
 * a {@link java.lang.Class Class} and corresponding {@link ClassMetadata ClassMetadata}
 * when done.
 */
public class ClassDef {
  private static final Log log = LogFactory.getLog(ClassDef.class)

  /** the name of the java class */
  String   className
  /**
   * the rdf type; if relative, it's set to uriPrefix + type; if unset, it's inherited from
   * superclass; if still unset, it's set to uriPrefix + className; set to null for no type.
   */
  String   type = ""
  /** the default model for properties in this class */
  String   model
  /** the uri prefix to use to create absolute uri's from names */
  String   uriPrefix
  /** the className of the class being extended */
  String   extendsClass
  /** true if this should be an abstract class; no id-field is generated if missing in this case */
  boolean  isAbstract
  /** the type of id-generator to use; can be a class name or shortcut; defaults to GUID */
  String   idGenerator = ''

  protected ClassDef parentClass
  protected List     fields = []

  private ClassMetadata metadata

  protected ClassDef(RdfBuilder rdf, Map attrs) {
    // apply the attributes
    attrs.each{ k, v ->
      setProperty(k, v)
    }

    // set up uriPrefix
    if (!uriPrefix)
      uriPrefix = inheritField('uriPrefix', true, null, rdf)
    if (!uriPrefix)
      uriPrefix = rdf.defUriPrefix
    uriPrefix = rdf.expandAlias(uriPrefix)

    // fix up type if necessary
    if (type && !type.toURI().isAbsolute()) {
      if (!uriPrefix)
        throw new OtmException("class '${className}': type '${type}' is not an absolute uri, " +
                               "but no uri-prefix has been configured")
      type = uriPrefix + type
    } else
      type = rdf.expandAlias(type)

    // find type, inheriting if necessary
    if (type == "") {
      ClassDef clsDef = this
      while (clsDef) {
        if (clsDef.type) {
          type = clsDef.type
          break
        }
        clsDef = rdf.classDefsByName[clsDef.extendsClass]
      }
    }

    // default type if necessary
    if (type == "") {
      if (!uriPrefix)
        throw new OtmException("class '${className}': type is unset, no type in superclass found," +
                               " and no uri-prefix has been configured")
      type = uriPrefix + className
    }

    // ensure model is set
    if (!model)
      model = inheritField('model', true, null, rdf)
    if (!model)
      model = rdf.defModel
    if (!model)
      throw new OtmException("No model has been set for class '${className}' and no default " +
                             "model has been defined either")

    // inherit id-generator
    if (idGenerator == '')
      idGenerator = inheritField('idGenerator', false, '', rdf)
    if (idGenerator == '')
      idGenerator = 'GUID'

    // register ourselves now so fields and nested classes can refer back to us
    rdf.classDefsByName[className] = this
    if (type && !rdf.classDefsByType[type])
      rdf.classDefsByType[type] = this
  }

  ClassMetadata toClass(RdfBuilder rdf) {
    if (metadata)
      return metadata

    // initialize fields (needs uriPrefix and type to be set up)
    for (f in fields)
      f.init(rdf, this)

    // collect all types and fields
    def allFields = []
    def allTypes  = [] as Set
    ClassDef clsDef = this
    while (clsDef) {
      if (clsDef.type)
        allTypes << clsDef.type
      allFields.addAll(clsDef.fields)
      clsDef = rdf.classDefsByName[clsDef.extendsClass]
    }

    // find the id-field and ensure there's only one; if there's none, create one
    def idFields = allFields.findAll{it.isId}
    if (idFields.size() > 1)
      throw new OtmException("more than one id-field defined for class '${className}': " +
                             "${idFields.collect{it.name}}");
    if (idFields.size() == 1 && idFields[0].maxCard != 1)
      throw new OtmException("id-fields may not be collections - class: '${className}', " +
                             "id-field: '${idFields[0].name}'");
    if (idFields.size() == 0 && allFields.any{it.name == 'id'} && !isAbstract)
      throw new OtmException("no id-fields defined for class '${className}', and one field is " +
                             "already named 'id'");

    if (idFields.size() == 0 && !isAbstract) {
      def idField = new FieldDef(name:'id', isId:true, type:'xsd:anyURI')
      idField.init(rdf, this)
      idFields  << idField
      fields    << idField
      allFields << idField
    }

    def idField = idFields[0]

    // generate the groovy class definition
    StringBuffer clsSrc = new StringBuffer(100)

    if (className.contains('.'))
      clsSrc << "package ${className.substring(0, className.lastIndexOf('.'))}\n"
    if (isAbstract)
      clsSrc << 'abstract '
    clsSrc << "class ${className.substring(className.lastIndexOf('.') + 1)} "
    if (extendsClass)
      clsSrc << "extends ${extendsClass} "
    clsSrc << "{\n"

    for (f in fields) {
      if (f.defaultValue)
        clsSrc << "  ${f.getJavaType(rdf)} ${f.name} = ${javaConst(f)}\n"
      else
        clsSrc << "  ${f.getJavaType(rdf)} ${f.name}\n"
    }

    clsSrc << "  public String toString() {\n"
    clsSrc << "    return '${className}' + [\n"
    for (f in allFields)
      clsSrc << "      ${f.name}:${f.name}, \n"
    clsSrc << "    ].toString()\n"
    clsSrc << "  }\n"

    if (fields.contains(idField)) {
      clsSrc << "  public int hashCode() {\n"
      clsSrc << "    return ${idField.name}.hashCode()\n"
      clsSrc << "  }\n"
    }

    clsSrc << "  public boolean equals(Object o) {\n"
    clsSrc << "    return \\\n"
    if (extendsClass)
      clsSrc << "      super.equals(o) &&\n"
    else
      clsSrc << "      o != null &&\n"
    for (f in fields)
      clsSrc << "      o.${f.name} == ${f.name} &&\n"
    clsSrc << "    true\n"
    clsSrc << "  }\n"

    clsSrc << "}\n"

    if (log.debugEnabled)
      log.debug "Class def: ${clsSrc}\n"

    // create class
    Class clazz = rdf.gcl.parseClass(clsSrc.toString(), className)

    // create class-metadata
    def mappers = []
    for (m in fields.findAll{it != idField && !it.isTransient}*.toMapper(rdf, clazz, null))
      mappers.addAll(m)
    def idmapper = fields.contains(idField) ? idField.toMapper(rdf, clazz, getIdGen())[0] : null

    clsDef = rdf.classDefsByName[extendsClass]
    while (clsDef) {
      mappers.addAll(clsDef.toClass().getRdfMappers())
      mappers.addAll(clsDef.toClass().getEmbeddedMappers())
      if (!idmapper)
        idmapper = clsDef.toClass().getIdField()
      clsDef = rdf.classDefsByName[clsDef.extendsClass]
    }

    def rdfs = []
    def embeds = []
    for (m in mappers) {
      if (m instanceof RdfMapper)
         rdfs.add(m)
      if (m instanceof EmbeddedMapper)
         embeds.add(m)
    }

    def blobBinder = null
    def binders = [(EntityMode.POJO) : new ClassBinder(clazz)]
    metadata = new ClassMetadata(binders, getShortName(clazz), type, allTypes, model,
                                 idmapper, rdfs, blobBinder, extendsClass, embeds)

    if (log.debugEnabled)
      log.debug "created metadata for class '${clazz.name}': ${metadata}"

    return metadata
  }

  private def inheritField(def fname, boolean fromParents, def defVal, RdfBuilder rdf) {
    ClassDef clsDef = this
    while (clsDef) {            // walk parent classes
      ClassDef supCls = clsDef
      while (supCls) {                  // walk superclasses
        if (supCls."${fname}" != defVal)
          return supCls."${fname}"
        supCls = rdf.classDefsByName[supCls.extendsClass]
      }
      clsDef = fromParents ? clsDef.parentClass : null
    }

    return defVal
  }

  private String getShortName(Class cls) {
    return cls.name.substring(cls.name.lastIndexOf('.') + 1)
  }

  private String javaConst(FieldDef f) {
    if (f.maxCard < 0 || f.maxCard > 1) {
      if (f.defaultValue instanceof Collection) {
        return '[' + f.defaultValue.collect{javaBaseConst(f, it)}.join(', ') + ']' +
            (f.colType?.toLowerCase() == 'set' ? ' as Set' : '')
      } else {
        return '[' + javaBaseConst(f, f.defaultValue) + ']' +
            (f.colType?.toLowerCase() == 'set' ? ' as Set' : '')
      }
    } else {
      return javaBaseConst(f, f.defaultValue)
    }
  }

  private String javaBaseConst(FieldDef f, def val) {
    switch (f.getBaseJavaType()) {
      case "String":
        return "'${val}'"

      case "boolean":
      case "byte":
      case "short":
      case "int":
        return val

      case "long":
        return val + 'L'

      case "float":
        return val + 'f'

      case "double":
        return val + 'd'

      case "URI":
        if (!(val instanceof URI))
          new URI(val)       // verify it parses
        return "URI.create('${val}')"

      case "Date":
        if (val instanceof Date) {
          return "new Date(${val.getTime()}L)"
        } else {
          try {
            val.toLong();
            return "new Date(${val}L)"
          } catch (NumberFormatException nfe) {
            return "new Date('${val}')"
          }
        }

      default:
        throw new OtmException("Don't know how to create a constant of type " +
                               "${f.getBaseJavaType()}; class '${className}', field '${f.name}'")
    }
  }

  private def getIdGen() {
    if (!idGenerator)
      return null

    Class generatorClazz
    ClassNotFoundException lastEx
    for (cl in [Thread.currentThread().getContextClassLoader(), ClassDef.class.getClassLoader()]) {
      try {
        // Try to find generator by using FQCN
        generatorClazz = Class.forName(idGenerator, true, cl);
      } catch (ClassNotFoundException cnfe) {
        try {
          // Allow classname to default to our package
          generatorClazz = Class.forName("org.topazproject.otm.id.${idGenerator}", true, cl)
        } catch (ClassNotFoundException cnfe2) {
          try {
            // Allow classname to leave off the normal Generator suffix too
            generatorClazz =
                Class.forName("org.topazproject.otm.id.${idGenerator}Generator", true, cl)
          } catch (ClassNotFoundException cnfe3) {
            lastEx = cnfe3;
          }
        }
      }
    }
    if (!generatorClazz)
      throw lastEx

    def gen = generatorClazz.newInstance();
    gen.uriPrefix = uriPrefix

    return gen
  }
}
