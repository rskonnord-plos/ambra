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
import org.topazproject.otm.SessionFactory;
import org.topazproject.mulgara.itql.ItqlHelper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a groovy-builder for OTM. It allows for the definition of classes in groovy.
 * Example:
 * <pre>
 *  cls = rdf.class('UserId', type:'foaf:OnlineAccount', uriPrefix:'topaz:') {
 *    authIds (pred:'topaz:hasAuthId', maxCard:-1, unique:true) {
 *      realm () 'local'
 *      value ()
 *    }
 *    state (pred:'topaz:accountState', type:'xsd:int')
 *  }
 * </pre>
 *
 * <p>The list of supported attributes for classes is defined as the list of properties
 * of {@link ClassDef ClassDef}; the list of supported attributes for non-class fields
 * is the list of properties of {@link FieldDef FieldDef}; the list of supported attributes
 * for fields defining nested classes is the union of the two.</p>
 *
 * <p>The builder can also be configured with a bunch of defaults to be used when creating
 * classes, such as the collection type, the model, and the uri-prefix.</p>
 */
public class RdfBuilder extends BuilderSupport {
  private static final Log log = LogFactory.getLog(RdfBuilder.class)

  /** the session factory to use for looking up serializers and registering class-metadata */
  SessionFactory sessFactory = new SessionFactory()
  /** the default collection type; defaults to 'List' */
  String         defColType = 'List'
  /** the default collection mapping type; defaults to 'Predicate' */
  String         defColMapping = 'Predicate'
  /** the default model to use */
  String         defModel
  /** the default uri-prefix to use */
  String         defUriPrefix

  protected static String capitalize(String str) {
    return str[0].toUpperCase() + (str.size() > 1 ? str[1..-1] : "")
  }

  protected static String uncapitalize(String str) {
    return str[0].toLowerCase() + (str.size() > 1 ? str[1..-1] : "")
  }

  String expandAliases(String str) {
    // FIXME: do general alias expansion
    return str?.replaceAll('^([^:]+):') { all, alias ->
      def r = ItqlHelper.defaultAliases[alias]; return r ? r : alias + ':'
    }
  }


  /* The methods are invoked as follows while processing a definition:
   *
   *  1. the appropriate createNode is invoked
   *  2. for everything except the top-level item the setParent method
   *     is invoked
   *  3. if the node is itself is structured (has { }) then step 1. is
   *     called recursively for all items in the structure
   *  4. nodeCompleted is invoked
   *
   * One of the consequences of the above sequence of calls is that you
   * don't know whether a node is simple or structured until step 3
   * when you receive a setParent call for a nested node. So some things
   * can't be done in step 2 but have to be deferred till step 4.
   */

  /**
   * This is overrriden because we need to use a different class while building the
   * structures (a ClassDef) than what we return to the user (a java.lang.Class).
   */
  protected Object doInvokeMethod(String methodName, Object name, Object args) {
    if (log.traceEnabled)
      log.trace "doInvokeMethod called with methodName = '${methodName}', name = '${name}'"

    Object node = super.doInvokeMethod(methodName, name, args);

    if (node instanceof ClassDef) {
      ClassMetadata md = node.toClass(this)
      if (md.isEntity())
        sessFactory.setClassMetadata(md)
      return md.getSourceClass()
    } else if (node instanceof FieldDef && node.classType) {
      ClassMetadata md = node.classType.toClass(this)
      if (md.isEntity())
        sessFactory.setClassMetadata(md)
      return node
    } else {
      return node;
    }
  }

  protected Object createNode(Object name) {
    return createNode(name, [:], null);
  }

  protected Object createNode(Object name, Object value) {
    return createNode(name, [:], value)
  }

  protected Object createNode(Object name, Map attributes) {
    return createNode(name, attributes, null)
  }

  protected Object createNode(Object name, Map attributes, Object value) {
    if (log.traceEnabled)
      log.trace "createNode called with name = '${name}', attributes = '${attributes}', " +
                "value = '${value}'"

    switch (name) {
      case 'class':
        // create a class
        if (!value && !attributes['className'])
          throw new OtmException("at least a class-name is required")
        if (value)
          attributes['className'] = value
        return new ClassDef(attributes)

      default:
        // create predicate
        attributes['name'] = name
        return new FieldDef(attributes)
    }
  }

  private ClassDef getClassDef(Object node) {
    if (node instanceof ClassDef)
      return node

    // nested class
    if (!node.classType)
      node.classType = new ClassDef([className:capitalize(node.name), *:node.classAttrs])
    return node.classType
  }

  protected void setParent(Object parent, Object child) {
    if (log.traceEnabled)
      log.trace "setParent called with parent = '${parent}', child = '${child}'"

    getClassDef(parent).fields << child
  }

  protected void nodeCompleted(Object parent, Object node) {
    if (log.traceEnabled)
      log.trace "nodeCompleted called with parent = '${parent}', node = '${node}'"

    if (node instanceof FieldDef && node.classType)
      node.classType.parentClass = getClassDef(parent);
  }
}
