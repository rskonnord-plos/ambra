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
import org.topazproject.otm.OtmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This holds the class information while the definitions are being parsed, and generates
 * a {@link java.lang.Class Class} and corresponding {@link ClassMetadata ClassMetadata}
 * when done.
 */
public class ClassDef {
  private static final Log log             = LogFactory.getLog(ClassDef.class)
  private static final Map classDefsByName = [:]
  private static final Map classDefsByType = [:]

  protected static final GroovyClassLoader gcl = new GroovyClassLoader()

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

  ClassMetadata toClass(RdfBuilder rdf) {
    if (metadata)
      return metadata

    // set up uriPrefix
    if (!uriPrefix)
      uriPrefix = inheritField('uriPrefix', true, null)
    if (!uriPrefix)
      uriPrefix = rdf.defUriPrefix
    uriPrefix = rdf.expandAliases(uriPrefix)

    // fix up type if necessary
    if (type && !type.toURI().isAbsolute()) {
      if (!uriPrefix)
        throw new OtmException("class '${className}': type '${type}' is not an absolute uri, " +
                               "but no uri-prefix has been configured")
      type = uriPrefix + type
    } else
      type = rdf.expandAliases(type)

    // initialize fields (needs uriPrefix and type to be set up)
    for (f in fields)
      f.init(rdf, this, classDefsByType)

    // collect all types and fields
    def allTypes  = [] as Set
    def allFields = []
    ClassDef clsDef = this
    while (clsDef) {
      if (clsDef.type) {
        if (type == "")
          type = clsDef.type
        allTypes << clsDef.type
      }
      allFields.addAll(clsDef.fields)
      clsDef = classDefsByName[clsDef.extendsClass]
    }

    // default type if necessary
    if (type == "") {
      if (!uriPrefix)
        throw new OtmException("class '${className}': type is unset, no type in superclass found," +
                               " and no uri-prefix has been configured")
      type = uriPrefix + className
      allTypes << type;
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
      idField.init(rdf, this, classDefsByName)
      idFields  << idField
      fields    << idField
      allFields << idField
    }

    def idField = idFields[0]

    // ensure model is set
    if (!model)
      model = inheritField('model', true, null)
    if (!model)
      model = rdf.defModel
    if (!model)
      throw new OtmException("No model has been set for class '${className}' and no default " +
                             "model has been defined either")

    // inherit id-generator
    if (idGenerator == '')
      idGenerator = inheritField('idGenerator', false, '')
    if (idGenerator == '')
      idGenerator = 'GUID'

    // generate the groovy class definition
    StringBuffer clsSrc = new StringBuffer(100)

    if (isAbstract)
      clsSrc << 'abstract '
    clsSrc << "class ${className} "
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
    Class clazz = gcl.parseClass(clsSrc.toString(), className)

    // create class-metadata
    def mappers = []
    for (m in fields.findAll{it != idField && !it.isTransient}*.toMapper(rdf, clazz, null))
      mappers.addAll(m)
    def idmapper = fields.contains(idField) ? idField.toMapper(rdf, clazz, getIdGen())[0] : null

    clsDef = classDefsByName[extendsClass]
    while (clsDef) {
      mappers.addAll(clsDef.toClass().getFields())
      if (!idmapper)
        idmapper = clsDef.toClass().getIdField()
      clsDef = classDefsByName[clsDef.extendsClass]
    }

    metadata = new ClassMetadata(clazz, getShortName(clazz), type, allTypes, model, uriPrefix,
                                 idmapper, mappers)

    if (log.debugEnabled)
      log.debug "created metadata for class '${clazz.name}': ${metadata}"

    // register and return
    classDefsByName[className] = this
    if (type && !classDefsByType[type])
      classDefsByType[type] = this

    return metadata
  }

  private def inheritField(def fname, boolean fromParents, def defVal) {
    ClassDef clsDef = this
    while (clsDef) {            // walk parent classes
      ClassDef supCls = clsDef
      while (supCls) {                  // walk superclasses
        if (supCls."${fname}" != defVal)
          return supCls."${fname}"
        supCls = classDefsByName[supCls.extendsClass]
      }
      clsDef = fromParents ? clsDef.parentClass : null
    }

    return defVal
  }

  private String getShortName(Class cls) {
    return cls.name - "${cls.getPackage()}.";
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
          return "new Date('${val}')"
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
    try {
      // Try to find generator by using FQCN
      generatorClazz = Class.forName(idGenerator);
    } catch (ClassNotFoundException cnfe) {
      try {
        // Allow classname to default to our package
        generatorClazz = Class.forName("org.topazproject.otm.id.${idGenerator}")
      } catch (ClassNotFoundException cnfe2) {
        // Allow classname to leave off the normal Generator suffix too
        generatorClazz = Class.forName("org.topazproject.otm.id.${idGenerator}Generator")
      }
    }

    def gen = generatorClazz.newInstance();
    gen.uriPrefix = uriPrefix

    return gen
  }
}
