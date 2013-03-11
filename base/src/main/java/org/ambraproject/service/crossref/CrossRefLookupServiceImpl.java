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
import com.google.gson.annotations.SerializedName;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    PostMethod post = createCrossRefPost(title, author, journal, volume, pages);
    Gson gson = new Gson();

    try {
      long timestamp = System.currentTimeMillis();
      int response = httpClient.executeMethod(post);

      log.debug("Http post finished in {} ms", System.currentTimeMillis() - timestamp);

      if (response == 200) {
        String json = post.getResponseBodyAsString();
        BufferedReader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
        CrossRefResponse res = gson.fromJson(reader, CrossRefResponse.class);

        //TODO: Build up list from CrossRef Response.  Just annotate CrossRefArticle?

        List<CrossRefArticle> results = new ArrayList<CrossRefArticle>();
        return results;
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

  @Override
  @Transactional(readOnly = true)
  public String findDoi(String title, String author, String journal, String volume, String pages) throws Exception {
    List<CrossRefArticle> articles = findArticles(title, author, journal, volume, pages);

    if(articles.size() > 0) {
      return articles.get(0).getDoi();
    } else {
      return null;
    }
  }

  private PostMethod createCrossRefPost(String title, String author, String journal, String volume, String pages)
  {
    StringBuilder builder = new StringBuilder();

    //["Young GC,Analytical methods in palaeobiogeography, and the role of early vertebrate studies;Palaeoworld;19;160-173"]

    builder.append("[\"")
      .append(author)
      .append(",")
      .append(title)
      .append(";")
      .append(journal)
      .append(";")
      .append(volume)
      .append(";")
      .append(pages);

    final String json = builder.toString();

    //TODO: Move URL to configuration?
    return new PostMethod("http://search.crossref.org/links") {{
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

  public class CrossRefResponse {
    public CrossRefResponse() {}

    public class CrossRefResult {
      public CrossRefResult() {}

      @SerializedName("text")
      public String text;
      @SerializedName("match")
      public Boolean match;
      @SerializedName("doi")
      public String doi;
      @SerializedName("score")
      public Long score;
    }

    @SerializedName("results")
    public CrossRefResult[] results;

    @SerializedName("query_ok")
    public Boolean queryOK;
  }
}


