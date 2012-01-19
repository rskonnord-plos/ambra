/* $HeadURL::                                                                            $
 * $Rdf: ClassMetadata.java 4960 2008-03-12 17:13:54Z pradeep $
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

import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.search.PreProcessor;

/**
 * The definition for a searchable property.
 *
 * @author Ronald Tschalär
 */
public class SearchableDefinition extends PropertyDefinition {
  /** The prefix to use for the names of all SearchableDefinition's */
  public static final String NS = "Searchable:";

  private String       baseName;
  private String       uri;
  private String       index;
  private Boolean      tokenize;
  private String       analyzer;
  private Integer      boost;
  private PreProcessor preProcessor;

  /**
   * Creates a new SearchableDefinition object.
   *
   * @param baseName     The base name of this definition (of the form "Entity:property"); it will
   *                     be prefixed with {@link #NS} to create this definition's name.
   * @param reference    The definition to refer to resolve undefined attribiutes or null.
   * @param supersedes   The definition that this supersedes or null.
   * @param uri          The rdf predicate. Null to resolve from reference or if the search indexes
   *                     not stored in a graph.
   * @param index        The search index to use; in the case where the index is stored in a graph
   *                     this is the name of the graph. Null to resolve from reference.
   * @param tokenize     Whether to tokenize the value or not when indexing and searching. Null to
   *                     resolve from reference.
   * @param analyzer     The analyzer to use for tokenization; ignored if <var>tokenize</var> is
   *                     false. Null to resolve from reference.
   * @param boost        The boost to use for the value. Null to resolve from reference.
   * @param preProcessor The pre-processor to apply to values before they are indexed. Null to
   *                     resolve from reference.
   */
  public SearchableDefinition(String baseName, String reference, String supersedes, String uri,
                              String index, Boolean tokenize, String analyzer, Integer boost,
                              PreProcessor preProcessor) {
    super(NS + baseName, reference, supersedes);

    this.baseName     = baseName;
    this.uri          = uri;
    this.index        = index;
    this.tokenize     = tokenize;
    this.analyzer     = analyzer;
    this.boost        = boost;
    this.preProcessor = preProcessor;
  }

  @Override
  protected void resolve(SessionFactory sf, Definition def) throws OtmException {
    if (def != null) {
      if (!(def instanceof SearchableDefinition))
        throw new OtmException("Reference '" + def.getName() + "' in '" + getName()
                               + "' is not a Searchable definition");

      SearchableDefinition ref = (SearchableDefinition) def;

      assert ref.getName().equals(getReference());      // this should be right. But just in case

      uri                = (uri == null) ? ref.uri : uri;
      index              = (index == null) ? ref.index : index;
      tokenize           = (tokenize == null) ? ref.tokenize : tokenize;
      analyzer           = (analyzer == null) ? ref.analyzer : analyzer;
      boost              = (boost == null) ? ref.boost : boost;
      preProcessor       = (preProcessor == null) ? ref.preProcessor : preProcessor;
    } else if ((def = sf.getDefinition(baseName)) != null) {
      if (def instanceof RdfDefinition) {
        RdfDefinition ref = (RdfDefinition) def;

        uri   = (uri == null) ? ref.getUri() : uri;
        index = (index == null) ? ref.getGraph() : index;
      } else if (def instanceof BlobDefinition) {
        ;       // nothing to inherit
      } else {
        throw new OtmException("Unexpected definition type found for '" + def.getName() + "' in '" +
                               getName() + "'");
      }
    }

    if (index == null)
      throw new OtmException("No index defined for '" + getName() + "'");
  }

  public String getBaseName() {
    return baseName;
  }

  public String getUri() {
    return uri;
  }

  public String getIndex() {
    return index;
  }

  public boolean tokenize() {
    return tokenize;
  }

  public String getAnalyzer() {
    return analyzer;
  }

  public Integer getBoost() {
    return boost;
  }

  public PreProcessor getPreProcessor() {
    return preProcessor;
  }

  public String toString() {
    return getClass().getName() + "[name=" + getName() + ", pred=" + uri + ", index=" + index +
           ", tokenize=" + tokenize + ", analyzer=" + analyzer + ", boost=" + boost +
           ", preProcessor=" + ((preProcessor != null) ? preProcessor.getClass() : "-null-") + "]";
  }

  /**
   * Attempt to find the SearchableDefinition for the given property. This walks the entity
   * hierarchy if necessary.
   *
   * @param sf   the session-factory to use
   * @param prop the name of the property for which to find the SearchableDefinition; must be
   *             a composite name (i.e. entity:field)
   * @return the SearchableDefinition or null if none was found
   */
  public static SearchableDefinition findForProp(SessionFactory sf, String prop) {
    SearchableDefinition sd = (SearchableDefinition) sf.getDefinition(NS + prop);
    if (sd != null)
      return sd;

    int colon = prop.indexOf(':');
    String entity = prop.substring(0, colon);
    String field  = prop.substring(colon + 1);

    for (String sup : sf.getClassMetadata(entity).getSuperEntities()) {
      sd = (SearchableDefinition) sf.getDefinition(NS + sup + ':' + field);
      if (sd != null)
        return sd;
    }

    return null;
  }
}
