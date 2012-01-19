#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
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

package ${package}.impl;

import java.io.IOException;
import java.util.Set;

import org.topazproject.xacml.AbstractSimplePEP;

import com.sun.xacml.ParsingException;
import com.sun.xacml.PDP;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for ${service}.
 *
 * @author foo
 */
public abstract class ${Svc}PEP extends AbstractSimplePEP {
  // Note: these operations must be referenced in the policy/${Svc}.xml policy file
  /** The action that represents the create-a-foo operation in XACML policies. */
  public static final String CREATE_FOO = "createFoo";
  /** The action that represents the delete-a-foo operation in XACML policies. */
  public static final String DELETE_FOO = "deleteFoo";

  /** The list of all supported actions */
  protected static final String[] SUPPORTED_ACTIONS = new String[] {
                                                           CREATE_FOO,
                                                           DELETE_FOO,
                                                         };

  /** The list of all supported obligations */
  protected static final String[][] SUPPORTED_OBLIGATIONS = new String[][] {
                                                           null,
                                                           null,
                                                         };

  protected ${Svc}PEP(PDP pdp, Set subjAttrs)
      throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
