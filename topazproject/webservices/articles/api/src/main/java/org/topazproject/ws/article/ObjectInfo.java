/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.article;

/**
 * This hold the information returned about an article related object. This may be either the
 * article itself or a secondary object; in either case certain fields may not apply and will
 * be null,
 *
 * <p>This class is a bean.
 *
 * @author Ronald Tschalär
 */
public class ObjectInfo {
  /** The uri of the object. */
  private String uri;
  /** The doi of the object, if it has one. */
  private String doi;
  /** The object's state. */
  private int state;
  /** The object's title. */
  private String title;
  /** The description associated with the object. */
  private String description;
  /** The article that supersedes this one. */
  private String supersededBy;
  /** A list of user-ids of authors of the article. */
  private String[] authorUserIds;
  /** The description associated with the object. */
  private RepresentationInfo[] representations;
  /** The the xml element in which this object is embedded (e.g. 'table-wrap', 'fig', etc). */
  private String contextElement;

  /**
   * Get the URI.
   *
   * @return the URI.
   */
  public String getUri() {
    return uri;
  }

  /**
   * Set the URI.
   *
   * @param uri the URI.
   */
  public void setUri(String uri) {
    this.uri = uri;
  }

  /**
   * Get the doi.
   *
   * @return the doi.
   */
  public String getDoi() {
    return doi;
  }

  /**
   * Set the doi.
   *
   * @param doi the doi.
   */
  public void setDoi(String doi) {
    this.doi = doi;
  }

  /**
   * Get the object's state.
   *
   * @return the state.
   */
  public int getState() {
    return state;
  }

  /**
   * Set the object's state.
   *
   * @param state the state.
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * Get the title.
   *
   * @return the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set the title.
   *
   * @param title the title.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the description.
   *
   * @return the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the description.
   *
   * @param description the description.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Get the uri of the article that supersededs this one.
   *
   * @return the uri of the supersededing article, or null.
   */
  public String getSupersededBy() {
    return supersededBy;
  }

  /**
   * Set the uri of the article that supersededs this one.
   *
   * @param supersededBy the uri of the supersededing article.
   */
  public void setSupersededBy(String supersededBy) {
    this.supersededBy = supersededBy;
  }

  /**
   * Get the list of user-ids of authors of the article. This is the subset of the article
   * authors that have user-accounts.
   *
   * @return the author user-ids.
   */
  public String[] getAuthorUserIds() {
    return authorUserIds;
  }

  /**
   * Set the list of user-ids of authors of the article.
   *
   * @param authorUserIds the author user-ids.
   */
  public void setAuthorUserIds(String[] authorUserIds) {
    this.authorUserIds = authorUserIds;
  }

  /**
   * Get the representations.
   *
   * @return the representations.
   */
  public RepresentationInfo[] getRepresentations() {
    return representations;
  }

  /**
   * Set the representations.
   *
   * @param representations the representations.
   */
  public void setRepresentations(RepresentationInfo[] representations) {
    this.representations = representations;
  }

  /**
   * Get the context element. This denotes the xml element in the article in which this object
   * appears, such as 'table-wrap', 'fig', etc.
   *
   * @return the context element, or if this is not a secondary object.
   */
  public String getContextElement() {
    return contextElement;
  }

  /**
   * Set the context element.
   *
   * @param contextElement the context element.
   */
  public void setContextElement(String contextElement) {
    this.contextElement = contextElement;
  }
}
