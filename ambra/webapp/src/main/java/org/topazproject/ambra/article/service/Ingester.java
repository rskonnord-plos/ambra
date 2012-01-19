/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.topazproject.ambra.article.service;

import com.thoughtworks.xstream.mapper.ImplicitCollectionMapper;
import com.thoughtworks.xstream.mapper.Mapper;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.permission.service.PermissionsService;
import org.topazproject.otm.Session;

import java.lang.reflect.*;
import java.util.*;

/**
 * The article ingestor.
 *
 * @author Ronald Tschalär
 * @author Eric Brown
 */
public interface Ingester {

  /**
   * Prepare the ingester for ingesting. This can be done outside a transaction
   * scope. It creates the object descriptions ready for ingest.
   *
   * @param configuration Ambra configuration
   * @throws IngestException on an error in reading the zip file
   */
  public void prepare(Configuration configuration) throws IngestException;

  /**
   * Ingest a new article.
   *
   * @param configuration Ambra configuration
   * @param sess     the OTM session to use to add the objects
   * @param permSvc  the permissions-service to use to add the permissions
   * @param force if true then don't check whether this article already exists but just
   *              save this new article.
   * @return the new article
   * @throws DuplicateArticleIdException if an article exists with the same URI as the new article
   *                                     and <var>force</var> is false
   * @throws IngestException if there's any other problem ingesting the article
   */
  public Article ingest(Configuration configuration, Session sess, PermissionsService permSvc,
                        boolean force)
      throws DuplicateArticleIdException, IngestException;

  /**
   * Custom implicit-collection-mapper for XStream. The primary purpose of this is to reduce the
   * amount of direct dependencies on package names and on the exact definition of the classes.
   * For collections we have the following options:
   * <ol>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       &lt;org.topazproject.ambra.models.UserProfile&gt;
   *         ...
   *       &lt;/org.topazproject.ambra.models.UserProfile&gt;
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       &lt;UserProfile&gt;
   *         ...
   *       &lt;/UserProfile&gt;
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   *   <li>
   *     <pre>
   *     &lt;authors&gt;
   *       ...
   *     &lt;/authors&gt;
   *     &lt;authors&gt;
   *       ...
   *     &lt;/authors&gt;
   *     </pre>
   *   </li>
   * </ol>
   * The first one requires the description generator to know about both component types of
   * collections and their fully-qualified package names; the second requires us to provide
   * explicit aliases for almost all model classes; the last one requires us to provide
   * implicit-collection-mappings for almost all model classes.
   *
   * <p>This class automates the third option by dynamically looking up the component type
   * of collections (thanks to generics). Unfortunately there's also a bug in XStream 1.3
   * with regards to subclasses, so we need to do most of the mappings management and lookup
   * ourselves.
   */
  public static class CollectionMapper extends ImplicitCollectionMapper {
    private final Logger log = LoggerFactory.getLogger(CollectionMapper.class);
    private final Map<Class<?>, Set<ImplicitCollectionMapping>> mappings =
                                      new HashMap<Class<?>, Set<ImplicitCollectionMapping>>();

    public CollectionMapper(Mapper wrapped) {
      super(wrapped);
    }

    @Override
    public String getFieldNameForItemTypeAndName(Class definedIn, Class itemType,
                                                 String itemFieldName) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (icm.getItemType().isAssignableFrom(itemType) &&
              itemFieldName.equals(icm.getFieldName()))
            return icm.getFieldName();
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    @Override
    public Class getItemTypeForItemFieldName(Class definedIn, String itemFieldName) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (itemFieldName.equals(icm.getFieldName()))
            return icm.getItemType();
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    @Override
    public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType,
                                                                          String fieldName) {
      ImplicitCollectionMapping icm = findMapping(itemType, fieldName);
      if (icm != null)
        return icm;

      // see if this is an array or collection, and if so get the component type
      Class<?> declType = null;
      Class<?> compType = null;
      try {
        // try this as a public field
        Field f = itemType.getField(fieldName);
        declType = f.getDeclaringClass();
        compType = getCompType(f.getType(), f.getGenericType());
      } catch (NoSuchFieldException nsfe) {
        if (log.isTraceEnabled())
          log.trace("class '" + itemType.getName() + "', field '" + fieldName + "'", nsfe);

        // not a public field, so look for getter
        String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
          Method m = itemType.getMethod(getter);
          declType = m.getDeclaringClass();
          compType = getCompType(m.getReturnType(), m.getGenericReturnType());
        } catch (NoSuchMethodException nsme) {
          if (log.isTraceEnabled())
            log.trace("class '" + itemType.getName() + "', method '" + getter + "'", nsme);
        }
      }

      if (compType != null) {
        super.add(declType, fieldName, compType);
        icm = super.getImplicitCollectionDefForFieldName(declType, fieldName);
        getOrCreateICM(declType).add(icm);

        if (log.isTraceEnabled())
          log.trace("created def: defIn=" + declType + ", fieldName=" +
                             icm.getFieldName() + ", fieldType=" + icm.getItemType() +
                             ", elemName=" + icm.getItemFieldName());
      }

      return icm;
    }

    private Set<ImplicitCollectionMapping> getOrCreateICM(Class<?> definedIn) {
      Set<ImplicitCollectionMapping> icmList = mappings.get(definedIn);
      if (icmList == null)
        mappings.put(definedIn, icmList = new HashSet<ImplicitCollectionMapping>());
      return icmList;
    }

    private ImplicitCollectionMapping findMapping(Class<?> definedIn, String field) {
      while (definedIn != null) {
        for (ImplicitCollectionMapping icm : getOrCreateICM(definedIn)) {
          if (icm.getFieldName().equals(field))
            return icm;
        }
        definedIn = definedIn.getSuperclass();
      }
      return null;
    }

    private static Class<?> getCompType(Class<?> clazz, Type type) {
      if (clazz.isArray() &&  clazz != byte[].class)
        return clazz.getComponentType();

      if (Collection.class.isAssignableFrom(clazz))
        return getCompType(type);

      return null;
    }

    private static Class<?> getCompType(Type collType) {
      return getClass(((ParameterizedType) collType).getActualTypeArguments()[0]);
    }

    private static Class<?> getClass(Type t) {
      if (t instanceof Class)
        return (Class<?>) t;

      if (t instanceof GenericArrayType)
        return Array.newInstance(getClass(((GenericArrayType) t).getGenericComponentType()), 0).
                     getClass();

      if (t instanceof ParameterizedType)
        return getClass(((ParameterizedType) t).getRawType());

      if (t instanceof WildcardType)
        return getClass(((WildcardType) t).getUpperBounds()[0]);

      return Object.class;
    }
  }

  public Zip getZip();
}