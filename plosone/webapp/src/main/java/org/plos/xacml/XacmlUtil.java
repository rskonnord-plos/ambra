/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.xacml;

import java.io.IOException;

import java.net.URI;

import java.util.Collections;
import java.util.Set;

import org.topazproject.configuration.ConfigurationStore;

import org.topazproject.xacml.PDPFactory;
import org.topazproject.xacml.Util;

import com.opensymphony.webwork.ServletActionContext;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.Subject;

/**
 * XACML related Utility functions.
 *
 * @author Pradeep Krishnan
 */
public class XacmlUtil extends Util {
  /**
   * The special value for when looking up a PDP in the config to indicate the default PDP.
   * Example config entry:<pre>  topaz.annotations.pdpName={@value}</pre>
   */
  public static final String SN_DEFAULT_PDP = "_default_";
  private static Attribute dummyAttr     =
    new Attribute(URI.create("plos:xacml:dummy"), null, null, new StringAttribute("dummy"));
  private static Set       dummySubjAttr =
    Collections.singleton(new Subject(URI.create("plos:xacml:dummy"),
                                      Collections.singleton(dummyAttr)));

  /**
   * Creates the set of subject attributes used in the xacml evaluation request from the
   * given web-server context.
   *
   * @return the subject attributes
   */
  public static Set createSubjAttrs() {
    return dummySubjAttr;
  }

  /**
   * Look up the PDP for the given service.
   *
   * @param pdpProp the name of config property defining name of the PDP configuration to use; the
   *        value must be the name of valid PDP configuration, or the special value {@link
   *        #SN_DEFAULT_PDP SN_DEFAULT_PDP} to indicate the default PDP config.
   *
   * @return the PDP
   *
   * @throws IOException on error in accessing the PDP config file
   * @throws ParsingException on error in parsing the PDP config file
   * @throws UnknownIdentifierException if no config entry named <var>pdpProp</var> is found or if
   *         the entry's value does not identify a valid PDP config
   */
  public static PDP lookupPDP(String pdpProp)
                       throws IOException, ParsingException, UnknownIdentifierException {
    String pdpName = ConfigurationStore.getInstance().getConfiguration().getString(pdpProp, null);

    if (pdpName == null)
      throw new UnknownIdentifierException("No config entry named '" + pdpProp + "' found");

    PDPFactory factory = PDPFactory.getInstance(ServletActionContext.getServletContext());

    return pdpName.equals(SN_DEFAULT_PDP) ? factory.getDefaultPDP() : factory.getPDP(pdpName);
  }
}
