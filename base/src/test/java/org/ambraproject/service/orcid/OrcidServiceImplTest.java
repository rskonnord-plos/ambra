/*
 * Copyright (c) 2007-2014 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.orcid;

import org.ambraproject.views.OrcidAuthorization;
import org.apache.commons.httpclient.HttpClientMock;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for the ORCiD service
 */
public class OrcidServiceImplTest {

  /* Test deserialization of authorize User */
  @Test
  public void authorizeUser() throws Exception {
    OrcidServiceImpl service = new OrcidServiceImpl();

    HttpClientMock mockHttpClient = new HttpClientMock(200,
      "{\"access_token\":\"8056b6d5-f96d-42c8-8df9-41f70908ad33\",\"token_type\":\"bearer\"," +
        "\"refresh_token\":\"d8c72880-00ac-4447-a2af-e90f1673f4bc\",\"expires_in\":631135971," +
        "\"scope\":\"/orcid-profile/read-limited\",\"orcid\":\"0000-0003-4954-7894\"}");

    service.setHttpClient(mockHttpClient);
    service.setClientID("bleh");
    service.setClientSecret("bleh");
    service.setTokenEndPoint("http://bleh.org");

    OrcidAuthorization orcidAuth = service.authorizeUser("blehcode");

    assertEquals(orcidAuth.getAccessToken(), "8056b6d5-f96d-42c8-8df9-41f70908ad33");
    assertEquals(orcidAuth.getTokenType(), "bearer");
    assertEquals(orcidAuth.getExpiresIn(), 631135971);
    assertEquals(orcidAuth.getRefreshToken(), "d8c72880-00ac-4447-a2af-e90f1673f4bc");
    assertEquals(orcidAuth.getOrcid(), "0000-0003-4954-7894");
    assertEquals(orcidAuth.getScope(), "/orcid-profile/read-limited");
  }

  /**
   * Test authorize failed
   *
   * @throws Exception
   */
  @Test
  public void badAuthCode() throws Exception {
    OrcidServiceImpl service = new OrcidServiceImpl();

    //Seems odd, but their API returns a 500 on a bad authCode
    HttpClientMock mockHttpClient = new HttpClientMock(500,
      "{\"message-version\":\"1.2_rc3\",\"error-desc\":{\"value\":\"Invalid authorization code: tzwP6s\"}");

    service.setHttpClient(mockHttpClient);
    service.setClientID("bleh");
    service.setClientSecret("bleh");
    service.setTokenEndPoint("http://bleh.org");

    OrcidAuthorization orcidAuth = service.authorizeUser("blehcode");
    assertNull(orcidAuth);
  }

  /**
   * Test bad credentials (invalid client code)
   *
   * @throws Exception
   */
  @Test(expectedExceptions = Exception.class)
  public void badClientCode() throws Exception {
    OrcidServiceImpl service = new OrcidServiceImpl();

    HttpClientMock mockHttpClient = new HttpClientMock(401,
      "{\"error\":\"invalid_client\",\"error_description\":\"Client not found: 0000-0002-6644-955\"}");

    service.setHttpClient(mockHttpClient);
    service.setClientID("bleh");
    service.setClientSecret("bleh");
    service.setTokenEndPoint("http://bleh.org");

    service.authorizeUser("blehcode");
  }


  /**
   * Test bad credentials (invalid secret)
   *
   * @throws Exception
   */
  @Test(expectedExceptions = Exception.class)
  public void badCredentials() throws Exception {
    OrcidServiceImpl service = new OrcidServiceImpl();

    HttpClientMock mockHttpClient = new HttpClientMock(401,
      "{\"error\":\"invalid_client\",\"error_description\":\"Bad client credentials\"}");

    service.setHttpClient(mockHttpClient);
    service.setClientID("bleh");
    service.setClientSecret("bleh");
    service.setTokenEndPoint("http://bleh.org");

    service.authorizeUser("blehcode");
  }
}
