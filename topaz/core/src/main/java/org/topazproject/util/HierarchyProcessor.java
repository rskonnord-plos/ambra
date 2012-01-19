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
import java.io.IOException;

/**
 * This class is used to process the files in a hierarchial structure.
 *
 * @author Paul Gearon
 */
public abstract class HierarchyProcessor {
  /** The function for processing files. */
  protected FileProcessor fileProc;

  /** The function for processing subdirectories. */
  protected FileProcessor dirProc;

  /** A filter for identifying the files to be processed. */
  protected FileFilter fileFilter;

  /** An optional filename extension to be used when determining what will be processed. */
  protected String ext;

  /**
   * Creates a processor fo handling files and subdirectories in a hierarchy.
   *
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   *        May be <code>null</code>.
   * @param fileFilter A filter for selecting which files are to be processed.
   *        If <code>null</code> then all files are processed.
   */
  HierarchyProcessor(FileProcessor fileProc, FileProcessor dirProc, FileFilter fileFilter) {
    if (fileProc == null)
      throw new IllegalArgumentException("Function for processing files must be provided.");
    this.fileProc = fileProc;
    this.dirProc = dirProc;
    this.fileFilter = (fileFilter == null) ? getStandardFileFilter() : fileFilter;;
  }

  /**
   * Creates a processor for handling files with a given extension in the hierarchy.
   *
   * @param fileProc The function for processing any files found.
   * @param ext The file extension to use.
   */
  HierarchyProcessor(FileProcessor fileProc, String ext) {
    this(fileProc, null, null);
    setFilenameExt(ext);
  }

  /**
   * Gets the filename extension to be processed by this instance.
   *
   * @return The filename extension to be processed. The "." character is included.
   */
  public String getFilenameExt(String ext) {
    return ext;
  }

  /**
   * Sets the filename extension to be processed by this instance.
   *
   * @param ext The filename extension to be processed. The "." character is optional.
   */
  public void setFilenameExt(String ext) {
    this.ext = ext.startsWith(".") ? ext : "." + ext;
    fileFilter = new FileExtFilter(this.ext);
  }

  /**
   * Process a directory by name and everything under it.
   *
   * @param pathName The name of the directory to process.
   *
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public abstract int process(String pathName) throws IOException;

  /**
   * Process a path by name. The contents of sub-paths in the hierarchy are optionally processed as well.
   *
   * @param path The path name to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into sub-paths.
   *                       Otherwise, processing is confined to just this path.
   *
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public abstract int process(String path, boolean processSubDirs) throws IOException;

  /**
   * Gets a filter that will return <code>true</code> for normal files, and <code>false</code>
   * for non-standard types, such as pipes and directories.
   *
   * @return an instance of a FileFilter for identifying files.
   */
  protected abstract FileFilter getStandardFileFilter();

}
