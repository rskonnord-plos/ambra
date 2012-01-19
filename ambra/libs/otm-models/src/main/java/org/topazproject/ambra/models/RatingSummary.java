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

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * General base rating class to store a RatingSummaryContent body.
 *
 * @author Stephen Cheng
 */
@Entity(type = "topaz:RatingSummaryAnnotation")
public class RatingSummary extends Annotation {
  private static final long serialVersionUID = -8110763767878695617L;

  @Predicate(uri = "annotea:body", fetch = FetchType.eager, cascade = { CascadeType.child })
  private RatingSummaryContent body;

  /**
   * Creates a new RatingSummary object.
   */
  public RatingSummary() {
  }

  /**
   * @return Returns the rating.
   */
  public RatingSummaryContent getBody() {
    return this.body;
  }

  /**
   * @param ratingSummaryContent The rating to set.
   */
  public void setBody(RatingSummaryContent ratingSummaryContent) {
    this.body = ratingSummaryContent;
  }
}
