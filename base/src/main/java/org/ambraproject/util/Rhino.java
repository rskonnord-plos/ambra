/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.util;

import org.ambraproject.views.article.ArticleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

/**
 * Utility methods currently called by both ambra and rhino.  If ambra
 * is retired, we will likely move this code back into rhino, or refactor
 * it in some other way.
 */
public final class Rhino {

  private static final Logger log = LoggerFactory.getLogger(Rhino.class);

  public static ArticleType getKnownArticleType(Set<String> types) {
    if (types == null) {
      throw new IllegalStateException("types not set");
    }
    ArticleType knownType = null;
    for (String artType : types) {
      URI articleTypeUri = URI.create(artType);
      ArticleType typeForURI = ArticleType.getKnownArticleTypeForURI(articleTypeUri);
      if (typeForURI != null) {
        if (knownType == null) {
          knownType = typeForURI;
        } else if (!knownType.equals(typeForURI) && log.isErrorEnabled()) {
          /*
           * The old behavior was to return the first value matched from the Set iterator.
           * To avoid introducing bugs, continue without changing the value of knownType.
           */
          log.error("Multiple article types ({}, {}) matched from: {}",
              new String[]{knownType.getHeading(), typeForURI.getHeading(), types.toString()});
        }
      }
    }
    return knownType == null ? ArticleType.getDefaultArticleType() : knownType;
  }
}
