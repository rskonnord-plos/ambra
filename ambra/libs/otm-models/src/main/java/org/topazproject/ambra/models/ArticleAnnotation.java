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
package org.topazproject.ambra.models;

import java.net.URI;

import java.util.Date;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * ArticleAnnotation interface represents a model Class that provides the necessary information
 * to  annotate an article. It is implemented by Comment, MinorCorrection, and FormalCorrection,
 * and may be implemented by other model classes that shall represent annotation types that are
 * overlaid on an Article.  Since Comment and Correction are semantically different, and inherit
 * from a different semantic hierarchy, this interface allows the UI to process different
 * annotation types more generically.
 *
 * @author Alex Worden
 */
@Entity()
public abstract class ArticleAnnotation extends Annotation {
  @Predicate(uri = "annotea:body", cascade = { CascadeType.peer, CascadeType.delete },
             fetch = FetchType.eager) // XXX: lazy?
  private AnnotationBlob body;

  /**
   * Gets the body as a blob. 
   *
   * @return Returns the body of the article annotation
   */
  public AnnotationBlob getBody() {
    return body;
  }

  /**
   * Sets the blob for the body.
   *
   * @param body The body of the article annotation
   */
  public void setBody(AnnotationBlob body) {
    this.body = body;
  }
}
