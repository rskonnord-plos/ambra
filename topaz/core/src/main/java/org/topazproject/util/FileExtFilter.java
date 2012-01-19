/* $HeadURL$
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

package org.topazproject.util;

import java.io.File;

/**
 * A File filter that indicates if a file has a given extension.
 *
 * @author Paul Gearon
 */
public class FileExtFilter implements FileFilter {
  /** The filter being looked for. */
  private String ext;

  /**
   * Creates a new filter based on a fiven extension.
   * @param ext The extension to filter on.
   */
  public FileExtFilter(String ext) {
    this.ext = ext.startsWith(".") ? ext : "." + ext;
  }

  /**
   * Gets the extension being filtered for.
   * @return The extention, including the . character.
   */
  public String getExt() {
    return ext;
  }

  /**
   * Test if a file should be returned from this filter.
   * @param file The file to be tested.
   * @return <code>true</code> if the file has the required extension.
   */
  public boolean accept(File file) {
    return file.getName().endsWith(ext);
  }

  /**
   * Test if a file should be returned from this filter.
   * @param filePath The path of the file to be tested.
   * @return <code>true</code> if the file has the required extension.
   */
  public boolean accept(String filePath) {
    return filePath.endsWith(ext);
  }
}
