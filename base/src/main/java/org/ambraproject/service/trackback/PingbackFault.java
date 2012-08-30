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

package org.ambraproject.service.trackback;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Fault codes to send to external servers as responses to pingbacks.
 */
public enum PingbackFault {

  // Specified for Pingback by http://www.hixie.ch/specs/pingback/pingback-1.0
  GENERIC(0, "Generic fault"),
  SOURCE_DNE(0x10, "The source URI does not exist"),
  NO_LINK_TO_TARGET(0x11, "The source URI does not contain a link to the target URI"),
  TARGET_DNE(0x20, "The specified target URI does not exist"),
  INVALID_TARGET(0x21, "The specified target URI cannot be used as a target"),
  ALREADY_REGISTERED(0x30, "The pingback has already been registered"),
  ACCESS_DENIED(0x31, "Access denied"),
  UPSTREAM(0x32, "Error from upstream server"),

  // Specified for XML-RPC by http://xmlrpc-epi.sourceforge.net/specs/rfc.fault_codes.php
  NOT_WELL_FORMED(-32700, "Parse error: Not well formed"),
  UNSUPPORTED_ENCODING(-32701, "Parse error: Unsupported encoding"),
  INVALID_CHAR(-32702, "Parse error: Invalid character for encoding"),
  INVALID_XMLRPC(-32600, "Server error: Invalid xml-rpc. Not conforming to spec."),
  METHOD_NOT_FOUND(-32601, "Server error: Requested method not found"),
  INVALID_PARAMS(-32602, "Server error: Invalid method parameters"),
  INTERNAL_ERROR(-32603, "Server error: Internal xml-rpc error"),
  APPLICATION_ERROR(-32500, "Application error"),
  SYSTEM_ERROR(-32400, "System error"),
  TRANSPORT_ERROR(-32300, "Transport error");

  private final int code;
  private final String message;

  private PingbackFault(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }


  /**
   * Construct an exception that will cause {@link org.apache.xmlrpc.webserver.XmlRpcServletServer} to return the code
   * for this fault in the HTTP response.
   *
   * @return the exception
   */
  public XmlRpcException getException() {
    return new XmlRpcException(code, message);
  }

  public XmlRpcException getException(String message) {
    return new XmlRpcException(code, message);
  }

  public XmlRpcException getException(Throwable cause) {
    return new XmlRpcException(code, message, cause);
  }

  public XmlRpcException getException(String message, Throwable cause) {
    return new XmlRpcException(code, message, cause);
  }

}
