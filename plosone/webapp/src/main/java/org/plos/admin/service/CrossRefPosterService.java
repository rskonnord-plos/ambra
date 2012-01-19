/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

public class CrossRefPosterService {
  private String doiXrefUrl;


  public void init() {
  }

  public void setDoiXrefUrl(final String doiXrefUrl) {
    this.doiXrefUrl = doiXrefUrl;
  }

  public int post(File file) throws HttpException, IOException {
    PostMethod poster = new PostMethod(doiXrefUrl);
    HttpClient client = new HttpClient();

    Part[] parts = {new FilePart("fname", file.getName(), file)};

    poster.setRequestEntity(
        new MultipartRequestEntity(parts, poster.getParams())
    );
    client.getHttpConnectionManager().getParams().setConnectionTimeout(25000);
    return client.executeMethod(poster);
  }
}
