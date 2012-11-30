/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.article;

import org.w3c.dom.Document;

import java.util.List;
import java.util.Set;

/**
 * @author Alex Kudlick
 *         Date: 7/3/12
 */
public interface ArticleClassifier {

  /**
   * Classify an article from its xml.
   *
   * @param articleXml the article xml
   * @return a list of categories to which the article belongs. Each entry should use <code>/</code>s to
   *         delimit subject hierarchy.  Categories are returned in descending order of the
   *         strength of the match.
   */
  public List<String> classifyArticle(Document articleXml) throws Exception;
}
