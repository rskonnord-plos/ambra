/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

import java.io.IOException;

import java.util.Set;

import org.plos.xacml.XacmlUtil;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Replies Web Service.
 *
 * @author Pradeep Krishnan
 */
public class RepliesPEP extends AbstractSimplePEP {
  /**
   * The action that represents a createReply operation in XACML policies.
   */
  public static final String CREATE_REPLY = "replies:createReply";

  /**
   * The action that represents a deleteReply operation in XACML policies.
   */
  public static final String DELETE_REPLY = "replies:deleteReply";

  /**
   * The action that represents a getReply operation in XACML policies.
   */
  public static final String GET_REPLY_INFO = "replies:getReplyInfo";

  /**
   * The action that represents a listReplies operation in XACML policies.
   */
  public static final String LIST_REPLIES = "replies:listReplies";

  /**
   * The action that represents a listAllReplies operation in XACML policies.
   */
  public static final String LIST_ALL_REPLIES = "replies:listAllReplies";

  /**
   * The action that represents a listReplies operation in XACML policies. Note that this
   * permission is checked against the base uri of annotations.
   */
  public static final String LIST_REPLIES_IN_STATE = "replies:listRepliesInState";

  /**
   * The action that represents a setReplyState operation in XACML policies.
   */
  public static final String SET_REPLY_STATE = "replies:setReplyState";

  /**
   * The list of all supported actions
   */
  public static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   CREATE_REPLY, DELETE_REPLY, GET_REPLY_INFO, LIST_REPLIES, LIST_ALL_REPLIES,
                   LIST_REPLIES_IN_STATE, SET_REPLY_STATE
    };

  /**
   * The list of all supported obligations
   */
  public static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null, null };

  static {
    init(RepliesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  public RepliesPEP() throws IOException, ParsingException, UnknownIdentifierException {
    this(XacmlUtil.lookupPDP("topaz.replies.pdpName"), XacmlUtil.createSubjAttrs());
  }

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected RepliesPEP(PDP pdp, Set subjAttrs)
                throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
