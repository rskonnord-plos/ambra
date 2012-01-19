/*
 * $HeadURL::                                                                            $ $Id:
 * PlosStreamResult.java 946 2006-11-03 22:23:42Z viru $
 * 
 * Copyright (c) 2006-2007 by Topaz, Inc. http://topazproject.org
 * 
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.struts2;

import org.apache.struts2.dispatcher.StreamResult;
import com.opensymphony.xwork2.ActionInvocation;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Custom webwork result class to stream back objects from Fedora. Takes appropriate http headers
 * and sets them the response stream as well as taking in an optional parameter indicating whether
 * to set the content-diposition to an attachment.
 */

public class PlosStreamResult extends StreamResult {
  private boolean isAttachment = false;

  private static final Log log = LogFactory.getLog(PlosStreamResult.class);

  protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
    InputStream oInput = null;
    OutputStream oOutput = null;

    try {
      // Find the inputstream from the invocation variable stack
      oInput = (InputStream) invocation.getStack().findValue(
          conditionalParse(this.inputName, invocation));

      if (oInput == null) {
        String msg = ("Can not find a java.io.InputStream with the name [" + this.inputName
            + "] in the invocation stack. " + "Check the <param name=\"inputName\"> tag specified for this action.");
        log.error(msg);
        throw new IllegalArgumentException(msg);
      }

      // Find the Response in context
      HttpServletResponse oResponse = (HttpServletResponse) invocation.getInvocationContext().get(
          HTTP_RESPONSE);

      // Set the content type
      oResponse.setContentType(getProperty("contentType", this.contentType, invocation));

      // Set the content length
      if (this.contentLength != null) {
        String _contentLength = conditionalParse(this.contentLength, invocation);
        int _contentLengthAsInt = -1;
        try {
          _contentLengthAsInt = Integer.parseInt(_contentLength);
          if (_contentLengthAsInt >= 0) {
            oResponse.setContentLength(_contentLengthAsInt);
          }
        } catch (NumberFormatException e) {
          log.warn("failed to recognize " + _contentLength
              + " as a number, contentLength header will not be set", e);
        }
      }

      // Set the content-disposition
      if (this.contentDisposition != null) {
        oResponse.addHeader("Content-disposition", (isAttachment ? "attachment; " : "")
            + getProperty("contentDisposition", this.contentDisposition, invocation));
      } else if (isAttachment) {
        oResponse.addHeader("Content-disposition", "attachment;");
      }

      // Get the outputstream
      oOutput = oResponse.getOutputStream();

      if (log.isDebugEnabled()) {
        log.debug("Streaming result [" + this.inputName + "] type=[" + this.contentType
            + "] length=[" + this.contentLength + "] content-disposition=["
            + this.contentDisposition + "]");
      }

      // Copy input to output
      log.debug("Streaming to output buffer +++ START +++");
      byte[] oBuff = new byte[this.bufferSize];
      int iSize;
      while (-1 != (iSize = oInput.read(oBuff))) {
        oOutput.write(oBuff, 0, iSize);
      }
      log.debug("Streaming to output buffer +++ END +++");

      // Flush
      oOutput.flush();

      oOutput.close();
      oOutput = null;

      oInput.close();
      oInput = null;
    } finally {
      try {
        if (oInput != null)
          oInput.close();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to close input stream", t);
      }
      try {
        if (oOutput != null)
          oOutput.close();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Failed to close output stream", t);
      }
    }
  }

  private String getProperty(final String propertyName, final String param,
      final ActionInvocation invocation) throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    final Object action = invocation.getAction();
    final String methodName = "get" + propertyName.substring(0, 1).toUpperCase()
        + propertyName.substring(1);
    final Method method = action.getClass().getMethod(methodName);
    final Object o = method.invoke(action);
    final String propertyValue = o.toString();
    if (null == propertyValue) {
      return conditionalParse(param, invocation);
    }

    return propertyValue;
  }

  /**
   * @return Returns the isAttachment.
   */
  public boolean isAttachment() {
    return isAttachment;
  }

  /**
   * If set to true, will add attachment to content disposition
   * 
   * @param isAttachment
   *          The isAttachment to set.
   */
  public void setIsAttachment(boolean isAttachment) {
    this.isAttachment = isAttachment;
  }
}
