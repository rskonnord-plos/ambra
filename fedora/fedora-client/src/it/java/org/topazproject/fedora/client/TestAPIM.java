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
    apim = APIMStubFactory.create(uri, uname, passwd);
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

}
