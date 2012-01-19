/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.annotation.service;

import org.topazproject.ambra.xacml.AbstractSimplePEP;

import com.sun.xacml.PDP;

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
   * The action that represents a updateAnnotation operation in XACML policies.
   */
  public static final String UPDATE_ANNOTATION = "annotations:updateAnnotation";

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
      CREATE_ANNOTATION, DELETE_ANNOTATION, UPDATE_ANNOTATION, GET_ANNOTATION_INFO,
      SUPERSEDE, LIST_ANNOTATIONS, LIST_ANNOTATIONS_IN_STATE, SET_ANNOTATION_STATE
    };

  /**
   * The list of all supported obligations
   */
  public static final String[][] SUPPORTED_OBLIGATIONS =
    new String[][] { null, null, null, null, null, null, null, null };

  static {
    init(AnnotationsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
  }

  public AnnotationsPEP(PDP pdp) {
    super(pdp);
  }
}
