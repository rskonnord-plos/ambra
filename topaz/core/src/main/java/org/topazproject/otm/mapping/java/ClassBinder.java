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
package org.topazproject.otm.mapping.java;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Connection;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.EntityBinder.LazyLoaded;
import org.topazproject.otm.mapping.EntityBinder.LazyLoader;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.VarMapper;
import org.topazproject.otm.query.Results;

/**
 * Binds an entity to an {@link org.topazproject.otm.EntityMode#POJO} specific implementation.
 *
 * @author Pradeep Krishnan
 *
 * @param <T> The object type instantiated by this class
 */
public class ClassBinder<T> implements EntityBinder {
  private final Class<T>           clazz;
  private final boolean            instantiable;
  private final boolean            suppressAlias;
  private ClassMetadata            cm;
  private Map<Method, FieldBinder> llInterceptors = new HashMap<Method, FieldBinder>();
  private Class<?extends T>        proxy;
  private static final Log         log            = LogFactory.getLog(ClassBinder.class);

  /**
   * Creates a new ClassBinder object.
   *
   * @param clazz the java class to bind this entity to
   */
  public ClassBinder(Class<T> clazz) {
    this(clazz, false);
  }

  /**
   * Creates a new ClassBinder object.
   *
   * @param clazz the java class to bind this entity to
   * @param suppressAlias to dis-allow registration of the class name as an alias. 
   *                      This allows binding the same class to multiple entities.
   */
  public ClassBinder(Class<T> clazz, boolean suppressAlias) {
    this.clazz = clazz;
    this.suppressAlias = suppressAlias;

    int mod = clazz.getModifiers();
    instantiable = !Modifier.isAbstract(mod) && !Modifier.isInterface(mod)
                    && Modifier.isPublic(mod);
  }

  /*
   * inherited javadoc
   */
  public void bindComplete(ClassMetadata cm) throws OtmException {
    if (this.cm != null)
      throw new OtmException(clazz + " already bound to " + this.cm + ". Cannot bind to " + cm);

    this.cm = cm;

    if (!instantiable)
      proxy = null;
    else {
      Method idGetter = getIdGetter();
      proxy = createProxy(clazz, (idGetter != null) ? new Method[] { idGetter } : new Method[] {  });
    }

    if (cm.getRdfMappers() != null) {
      for (RdfMapper m : cm.getRdfMappers()) {
        if (m.isAssociation() || (m.getColType() != CollectionType.PREDICATE)) {
          FieldBinder fb = (FieldBinder) m.getBinder(EntityMode.POJO);
          addInterceptor(fb.getGetter(), fb);
          addInterceptor(fb.getSetter(), fb);
        }
      }
    }
  }

  private void addInterceptor(Method m, FieldBinder fb) {
    if ((m != null) && m.getDeclaringClass().isAssignableFrom(clazz))
      llInterceptors.put(m, fb);
  }

  private Method getIdGetter() {
    Mapper idField = cm.getIdField();

    if (idField == null)
      return null;

    Method getter = ((FieldBinder) idField.getBinders().get(EntityMode.POJO)).getGetter();

    return (getter != null) && getter.getDeclaringClass().isAssignableFrom(clazz) ? getter : null;
  }

  /*
   * inherited javadoc
   */
  public T newInstance() throws OtmException {
    try {
      return clazz.newInstance();
    } catch (Exception t) {
      throw new OtmException("Failed to create a new instance of " + clazz, t);
    }
  }

  /*
   * inherited javadoc
   */
  public LazyLoaded newLazyLoadedInstance(LazyLoader ll)
                                   throws OtmException {
    try {
      Object o = proxy.newInstance();

      if (cm.getEmbeddedMappers() != null) {
        for (EmbeddedMapper em : cm.getEmbeddedMappers()) {
          em.getBinder(EntityMode.POJO)
             .setRawValue(o,
                           em.getEmbeddedClass().getEntityBinder(EntityMode.POJO)
                              .newLazyLoadedInstance(ll));
        }
      }

      if (cm.getIdField() != null)
        cm.getIdField().getBinder(EntityMode.POJO).set(o, Collections.singletonList(ll.getId()));

      ((ProxyObject) o).setHandler(new Handler(ll));

      return (LazyLoaded) o;
    } catch (Exception t) {
      throw new OtmException("Failed to create a new lazy-loaded instance of " + clazz, t);
    }
  }

