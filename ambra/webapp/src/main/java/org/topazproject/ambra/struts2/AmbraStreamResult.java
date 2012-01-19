/*
 * $HeadURL::                                                                            $ $Id:
 * AmbraStreamResult.java 946 2006-11-03 22:23:42Z viru $
 *
 * Copyright (c) 2006-2007 by Topaz, Inc. http://topazproject.org
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
package org.topazproject.ambra.struts2;

import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts2.dispatcher.StreamResult;

import org.topazproject.ambra.web.HttpResourceServer;

import com.opensymphony.xwork2.ActionInvocation;

/**
 * Custom webwork result class to stream back objects from OTM blobs. Takes appropriate http
 * headers and sets them the response stream as well as taking in an optional parameter indicating
 * whether to set the content-diposition to an attachment.
 *
 * Reading inputStream requires a transaction. That can be acomplished if action implements
 * TransactionAware interface.
 * 
 * @see Span
 */
public class AmbraStreamResult extends StreamResult {

  private boolean isAttachment = false;
  private static final Log log = LogFactory.getLog(AmbraStreamResult.class);
  private HttpResourceServer server = new HttpResourceServer();

  /*
   * inherited javadoc
   */
  protected void doExecute(String finalLocation, ActionInvocation invocation)
      throws Exception {

    final InputStream inputStream = (InputStream) invocation.getStack().findValue("inputStream");
    Date date = (Date) invocation.getStack().findValue("lastModified");

    HttpResourceServer.Resource resource;
    long lastModified = (date == null) ? System.currentTimeMillis() : date.getTime();

    if (log.isDebugEnabled()) {
      log.debug("LastModified "+new Date(lastModified));
    }


    HttpServletResponse oResponse    =
      (HttpServletResponse) invocation.getInvocationContext().get(HTTP_RESPONSE);

    String name = "--unnamed--";
    // Set the content-disposition
    if (this.contentDisposition != null) {
      name = getProperty("contentDisposition", this.contentDisposition, invocation);
      oResponse.addHeader("Content-disposition", (isAttachment ? "attachment; " : "") + name);
    } else if (isAttachment) {
      oResponse.addHeader("Content-disposition", "attachment;");
    }

    String contentType = getProperty("contentType", this.contentType, invocation);

    if (inputStream == null) {

      final byte[] objRep = (byte[]) invocation.getStack().findValue("inputByteArray");

      if (objRep == null)
        throw new IllegalArgumentException("'inputByteArray' must be set in '"
            + invocation.getAction().getClass());

      log.debug("Received byte array");

      resource = new HttpResourceServer.Resource(name, contentType, objRep.length, lastModified) {
        public byte[] getContent() {
          return objRep;
        }

        public InputStream streamContent() throws IOException {
          return null;
        }
      };

    } else {


      Long contentLength = (Long) invocation.getStack().findValue("contentLength");
      if (contentLength == null)
        throw new IllegalArgumentException("'contentLength' must be set in '"
            + invocation.getAction().getClass());

      if (log.isDebugEnabled())
        log.debug("Received InputStream of length="+contentLength);

      resource = new HttpResourceServer.Resource(name, contentType, contentLength, lastModified) {
        public byte[] getContent() {
          return null;
        }

        public InputStream streamContent() throws IOException {
          return inputStream;
        }
      };
    }

    HttpServletRequest  oRequest     =
      (HttpServletRequest) invocation.getInvocationContext().get(HTTP_REQUEST);

    server.serveResource(oRequest, oResponse,
        resource);
  }

  private String getProperty(final String propertyName, final String param,
                             final ActionInvocation invocation)
                      throws NoSuchMethodException, IllegalAccessException,
                             InvocationTargetException {
    final Object action        = invocation.getAction();
    final String methodName    =
      "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    final Method method        = action.getClass().getMethod(methodName);
    final Object o             = method.invoke(action);
    final String propertyValue = o.toString();

    if (null == propertyValue) {
      return conditionalParse(param, invocation);
    }

    return propertyValue;
  }

  /**
   * Tests if the content disposition-type is "attachment".
   *
   * @return Returns the isAttachment.
   */
  public boolean isAttachment() {
    return isAttachment;
  }

  /**
   * If set to true, will add attachment to content disposition
   *
   * @param isAttachment The isAttachment to set.
   */
  public void setIsAttachment(boolean isAttachment) {
    this.isAttachment = isAttachment;
  }
}
