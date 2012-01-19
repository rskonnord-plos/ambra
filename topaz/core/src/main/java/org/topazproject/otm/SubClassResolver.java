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
package org.topazproject.otm;

import java.util.Collection;

/**
 * An application specific resolver that can resolve to the most specific sub-class based on
 * rdf statements about it.
 *
 * @see SessionFactory#getSubClassMetadata
 *
 * @author Pradeep Krishnan
 */
public interface SubClassResolver {
  /**
   * Resolve the sub-class to use based on a set of RDF statements loaded. Note that
   * this resolver will be called only if the SessionFactory detects multiple candidates
   * all matching a set of rdf:type values.
   *
   * @param superEntity      the starting point (or could be null). If not null, the set of
   *                         {@link ClassMetadata#getTypes rdf:types} defined on it is a subset
   *                         of the rdf:type values supplied in the <code>typeUris</code>.
   * @param instantiatableIn if not null, the resolved ClassMetadata must be {@link
   *                         org.topazproject.otm.mapping.EntityBinder#isInstantiable instantiatable}
   *                         in this EntityMode.
   * @param sf               the SessionFactory to do any additional lookups
   * @param typeUris         collection of rdf:type URIs
   * @param statements       the set of RDF statements
   * @return                 the resolved ClassMetadata or null. The resolved ClassMetadata
   *                         must be a sub-class of the <code>superEntity</code> and must
   *                         satisfy the <code>instantialeIn</code> requirement and its {@link
   *                         ClassMetadata#getTypes rdf:types} must be a subset of the
   *                         supplied <code>typeUris</code>. If these conditions are not met, the
   *                         resolved ClassMetadata may be discarded.
   */
  public ClassMetadata resolve(ClassMetadata superEntity, EntityMode instantiatableIn,
                    SessionFactory sf, Collection<String> typeUris, TripleStore.Result statements);
}
