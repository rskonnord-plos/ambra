/* $HeadURL::                                                                                     $
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.activation.DataSource;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public Enumeration<? extends ZipEntry> getEntries() throws IOException;

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

    public Enumeration<? extends ZipEntry> getEntries() throws IOException {
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
    private static final Logger log = LoggerFactory.getLogger(StreamZip.class);
    public static final Map<String, String> archiverTypes   = new HashMap<String, String>();
    public static final Map<String, String> compressorTypes = new HashMap<String, String>();

    static {
      archiverTypes.put("application/zip",              "zip");
      archiverTypes.put("application/x-zip",            "zip");
      archiverTypes.put("application/x-zip-compressed", "zip");
      archiverTypes.put("application/tar",              "tar");
      archiverTypes.put("application/x-tar",            "tar");
      archiverTypes.put("application/x-tar-gz",         "tar");
      archiverTypes.put("application/x-tar-bz",         "tar");

      compressorTypes.put("application/x-tar-gz", "gz");
      compressorTypes.put("application/x-tar-bz", "bzip2");
    }

    public static boolean isZip(String contentType) {
      return "zip".equals(archiverTypes.get(contentType));
    }

    public static boolean isArchive(String contentType) {
      return archiverTypes.get(contentType) != null;
    }

    private ArchiveInputStream curStream = null;
    private Map<String, File> tmpFiles = new HashMap<String, File>();
    private boolean readCompletely = false;
    private ArrayList<ZipEntry> entries = new ArrayList<ZipEntry>();

    protected abstract InputStream getStream() throws IOException;
    protected abstract String getContentType();

    @Override
    protected void finalize() throws Throwable {
      for (File f : tmpFiles.values()) {
        try {
          f.delete();
        } catch (Throwable t) {
          log.warn("Failed to delete temp file '" + f, t);
        }
      }
      super.finalize();
    }

    public Enumeration<? extends ZipEntry> getEntries() throws IOException {
      if (readCompletely) {
        return new Enumeration<ZipEntry>() {

          private Iterator<ZipEntry> it = entries.iterator();

          public boolean hasMoreElements() {
            return it.hasNext();
          }

          public ZipEntry nextElement() {
            return it.next();
          }

        };
      }

      final ArchiveInputStream zis = getArchiveStream();

      return new Enumeration<ZipEntry>() {
        private ZipEntry ze;

        {
          nextElement();
        }

        public boolean hasMoreElements() {
          return (ze != null);
        }

        public ZipEntry nextElement() {
          try {
            ZipEntry cur = ze;

            ze = toZipEntry(zis.getNextEntry());
            if (ze != null)
              save(zis, ze);
            else {
              readCompletely = true;
              zis.close();
            }
            return cur;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      };
    }

    private void save(ArchiveInputStream zis, ZipEntry ze) throws IOException {
      OutputStream out = null;
      File f = null;
      try {
        f = File.createTempFile("ambra-unzip-", "");
        if (log.isDebugEnabled())
          log.debug("Unzipping " + ze.getName() + " as " + f);
        out = new FileOutputStream(f);
        IOUtils.copy(zis, out);
        entries.add(ze);
        tmpFiles.put(ze.getName(), f);
        f = null;
      } finally {
        if (out != null)
          out.close();
        if (f != null)
          if (!f.delete()) {
            log.error("Failed deleting temp file " + f.getAbsolutePath());
          }
      }

    }

    public InputStream getStream(String name, long[] size) throws IOException {
      File f = tmpFiles.get(name);
      if (f != null) {
        size[0] = f.length();
        return new FileInputStream(f);
      }

      boolean restart = loadStream();

      ZipEntry ze;
      while (true) {
        while ((ze = toZipEntry(curStream.getNextEntry())) != null) {
          if (tmpFiles.get(ze.getName()) == null)
            save(curStream, ze);
          if (ze.getName().equals(name)) {
            size[0] = ze.getSize();
            return new FileInputStream(tmpFiles.get(name));
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

      curStream = getArchiveStream();
      return false;
    }

    private ArchiveInputStream getArchiveStream() throws IOException {
      InputStream in         = getStream();
      if (in == null)
        throw new NullPointerException("Missing input stream");
      String      ct         = getContentType();
      String      compressor = compressorTypes.get(ct);
      String      archiver   = archiverTypes.get(ct);

      if (archiver == null)
        throw new IOException("Unknown content-type: '" + ct + "'");

      try {
        if (compressor != null) {
          if ("bzip2".equals(compressor))
            in.read(new byte[2]);  // Remove "Bz"
          CompressorStreamFactory csf = new CompressorStreamFactory();
          in = csf.createCompressorInputStream(compressor, in);
        }
        ArchiveStreamFactory asf = new ArchiveStreamFactory();
        return asf.createArchiveInputStream(archiver, in);
      } catch (CompressorException e) {
        IOException ioe = new IOException("Failed to create stream");
        ioe.initCause(e);
        throw ioe;
      } catch (ArchiveException e) {
        IOException ioe = new IOException("Failed to create stream");
        ioe.initCause(e);
        throw ioe;
      }
    }

    private ZipEntry toZipEntry(ArchiveEntry entry) {
      if (entry == null)
        return null;

      if (entry instanceof ZipEntry)
        return (ZipEntry) entry;

      if (entry instanceof TarArchiveEntry) {
        TarArchiveEntry te = (TarArchiveEntry) entry;
        ZipEntry ze = new ZipEntry(te.getName());
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(te.getSize());
        ze.setCompressedSize(te.getSize());
        if (te.getModTime() != null)
          ze.setTime(te.getModTime().getTime());
        return ze;
      }

      // Should not happen
      throw new Error("Unknown entry type: " + entry.getClass());
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

    protected String getContentType() {
      return "application/zip";
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
     * @throws IOException on an error
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

    protected String getContentType() {
      return zs.getContentType();
    }
  }
}