  private Object newLazyLoadedInstance(Session session, String id)
                                throws OtmException {
    LazyLoader ll = createLazyLoader(session, id);

    return newLazyLoadedInstance(ll);
  }

  private LazyLoader createLazyLoader(final Session session, final String id) {
    return new LazyLoader() {
        private Map<PropertyBinder, PropertyBinder.RawFieldData> rawData =
           new HashMap<PropertyBinder, PropertyBinder.RawFieldData>();

        public void ensureDataLoad(LazyLoaded self, String operation)
                            throws OtmException {
          // nothing to do
        }

        public boolean isLoaded() {
          return true;
        }

        public boolean isLoaded(PropertyBinder b) {
          return !rawData.containsKey(b);
        }

        public void setRawFieldData(PropertyBinder b, PropertyBinder.RawFieldData d) {
          if (d == null)
            rawData.remove(b);
          else
            rawData.put(b, d);
        }

        public PropertyBinder.RawFieldData getRawFieldData(PropertyBinder b) {
          return rawData.get(b);
        }

        public Session getSession() {
          return session;
        }

        public String getId() {
          return id;
        }

        public ClassMetadata getClassMetadata() {
          return cm;
        }
      };
  }

  /*
   * inherited javadoc
   */
  public Object loadInstance(Object instance, String id, TripleStore.Result result, Session session)
                      throws OtmException {
    if (log.isDebugEnabled())
      log.debug("Instantiating object with '" + id + "' for " + cm + ", fwd-triples = " + result.getFValues() + ", rev-triples = " + result.getRValues());

    if (instance == null)
      instance = newLazyLoadedInstance(session, id);

    if (cm.getIdField() != null)
      cm.getIdField().getBinder(EntityMode.POJO).set(instance, Collections.singletonList(id));

    final Map<String, List<String>> fvalues = result.getFValues();
    final Map<String, List<String>> rvalues = result.getRValues();
    final SessionFactory            sf      = session.getSessionFactory();

    LazyLoader                      lh      = null;
    Map<String, List<String>>       pmap    = null;

    if (instance instanceof LazyLoaded) {
      LazyLoaded ll = (LazyLoaded) instance;
      lh = ll.getLazyLoader(ll);
    }

    // look for predicate-maps
    if (cm.getMapperByUri(sf, null, false, null) != null)
      pmap = new HashMap<String, List<String>>(fvalues);

    for (RdfMapper m : cm.getRdfMappers()) {
      List<String> v = m.hasInverseUri() ? rvalues.get(m.getUri()) : fvalues.get(m.getUri());

      if (v == null)
        v = Collections.emptyList();

      if (!v.isEmpty()) {
        if ((pmap != null) && !m.hasInverseUri())
          pmap.remove(m.getUri());

        if (!m.hasInverseUri() && (m.getColType() != CollectionType.PREDICATE))
          v = loadCollection(id, m, session);
      }

      PropertyBinder b = m.getBinder(EntityMode.POJO);

      if ((lh == null) || !m.isAssociation() || v.isEmpty())
        b.load(instance, v, m, session);
      else {
        lh.setRawFieldData(b = getLocal(b), newRawFieldData((LazyLoaded) instance, v, result));

        if (log.isDebugEnabled())
          log.debug("Stashed away raw-data for " + b + " on '" + id);
      }
    }

    if (pmap != null) {
      for (RdfMapper m : cm.getRdfMappers()) {
        if (m.isPredicateMap())
          m.getBinder(EntityMode.POJO).setRawValue(instance, pmap);
      }
    }

    return instance;
  }

  private PropertyBinder getLocal(PropertyBinder b) {
    if (b instanceof EmbeddedClassMemberFieldBinder)
      return getLocal(((EmbeddedClassMemberFieldBinder) b).getFieldBinder());

    return b;
  }

