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

import org.topazproject.authentication.PasswordProtectedService;
import org.topazproject.authentication.ProtectedService;

import junit.framework.TestCase;

/**
 * Tests for APIM client stub.
 *
 * @author Pradeep Krishnan
 */
public class TestAPIM extends TestCase {
  private static String uri    = "http://localhost:9090/fedora/services/management";
  private static String uname  = "fedoraAdmin";
  private static String passwd = "fedoraAdmin";


  //
  private static final String FOXML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<foxml:digitalObject xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\">"
    + "<foxml:objectProperties>"
    + "<foxml:property NAME=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" VALUE=\"FedoraObject\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\"Test Object\"/>"
    + "<foxml:property NAME=\"info:fedora/fedora-system:def/model#contentModel\" VALUE=\"TEST\"/>"
    + "</foxml:objectProperties>" + "</foxml:digitalObject>";

  //
  private FedoraAPIM apim;

  /**
   * Sets up the tests. Creats the stub.
   *
   * @throws Exception on failure
   */
  public void setUp() throws Exception {
    ProtectedService svc = new ReAuthProtectedService(uri, uname, passwd);
    apim = APIMStubFactory.create(svc);
  }

  /**
   * Tests ingest/purge
   *
   * @throws Exception on failure
   */
  public void testIngest() throws Exception {
    String pid = apim.ingest(FOXML.getBytes("UTF-8"), "foxml1.0", "created");
    apim.purgeObject(pid, "deleted", false);
  }

  /**
   * Tests Id generation
   *
   * @throws Exception on failure
   */
  public void testGetNextPID() throws Exception {
    String pid = apim.getNextPID(new org.apache.axis.types.NonNegativeInteger("1"), "test")[0];
    assertTrue(pid.startsWith("test:"));
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
