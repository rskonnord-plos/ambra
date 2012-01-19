/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.action;

import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;

/**
 * Action class to get a list of all commentary for an article and the threads
 * associated with each base comment.
 * 
 * @author Stephen Cheng
 * @author jkirton
 */
@SuppressWarnings("serial")
public class GetCommentaryAction extends AbstractCommentaryAction {

  /**
   * For this use case, we provide only comment (non-correction) related
   * annotations
   */
  @Override
  protected WebAnnotation[] getAnnotations() throws ApplicationException {
    return getAnnotationService().listComments(getTarget());
  }

  @Override
  protected String useCaseDescriptor() {
    return "all Commentary";
  }
}
