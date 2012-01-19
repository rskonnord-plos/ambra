/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.topazproject.ws.pap.Profiles;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.ctx.Attribute;

import org.topazproject.ws.users.NoSuchUserIdException;

/**
 * The XACML PEP for profiles.
 *
 * @author Ronald Tschalär
 */
public abstract class ProfilesPEP extends AbstractSimplePEP implements Profiles.Permissions {
  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           GET_DISP_NAME,
                                                           GET_REAL_NAME,
                                                           GET_GIVEN_NAMES,
                                                           GET_SURNAMES,
                                                           GET_TITLE,
                                                           GET_GENDER,
                                                           GET_POSITION_TYPE,
                                                           GET_ORGANIZATION_NAME,
                                                           GET_ORGANIZATION_TYPE,
                                                           GET_POSTAL_ADDRESS,
                                                           GET_COUNTRY,
                                                           GET_CITY,
                                                           GET_EMAIL,
                                                           GET_HOME_PAGE,
                                                           GET_WEBLOG,
                                                           GET_BIOGRAPHY,
                                                           GET_INTERESTS,
                                                           GET_PUBLICATIONS,
                                                           GET_BIOGRAPHY_TEXT,
                                                           GET_INTERESTS_TEXT,
                                                           GET_RESEARCH_AREAS_TEXT,
                                                           SET_PROFILE,
                                                           FIND_USERS_BY_PROF,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = null;

  protected ProfilesPEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
