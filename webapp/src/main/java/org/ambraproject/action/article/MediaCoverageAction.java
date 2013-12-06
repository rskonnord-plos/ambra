package org.ambraproject.action.article;

import org.ambraproject.action.BaseActionSupport;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MediaCoverageAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(MediaCoverageAction.class);

  private String uri;
  private String link;
  private String comment;
  private String name;
  private String email;

  @Override
  public String execute() throws Exception {

    String status = ERROR;

    HttpClient httpClient = new DefaultHttpClient();

    String linkComment = this.name + ", " + this.email + " " + this.comment;

    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("doi", this.uri));
    params.add(new BasicNameValuePair("link", this.link));
    params.add(new BasicNameValuePair("comment", linkComment));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = configuration.getString("ambra.services.mediaCoverage.url", null);

    if (mediaCurationUrl != null) {
      HttpPost httpPost = new HttpPost(mediaCurationUrl);
      try {
        httpPost.setEntity(entity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        int statusCode = httpResponse.getStatusLine().getStatusCode();

        // check for status code
        if (statusCode == HttpStatus.SC_CREATED) {
          status = SUCCESS;
        }

      } catch(Exception e) {
        log.error("Failed to submit the link to media curation app", e);
      } finally {
        httpPost.releaseConnection();
      }
    }

    return status;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
