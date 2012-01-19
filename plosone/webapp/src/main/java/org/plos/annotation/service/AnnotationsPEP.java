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
 * The XACML PEP for Annotation Web Service.
 *
 * @author Pradeep Krishnan
 */
public class AnnotationsPEP extends AbstractSimplePEP {
  /**
   * The action that represents a createAnnotation operation in XACML policies.
   */
  public static final String CREATE_ANNOTATION = "annotations:createAnnotation";

  /**
   * The action that represents a deleteAnnotation operation in XACML policies.
   */
  public static final String DELETE_ANNOTATION = "annotations:deleteAnnotation";

  /**
   * The action that represents a getAnnotation operation in XACML policies.
   */
  public static final String GET_ANNOTATION_INFO = "annotations:getAnnotationInfo";

  /**
   * The action that represents a supersede operation in XACML policies.
   */
  public static final String SUPERSEDE = "annotations:supersede";

  /**
   * The action that represents a listAnnotations operation in XACML policies. Note that this
   * permission is checked against the a:annotates resource.
   */
  public static final String LIST_ANNOTATIONS = "annotations:listAnnotations";

  /**
   * The action that represents a listAnnotations operation in XACML policies. Note that this
   * permission is checked against the base uri of annotations.
   */
  public static final String LIST_ANNOTATIONS_IN_STATE = "annotations:listAnnotationsInState";

  /**
   * The action that represents a listAnnotations operation in XACML policies.
   */
  public static final String SET_ANNOTATION_STATE = "annotations:setAnnotationState";

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

  static {
    init(AnnotationsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  public AnnotationsPEP() throws IOException, ParsingException, UnknownIdentifierException {
    this(XacmlUtil.lookupPDP("topaz.annotations.pdpName"), XacmlUtil.createSubjAttrs());
  }

  /*
   *    *@see org.topazproject.xacml.AbstractSimplePEP
   *
   */
  protected AnnotationsPEP(PDP pdp, Set subjAttrs)
                    throws IOException, ParsingException, UnknownIdentifierException {
    super(pdp, subjAttrs);
  }
}
