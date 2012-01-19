/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.annotation.impl;

import java.io.IOException;

import java.util.Set;

import org.topazproject.ws.annotation.Replies;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Replies Web Service.
 *
 * @author Pradeep Krishnan
 */
public class RepliesPEP extends AbstractSimplePEP implements Replies.Permissions {
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

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected RepliesPEP(PDP pdp, Set subjAttrs)
                throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
