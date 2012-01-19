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
package org.topazproject.otm.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.PropertyBinder;
import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.BlobMapperImpl;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.EmbeddedMapperImpl;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.IdMapperImpl;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.RdfMapperImpl;

/**
 * Defines entities.
 *
 * @author Pradeep Krishnan
 */
public class EntityDefinition extends ClassDefinition {
  private final Set<String> types;
  private final Set<String> sups;
  private final String graph;

  /**
   * Creates a new EntityDefinition object.
   *
   * @param name   The name of this definition.
   * @param types   The declared rdf:types
   * @param graph  The graph or null
   * @param sups    The super class or null
   */
  public EntityDefinition(String name, Set<String> types, String graph, Set<String> sups) {
    super(name);
    this.types    = Collections.unmodifiableSet(new HashSet<String>(types));
    this.sups     = Collections.unmodifiableSet(new HashSet<String>(sups));
    this.graph   = graph;
  }

  /**
   * Get type.
   *
   * @return declared rdf:types
   */
  public Set<String> getTypes() {
    return types;
  }

  /**
   * Get graph.
   *
   * @return graph or null
   */
  public String getGraph() {
    return graph;
  }

  /**
   * Get the super entities.
   *
   * @return super entities
   */
  public Set<String> getSuperEntities() {
    return sups;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata buildClassMetadata(SessionFactory sf)
                                      throws OtmException {
    Set<String>                allTypes    = new HashSet<String>();
    Set<String>                types       = new HashSet<String>();
    Collection<RdfMapper>      fields      = new ArrayList<RdfMapper>();
    Collection<EmbeddedMapper> embeds      = new ArrayList<EmbeddedMapper>();
    Set<String>                superGraphs = new HashSet<String>();
    Set<IdMapper>              superIds    = new HashSet<IdMapper>();
    Set<BlobMapper>            superBlobs  = new HashSet<BlobMapper>();

    for (String s : sups) {
      ClassMetadata superMeta = sf.getClassMetadata(s);

      superGraphs.add(superMeta.getGraph());
      superIds.add(superMeta.getIdField());
      superBlobs.add(superMeta.getBlobField());

      types.addAll(superMeta.getTypes());
      allTypes.addAll(superMeta.getAllTypes());
      fields.addAll(superMeta.getRdfMappers());
      embeds.addAll(superMeta.getEmbeddedMappers());
    }

    superGraphs.remove(null);
    superIds.remove(null);
    superBlobs.remove(null);

    if (!this.types.isEmpty()) {
      for (String t : this.types)
        if (!allTypes.add(t))
          throw new OtmException("Duplicate rdf:type '" + t + "'in class hierarchy "
                               + getName());

      types = this.types;
    }

    String                     graph     = null;
    if (this.graph != null)
      graph = this.graph;
    else if (superGraphs.size() > 1)
      throw new OtmException(superGraphs + " are all possible candidates for graph for '"
          + getName() + "'. Need an explicit graph definition to disambiguate");
    else if (superGraphs.size() == 1)
      graph = superGraphs.iterator().next();

    IdMapper                   idField   = null;
    if (superIds.size() > 1)
      throw new OtmException("Duplicate inherited Id fields " + superIds + " in " + getName());
    else if (superIds.size() == 1)
      idField = superIds.iterator().next();

    BlobMapper                 blobField = null;
    if (superBlobs.size() > 1)
      throw new OtmException("Duplicate inherited Blob fields " + superBlobs + " in " + getName());
    else if (superBlobs.size() == 1)
      blobField = superBlobs.iterator().next();

    ClassBinding bin = sf.getClassBinding(getName());

    for (String prop : bin.getProperties()) {
      Definition def = sf.getDefinition(prop);

      if (def == null)
        throw new OtmException("No such property :'" + prop + "' bound to entity '" + getName());

      def.resolveReference(sf);

      Map<EntityMode, PropertyBinder> propertyBinders = bin.resolveBinders(prop, sf);

      if (def instanceof RdfDefinition) {
        if (def.getSupersedes() != null) {
          for (RdfMapper m : fields) {
            if (def.getSupersedes().equals(m.getDefinition().getName())) {
              fields.remove(m);

              break;
            }
          }
        }

        fields.add(new RdfMapperImpl((RdfDefinition) def, propertyBinders));
      } else if (def instanceof IdDefinition) {
        if (idField != null)
          throw new OtmException("Duplicate Id field " + def.getName() + " in " + getName());

        idField = new IdMapperImpl((IdDefinition) def, propertyBinders);
      } else if (def instanceof BlobDefinition) {
        if ((blobField != null) && (def.getSupersedes() != null)
            && def.getSupersedes().equals(blobField.getDefinition().getName()))
          blobField = null;
        if (blobField != null)
          throw new OtmException("Duplicate Blob field " + def.getName() + " in " + getName());

        blobField = new BlobMapperImpl((BlobDefinition) def, propertyBinders);
      } else if (def instanceof EmbeddedDefinition) {
        EmbeddedDefinition edef = (EmbeddedDefinition) def;
        ClassMetadata      ecm  = sf.getClassMetadata(edef.getEmbedded());

        if (ecm == null)
          throw new OtmException("Can't find definition for embedded entity " + edef.getEmbedded());

        EmbeddedMapperImpl em = new EmbeddedMapperImpl(edef, propertyBinders, ecm);
        embeds.add(em);

        for (RdfMapper p : ecm.getRdfMappers())
          fields.add((RdfMapper) em.promote(p));

        if (ecm.getIdField() != null) {
          if (idField != null)
            throw new OtmException("Duplicate Id field " + ecm.getIdField().getName() + " in "
                                   + getName());

          idField = (IdMapper) em.promote(ecm.getIdField());
        }

        if (ecm.getBlobField() != null) {
          if (blobField != null)
            throw new OtmException("Duplicate Blob field " + ecm.getBlobField().getName() + " in "
                                   + getName());

          blobField = (BlobMapper) em.promote(ecm.getBlobField());
        }

        for (String p : sf.getClassBinding(edef.getEmbedded()).getProperties()) {
          SearchableDefinition sd =
              (SearchableDefinition) sf.getDefinition(SearchableDefinition.NS + p);

          if (sd != null) {
            String baseName = getName() + ':' + em.getName() + '.' + sd.getLocalName();
            sf.addDefinition(
              new SearchableDefinition(baseName, sd.getReference(), sd.getSupersedes(),
                                       sd.getUri(), sd.getIndex(), sd.tokenize(), sd.getAnalyzer(),
                                       sd.getBoost(), sd.getPreProcessor()));
          }
        }
      } else {
        throw new OtmException("Invalid definition type '" + def.getClass() + "' for property "
                               + def.getName() + " in " + getName());
      }
    }

    return new ClassMetadata(bin.getBinders(), getName(), types, allTypes, graph, idField, fields,
                             blobField, sups, embeds);
  }
}
