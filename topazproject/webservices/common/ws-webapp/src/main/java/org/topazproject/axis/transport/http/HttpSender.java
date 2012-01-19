/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.axis.transport.http;

import java.net.URL;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.CommonsHTTPSender;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;

/**
 * A modified CommonsHTTPSender. Does pre-emptive authentication
 *
 * @author Pradeep Krishnan
 */
public class HttpSender extends CommonsHTTPSender {
  /*
   *
   * @see org.apache.axis.transport.http.CommonsHTTPSender
   */
  protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext context,
                                                   URL targetURL) {
    client.getParams().setAuthenticationPreemptive(true);

    return super.getHostConfiguration(client, context, targetURL);
  }
}
