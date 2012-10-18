/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

package org.ambraproject.service.article;

import org.ambraproject.service.hibernate.HibernateService;
import org.ambraproject.views.AuthorExtra;
import org.ambraproject.views.CitationReference;
import org.ambraproject.views.article.ArticleInfo;
import org.w3c.dom.Document;

import java.util.List;

/**
 * Fetch article service.
 */
public interface FetchArticleService extends HibernateService {
  /**
   * Get the URI transformed as HTML.
   *
   * @param article The Article to transform into HTML
   * @return String representing the annotated article as HTML
   * @throws org.ambraproject.ApplicationException
   *          ApplicationException
   */
  public String getArticleAsHTML(final ArticleInfo article) throws Exception;

  /**
   * Get the article xml
   *
   * @param article the article
   * @return article xml
   */
  public Document getArticleDocument(final ArticleInfo article);


  /**
   * Get the author affiliations for a given article
   *
   * @param doc article xml
   * @return author affiliations
   */
  public List<AuthorExtra> getAuthorAffiliations(Document doc);

  /**
   * Get the corresponding Author's email
   *
   * @param doc
   * @return an email address
   */
  public List<String> getCorrespondingAuthors(Document doc);

  /**
   * Get the author contributions
   *
   * @param doc
   * @return the author contributions
   */
  public List<String> getAuthorContributions(Document doc);

  /**
   * Get the authors competing interest
   *
   * @param doc
   * @return the authors competing interest
   */
  public List<String> getAuthorCompetingInterests(Document doc);

  /**
   * Get references for a given article
   *
   * @param doc article xml
   * @return references
   */
  public List<CitationReference> getReferences(Document doc);

  /**
   * Returns abbreviated journal name
   *
   * @param doc article xml
   * @return abbreviated journal name
   */
  public String getJournalAbbreviation(Document doc);
}
