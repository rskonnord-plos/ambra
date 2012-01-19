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
package org.topazproject.ambra.annotation.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.AnnotationService;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Set the annotation as public.
 */
@SuppressWarnings("serial")
public class SetAnnotationPublicAction extends BaseActionSupport {
  private String targetId;
  protected AnnotationService annotationService;
  private static final Log log = LogFactory.getLog(SetAnnotationPublicAction.class);

  /**
   * Set an annotation as public/private
   * @return Webwork status code
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String executeSetAnnotationPublic() {
    try {
      annotationService.setPublicPermissions(targetId);
    } catch (final Exception e) {
      log.error(e, e);
      addActionError("Annotation creation failed with error message: " + e.getMessage());
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * Getter for targetId.
   * @return Value of targetId.
   */
  @RequiredStringValidator(message = "Annotation id is a required field")
  public String getTargetId() {
    return targetId;
  }

  /**
   * Setter for targetId.
   * @param targetId Value to set for targetId.
   */
  public void setTargetId(final String targetId) {
    this.targetId = targetId;
  }

  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
