/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.trackback;

import java.net.URL;

/**
 * Container for information found while scanning an external Web page, typically a blog post.
 */
public class BlogLinkDigest {

  private final URL link;
  private final String title;

  public BlogLinkDigest(URL link, String title) {
    this.link = link;
    this.title = title;
  }

  public URL getLink() {
    return link;
  }

  public String getTitle() {
    return title;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BlogLinkDigest that = (BlogLinkDigest) o;

    if (link != null ? !link.equals(that.link) : that.link != null) return false;
    if (title != null ? !title.equals(that.title) : that.title != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = link != null ? link.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    return result;
  }

}
