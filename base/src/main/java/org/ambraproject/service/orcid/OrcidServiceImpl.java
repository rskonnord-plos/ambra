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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.ambraproject.views.OrcidAuthorization;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;

/**
 * {@inheritDoc}
 */
public class OrcidServiceImpl implements OrcidService {
  private static final Logger log = LoggerFactory.getLogger(OrcidServiceImpl.class);

  private String tokenEndPoint;
  private HttpClient httpClient;
  private String clientID;
  private String clientSecret;
  private String redirectURL;

  /**
   * This sets the access level to request of ORCiD
   *
   * http://support.orcid.org/knowledgebase/articles/120162-orcid-scopes
   *
   * We'll only want to READ data so I've chosen: "/orcid-profile/read-limited"
   *
   */
  public static String API_SCOPE = "/orcid-profile/read-limited";

  private Gson gson;

  public OrcidServiceImpl()
  {
    /**
     * This never changes, lets only create it once.
     */
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(OrcidAuthorization.class, new JsonDeserializer<OrcidAuthorization>() {
      @Override
      public OrcidAuthorization deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = (JsonObject)jsonElement;

        return OrcidAuthorization.builder()
          .setAccessToken(json.get("access_token").getAsString())
          .setTokenType(json.get("token_type").getAsString())
          .setRefreshToken(json.get("refresh_token").getAsString())
          .setExpiresIn(json.get("expires_in").getAsInt())
          .setScope(json.get("scope").getAsString())
          .setOrcid(json.get("orcid").getAsString())
          .build();
      }
    });

    gson = gsonBuilder.create();
  }

  /**
   * {@inheritDoc}
   */
  public OrcidAuthorization authorizeUser(String authorizationCode) throws Exception
  {
    //Currently we authorize the account, but all we really want is the ORCiD
    //which comes down along with the authorization token, so we
    //never actually have to use the returned accessToken.  However
    //I am capturing it for future use.

    PostMethod post = createOrcIDAccessTokenQuery(authorizationCode);

    try {
     long timestamp = System.currentTimeMillis();
      int response = httpClient.executeMethod(post);

      log.debug("Http post finished in {} ms", System.currentTimeMillis() - timestamp);

      //The error handling here is a bit weird

      //OrcID will return a 500 error if an invalid token is present
      //It will also return this code if the application throws an exception on their end
      //The only way to seemingly disambiguate would be to parse the response text if we
      //want to improve on this later
      //For now, I log the error and return null.

      //OrcID will return 401 when an invalid client-id or client-secret is present
      //OrcID returns 200 on success

      if (response == 200) {
        String result = post.getResponseBodyAsString();
        if(result != null) {

          /** Example response:
           {
             "access_token": "8056b6d5-f96d-42c8-8df9-41f70908ad33",
             "token_type": "bearer",
             "refresh_token": "d8c72880-00ac-4447-a2af-e90f1673f4bc",
             "expires_in": 631138286,
             "scope": "/orcid-profile/read-limited",
             "orcid": "0000-0003-4954-7894"
           }*/
          log.trace("Response received: {}", result);

          return gson.fromJson(result, OrcidAuthorization.class);
        }

        log.error("Received empty response, response code {}, when executing query  {}", response, post.getURI().toString());
        throw new Exception("Received empty response from ORCiD");

      } else if (response == 401) {
        throw new Exception("Invalid client ID or secret not defined for ORCiD, check your ambra configuration");
      } else {
        log.error("Received response code {} when executing query {}", response, post.getURI().toString());
        log.error("Received response body: {}", post.getResponseBodyAsString());
        return null;
      }
    } finally {
      // be sure the connection is released back to the connection manager
      post.releaseConnection();
    }
  }


  private PostMethod createOrcIDAccessTokenQuery(String authorizationCode) throws UnsupportedEncodingException {
    final String query = "code=" + authorizationCode +
      "&redirect_uri=" + URLEncoder.encode(this.redirectURL, "UTF-8") +
      "&client_id=" + this.clientID +
      "&scope=" + URLEncoder.encode(API_SCOPE, "UTF-8") +
      "&client_secret=" + this.clientSecret +
      "&grant_type=authorization_code";

    return new PostMethod(this.tokenEndPoint) {{
      setRequestEntity(new RequestEntity() {
        @Override
        public boolean isRepeatable() {
          return false;
        }

        @Override
        public void writeRequest(OutputStream outputStream) throws IOException {
          outputStream.write(query.getBytes());
        }

        @Override
        public long getContentLength() {
          return query.getBytes().length;
        }

        @Override
        public String getContentType() {
          return "application/x-www-form-urlencoded";
        }
      });
    }};
  }

  public void setTokenEndPoint(String tokenEndPoint) {
    this.tokenEndPoint = tokenEndPoint;
  }

  @Required
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public void setClientID(String clientID) {
    this.clientID = clientID;
  }

  public void setRedirectURL(String redirectURL) {
    this.redirectURL = redirectURL;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  /**
   * {@inheritDoc}
   */
  public String getClientID() {
    return clientID;
  }

  /**
   * {@inheritDoc}
   */
  public String getScope() {
    return API_SCOPE;
  }
}
