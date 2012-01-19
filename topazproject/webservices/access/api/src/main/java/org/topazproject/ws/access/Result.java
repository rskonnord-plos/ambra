/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.access;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The result of acces evaluate. No obligations are passed in the result. This may change in the
 * future.
 *
 * @author Pradeep Krishnan
 */
public class Result {
  public static final int    DECISION_DENY            = 1;
  public static final int    DECISION_INDETERMINATE   = 2;
  public static final int    DECISION_NOT_APPLICABLE  = 3;
  public static final int    DECISION_PERMIT          = 0;
  public static final String STATUS_MISSING_ATTRIBUTE =
    "urn:oasis:names:tc:xacml:1.0:status:missing-attribute";
  public static final String STATUS_OK               = "urn:oasis:names:tc:xacml:1.0:status:ok";
  public static final String STATUS_PROCESSING_ERROR =
    "urn:oasis:names:tc:xacml:1.0:status:processing-error";
  public static final String STATUS_SYNTAX_ERROR =
    "urn:oasis:names:tc:xacml:1.0:status:syntax-error";
  private int                decision;
  private String             resource;
  private String             code;
  private String             message;

  /**
   * Creates a new Result object.
   *
   * @param decision the access control decision
   * @param resource the resource in question
   * @param code the status code in case the decision is not a permit or deny
   * @param message additional message in case the decision is not a permit or deny
   */
  public Result(int decision, String resource, String code, String message) {
    this.decision   = decision;
    this.resource   = resource;
    this.code       = code;
    this.message    = message;
  }

  /**
   * Creates a new Result object.
   */
  public Result() {
    this(DECISION_INDETERMINATE, null, null, null);
  }

  /**
   * Get decision.
   *
   * @return decision as int.
   */
  public int getDecision() {
    return decision;
  }

  /**
   * Set decision.
   *
   * @param decision the value to set.
   */
  public void setDecision(int decision) {
    this.decision = decision;
  }

  /**
   * Get resource.
   *
   * @return resource as String.
   */
  public String getResource() {
    return resource;
  }

  /**
   * Set resource.
   *
   * @param resource the value to set.
   */
  public void setResource(String resource) {
    this.resource = resource;
  }

  /**
   * Get code.
   *
   * @return code as String.
   */
  public String getCode() {
    return code;
  }

  /**
   * Set code.
   *
   * @param code the value to set.
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Get message.
   *
   * @return message as String.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set message.
   *
   * @param message the value to set.
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