  private RdfMapper getRootMapper(ClassMetadata cm, PropertyBinder b)
                           throws OtmException {
    // quick check for the most common case
    Mapper m = cm.getMapperByName(b.getName());

    if (m instanceof RdfMapper)
      return (RdfMapper) m;

    // now scan for match with localized in embedded
    for (RdfMapper rm : cm.getRdfMappers())
      if (b.equals(getLocal(rm.getBinder(EntityMode.POJO))))
        return rm;

    throw new OtmException("Cannot find binder " + b + " in " + cm);
  }

  private PropertyBinder.RawFieldData newRawFieldData(final LazyLoaded instance, final List<String> values,
                                              final TripleStore.Result r)
                                       throws OtmException {
    return new PropertyBinder.RawFieldData() {
        public List<String> getValues() {
          return values;
        }

        public LazyLoaded getRootInstance() {
          return instance;
        }
      };
  }

  private List<String> loadCollection(String id, RdfMapper m, Session session)
                               throws OtmException {
    SessionFactory sf    = session.getSessionFactory();
    String         p     = m.getUri();
    String         graph = m.getGraph();

    if (graph == null)
      graph = cm.getGraph();

    String       mUri  = getGraphUri(graph, sf);
    List<String> vals;

    TripleStore  store = sf.getTripleStore();
    Connection   con   = session.getTripleStoreCon();

    if (log.isDebugEnabled())
      log.debug("Loading rdf:list/bag for " + m + " on '" + id);

    // load from the triple-store
    if (m.getColType() == CollectionType.RDFLIST)
      vals = store.getRdfList(id, p, mUri, con);
    else
      vals = store.getRdfBag(id, p, mUri, con);

    return vals;
  }

  private String getGraphUri(String graphId, SessionFactory sf)
                      throws OtmException {
    GraphConfig mc = sf.getGraph(graphId);

    if (mc == null) // Happens if using a Class but the graph was not added
      throw new OtmException("Unable to find graph '" + graphId + "'");

    return mc.getUri().toString();
  }

  /*
   * inherited javadoc
   */
  public Object loadInstance(Object obj, String id, Results r, Session sess)
                      throws OtmException {
    if (obj == null)
      obj = newInstance();

    if ((id != null) && (cm.getIdField() != null))
      cm.getIdField().getBinder(EntityMode.POJO).set(obj, Collections.singletonList(id));

    // a proj-var may appear multiple times, so have to delay closing of subquery results
    Set<Results> sqr = new HashSet<Results>();

    for (VarMapper m : cm.getVarMappers()) {
      int    idx = r.findVariable(m.getProjectionVar());
      Object val =
        getValue(r, idx, ((FieldBinder) m.getBinder(EntityMode.POJO)).getComponentType(),
                 m.getFetchType() == FetchType.eager, sqr, sess);
      PropertyBinder b   = m.getBinder(EntityMode.POJO);

      if (val instanceof List)
        b.set(obj, (List) val);
      else
        b.setRawValue(obj, val);
    }

    for (Results sr : sqr)
      sr.close();

    return obj;
  }

  private Object getValue(Results r, int idx, Class<?> type, boolean eager, Set<Results> sqr,
                          Session sess) throws OtmException {
    switch (r.getType(idx)) {
    case CLASS:
      return (type == String.class) ? r.getString(idx) : r.get(idx, eager);

    case LITERAL:
      return (type == String.class) ? r.getString(idx) : r.getLiteralAs(idx, type);

    case URI:
      return (type == String.class) ? r.getString(idx) : r.getURIAs(idx, type);

    case SUBQ_RESULTS:

      ClassMetadata scm       = sess.getSessionFactory().getClassMetadata(type);
      boolean       isSubView = (scm != null) && !scm.isView() && !scm.isPersistable();

      List<Object>  vals      = new ArrayList<Object>();

      Results       sr        = r.getSubQueryResults(idx);
      sr.setAutoClose(false);

      sr.beforeFirst();

      while (sr.next())
        vals.add(isSubView
                 ? scm.getEntityBinder(EntityMode.POJO).loadInstance(null, null, sr, sess)
                 : getValue(sr, 0, type, eager, null, sess));

      if (sqr != null)
        sqr.add(sr);
      else
        sr.close();

      return vals;

    default:
      throw new Error("unknown type " + r.getType(idx) + " encountered");
    }
  }

