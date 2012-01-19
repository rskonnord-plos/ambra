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

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MimeTypeToFileExtMapper {
  private Resource mappingLocation = new ClassPathResource("mime.types");
  private BufferedReader bufferedReader;

  public MimeTypeToFileExtMapper() throws IOException {
    bufferedReader = new BufferedReader(new InputStreamReader(mappingLocation.getInputStream(), "UTF-8"));
  }

  public Map<String, String> getFileExtListByMimeType() throws IOException {
    final Map<String, String> mimeMap = new HashMap<String, String>();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      if (!line.startsWith("#") && !StringUtils.isBlank(line)) {
        final String[] parts = line.split("\\s+");
        mimeMap.put(parts[0], parts[1].trim());
      }
    }

    mimeMap.put("text/xml", "xml");
    return mimeMap;
  }
}
