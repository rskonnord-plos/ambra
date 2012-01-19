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
 * This class is used to process the contents of a directory and everything under it.
 * @author Paul Gearon
 */
public class DirProcessor extends HierarchyProcessor {

  /**
   * Creates a processor fo handling files and subdirectories under a directory.
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   *        May be <code>null</code>.
   * @param fileFilter A filter for selecting which files are to be processed.
   *        If <code>null</code> then all files are processed.
   */
  public DirProcessor(FileProcessor fileProc, FileProcessor dirProc, FileFilter fileFilter) {
    super(fileProc, dirProc, fileFilter);
  }

  /**
   * Creates a processor fo handling files and subdirectories under a directory.
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   */
  public DirProcessor(FileProcessor fileProc, FileProcessor dirProc) {
    this(fileProc, dirProc, null);
  }

  /**
   * Creates a processor for handling files under a directory.
   * @param fileProc The function for processing any files found.
   */
  public DirProcessor(FileProcessor fileProc) {
    this(fileProc, null, null);
  }

  /**
   * Creates a processor for handling files with a given extension under a directory.
   * @param fileProc The function for processing any files found.
   * @param ext The file extension to use.
   */
  public DirProcessor(FileProcessor fileProc, String ext) {
    super(fileProc, ext);
  }

  /**
   * Process a directory by name and everything under it.
   * @param dirName The name of the directory to process.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(String dirName) throws IOException {
    return process(new File(dirName), true);
  }

  /**
   * Process a path by name. The contents of sub-paths in the hierarchy are optionally processed as well.
   * @param path The path name to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into sub-paths.
   *                       Otherwise, processing is confined to just this path.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(String path, boolean processSubDirs) throws IOException {
    return process(new File(path), processSubDirs);
  }

  /**
   * Process a directory and everything under it.
   * @param dirName The name of the directory to process.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(File dir) throws IOException {
    return process(dir, true);
  }

  /**
   * Process a directory. The contents of subdirectories are optionally processed as well.
   * @param dir The directory to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into subdirectories.
   *                       Otherwise, processing is confined to just this directory.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(File dir, boolean processSubDirs) throws IOException {
    int total = 0;
    for (File file: dir.listFiles(fileFilter)) total += fileProc.fn(file.getPath());
    for (File d: dir.listFiles(DirFilter.INSTANCE)) {
      if (dirProc != null)
        total += dirProc.fn(d.getPath());
      if (processSubDirs)
        total += process(d, true);
    }
    return total;
  }

  /**
   * Gets a filter that will return <code>true</code> for normal files, and <code>false</code>
   * for non-standard types, such as pipes and directories.
   *
   * @return an instance of a FileFilter for identifying files.
   */
  protected FileFilter getStandardFileFilter() {
    return new AllStandardFiles();
  }

  /**
   * A FileFilter that passes all standard files.
   */
  private static class AllStandardFiles implements FileFilter {
    public boolean accept(File file) {
      return file.isFile();
    }
    public boolean accept(String path) {
      return new File(path).isFile();
    }
  }

  /**
   * A FileFilter that identifies all directories.
   */
  static class DirFilter implements java.io.FileFilter {
    public static DirFilter INSTANCE = new DirFilter();
    private DirFilter() { }
    public boolean accept(File file) {
      return file.isDirectory();
    }
  }
}
