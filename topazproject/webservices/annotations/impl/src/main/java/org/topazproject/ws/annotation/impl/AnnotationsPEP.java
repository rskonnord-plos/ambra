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

import org.topazproject.ws.annotation.Annotations;

import org.topazproject.xacml.AbstractSimplePEP;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;

/**
 * The XACML PEP for Annotation Web Service.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationsPEP extends AbstractSimplePEP implements Annotations.Permissions {
  /**
   * The list of all supported actions
   */
  public static final String[] SUPPORTED_ACTIONS =
    new String[] {
                   CREATE_ANNOTATION, DELETE_ANNOTATION, GET_ANNOTATION_INFO, SUPERSEDE,
                   LIST_ANNOTATIONS, LIST_ANNOTATIONS_IN_STATE, SET_ANNOTATION_STATE
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
  protected AnnotationsPEP(PDP pdp, Set subjAttrs)
                    throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
