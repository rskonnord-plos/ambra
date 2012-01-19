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

import java.net.URL;

import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;

/**
 * Represents a trackback on a resource.
 *
 * @author Stephen Cheng
 */
@Entity(types = {"topaz:TrackbackAnnotation"})
public class Trackback extends Annotation<TrackbackContent> {
  private static final long serialVersionUID = -7569629877623116742L;

  @Override
  public String getType() {
    return Rdf.topaz + "TrackbackAnnotation";
  }

  /**
   * @return Returns the url.
   */
  public URL getUrl() {
    if (getBody() != null)
      return getBody().getUrl();
    return null;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    if (getBody() != null)
      return getBody().getTitle();
    return "";
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    if (getBody() != null)
      return getBody().getExcerpt();
    return "";
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    if (getBody() != null)
      return getBody().getBlog_name();
    return "";
  }

  @Override
  public String getWebType() {
    return null;
  }
}
