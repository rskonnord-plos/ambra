
/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 *    http://plos.org
 *    http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.amendment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ambraproject.views.ArticleAmendment;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AmendmentServiceImpl implements AmendmentService {

  private static final Logger LOG = LoggerFactory.getLogger(AmendmentServiceImpl.class);

  private String ambraServer;

  @Override
  public List<ArticleAmendment> fetchAmendmentsFromAmbra(String articleDoi) {
    String json = "";
    if (!articleDoi.startsWith("info:doi/"))
      articleDoi = "info:doi/" + articleDoi;

    try {
      URL url = new URL(ambraServer + "/article/amendments?articleURI=" + URLEncoder.encode(articleDoi, "UTF-8"));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.connect();
      InputStream in = conn.getInputStream();
      try {
        json = IOUtils.toString(in).trim();
      } finally {
        in.close();
      }
    } catch (IOException e) {
      LOG.error("Failed to fetch amendments from ambra", e);
    }
    return parseJsonFromAmbra(json);
  }

  public List<ArticleAmendment> parseJsonFromAmbra(String json) {
    List<ArticleAmendment>  amendments = new ArrayList<ArticleAmendment>();
    // uncomment the json received from the ambra action
    if (json.startsWith("/*")) {
      json = json.substring(2);
    }
    if (json.endsWith("*/")) {
      json = json.substring(0, json.length() - 2);
    }

    JsonParser parser = new JsonParser();
    JsonObject obj = parser.parse(json).getAsJsonObject();
    JsonArray amendmentArr = obj.getAsJsonArray("amendments");
    for (JsonElement element : amendmentArr) {
      JsonObject amendment = element.getAsJsonObject();
      amendments.add(ArticleAmendment
              .builder()
              .setParentArticleURI(amendment.get("parentArticleURI").getAsString())
              .setOtherArticleDoi(amendment.get("otherArticleDoi").getAsString())
              .setRelationshipType(amendment.get("relationshipType").getAsString())
              .build());
    }
    return amendments;
  }

  public void setAmbraServer(String ambraServer) {
    this.ambraServer = ambraServer;
  }
}
