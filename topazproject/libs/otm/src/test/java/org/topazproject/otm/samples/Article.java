/* $HeadURL::                                                                                 $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.samples;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Rdf;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;

/**
 * This returns meta-data about an article.
 *
 * @author Eric Brown
 * @version $Id$
 */
@Entity(type = Rdf.topaz + "Article", model = "ri")
public class Article extends ObjectInfo {
  @Predicate(uri = Rdf.dc_terms + "available")
  private Date     articleDate;
  @Predicate(uri = Rdf.dc + "subject")
  private String[] subjects;
  @Predicate(uri = Rdf.topaz + "hasCategory")
  private String[] categories;
  @Predicate(uri = Annotation.NS + "annotates", inverse=true, notOwned=true)
  private List<PublicAnnotation> publicAnnotations = new ArrayList<PublicAnnotation>();
  @Predicate(uri = Annotation.NS + "annotates", inverse=true, notOwned=true)
  private List<PrivateAnnotation> privateAnnotations = new ArrayList<PrivateAnnotation>();
  @Predicate(uri = Reply.NS + "inReplyTo", inverse=true, notOwned=true)
  private List<ReplyThread> replies = new ArrayList<ReplyThread>();
  @Predicate(uri = Rdf.dc_terms + "hasPart")
  private Set<ObjectInfo> parts = new HashSet<ObjectInfo>();

  /**
   * Get the article date.
   *
   * @return the article date.
   */
  public Date getArticleDate() {
    return articleDate;
  }

  /**
   * Set the article date.
   *
   * @param articleDate the article date.
   */
  public void setArticleDate(Date articleDate) {
    this.articleDate = articleDate;
  }

  /**
   * Get the article's subjects. These are strings that represent the combination category
   * and subcategory. This is likely to be deprecated in the future.
   *
   * @return the article's subjects.
   */
  public String[] getSubjects() {
    return subjects;
  }

  /**
   * Set the article's subjects.
   *
   * @param subjects the article's subjects.
   */
  public void setSubjects(String[] subjects) {
    this.subjects = subjects;
  }

  /**
   * Get the article's categories. These are the primary categories for the article.
   * It does not include the sub-categories.
   *
   * @return the article's categories.
   */
  public String[] getCategories() {
    return categories;
  }

  /**
   * Set the article's categories.
   *
   * @param categories the article's categories.
   */
  public void setCategories(String[] categories) {
    this.categories = categories;
  }

  public Set<ObjectInfo> getParts() {
    return parts;
  }

  public void setParts(Set<ObjectInfo> parts) {
    this.parts = parts;
  }

  public void setPrivateAnnotations(List<PrivateAnnotation> privateAnnotations) {
    this.privateAnnotations = privateAnnotations;
  }

  public List<PrivateAnnotation> getPrivateAnnotations() {
    return privateAnnotations;
  }

  public void setPublicAnnotations(List<PublicAnnotation> publicAnnotations) {
    this.publicAnnotations = publicAnnotations;
  }

  public List<PublicAnnotation> getPublicAnnotations() {
    return publicAnnotations;
  }

  public List<ReplyThread> getReplies() {
    return replies;
  }

  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  public void addReply(ReplyThread r) {
    r.setRoot(getUri());
    r.setInReplyTo(getUri());
    replies.add(r);
  }
}
