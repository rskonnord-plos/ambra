/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.plos.xacml;

import java.io.IOException;

import org.plos.configuration.ConfigurationStore;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * XACML related Utility functions.
 *
 * @author Pradeep Krishnan
 */
public class XacmlUtil extends Util {
  /**
   * The special value for when looking up a PDP in the config to indicate the default PDP.
   * Example config entry:<pre>  ambra.services.xacml.annotations.pdpName={@value}</pre>
   */
  public static final String SN_DEFAULT_PDP = "_default_";

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

    PDPFactory factory = PDPFactory.getInstance();

    return pdpName.equals(SN_DEFAULT_PDP) ? factory.getDefaultPDP() : factory.getPDP(pdpName);
  }
}
