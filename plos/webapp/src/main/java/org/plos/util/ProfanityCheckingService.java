/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Checks that content is not profane. It could be used to check that the user's posts don't contain profane words like F***, GEORGE, BUSH, etc.
 */
public class ProfanityCheckingService {
  private Map<String, Pattern> profanePatterns;

  /**
   * Validate that the content is profane or not and return the list of profane words found.
   * @param content content to check for profanity
   * @return list of profane words
   */
  public List<String> validate(final String content) {
    final List<String> profaneWordsFound = new ArrayList<String>();
    if (content != null) {
      final String contentLowerCase = content.toLowerCase();

      for (final Map.Entry<String,Pattern> patternEntry : profanePatterns.entrySet()) {
        final Pattern pattern = patternEntry.getValue();
        if (pattern.matcher(contentLowerCase).find()) {
          profaneWordsFound.add(patternEntry.getKey());
        }
      }
    }
    return profaneWordsFound;
  }

  /**
   * Set the list of profane words.
   * @param profaneWords profaneWords
   */
  public void setProfaneWords(final Collection<String> profaneWords) {
    final Map<String, Pattern> patterns = new HashMap<String, Pattern>(profaneWords.size());
    for (final String profaneWord : profaneWords) {
      final Pattern pattern = Pattern.compile("\\b" + profaneWord.toLowerCase() + "\\b");
      patterns.put(profaneWord, pattern);
    }
    this.profanePatterns = patterns;
  }
}
