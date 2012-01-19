/* $HeadURL::                                                                                     $
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
package org.topazproject.otm;

import static org.testng.AssertJUnit.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.activation.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.topazproject.otm.annotations.Blob;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.UriPrefix;


/**
 * Blob tests.
 *
 * @author Pradeep Krishnan
 */
@Test(sequential = true)
public class BlobTest extends AbstractOtmTest {
  private static final Log  log     = LogFactory.getLog(BlobTest.class);
  private final URI         foo     = URI.create("foo:1");
  final byte[] data = "Some really really big thing ....".getBytes();
  final byte[] update = "People are crying out for change. Washington ...".getBytes();

  @BeforeClass
  public void setUp() throws OtmException {
    try {
      initFactory();
      initGraphs();
    } catch (OtmException e) {
      log.error("OtmException in setup", e);
      throw e;
    } catch (RuntimeException e) {
      log.error("Exception in setup", e);
      throw e;
    } catch (Error e) {
      log.error("Error in setup", e);
      throw e;
    }
  }

  @Test
  public void testIngest() throws OtmException {
    log.info("Testing ingestion of blob ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {

          BlobTest1 b = new BlobTest1();
          b.setId(foo);
          b.setBlob(null);
          session.saveOrUpdate(b);
          assertNotNull(b.getBlob());
          try {
            b.write(data);
            assertEquals(data, b.read());
          } catch (IOException e) {
            throw new OtmException("Test failed.", e);
          }
        }
      });

    doInSession(new Action() {
        public void run(Session session) throws OtmException {

          BlobTest1 b = session.get(BlobTest1.class, foo.toString());
          assertNotNull(b);
          assertNotNull(b.getBlob());
          try {
            assertEquals(data, b.read());
          } catch (IOException e) {
            throw new OtmException("Test failed.", e);
          }
        }
      });
  }

  @Test(dependsOnMethods={"testIngest"})
  public void testUpdate() throws OtmException {
    log.info("Testing updating of blob ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {

          BlobTest1 b = session.get(BlobTest1.class, foo.toString());
          assertNotNull(b);
          assertNotNull(b.getBlob());
          try {
            b.write(update);
            assertEquals(update, b.read());
          } catch (IOException e) {
            throw new OtmException("Test failed.", e);
          }
        }
      });

    doInSession(new Action() {
      public void run(Session session) throws OtmException {

        BlobTest1 b = session.get(BlobTest1.class, foo.toString());
        assertNotNull(b);
        assertNotNull(b.getBlob());
        try {
          assertEquals(update, b.read());
        } catch (IOException e) {
          throw new OtmException("Test failed.", e);
        }
      }
    });
  }

  @Test(dependsOnMethods={"testUpdate"})
  public void testDelete() throws OtmException {
    log.info("Testing deleting of blob ...");
    doInSession(new Action() {
        public void run(Session session) throws OtmException {

          BlobTest1 b = session.get(BlobTest1.class, foo.toString());
          assertNotNull(b);
          assertNotNull(b.getBlob());
          session.delete(b);

          assertNull(session.get(BlobTest1.class, foo.toString()));
        }
      });

    doInSession(new Action() {
      public void run(Session session) throws OtmException {
        assertNull(session.get(BlobTest1.class, foo.toString()));
      }
    });
  }

  @Test(dependsOnMethods={"testDelete"})
  public void testMultiTxnSession() throws OtmException {
    log.info("Testing multi-txn session on a blob ...");
    Session     session = factory.openSession();
    Transaction tx      = null;

    try {
      URI id = URI.create("blob:2");
      tx = session.beginTransaction(false, -1);
      BlobTest2 b = new BlobTest2();
      b.setId(id);
      b.setBlob(null);
      session.saveOrUpdate(b);
      assertNotNull(b.getBlob());
      try {
        b.write(data);
        assertEquals(data, b.read());
      } catch (IOException e) {
        throw new OtmException("Test failed.", e);
      }
      tx.commit();
      tx = session.beginTransaction(false, -1);
      try {
        assertEquals(data, b.read());
      } catch (IOException e) {
        throw new OtmException("Test failed.", e);
      }
      session.delete(b);
      tx.commit();
      tx = session.beginTransaction(false, -1);
      assertNull(session.get(BlobTest2.class, id.toString()));
      assertNotNull(b.getBlob());
      assertFalse(b.getBlob().exists());
      tx.commit();
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }

      throw e;
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
  }


  @UriPrefix("foo:")
  @Entity(graph="ri")
  public static class BlobTest1 {
    private URI id;
    private DataSource blob;

    public URI getId() {
      return id;
    }

    @Id
    public void setId(URI id) {
      this.id = id;
    }

    public DataSource getBlob() {
      return blob;
    }

    @Blob
    public void setBlob(DataSource blob) {
      this.blob = blob;
    }

    private void write(byte[] buf) throws IOException {
      OutputStream out = blob.getOutputStream();
      out.write(buf);
      out.close();
    }

    private byte[] read() throws IOException {
      InputStream in = blob.getInputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
      byte[] b = new byte[4096];
      int len;
      while ((len = in.read(b)) != -1)
        out.write(b, 0, len);

      in.close();
      out.close();

      return out.toByteArray();
    }
  }

  @UriPrefix("foo:")
  @Entity(graph="ri")
  public static class BlobTest2 {
    private URI id;
    private org.topazproject.otm.Blob blob;

    public URI getId() {
      return id;
    }

    @Id
    public void setId(URI id) {
      this.id = id;
    }

    public org.topazproject.otm.Blob getBlob() {
      return blob;
    }

    @Blob
    public void setBlob(org.topazproject.otm.Blob blob) {
      this.blob = blob;
    }

    private void write(byte[] buf) throws IOException {
      blob.writeAll(buf);
    }

    private byte[] read() throws IOException {
      return blob.readAll();
    }
  }

}
