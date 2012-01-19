/* $HeadURL::                                                                            $
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
package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import java.net.URI;
import java.util.concurrent.SynchronousQueue;

import junit.framework.TestCase;

/**
 * Test upload to Fedora.
 *
 * @author Pradeep Krishnan
 */
public class TestUpload extends TestCase {
  private static String uri    = "http://localhost:9090/fedora/management/upload";
  private static String uname  = "fedoraAdmin";
  private static String passwd = "fedoraAdmin";

  //
  private Uploader                uploader;

  /**
   * Sets up the tests. Creats the stub.
   *
   * @throws Exception on failure
   */
  public void setUp() throws Exception {
    uploader = new Uploader(uri, uname, passwd);
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadBytes() throws Exception {
    String s = uploader.upload(new byte[100000]);
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadStream() throws Exception {
    String s = uploader.upload(new ByteArrayInputStream(new byte[100000]));
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }

  /**
   * Tests upload
   *
   * @throws Exception on failure
   */
  public void testUploadFixedStream() throws Exception {
    String s = uploader.upload(new ByteArrayInputStream(new byte[100000]), 100000);
    URI    u = new URI(s);
    assertTrue(u.isAbsolute());
  }

  /**
   * Tests download.
   *
   * @throws Exception on failure
   */
  public void testDownload() throws Exception {
    for (String str : new String[]{"Some random string", "Yet another one"}) {
      byte[] in = str.getBytes();
      String ref = uploader.upload(in);
      byte[] out = new byte[in.length + 1];

      assertEquals(in.length, uploader.download(ref).read(out));
      assertEquals(str, new String(out, 0, in.length));
    }

    byte[] in = new byte[20000];
    String ref = uploader.upload(in);
    byte[] out = new byte[in.length];

    assertEquals(in.length, uploader.download(ref).read(out));
    for (int i = 0; i < in.length; i++)
      assertEquals("i=" + i, in[i], out[i]);
  }

  /**
   * Tests upload via output stream.
   *
   * @throws Exception on failure
   */
  public void testUploaderStream() throws Exception {
    final SynchronousQueue<Object> exchange = new SynchronousQueue<Object>();
    byte[] in = new byte[40000];

    OutputStream out = uploader.getOutputStream(new Uploader.UploadListener() {

      public void uploadComplete(String reference) {
        try {
          exchange.put(reference);
        } catch (InterruptedException e) {
          throw new Error("Interrupted during notify", e);
        }
      }

      public void uploadComplete(Throwable err) {
        try {
          exchange.put(err);
        } catch (InterruptedException e) {
          throw new Error("Interrupted during notify", err);
        }
      }
    }, -1);

    try {
      out.write(in);
    } finally {
      out.close();
    }

    Object result = exchange.take();

    if (result instanceof Error)
      throw (Error)result;

    if (result instanceof Exception)
      throw (Exception)result;

    if (!(result instanceof String))
      throw new Exception("Unexpected result " + result);

    String ref = (String) result;
    byte[] b = new byte[in.length];
    assertEquals(in.length, uploader.download(ref).read(b));
    for (int i = 0; i < in.length; i++)
      assertEquals("i=" + i, in[i], b[i]);
  }
}