  /*
   * inherited javadoc
   */
  public boolean isInstantiable() {
    return instantiable;
  }

  /*
   * inherited javadoc
   */
  public boolean isInstance(Object o) {
    return clazz.isInstance(o);
  }

  /*
   * inherited javadoc
   */
  public boolean isAssignableFrom(EntityBinder other) {
    return (other instanceof ClassBinder) && clazz.isAssignableFrom(((ClassBinder) other).clazz);
  }

  /**
   * Gets the java class that this binder binds to.
   *
   * @return the java class bound by this
   */
  public Class<T> getSourceClass() {
    return clazz;
  }

  private static <T> Class<?extends T> createProxy(Class<T> clazz, final Method[] ignoreList) {
    MethodFilter mf =
      new MethodFilter() {
        public boolean isHandled(Method m) {
          if (m.getName().equals("finalize"))
            return false;

          for (Method ignore : ignoreList)
            if (m.equals(ignore))
              return false;

          return true;
        }
      };

    ProxyFactory f  = new ProxyFactory();
    f.setSuperclass(clazz);

    if (Serializable.class.isAssignableFrom(clazz))
      f.setInterfaces(new Class[] { WriteReplace.class, LazyLoaded.class });
    else
      f.setInterfaces(new Class[] { LazyLoaded.class });

    f.setFilter(mf);

    Class<?extends T> c = f.createClass();

    return c;
  }

  /*
   * inherited javadoc
   */
  public String[] getNames() {
    return suppressAlias ? new String[] {} : new String[] { clazz.getName() };
  }

  public static interface WriteReplace {
    public Object writeReplace() throws ObjectStreamException;
  }

  private class Handler implements MethodHandler {
    private final LazyLoader ll;

    public Handler(LazyLoader ll) {
      this.ll = ll;
    }

    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                  throws Throwable {
      if ("getLazyLoader".equals(thisMethod.getName()) && (args.length == 1) && (args[0] == self))
        return ll;

      ll.ensureDataLoad((LazyLoaded) self, thisMethod.getName());

      if ("writeReplace".equals(thisMethod.getName()) && (args.length == 0)
           && (self instanceof Serializable))
        return getReplacement(self);

      PropertyBinder b = llInterceptors.get(thisMethod);

      if (b != null) {
        PropertyBinder.RawFieldData d = ll.getRawFieldData(b);

        if (d != null) {
          if (log.isDebugEnabled())
            log.debug("Retrieved stashed raw-data for " + b + " on '" + ll.getId());

          ll.setRawFieldData(b, null);

          RdfMapper m = getRootMapper(ll.getClassMetadata(), b);
          b = m.getBinder(EntityMode.POJO);

          List<String> v = d.getValues();

          b.load(d.getRootInstance(), v, m, ll.getSession());
          ll.getSession().delayedLoadComplete(d.getRootInstance(), m);
        }
      }

      try {
        return proceed.invoke(self, args);
      } catch (InvocationTargetException ite) {
        log.warn("Caught ite while invoking '" + proceed + "' on '" + self + "'", ite);
        throw ite.getCause();
      }
    }

    private Object getReplacement(Object o) throws Throwable {
      EntityMode em  = ll.getSession().getEntityMode();
      Object     rep = cm.getEntityBinder(em).newInstance();

      for (Mapper m : new Mapper[] { cm.getIdField(), cm.getBlobField() })
        if (m != null) {
          PropertyBinder b = m.getBinder(em);
          b.setRawValue(rep, b.getRawValue(o, false));
        }

      for (Mapper m : cm.getRdfMappers()) {
        PropertyBinder b = m.getBinder(em);
        b.setRawValue(rep, b.getRawValue(o, false));
      }

      if (log.isDebugEnabled())
        log.debug("Serializable replacement created for " + o);

      return rep;
    }
  }
}
