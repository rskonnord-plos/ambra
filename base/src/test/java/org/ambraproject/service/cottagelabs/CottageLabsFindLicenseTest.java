/*
 * Copyright (c) 2006-2013 by Public Library of Science
 *
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
package org.ambraproject.service.cottagelabs;

import org.ambraproject.service.cottagelabs.json.Response;
import org.apache.commons.httpclient.HttpClientMock;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import java.io.InputStream;
import java.io.StringWriter;

import static org.testng.Assert.*;

/**
 * Unit test for the cottage labs service
 */
public class CottageLabsFindLicenseTest {

  @Test
  public void testFindArticles() throws Exception {
    CottageLabsLicenseServiceImpl service = new CottageLabsLicenseServiceImpl();

    InputStream stream = this.getClass().getClassLoader().getResourceAsStream("cottagelabs/response.json");
    StringWriter writer = new StringWriter();
    IOUtils.copy(stream, writer, "UTF-8");
    String json = writer.toString();

    HttpClientMock mockHttpClient = new HttpClientMock(200, json);
    service.setHttpClient(mockHttpClient);
    service.setCottageLabsURL("http://bleh.bleh");
    Response response = service.findLicenses(new String[] { "Synopsis of the families of Vertebrata" });

    //Test that we're parsing the returned JSON correctly
    assertEquals(response.getResults().get(0).getLicense().get(0).getTitle(), "Creative Commons CCZero");

    assertEquals(response.getErrors().size(), 1);
    assertEquals(response.getErrors().get(0).getIdentifer().getId(), "10.1128/aem.70.11.6855-6864.2004");

    assertEquals(response.getProcessing().size(), 4);
    assertEquals(response.getProcessing().get(0).getIdentifier().getId(), "10.1007/978-94-017-3284-0_2");
    assertEquals(response.getProcessing().get(0).getIdentifier().getCanonical(), "doi:10.1007/978-94-017-3284-0_2");
    assertEquals(response.getProcessing().get(0).getIdentifier().getType(), "doi");
  }
}
