/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
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

package org.ambraproject.action.trackback;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.trackback.PingbackFault;
import org.ambraproject.service.trackback.PingbackService;
import org.ambraproject.web.VirtualJournalContext;
import org.apache.struts2.ServletActionContext;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for incoming pingback requests.
 *
 * @author Ryan Skonnord
 */
public class CreatePingbackAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(CreatePingbackAction.class);

  protected static final String CONTENT_TYPE_HEADER = "content-type";
  protected static final String XML_CONTENT_TYPE = "text/xml";

  /**
   * The standard XML-RPC method name string for sending requests in the pingback protocol. Specified by <a
   * href="http://www.hixie.ch/specs/pingback/pingback-1.0">Pingback 1.0</a>
   */
  protected static final String PINGBACK_METHOD_NAME = "pingback.ping";

  private PingbackService pingbackService;

  @Required
  public void setPingbackService(PingbackService pingbackService) {
    this.pingbackService = pingbackService;
  }

  /**
   * Return a string to be the return value to a successful Pingback RPC. The <a href="http://www.hixie.ch/specs/pingback/pingback-1.0">Pingback
   * 1.0 specification</a> requires that, if a fault code is not returned, the RPC must return a single string
   * "containing as much information as the server deems useful. This string is only expected to be used for debugging
   * purposes."
   *
   * @param sourceUri
   * @return the string to send as an RPC return value
   */
  private static String makeSuccessMessage(URI sourceUri, URI targetUri) {
    return "Received " + CreatePingbackAction.PINGBACK_METHOD_NAME
        + "(\"" + sourceUri.toASCIIString() + "\", \"" + targetUri.toASCIIString() + "\")";
  }

  private static final String FAULT_LOG_FORMAT =
      "XML-RPC Fault: code=%d; message=\"%s\"; RPC method=\"%s\"; RPC parameters=%s";

  /**
   * Log the content of and response to a faulty pingback request.
   * <p/>
   * When the XML-RPC server library handles an {@link XmlRpcException}, it logs the stack trace, but otherwise the
   * system would not retain data about the faulty input from the client. So this method does. (This makes it an
   * exception to the "log or throw but not both" pattern: it logs data that would be discarded after the exception is
   * thrown.)
   *
   * @param fault   the exception encapsulating the fault message to send as an XML-RPC response
   * @param request the faulty request
   */
  private void logXmlRpcFault(XmlRpcException fault, XmlRpcRequest request) {
    if (log.isInfoEnabled()) {
      int parameterCount = request.getParameterCount();
      List<String> requestParameters = new ArrayList<String>(parameterCount);
      for (int i = 0; i < parameterCount; i++) {
        requestParameters.add(String.valueOf(request.getParameter(i)));
      }

      String logMessage = String.format(FAULT_LOG_FORMAT, fault.code, fault.getMessage(),
          request.getMethodName(), requestParameters);
      log.info(logMessage);
    }
  }

  /**
   * Log the content of and response to an XML-RPC request to an unsupported method name.
   *
   * @param fault      the exception encapsulating the fault message to send as an XML-RPC response
   * @param methodName the remote procedure name
   */
  private void logXmlRpcFault(XmlRpcException fault, String methodName) {
    if (log.isInfoEnabled()) {
      String logMessage = String.format(FAULT_LOG_FORMAT, fault.code, fault.getMessage(), methodName, null);
      log.info(logMessage);
    }
  }

  /**
   * Adapter object to make {@link XmlRpcServletServer} delegate to {@link PingbackService}.
   */
  private class PingbackHandler implements XmlRpcHandler {
    private final String pingbackServerHostname;

    private PingbackHandler(String pingbackServerHostname) {
      this.pingbackServerHostname = pingbackServerHostname;
    }

    @Override
    public Object execute(XmlRpcRequest request) throws XmlRpcException {
      try {
        return handleRequest(request);
      } catch (XmlRpcException fault) {
        logXmlRpcFault(fault, request);
        throw fault;
      }
    }

    /**
     * Receive an XML-RPC request and, if it's a valid pingback, store it.
     *
     * @param request the XML-RPC request
     * @return a response message
     * @throws XmlRpcException if the request is invalid or the pingback will not be stored
     */
    private String handleRequest(XmlRpcRequest request) throws XmlRpcException {
      int paramCount = request.getParameterCount();
      if (paramCount != 2) {
        throw PingbackFault.INVALID_PARAMS.getException();
      }
      String sourceUriStr;
      String targetUriStr;
      try {
        sourceUriStr = (String) request.getParameter(0);
        targetUriStr = (String) request.getParameter(1);
      } catch (ClassCastException e) {
        throw PingbackFault.INVALID_PARAMS.getException(e);
      }

      URI sourceUri;
      try {
        sourceUri = new URI(sourceUriStr);
      } catch (URISyntaxException e) {
        throw PingbackFault.SOURCE_DNE.getException(e);
      }

      URI targetUri;
      try {
        targetUri = new URI(targetUriStr);
      } catch (URISyntaxException e) {
        throw PingbackFault.TARGET_DNE.getException(e);
      }

      pingbackService.createPingback(sourceUri, targetUri, pingbackServerHostname);
      return makeSuccessMessage(sourceUri, targetUri);
    }
  }

  /**
   * @return a server object that, when it executes on an XML-RPC request, will translate to pingback parameters and
   *         delegate to {@link PingbackService}
   */
  private XmlRpcServletServer constructServer(final String pingbackServerHostname) {
    XmlRpcServletServer server = new XmlRpcServletServer();
    XmlRpcHandlerMapping mapping = new XmlRpcHandlerMapping() {
      @Override
      public XmlRpcHandler getHandler(String handlerName) throws XmlRpcException {
        if (!PINGBACK_METHOD_NAME.equals(handlerName)) {
          String message = "Method not found: \"" + handlerName + "\". The only supported RPC method is \""
              + PINGBACK_METHOD_NAME + "\".";
          XmlRpcException fault = PingbackFault.METHOD_NOT_FOUND.getException(message);
          logXmlRpcFault(fault, handlerName);
          throw fault;
        }
        return new PingbackHandler(pingbackServerHostname);
      }
    };
    server.setHandlerMapping(mapping);
    return server;
  }


  @Override
  public String execute() throws IOException, ServletException {
    HttpServletRequest request = ServletActionContext.getRequest();
    String contentType = request.getHeader(CONTENT_TYPE_HEADER);
    if (!XML_CONTENT_TYPE.equals(contentType)) {
      return ERROR;
    }

    VirtualJournalContext requestContent = (VirtualJournalContext) request.getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT);
    URL baseUrl = new URL(requestContent.getBaseUrl());
    String pingbackServerHostname = baseUrl.getHost();

    HttpServletResponse response = ServletActionContext.getResponse();
    constructServer(pingbackServerHostname).execute(request, response);

    // The XmlRpcServletServer has already written the response; return null so Struts won't try to
    return null;
  }

}
