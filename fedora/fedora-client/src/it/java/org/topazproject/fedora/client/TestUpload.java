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

import java.net.URI;

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

}
