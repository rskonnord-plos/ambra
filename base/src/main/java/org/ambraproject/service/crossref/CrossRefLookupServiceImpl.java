/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Query crossref for article details
 *
 * @author Joe Osowski
 */
public class CrossRefLookupServiceImpl implements CrossRefLookupService {

  private static final Logger log = LoggerFactory.getLogger(CrossRefLookupServiceImpl.class);

  private String crossRefUrl;
  private HttpClient httpClient;

  @Required
  public void setHttpClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Required
  public void setCrossRefUrl(String crossRefUrl) {
    this.crossRefUrl = crossRefUrl;
  }

  /**
   * Do the author query as described in: <a href="http://www.crossref.org/help/Content/04_Queries_and_retrieving/author_title_query.htm">Author / article-title query</a>
   *
   * @param title  Article title
   * @param author Author name
   * @return List of articles that match the criteria
   * @see CrossRefArticle
   */
  public List<CrossRefArticle> findArticles(String title, String author, String journal, String volume, String pages) throws Exception {
    CrossRefResponse response = queryCrossRef(title, author, journal, volume, pages);

    //TODO: I don't believe this method is used any longer, remove it
    //It is called in one place, and that method is not called anyplace I can find
    List<CrossRefArticle> results = new ArrayList<CrossRefArticle>();

    for(final CrossRefResult res : response.results) {
      CrossRefArticle article = new CrossRefArticle();

      //Split up text string into various parts
      String[] data = res.text.split(";|,");

      //Basic protection against an incomplete response, not sure if this
      //ever actually happens
      article.setFirstAuthor((data.length > 0)?data[0]:null);
      article.setSerTitle((data.length > 1)?data[1]:null);
      article.setTitle((data.length > 2)?data[2]:null);
      article.setVolume((data.length > 3)?data[3]:null);
      article.setPage((data.length > 4)?data[4]:null);
      article.setDoi(res.doi);

      results.add(article);
    }

    return results;
   }

  @Override
  @Transactional(readOnly = true)
  public String findDoi(String title, String author, String journal, String volume, String pages) throws Exception {
    CrossRefResponse response = queryCrossRef(title, author, journal, volume, pages);

    if(response != null && response.results.length > 0) {
      return response.results[0].doi;
    } else {
      return null;
    }
  }

  private CrossRefResponse queryCrossRef(String title, String author, String journal, String volume, String pages)
  {
    PostMethod post = createCrossRefPost(title, author, journal, volume, pages);

    try {
      long timestamp = System.currentTimeMillis();
      int response = httpClient.executeMethod(post);

      log.debug("Http post finished in {} ms", System.currentTimeMillis() - timestamp);

      if (response == 200) {
        String result = post.getResponseBodyAsString();
        if(result != null) {
          log.trace("JSON response received: {}", result);
          return parseJSON(result);
        }
        log.error("Received empty response, response code {}, when executing query  {}", response, crossRefUrl);
      } else {
        log.error("Received response code {} when executing query {}", response, crossRefUrl);
      }
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    } finally {
      // be sure the connection is released back to the connection manager
      post.releaseConnection();
    }
    return null;
  }

  /**
   * Parse the JSON into native types
   *
   * @param json the JSON string to convert to a java native type
   *
   * @return a CrossRefResponse object
   */
  private CrossRefResponse parseJSON(final String json) {
    return new CrossRefResponse() {{
      JsonParser parser = new JsonParser();
      JsonObject responseObject = parser.parse(json).getAsJsonObject();

      queryOK = (responseObject.getAsJsonPrimitive("query_ok")).getAsBoolean();

      List<CrossRefResult> resultTemp = new ArrayList<CrossRefResult>();

      for(final JsonElement resultElement : responseObject.getAsJsonArray("results")) {
        JsonObject resultObj = resultElement.getAsJsonObject();
        CrossRefResult res = new CrossRefResult();

        if(resultObj.getAsJsonPrimitive("text") != null) {
          res.text = resultObj.getAsJsonPrimitive("text").getAsString();
        }

        if(resultObj.getAsJsonPrimitive("match") != null) {
          res.match = resultObj.getAsJsonPrimitive("match").getAsBoolean();
        }

        if(resultObj.getAsJsonPrimitive("doi") != null) {
          res.doi = resultObj.getAsJsonPrimitive("doi").getAsString();
        }

        if(resultObj.getAsJsonPrimitive("score") != null) {
          res.score = resultObj.getAsJsonPrimitive("score").getAsLong();
        }

        //Some results aren't actually valid
        if(res.doi != null) {
          resultTemp.add(res);
        }
      }

      this.results = resultTemp.toArray(new CrossRefResult[resultTemp.size()]);
    }};
  }

  private PostMethod createCrossRefPost(String title, String author, String journal, String volume, String pages)
  {
    StringBuilder builder = new StringBuilder();

    //Example query to post:
    //["Young GC,Analytical methods in palaeobiogeography, and the role of early vertebrate studies;Palaeoworld;19;160-173"]

    builder.append(author)
      .append(",")
      .append(title)
      .append(";")
      .append(journal)
      .append(";")
      .append(volume)
      .append(";")
      .append(pages);

    //Use toJSON to encode strings with proper escaping
    final String json = "[" + (new Gson()).toJson(builder.toString()) + "]";

    if(this.crossRefUrl == null) {
      throw new RuntimeException("ambra.services.crossref.query.url value not found in configuration.");
    }

    return new PostMethod(this.crossRefUrl) {{
      addRequestHeader("Content-Type","application/json");
      setRequestEntity(new RequestEntity() {
        @Override
        public boolean isRepeatable() {
          return false;
        }

        @Override
        public void writeRequest(OutputStream outputStream) throws IOException {
          outputStream.write(json.getBytes());
        }

        @Override
        public long getContentLength() {
          return json.getBytes().length;
        }

        @Override
        public String getContentType() {
          return "application/json";
        }
      });
    }};
  }

  /* utility class for internally tracking data */
  private class CrossRefResult {
    public String text;
    public Boolean match;
    public String doi;
    public Long score;
  }

  /* utility class for internally tracking data */
  private class CrossRefResponse {
    public CrossRefResult[] results;
    public Boolean queryOK;
  }
}


