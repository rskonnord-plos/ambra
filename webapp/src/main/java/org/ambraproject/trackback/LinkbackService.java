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

import org.ambraproject.service.HibernateService;
import org.ambraproject.views.LinkbackView;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public interface LinkbackService extends HibernateService {

  /**
   * Get a list of trackbacks on the given article, ordered newest to oldest
   *
   * @param articleDoi the doi of the article
   * @return an ordered list of trackbacks
   */
  List<LinkbackView> getLinkbacksForArticle(String articleDoi);

  /**
   * Count the number of trackbacks on the given article
   *
   * @param articleDoi the doi of the article to use
   * @return the number of trackbacks on the article
   */
  int countLinkbacksForArticle(String articleDoi);

  public static interface LinkValidator {
    /**
     * @param link a link appearing in an external blog page
     * @return {@code true} if the link satisfies the criteria for this type of linkback
     */
    public abstract boolean isValid(URL link);
  }

  /**
   * Check whether an external page contains a link to the article URL. The external page has sent a linkback
   * notification for an article, and is typically a blog post. Also collects other information to populate a {@link
   * BlogLinkDigest}, namely the text of the blog post's HTML title element (this may be extended).
   *
   * @param blogUrl       the URL of the blog
   * @param linkValidator how to accept a link to the article
   * @return the data picked up from the blog
   * @throws IOException on an error following the blog URL or reading the page
   */
  public abstract BlogLinkDigest examineBlogPage(URL blogUrl, LinkValidator linkValidator) throws IOException;

}
