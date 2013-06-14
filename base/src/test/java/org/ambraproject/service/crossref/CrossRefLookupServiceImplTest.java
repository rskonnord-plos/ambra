/*
 * Copyright (c) 2006-2013 by Public Library of Science
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

package org.ambraproject.service.crossref;

import org.apache.commons.httpclient.HttpClientMock;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Dragisa Krsmanovic
 * @author Joe Osowski
 */
public class CrossRefLookupServiceImplTest {

  @Test
  public void testFindArticles() throws Exception {
    CrossRefLookupServiceImpl service = new CrossRefLookupServiceImpl();

    HttpClientMock mockHttpClient = new HttpClientMock(200,
      "{ \"results\": [ { \"text\": \"Cope ED,Synopsis of the families of Vertebrata;American Naturalist;23;" +
        "849-887\", \"match\": true, \"doi\": \"10.1086/275018\", \"score\": 2.6771188 } ], \"query_ok\": true }");
    service.setHttpClient(mockHttpClient);
    service.setCrossRefUrl("http://bleh.bleh");

    String doi = service.findDoi("Synopsis of the families of Vertebrata");
    assertEquals(doi, "10.1086/275018");
  }

  @Test
  public void testPunctuationCharacters() throws Exception {
    CrossRefLookupServiceImpl service = new CrossRefLookupServiceImpl();

    HttpClientMock mockHttpClient = new HttpClientMock(200,
      "{ \"results\": [ {  \"text\": \"Young GC,Placoderms (armored fish): dominant vertebrates of the " +
        "Devonian Period;Annual Review of Earth and Planetary Sciences;38;523-550\", \"match\": true, \"doi\": " +
        "\"10.1146/annurev-earth-040809-152507\", \"score\": \"4.767027\" } ], \"query_ok\": true }");
    service.setHttpClient(mockHttpClient);
    service.setCrossRefUrl("http://bleh.bleh");

    String doi = service.findDoi("Proc; Natl/ Acad? Sci: USA & Canada\n a = b");
    assertEquals(doi, "10.1146/annurev-earth-040809-152507");
  }
}