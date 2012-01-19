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

package org.topazproject.ambra.article.service;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.AuthorExtra;
import org.topazproject.ambra.article.CitationReference;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch article service.
 */
public interface FetchArticleService {

  /**
   * All Article(transformed)/ArticleInfo/Annotation/Citation cache activity is syncronized on
   * ARTICLE_LOCK.
   */
  public  static final String ARTICLE_LOCK     = "ArticleAnnotationCache-Lock-";

  /**
   * Get the URI transformed as HTML.
   * @param articleURI articleURI
   * @return String representing the annotated article as HTML
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   */
  public String getURIAsHTML(final String articleURI) throws Exception;

  /**
   * Return the annotated content as a String
   * @param articleURI articleURI
   * @return an the annotated content as a String
   * @throws ParserConfigurationException ParserConfigurationException
   * @throws SAXException SAXException
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws org.topazproject.ambra.ApplicationException ApplicationException
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  public String getAnnotatedContent(final String articleURI)
      throws ParserConfigurationException, SAXException, IOException, URISyntaxException,
             ApplicationException, NoSuchArticleIdException,TransformerException;


  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @return list of article uri's
   * @throws ApplicationException ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state)
      throws ApplicationException;

  /**
   * Get a list of ids of all articles that match the given criteria.
   *
   * @param startDate startDate
   * @param endDate   endDate
   * @param state     array of matching state values
   * @param ascending controls the sort order (by date).
   * @return list of article uri's
   * @throws ApplicationException
   */
  public List<String> getArticleIds(String startDate, String endDate, int[] state, boolean ascending)
    throws ApplicationException;

  /**
   * Get the article xml
   * @param articleURI article uri
   * @return article xml
   */
  public Document getArticleDocument(String articleURI);


  /**
   * Get the author affiliations for a given article
   * @param doc article xml
   * @return author affiliations
   */
  public ArrayList<AuthorExtra> getAuthorAffiliations(Document doc);

  /**
   * Get references for a given article
   * @param doc article xml
   * @return references
   */
  public ArrayList<CitationReference> getReferences(Document doc);

  /**
   * Returns abbreviated journal name
   * @param doc article xml
   * @return abbreviated journal name
   */
  public String getJournalAbbreviation(Document doc);
}
