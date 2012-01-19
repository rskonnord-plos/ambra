/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.fedora.client;

import java.io.ByteArrayInputStream;

import java.net.URI;

import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.ProtectedService;

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
    ProtectedService svc      = new ReAuthProtectedService(uri, uname, passwd);
    uploader = new Uploader(svc);
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


  private static class ReAuthProtectedService extends PasswordProtectedService {
    boolean reload = true;

    public ReAuthProtectedService(String uri, String uname, String pswd) {
      super(uri, uname, pswd);
    }

    public String getPassword() {
      if (reload)
        return "";

      return super.getPassword();
    }

    public boolean hasRenewableCredentials() {
      return reload;
    }

    public boolean renew() {
      boolean status = reload;
      reload = false;

      return status;
    }
  }
}
