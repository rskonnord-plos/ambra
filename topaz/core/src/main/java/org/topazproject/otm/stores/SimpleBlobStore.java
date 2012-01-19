/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
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
package org.topazproject.otm.stores;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;

/**
 * A simple File backed BlobStore that supports transactions and ensures repeatable reads. A
 * store-wide write lock is held during the prepare-commit phase and all reads from the store is
 * performed by obtaining a store-wide read-lock.
 *
 * @author Pradeep Krishnan
 */
public class SimpleBlobStore extends FileBackedBlobStore {
  private final File storage;

  /**
   * Creates a new SimpleBlobStore object.
   *
   * @throws OtmException on an error
   */
  public SimpleBlobStore() throws OtmException {
    this(new File(new File(System.getProperty("user.dir")), "blobstore"));
  }

  /**
   * Creates a new SimpleBlobStore object.
   *
   * @param root the root directory for blobstore
   *
   * @throws OtmException DOCUMENT ME!
   */
  public SimpleBlobStore(String root) throws OtmException {
    this(new File(root));
  }

  /**
   * Creates a new SimpleBlobStore object.
   *
   * @param root the root directory
   * @throws OtmException on an error
   */
  public SimpleBlobStore(File root) throws OtmException {
    super(root);
    this.storage = new File(root, "blobs");
  }

  public Connection openConnection(Session sess, boolean readOnly) throws OtmException {
    return new SimpleBlobStoreConnection(this, sess);
  }

  private final class SimpleBlobStoreConnection extends FileBackedBlobStoreConnection {
    private SimpleBlobStoreConnection(FileBackedBlobStore store, Session sess) throws OtmException {
      super(store, sess);
    }

    @Override
    protected FileBackedBlob doGetBlob(ClassMetadata cm, String id, Object instance, File work)
        throws OtmException {
      File file = toFile(SimpleBlobStore.this.storage, id, ".dat");
      File bak = toFile(SimpleBlobStore.this.storage, id, ".bak");

      return new FileBlob(id, file, work, bak);
    }

    private final class FileBlob extends FileBackedBlob {
      private final File file;

      public FileBlob(String id, File file, File work, File bak) {
        super(id, work, bak);
        this.file = file;
      }

      protected boolean copyFromStore(OutputStream to, boolean backup) throws OtmException {
        InputStream in = null;
        try {
          getStoreLock().acquireRead(SimpleBlobStoreConnection.this);
          if (!file.exists()) {
            if (log.isTraceEnabled())
              log.trace("copyFromStore(no-op): " + file + " does not exist for " + getId());
            return false;
          }

          if (log.isTraceEnabled())
            log.trace("Creating " + (backup ? "backup " : "copy ") + to + " from " + file
                + " for id " + getId());
          try {
            copy(in = new FileInputStream(file), to);
          } catch (IOException e) {
            throw new OtmException("Failed to copy from store " + file + " to " + to + " for "
                + getId());
          }

          return true;
        } catch (InterruptedException e) {
          throw new OtmException("Interrupted while acquiring read-lock for " + getId(), e);
        } finally {
          closeAll(in);
          getStoreLock().releaseRead(SimpleBlobStoreConnection.this);
        }
      }

      protected boolean createInStore() throws OtmException {
        boolean success = file.exists();
        if (!success) {
          if (log.isTraceEnabled())
            log.trace("Commit-Creating " + file + " for " + getId());
          try {
            success = file.createNewFile();
          } catch (IOException e) {
            throw new OtmException("Failed to create : " + file + " for " + getId());
          }
        }
        return success;
      }

      protected boolean moveToStore(File from) throws OtmException {
        if (log.isTraceEnabled())
          log.trace("Commit-Saving " + from + " to " + file + " for " + getId());

        return from.renameTo(file);
      }

      protected boolean deleteFromStore() throws OtmException {
        boolean success = !file.exists();
        if (!success) {
          if (log.isTraceEnabled())
            log.trace("Commit-Deleting " + file + " for " + getId());
          success = file.delete();
        }

        return success;
      }
    }
  }
}
