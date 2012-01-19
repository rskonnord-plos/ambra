/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.plos.filestore.impl;

import com.guba.mogilefs.LocalFileMogileFSImpl;
import org.plos.filestore.FileStoreException;
import org.plos.filestore.FileStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link org.plos.filestore.FileStoreService} that uses local file
 * system for storing files;
 *
 * @author Bill OConnor
 *
 **/

public class FileSystemImpl extends LocalFileMogileFSImpl implements FileStoreService {
  private static final Logger log = LoggerFactory.getLogger(FileSystemImpl.class);

  private File workingDirectory;

  /*
  * Constructor
  */
  public FileSystemImpl (File topDir, String domain) throws IOException {
    super(topDir, domain);
    this.workingDirectory = new File(topDir, domain);
  }

  public Boolean hasXReproxy() {
    return false;
  }

  /*
  *    {@link org.plos.filestore.FileStoreService#}
  */
  public InputStream getFileInStream(String id) throws FileStoreException {
    try {
      return new FileInputStream(new File(workingDirectory, id));
    } catch (Exception e) {
      throw new FileStoreException("Error opening stream to file: " + id, e);
    }
  }

  public byte[] getFileByteArray(String id) throws FileStoreException {
    try {
      //getFileBytes() from  LocalFileMogileFSImpl is broken (never completes)
      File storedFile = new File(workingDirectory, id);
      if (!storedFile.exists()) {
        throw new FileStoreException("Specified file: " + storedFile.getAbsolutePath() + " doesn't exist");
      }
      byte[] bytes = new byte[(int) storedFile.length()];
      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream(storedFile);
        int offset = 0;
        int numRead = 0;
        while ((offset < bytes.length)
            && (numRead = inputStream.read(bytes, offset, bytes.length - offset)) >=0) {
          offset += numRead;
        }
        return bytes;
      } finally {
        closeStreams(inputStream);
      }
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:getFileByteArray ", e);
    }
  }

  public URL[] getRedirectURL(String id) throws FileStoreException {
    URL[] urls;
    try {
      String[] paths = getPaths(id, true);
      int pathCount = paths.length;
      urls = new URL[pathCount];

      for(int i = 1; i <= pathCount; i++) {
        urls[i] = new URL(paths[i]);
      }
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:getRedirectURL ", e);
    }
    return urls;
  }

  public Map<String, String> listFiles(String doi) throws FileStoreException {
    try {
      File parentDir = new File(workingDirectory, doi.substring(0,doi.lastIndexOf(File.separator)));
      if (!parentDir.exists()) {
        throw new FileStoreException("Couldn't find directory: " + parentDir.getAbsolutePath());
      }

      String files[] = parentDir.list();
      Map<String, String> map = new HashMap<String,String>();

      for(String file : files) {
        map.put(file, file);
      }

      return map;
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:listFiles ", e);
    }
  }

  public OutputStream getFileOutStream(String id, long byteCount) throws FileStoreException {
    try {
      File parentDir = new File(workingDirectory, id.substring(0,id.lastIndexOf(File.separator)));
      if (!parentDir.exists()) {
        if(!parentDir.mkdirs()) {
          throw new FileStoreException("Couldn't create directory: " + parentDir.getAbsolutePath());
        }
      }
      return newFile(id, "", byteCount);
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:getFileOutStream ", e);
    }
  }

  public void copyFileToStore(String id, File srcFile) throws FileStoreException {
    try {
      File destFile = new File(workingDirectory, id);
      final File parentDir = new File(workingDirectory, id.substring(0, id.lastIndexOf(File.separator)));
      if (id.contains(File.separator) &&
          !parentDir.exists()) {
        if (!parentDir.mkdirs()) {
          throw new FileStoreException("Couldn't create directory: " + parentDir.getAbsolutePath());
        }
      }
      InputStream inputStream = null;
      OutputStream outputStream = null;
      try {
        inputStream = new FileInputStream(srcFile);
        outputStream = new FileOutputStream(destFile);
        copy(inputStream, outputStream);
      } finally {
        closeStreams(inputStream, outputStream);
      }
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:copyFileTo ", e);
    }
  }

  public File copyFileFromStore(String id, File srcFile) throws FileStoreException {
    try {
      return getFile(id, srcFile);
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:copyFileFrom ", e);
    }
  }

  public void copy(InputStream from, OutputStream to) throws IOException {
    byte[] b = new byte[4*1024];
    int read;
    while ((read = from.read(b)) != -1) {
      to.write(b, 0, read);
    }
  }

  public void deleteFile(String id) throws FileStoreException {
    try {
      delete(id);
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:deleteFile ", e);
    }
  }

  public void renameFile(String oldId, String newId) throws FileStoreException {
    try {
      rename(oldId, newId);
    } catch (Exception e) {
      throw new FileStoreException("FileSystemImpl:renameFile ", e);
    }
  }

  private void closeStreams(Closeable... closeables) {
    boolean success = true;
    String errorMessage = "Couldn't close I/O stream(s)";
    for (Closeable c : closeables) {
      try {
        if (c != null) {
          c.close();
        }
      } catch (IOException e) {
        success = false;
        errorMessage += "; " + e.getMessage();
      }
    }
    if (!success) {
      log.error(errorMessage);
    }
  }
}
