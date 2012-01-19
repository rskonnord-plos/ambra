/* $HeadURL::                                                                                 $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.topazproject.otm.samples;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * This returns meta-data about an article.
 *
 * @author Eric Brown
 * @version $Id$
 */
@Entity(types = {"topaz:Article"}, graph = "ri")
public class Article extends ObjectInfo {
  private Date     articleDate;
  private String[] subjects;
  private String[] categories;
  private List<PublicAnnotation> publicAnnotations = new ArrayList<PublicAnnotation>();
  private List<PrivateAnnotation> privateAnnotations = new ArrayList<PrivateAnnotation>();
  private List<ReplyThread> replies = new ArrayList<ReplyThread>();
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
  @Predicate(uri = Rdf.dc_terms + "available")
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
  @Predicate(uri = Rdf.dc + "subject")
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
  @Predicate(uri = Rdf.topaz + "hasCategory")
  public void setCategories(String[] categories) {
    this.categories = categories;
  }

  public Set<ObjectInfo> getParts() {
    return parts;
  }

  @Predicate(uri = Rdf.dc_terms + "hasPart", cascade = {CascadeType.child})
  public void setParts(Set<ObjectInfo> parts) {
    this.parts = parts;
  }

  @Predicate(uri = Annotation.NS + "annotates", inverse=Predicate.BT.TRUE, notOwned=Predicate.BT.TRUE,
      cascade = {CascadeType.child})
  public void setPrivateAnnotations(List<PrivateAnnotation> privateAnnotations) {
    this.privateAnnotations = privateAnnotations;
  }

  public List<PrivateAnnotation> getPrivateAnnotations() {
    return privateAnnotations;
  }

  @Predicate(uri = Annotation.NS + "annotates", inverse=Predicate.BT.TRUE, notOwned=Predicate.BT.TRUE,
      fetch=FetchType.eager, cascade = {CascadeType.child})
  public void setPublicAnnotations(List<PublicAnnotation> publicAnnotations) {
    this.publicAnnotations = publicAnnotations;
  }

  public List<PublicAnnotation> getPublicAnnotations() {
    return publicAnnotations;
  }

  public List<ReplyThread> getReplies() {
    return replies;
  }

  @Predicate(uri = Reply.NS + "inReplyTo", inverse=Predicate.BT.TRUE, notOwned=Predicate.BT.TRUE)
  public void setReplies(List<ReplyThread> replies) {
    this.replies = replies;
  }

  public void addReply(ReplyThread r) {
    r.setRoot(getUri());
    r.setInReplyTo(getUri());
    replies.add(r);
  }
}
