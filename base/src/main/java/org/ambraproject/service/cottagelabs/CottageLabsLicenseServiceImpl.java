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

import com.google.gson.Gson;
import org.ambraproject.service.cottagelabs.json.Response;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @inheritDoc
 */
public class CottageLabsLicenseServiceImpl implements CottageLabsLicenseService {
  private static final Logger log = LoggerFactory.getLogger(CottageLabsLicenseServiceImpl.class);
  private HttpClient httpClient;
  private String cottageLabsURL;

  @Required
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Required
  public void setCottageLabsURL(String cottageLabsURL) {
    this.cottageLabsURL = cottageLabsURL;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Response findLicense(String doi) throws Exception {
    Gson gson = new Gson();
    GetMethod get = createGetMethod(doi);

    try {
      int response = httpClient.executeMethod(get);

      log.debug("Http get complete");

      if (response == 200) {
        String result = get.getResponseBodyAsString();
        if(result != null) {
          log.trace("JSON response received: {}", result);
          return gson.fromJson(result, Response.class);
        }
        log.error("Received empty response, response code {}, when executing query  {}", new Object[] {
          response, get.getURI().toString() } );
      } else {
        log.error("Received response code {} when executing query {}", new Object[] {
          response, get.getURI().toString() } );
      }
    } finally {
      get.releaseConnection();
    }

    return null;
  }

  private GetMethod createGetMethod(String doi) throws UnsupportedEncodingException {
    return new GetMethod(this.cottageLabsURL + "/" + URLEncoder.encode(doi, "UTF-8")) {{
      addRequestHeader("Content-Type","application/json");
    }};
  }
}
