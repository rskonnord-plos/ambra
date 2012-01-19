/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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

package org.plos.rating.service;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.plos.xacml.AbstractSimplePEP;
import org.plos.xacml.Util;
import org.plos.xacml.XacmlUtil;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.ctx.Attribute;

/**
 * The XACML PEP for ratings.
 *
 * @author Ronald Tschalär
 */
public class RatingsPEP extends AbstractSimplePEP {
  /** The action that represents the delete-ratings operation in XACML policies. */
  public static final String DELETE_RATINGS = "ratings:deleteRatings";
  /** The action that represents the set-ratings operation in XACML policies. */
  public static final String SET_RATINGS = "ratings:setRatings";
    /** The action that represents the get-ratings operation in XACML policies. */
  public static final String GET_RATINGS = "ratings:getRatings";
    /** The action that represents the get-stats operation in XACML policies. */
  public static final String GET_STATS = "ratings:getStats";
    /** The action that represents the get-stats operation in XACML policies. */
  public static final String SET_STATS = "ratings:setStats";
  /** The id of the attribute containing the URI of the object */
  public static final URI OBJ_ID =
      URI.create("urn:topazproject:names:tc:xacml:1.0:resource:object-uri");

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           DELETE_RATINGS,
                                                           SET_RATINGS,
                                                           GET_RATINGS,
                                                           GET_STATS,
                                                           SET_STATS,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                         };

  static {
    init(RatingsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  public RatingsPEP()
      throws IOException, ParsingException, UnknownIdentifierException {
    this(XacmlUtil.lookupPDP("ambra.services.xacml.ratings.pdpName"));
  }

  protected RatingsPEP(PDP pdp)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp);
  }

  /**
   * Check if the user may perform the requested action on the given object.
   *
   * @param action one of the actions defined above
   * @param userId the ratings owner's internal id
   * @param object the object for which to get/set the ratings; may be null
   */
  public void checkObjectAccess(String action, URI userId, URI object) throws SecurityException {
    Set resourceAttrs = new HashSet();

    resourceAttrs.add(
        new Attribute(Util.RESOURCE_ID, null, null, new AnyURIAttribute(userId)));
    if (object != null)
      resourceAttrs.add(new Attribute(OBJ_ID, null, null, new AnyURIAttribute(object)));

    checkAccess(action, resourceAttrs);
  }
}
