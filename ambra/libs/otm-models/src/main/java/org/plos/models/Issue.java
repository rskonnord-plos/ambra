/* $HeadURL::                                                                                     $
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
package org.plos.models;

import java.net.URI;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Predicate;

/**
 * Marker class to mark an Aggregation as a "Issue".
 *
 * @author Jeff Suttor
 */
@Entity(type = PLoS.plos + "Issue", model = "ri")
public class Issue extends Aggregation {

  /** Display name.  Human friendly. */
  @Predicate(uri = PLoS.plos + "displayName")
  private String displayName;

  /** Arbitrary URI to an image */
  @Predicate(uri = PLoS.plos + "image")
  private URI image;

  /**
   * Get the image for this Issue.
   *
   * @return URI for the image, may be null.
   */
  public URI getImage() {
    return image;
  }

  /**
   * Set the image for this Issue.
   *
   * @param image arbitrary URI to the image, may be null.
   */
  public void setImage(URI image) {
    this.image = image;
  }


  /**
   * Get the display name for this Issue.
   *
   * @return the display name.  will not be null.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Set the display name for this Issue.
   *
   * The display name should be human friendly.
   *
   * @param displayName the display name, may not be null.
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * String representation for debugging.
   * 
   * @return String representation for debugging.
   */
  @Override
  public String toString() {
    return "Issue: ["
            + "displayName: " + getDisplayName()
            + ", image: " + getImage()
            + ", " + super.toString()
            + "]";
  }
}
