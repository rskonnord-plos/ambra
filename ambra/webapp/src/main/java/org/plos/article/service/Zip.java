/* $HeadURL::                                                                                     $
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

package org.plos.article.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.activation.DataSource;

/**
 * A simple abstraction of a zip archive, as needed by the ingester.
 *
 * @author Ronald Tschalär
 */
public interface Zip {
  /**
   * Get the name of the archive.
   *
   * @return the name of the archive, or null if unknown.
   */
  public String getName();

  /**
   * Get a list of all the entries in the archive.
   *
   * @return an Enumeration of {@link java.util.zip.ZipEntry ZipEntry} that enumerates all the
   *         entries
   * @throws IOException if an error occurred getting the enumeration
   */
  public Enumeration getEntries() throws IOException;

  /**
   * Get the given entry's contents as an InputStream.
   *
   * @param name the full pathname of the entry to retrieve
   * @param size (output) must be an array of length &gt;= 1; on return the 0'th
   *             element will contain the size of the contents, or -1 if unknown.
   * @return the entry's contents, or null if no entry by the given name exists
   * @throws IOException if an error occurred getting the contents
   */
  public InputStream getStream(String name, long[] size) throws IOException;

  /**
   * An implementation of {@link Zip Zip} where the zip archive is stored in a file.
   */
  public static class FileZip implements Zip {
    private final ZipFile zf;

    /**
     * Create a new instance.
     *
     * @param zipFile the file-name of the zip archive
     * @throws IOException if an error occurred accessing the file
     */
    public FileZip(String zipFile) throws IOException {
      zf = new ZipFile(zipFile);
    }

    public String getName() {
      return zf.getName();
    }

    public Enumeration getEntries() {
      return zf.entries();
    }

    public InputStream getStream(String name, long[] size) throws IOException {
      ZipEntry ze = zf.getEntry(name);
      if (ze != null) {
        size[0] = ze.getSize();
        return zf.getInputStream(ze);
      }

      size[0] = -1;
      return null;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive is available from a stream.
   */
  public static abstract class StreamZip implements Zip {
    private ZipInputStream curStream = null;

    protected abstract InputStream getStream() throws IOException;

    public Enumeration getEntries() throws IOException {
      final ZipInputStream zis = new ZipInputStream(getStream());

      return new Enumeration() {
        private ZipEntry ze;

        {
          nextElement();
        }

        public boolean hasMoreElements() {
          return (ze != null);
        }

        public Object nextElement() {
          try {
            ZipEntry cur = ze;

            ze = zis.getNextEntry();
            if (ze == null)
              zis.close();

            return cur;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      };
    }

    public InputStream getStream(String name, long[] size) throws IOException {
      boolean restart = loadStream();

      ZipEntry ze;
      while (true) {
        while ((ze = curStream.getNextEntry()) != null) {
          if (ze.getName().equals(name)) {
            size[0] = ze.getSize();
            return curStream;
          }
        }

        curStream.close();
        curStream = null;
        if (restart)
          restart = loadStream();
        else
          break;
      }

      size[0] = -1;
      return null;
    }

    private boolean loadStream() throws IOException {
      if (curStream != null)
        return true;

      curStream = new ZipInputStream(getStream());
      return false;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive is stored in memory.
   */
  public static class MemoryZip extends StreamZip {
    private final ByteArrayInputStream zs;
    private final String               name;

    /**
     * Create a new instance.
     *
     * @param zipBytes the zip archive as an array of bytes
     * @param name the name of the archive, or null if unknown
     */
    public MemoryZip(byte[] zipBytes, String name) {
      zs = new ByteArrayInputStream(zipBytes);
      this.name = name;
    }

    public String getName() {
      return name;
    }

    protected InputStream getStream() {
      zs.reset();
      return zs;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive available as a DataSource.
   * NOTE: this assumes that either A) the data-source's input-stream supports reset(), or
   * that B) each DataSource.getInputStream() returns a new stream that starts at the beginning
   * (like Axis's ManagedMemoryDataSource does).
   */
  public static class DataSourceZip extends StreamZip {
    private final DataSource  zs;
    private final InputStream is;

    /**
     * Create a new instance.
     *
     * @param zipSource the zip archive as a datasource
     */
    public DataSourceZip(DataSource zipSource) throws IOException {
      zs = zipSource;
      InputStream i = zs.getInputStream();
      if (i.markSupported()) {
        i.mark(Integer.MAX_VALUE);
        is = i;
      } else {
        i.close();
        is = null;
      }
    }

    public String getName() {
      return zs.getName();
    }

    protected InputStream getStream() throws IOException {
      if (is != null) {
        is.reset();
        return is;
      } else
        return zs.getInputStream();
    }
  }
}
