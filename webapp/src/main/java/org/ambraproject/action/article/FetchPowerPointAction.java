/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.action.article;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.article.ArticleAssetService;
import org.ambraproject.service.article.NoSuchArticleIdException;
import org.springframework.beans.factory.annotation.Required;
import java.io.IOException;
import java.io.InputStream;

public class FetchPowerPointAction extends BaseActionSupport {

  private static final String CONTENT_TYPE = "application/vnd.ms-powerpoint";
  private ArticleAssetService articleAssetService;
  
  private String uri;
  
  private InputStream inputStream;
  private long contentLength;
  private String contentDisposition;
  
  @Override
  public String execute() throws NoSuchArticleIdException, IOException {
    try {
        contentDisposition = "filename=\"" + uri.replaceFirst("info:doi/", "") + ".ppt\"";
        inputStream = articleAssetService.getPowerPointSlide(uri, getAuthId());
        contentLength = (long) inputStream.available();
      } catch (Exception e) {
        return ERROR;
      }
    return SUCCESS;
  }
  
  @Required
  public void setArticleAssetService(ArticleAssetService articleAssetService) {
    this.articleAssetService = articleAssetService;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public InputStream getInputStream() {
    return inputStream;
  }
  
  public String getContentType() {
    return CONTENT_TYPE;
  }
  
  public long getContentLength() {
    return contentLength;
  }
  
  public String getContentDisposition() {
    return contentDisposition;
  }
}
