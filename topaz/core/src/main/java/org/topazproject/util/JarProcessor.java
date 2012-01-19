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
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;


/**
 * This class is used to process the contents of a Jar.
 * @author Paul Gearon
 */
public class JarProcessor extends HierarchyProcessor {

  public static final String JAR_PATH_SEP = "/";

  /**
   * Creates a processor fo handling files and directories in a jar.
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   *        May be <code>null</code>.
   * @param fileFilter A filter for selecting which files are to be processed.
   *        If <code>null</code> then all files are processed.
   */
  public JarProcessor(FileProcessor fileProc, FileProcessor dirProc, FileFilter fileFilter) {
    super(fileProc, dirProc, fileFilter);
  }

  /**
   * Creates a processor fo handling files and directories in a jar.
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   */
  public JarProcessor(FileProcessor fileProc, FileProcessor dirProc) {
    this(fileProc, dirProc, null);
  }

  /**
   * Creates a processor for handling files in a jar.
   * @param fileProc The function for processing any files found.
   */
  public JarProcessor(FileProcessor fileProc) {
    this(fileProc, null, null);
  }

  /**
   * Creates a processor for handling files with a given extension in a jar.
   * @param fileProc The function for processing any files found.
   * @param ext The file extension to use.
   */
  public JarProcessor(FileProcessor fileProc, String ext) {
    super(fileProc, ext);
  }

  /**
   * Process a jar by path.
   * @param jarPath The path of the jar to process..
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(String jarPath) throws IOException {
    return process(new JarFile(jarPath));
  }

  /**
   * Process a jar by path. Subdirectories found in a jar are optionally processed.
   * @param path The path name to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into sub-paths.
   *                       Otherwise, processing is confined to just this path.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(String path, boolean processSubDirs) throws IOException {
    return process(new JarFile(path), processSubDirs);
  }

  /**
   * Process everything in a jar.
   * @param jarPath The path of the jar to process.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the file.
   */
  public int process(JarFile jar) throws IOException {
    return process(jar, true);
  }

  /**
   * Process a jar. The contents of subdirectories in the jar are optionally processed as well.
   * @param jar The jar to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into subdirectories.
   *                       Otherwise, processing is confined to the root of the archive.
   * @return The number of items processed.
   * @throws IOException If there was an error processing the archive.
   */
  public int process(JarFile jar, boolean processSubDirs) throws IOException {
    int total = 0;
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String entryPath = entry.getName();
      // truncate the trailing / from directory names
      if (entry.isDirectory())
        entryPath = entryPath.substring(0, entryPath.length() - 1);
      // if we are not recursing and this contains path separators, then continue
      if (!processSubDirs && entryPath.contains(JAR_PATH_SEP))
        continue;
      if (entry.isDirectory()) {
        if (dirProc != null)
          total += dirProc.fn(entryPath);
      } else {
        if (fileFilter.accept(entryPath))
          total += fileProc.fn(entryPath);
      }
    }
    return total;
  }

  /**
   * Gets a filter that will return <code>true</code> for normal files.
   * @return an instance of a FileFilter for identifying files.
   */
  protected FileFilter getStandardFileFilter() {
    return new AllFiles();
  }

  /**
   * A FileFilter that passes all files. There is no need to distinguish between files and
   * directories because this functor is only called for files.
   */
  private static class AllFiles implements FileFilter {
    public boolean accept(File file) {
      return true;
    }
    public boolean accept(String path) {
      return true;
    }
  }

}
