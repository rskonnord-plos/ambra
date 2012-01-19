/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2009 by Topaz, Inc.
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
package org.topazproject.ambra.models;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Searchable;

/**
 * Represents the body of an Annotation object.
 *
 * @author Pradeep Krishnan
 */
@Entity()
public class AnnotationBlob extends Blob implements CompetingInterest {
  private static final long serialVersionUID = 6834878451274759551L;
  private String id;
  private String ciStatement;

  /**
   * Creates a new AnnotationBlob object.
   */
  public AnnotationBlob() {
  }

  /**
   * Creates a new AnnotationBlob object.
   *
   * @param contentType the content-type
   */
  public AnnotationBlob(String contentType) {
    super(contentType);
  }

  /**
   * Get id.
   *
   * @return id as String.
   */
  public String getId() {
    return id;
  }

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  @Id
  @GeneratedValue(uriPrefix = "annoteaBodyId:",
      generatorClass = "org.topazproject.ambra.models.support.BlobIdGenerator")
  public void setId(String id) {
    this.id = id;
  }

  @Override
  @Searchable(index = "lucene", uri = "topaz:body")
  @org.topazproject.otm.annotations.Blob  // FIXME: remove
  public void setBody(org.topazproject.otm.Blob body) {
    super.setBody(body);
  }

  /**
   * Get the competing Interest statement
   * @return the competing interest statement
   */
  public String getCIStatement() {
    return ciStatement;
  }

  /**
   * Set the competing interest statement
   * @param ciStatement The statement to save
   */
  public void setCIStatement(String ciStatement) {
    this.ciStatement = ciStatement;
  }
}
