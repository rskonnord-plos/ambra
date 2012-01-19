/* $$HeadURL:: $$
 * $$Id: $$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.annotation.action;

import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;

/**
 * Action class to get a list of all corrections for an article and the threads
 * associated with them.
 * 
 * @author jkirton
 */
@SuppressWarnings("serial")
public class GetCorrectionsCommentaryAction extends AbstractCommentaryAction {

  @Override
  protected WebAnnotation[] getAnnotations() throws ApplicationException {
    return getAnnotationService().listCorrections(getTarget());
  }

  @Override
  protected String useCaseDescriptor() {
    return "Corrections Commentary";
  }
}
