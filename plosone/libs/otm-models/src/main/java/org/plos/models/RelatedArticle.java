/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Rdf;

/**
 * Model for related articles.
 *
 * <p>Note that the article field is modelled as a URI, <em>not</em> an {@link Article Article}.
 * The reason for this has to do with OTM filters and the fact that the article being referenced
 * may not (yet) be present. If the article is not present, then any filters on articles will
 * cause OTM to fill in a null for the article on retrieval, and a subsequent save will then
 * remove the reference.
 *
 * @author Ronald Tschalär
 */
@Entity(type = PLoS.plos + "RelatedArticle", model = "ri")
public class RelatedArticle {
  @Id @GeneratedValue(uriPrefix = "info:doi/10.1371/relatedArticle/")
  private URI id;

  @Predicate(uri = Rdf.dc_terms + "references")
  private URI article;

  @Predicate(uri = PLoS.plos + "articleRelationType")
  private String relationType;

  /**
   * Return the identifier of the object
   *
   * @return the id
   */
  public URI getId() {
    return id;
  }

  /**
   * Set the identifier of the object
   *
   * @param id the id to set
   */
  public void setId(URI id) {
    this.id = id;
  }

  /**
   * Get the article uri.
   *
   * @return the article uri.
   */
  public URI getArticle() {
    return article;
  }

  /**
   * Set the article uri.
   *
   * @param article the article uri.
   */
  public void setArticle(URI article) {
    this.article = article;
  }

  /**
   * Get the relation type.
   *
   * @return the relation type.
   */
  public String getRelationType() {
    return relationType;
  }

  /**
   * Set the relation type.
   *
   * @param relationType the relation type.
   */
  public void setRelationType(String relationType) {
    this.relationType = relationType;
  }
}
