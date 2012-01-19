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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.net.URI;
import java.net.URL;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Alias;
import org.topazproject.otm.annotations.Aliases;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Embeddable;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.Predicate.PropType;
import org.topazproject.otm.annotations.PredicateMap;
import org.topazproject.otm.annotations.Projection;
import org.topazproject.otm.annotations.SubView;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.annotations.View;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.mapping.java.ArrayFieldBinder;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.CollectionFieldBinder;
import org.topazproject.otm.mapping.java.EmbeddedClassFieldBinder;
import org.topazproject.otm.mapping.java.FieldBinder;
import org.topazproject.otm.mapping.java.ScalarFieldBinder;
import org.topazproject.otm.serializer.Serializer;

/**
 * A factory that processes annotations on a class and creates
 * ClassMetadata for it.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationClassMetaFactory {
  private static final Log log = LogFactory.getLog(AnnotationClassMetaFactory.class);
  private SessionFactory   sf;

  /**
   * Creates a new AnnotationClassMetaFactory object.
   *
   * @param sf the session factory
   */
  public AnnotationClassMetaFactory(SessionFactory sf) {
    this.sf                    = sf;
  }

  /**
   * Creates a new ClassMetadata object.
   *
   * @param clazz the class with annotations
   *
   * @return a newly created ClassMetadata object
   *
   * @throws OtmException on an error
   */
  public ClassMetadata create(Class clazz) throws OtmException {
    addAliases(clazz);

    if ((clazz.getAnnotation(View.class) != null) || (clazz.getAnnotation(SubView.class) != null))
      return createView(clazz);
    else

      return createEntity(clazz);
  }

  private void addAliases(Class<?> clazz) throws OtmException {
    Aliases aliases = clazz.getAnnotation(Aliases.class);

    if (aliases != null) {
      for (Alias a : aliases.value())
        sf.addAlias(a.alias(), a.value());
    }

    aliases = (clazz.getPackage() != null) ? clazz.getPackage().getAnnotation(Aliases.class) : null;

    if (aliases != null) {
      for (Alias a : aliases.value())
        sf.addAlias(a.alias(), a.value());
    }
  }

  private ClassMetadata createEntity(Class<?> clazz) throws OtmException {
    String type      = null;
    String model     = null;
    String uriPrefix = null;
    String sup       = null;

    if (log.isDebugEnabled())
      log.debug("Creating class-meta for " + clazz);

    Entity entity = clazz.getAnnotation(Entity.class);

    if (entity != null) {
      if (!"".equals(entity.model()))
        model = entity.model();

      if (!"".equals(entity.type()))
        type = sf.expandAlias(entity.type());
    }

    String   name = getEntityName(clazz);
    Class<?> s    = clazz.getSuperclass();

    if ((s != null) && !s.equals(Object.class))
      sup = getEntityName(s);

    EntityDefinition ed           = new EntityDefinition(name, type, model, sup);

    UriPrefix        uriPrefixAnn = clazz.getAnnotation(UriPrefix.class);

    if (uriPrefixAnn != null)
      uriPrefix = sf.expandAlias(uriPrefixAnn.value());

    return createMeta(ed, clazz, uriPrefix);
  }

  private ClassMetadata createView(Class<?> clazz) throws OtmException {
    String name;
    String query = null;

    View   view  = clazz.getAnnotation(View.class);

    if (view != null) {
      name    = (!"".equals(view.name())) ? view.name() : getName(clazz);
      query   = view.query();
    } else {
      SubView sv = clazz.getAnnotation(SubView.class);
      name = (!"".equals(sv.name())) ? sv.name() : getName(clazz);
    }

    ViewDefinition vd = new ViewDefinition(name, query);

    return createMeta(vd, clazz, null);
  }

  private ClassMetadata createMeta(ClassDefinition def, Class<?> clazz, String uriPrefix)
                            throws OtmException {
    sf.addDefinition(def);

    ClassBindings bin = sf.getClassBindings(def.getName());
    bin.bind(EntityMode.POJO, new ClassBinder(clazz));

    for (Field f : clazz.getDeclaredFields()) {
      FieldInfo fi = FieldInfo.create(def, f);

      if (fi == null) {
        log.info("Skipped " + f.getName() + " in " + clazz.getName());

        continue;
      }

      PropertyDefinition d = fi.getDefinition(sf, uriPrefix);

      if (d == null) {
        log.info("Skipped " + fi);

        continue;
      }

      FieldBinder b = fi.getBinder(sf, d);

      sf.addDefinition(d);
      bin.addAndBindProperty(d.getName(), EntityMode.POJO, b);
    }

    return def.buildClassMetadata(sf);
  }

  private static String getName(Class<?> clazz) {
    String  name = clazz.getName();
    Package p    = clazz.getPackage();

    if (p != null)
      name = name.substring(p.getName().length() + 1);

    return name;
  }

  private static String getModel(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.model()))
      return entity.model();

    return getModel(clazz.getSuperclass());
  }

  private static String getEntityName(Class<?> clazz) {
    if (clazz == null)
      return null;

    Entity entity = clazz.getAnnotation(Entity.class);

    if ((entity != null) && !"".equals(entity.name()))
      return entity.name();

    return getName(clazz);
  }

  private static class FieldInfo {
    final ClassDefinition cd;
    final Field           field;
    final String          name;
    final Class<?>        type;
    final Method          getter;
    final Method          setter;
    final boolean         isArray;
    final boolean         isCollection;

    private FieldInfo(ClassDefinition cd, Field field)
               throws OtmException {
      this.cd        = cd;
      this.field     = field;
      name           = cd.getName() + ":" + field.getName();

      int     mod    = field.getModifiers();
      boolean readOnly = cd instanceof ViewDefinition;

      if (Modifier.isFinal(mod))
        throw new OtmException("'final' field " + this + " can't be persisted.");

      String mn = capitalize(field.getName());
      getter         = getGetter(field.getDeclaringClass(), "get" + mn);
      setter         = getSetter(field.getDeclaringClass(), "set" + mn, field.getType());

      if ((((getter == null) && !readOnly) || (setter == null)) && !Modifier.isPublic(mod))
        throw new OtmException("The field " + this + " is inaccessible and can't be persisted.");

      isArray        = field.getType().isArray();
      isCollection   = Collection.class.isAssignableFrom(field.getType());

      if (isArray)
        type = field.getType().getComponentType();
      else if (isCollection)
        type = collectionType(field);
      else
        type = field.getType();
    }

    static FieldInfo create(ClassDefinition cd, Field field)
                     throws OtmException {
      int     mod      = field.getModifiers();
      boolean readOnly = cd instanceof ViewDefinition;

      if (Modifier.isStatic(mod) || (!readOnly && Modifier.isTransient(mod)))
        return null;

      return new FieldInfo(cd, field);
    }

    private static String capitalize(String name) {
      return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static Method getGetter(Class<?> invokedOn, String name) {
      try {
        return invokedOn.getMethod(name);
      } catch (NoSuchMethodException e) {
        return null;
      }
    }

    private static Method getSetter(Class<?> invokedOn, String name, Class<?> type) {
      for (Class<?> t = type; t != null; t = t.getSuperclass()) {
        try {
          return invokedOn.getMethod(name, t);
        } catch (NoSuchMethodException e) {
        }
      }

      for (Class<?> t : type.getInterfaces()) {
        try {
          return invokedOn.getMethod(name, t);
        } catch (NoSuchMethodException e) {
        }
      }

      return null;
    }

    private static Class collectionType(Field field) {
      Type  type   = field.getGenericType();
      Class result = Object.class;

      if (type instanceof ParameterizedType) {
        ParameterizedType ptype = (ParameterizedType) type;
        Type[]            targs = ptype.getActualTypeArguments();

        if ((targs.length > 0) && (targs[0] instanceof Class))
          result = (Class) targs[0];
      }

      return result;
    }

    public String getName() {
      return name;
    }

    public String toString() {
      return "'" + field.getName() + "' in " + field.getDeclaringClass();
    }

    public PropertyDefinition getDefinition(SessionFactory sf, String uriPrefix)
                                     throws OtmException {
      GeneratedValue gv   = null;
      Annotation     ann  = null;
      String         ours = Id.class.getPackage().getName();

      for (Annotation a : field.getAnnotations()) {
        if (a instanceof GeneratedValue)
          gv = (GeneratedValue) a;
        else if (a.annotationType().getPackage().getName().equals(ours)) {
          if (ann != null)
            throw new OtmException("Only one of @Id, @Predicate, @Blob, @Projection, @PredicateMap"
                                   + " or @Embedded can be applied to " + this);

          ann = a;
        }
      }

      if (ann == null)
        ann = type.getAnnotation(Embeddable.class);

      if (((ann != null) && !(ann instanceof Id)) && 
          ((cd instanceof ViewDefinition) ^ (ann instanceof Projection)))
        throw new OtmException("@Projection can-only/must be applied to Views or Sub-Views : "
            + this);

      if ((ann == null) && (cd instanceof ViewDefinition))
        return null;

      IdentifierGenerator generator;

      if (gv == null)
        generator = null;
      else if ((ann instanceof Id) || (ann instanceof Predicate) || (ann == null))
        generator = getGenerator(sf, uriPrefix, gv);
      else
        throw new OtmException("@GeneratedValue can only be specified for @Id and @Predicate : "
                               + this);

      if ((ann instanceof Predicate) || (ann == null))
        return getRdfDefinition(sf, uriPrefix, (Predicate) ann, generator);

      if (ann instanceof Projection)
        return getVarDefinition(sf, (Projection) ann);

      if (ann instanceof Id)
        return getIdDefinition(sf, (Id) ann, generator);

      if (ann instanceof Blob)
        return getBlobDefinition(sf, (Blob) ann);

      if ((ann instanceof Embedded) || (ann instanceof Embeddable))
        return getEmbeddedDefinition(sf);

      if (ann instanceof PredicateMap)
        return getPredicateMapDefinition(sf, (PredicateMap) ann);

      throw new OtmException("Unexpected annotation " + ann.annotationType() + " on " + this);
    }

    public IdentifierGenerator getGenerator(SessionFactory sf, String uriPrefix, GeneratedValue gv)
                                     throws OtmException {
      IdentifierGenerator generator;

      try {
        generator = (IdentifierGenerator) Thread.currentThread().getContextClassLoader()
                                                 .loadClass(gv.generatorClass()).newInstance();
      } catch (Throwable t) {
        // Between Class.forName() and newInstance() there are a half-dozen possible excps
        throw new OtmException("Unable to find implementation of '" + gv.generatorClass()
                               + "' generator for " + this, t);
      }

      String pre = sf.expandAlias(gv.uriPrefix());

      if (pre.equals("")) {
        pre   = ((uriPrefix == null) || uriPrefix.equals("")) ? Rdf.topaz : uriPrefix;
        // Compute default uriPrefix: Rdf.topaz/clazz/generatorClass#
        pre   = Rdf.topaz + field.getDeclaringClass().getName() + '/' + field.getName() + '#';

        //pre = pre + cd.getName() + '/' + field.getName() + '/';
      } else {
        try {
          // Validate that we have a valid uri
          URI.create(pre);
        } catch (IllegalArgumentException iae) {
          throw new OtmException("Illegal uriPrefix '" + pre + "' in @GeneratedValue for " + this,
                                 iae);
        }
      }

      generator.setUriPrefix(pre);

      return generator;
    }

    public RdfDefinition getRdfDefinition(SessionFactory sf, String ns, Predicate rdf,
                                          IdentifierGenerator generator)
                                   throws OtmException {
      String uri =
        ((rdf != null) && !"".equals(rdf.uri())) ? sf.expandAlias(rdf.uri())
        : ((ns != null) ? (ns + field.getName()) : null);

      if (uri == null)
        throw new OtmException("Missing attribute 'uri' in @Predicate for " + this);

      String dt =
        ((rdf == null) || "".equals(rdf.dataType()))
        ? sf.getSerializerFactory().getDefaultDataType(type) : sf.expandAlias(rdf.dataType());

      if (Predicate.UNTYPED.equals(dt))
        dt = null;

      Serializer serializer = sf.getSerializerFactory().getSerializer(type, dt);

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' with dataType '" + dt
                               + "' for field " + this);

      boolean inverse = (rdf != null) && rdf.inverse();
      String  model   = ((rdf != null) && !"".equals(rdf.model())) ? rdf.model() : null;

      String  assoc   = (serializer == null) ? getEntityName(type) : null;

      if (inverse && (model == null) && (serializer == null))
        model = getModel(type);

      boolean        notOwned       = (rdf != null) && rdf.notOwned();

      CollectionType mt             = getColType(rdf);
      CascadeType[]  ct             =
        (rdf != null) ? rdf.cascade() : new CascadeType[] { CascadeType.all };
      FetchType      ft             = (rdf != null) ? rdf.fetch() : FetchType.lazy;

      boolean        objectProperty = false;

      if ((rdf == null) || PropType.UNDEFINED.equals(rdf.type())) {
        boolean declaredAsUri =
          URI.class.isAssignableFrom(type) || URL.class.isAssignableFrom(type);
        objectProperty = (serializer == null) || inverse || declaredAsUri;
      } else if (PropType.OBJECT.equals(rdf.type())) {
        if (!"".equals(rdf.dataType()))
          throw new OtmException("Datatype cannot be specified for an object-Property field "
                                 + this);

        objectProperty = true;
      } else if (PropType.DATA.equals(rdf.type())) {
        if (serializer == null)
          throw new OtmException("No serializer found for '" + type + "' with dataType '" + dt
                                 + "' for a data-property field " + this);

        if (inverse)
          throw new OtmException("Inverse mapping cannot be specified for a data-property field "
                                 + this);
      }

      if (serializer != null)
        ft = null;

      return new RdfDefinition(getName(), uri, dt, inverse, model, mt, !notOwned, generator, ct,
                               ft, assoc, objectProperty);
    }

    private CollectionType getColType(Predicate rdf) throws OtmException {
      return (rdf == null) ? CollectionType.PREDICATE : rdf.collectionType();
    }

    public VarDefinition getVarDefinition(SessionFactory sf, Projection proj)
                                   throws OtmException {
      String     var        = "".equals(proj.value()) ? field.getName() : proj.value();
      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' for field " + this);

      // XXX: shouldn't we parse the query to figure this out?
      String assoc = (serializer == null) ? getEntityName(type) : null;

      return new VarDefinition(getName(), var, proj.fetch(), assoc);
    }

    public IdDefinition getIdDefinition(SessionFactory sf, Id id, IdentifierGenerator generator)
                                 throws OtmException {
      if (!type.equals(String.class) && !type.equals(URI.class) && !type.equals(URL.class))
        throw new OtmException("@Id field '" + this + "' must be a String, URI or URL.");

      return new IdDefinition(getName(), generator);
    }

    public BlobDefinition getBlobDefinition(SessionFactory sf, Blob blob)
                                     throws OtmException {
      if (!isArray || !type.equals(Byte.TYPE))
        throw new OtmException("@Blob may only be applied to a 'byte[]' field: " + this);

      return new BlobDefinition(getName());
    }

    public RdfDefinition getPredicateMapDefinition(SessionFactory sf, PredicateMap pmap)
                                            throws OtmException {
      String model = null; // TODO: allow predicate maps from other models
      Type   type  = field.getGenericType();

      if (Map.class.isAssignableFrom(field.getType()) && (type instanceof ParameterizedType)) {
        ParameterizedType ptype = (ParameterizedType) type;
        Type[]            targs = ptype.getActualTypeArguments();

        if ((targs.length == 2) && (targs[0] instanceof Class)
             && String.class.isAssignableFrom((Class) targs[0])
             && (targs[1] instanceof ParameterizedType)) {
          ptype   = (ParameterizedType) targs[1];
          type    = ptype.getRawType();

          if ((type instanceof Class) && (List.class.isAssignableFrom((Class) type))) {
            targs = ptype.getActualTypeArguments();

            if ((targs.length == 1) && (targs[0] instanceof Class)
                 && String.class.isAssignableFrom((Class) targs[0]))
              return new RdfDefinition(getName(), model);
          }
        }
      }

      throw new OtmException("@PredicateMap can be applied to a Map<String, List<String>> field only."
                             + "It cannot be applied to " + this);
    }

    public EmbeddedDefinition getEmbeddedDefinition(SessionFactory sf)
                                             throws OtmException {
      Serializer serializer = sf.getSerializerFactory().getSerializer(type, null);

      if (isArray || isCollection || (serializer != null))
        throw new OtmException("@Embedded class field " + this
                               + " can't be an array, collection or a simple field");

      sf.preload(type);

      ClassMetadata cm = sf.getClassMetadata(type);

      return new EmbeddedDefinition(getName(), cm.getName());
    }

    public FieldBinder getBinder(SessionFactory sf, PropertyDefinition pd)
                          throws OtmException {
      if (pd instanceof Reference) {
        String     ref = ((Reference) pd).getReferred();
        Definition d   = sf.getDefinition(ref);

        if (!(d instanceof PropertyDefinition))
          throw new OtmException("Undefined/Invalid reference '" + ref + "' in '" + this);

        return getBinder(sf, (PropertyDefinition) d);
      }

      if (pd instanceof EmbeddedDefinition)
        return new EmbeddedClassFieldBinder(field, getter, setter);

      Serializer serializer;

      if (pd instanceof BlobDefinition)
        serializer = null;
      else if (pd instanceof RdfDefinition) {
        RdfDefinition rd = (RdfDefinition) pd;
        serializer = (rd.isAssociation()) ? null
                     : sf.getSerializerFactory().getSerializer(type, rd.getDataType());
      } else if (pd instanceof VarDefinition) {
        VarDefinition vd = (VarDefinition) pd;
        serializer = (vd.getAssociatedEntity() == null) ? null
                     : sf.getSerializerFactory().getSerializer(type, null);
      } else
        serializer = sf.getSerializerFactory().getSerializer(type, null);

      if (isArray)
        return new ArrayFieldBinder(field, getter, setter, serializer, type);

      if (isCollection)
        return new CollectionFieldBinder(field, getter, setter, serializer, type);

      return new ScalarFieldBinder(field, getter, setter, serializer);
    }
  }
}
