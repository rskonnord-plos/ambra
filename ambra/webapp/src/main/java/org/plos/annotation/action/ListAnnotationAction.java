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
package org.plos.annotation.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import org.plos.ApplicationException;
import org.plos.annotation.service.WebAnnotation;
import org.plos.models.FormalCorrection;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;

/**
 * Action class to get a list of annotations.
 */
@SuppressWarnings("serial")
public class ListAnnotationAction extends AnnotationActionSupport {
  private String target;
  private WebAnnotation[] annotations;
  private WebAnnotation[] formalCorrections;

  private static final Log log = LogFactory.getLog(ListAnnotationAction.class);

  /**
   * Loads all annotations for a given target.
   * @return status
   */
  private String loadAnnotations() {
    try {
      annotations = getAnnotationService().listAnnotations(target);
    } catch (final ApplicationException e) {
      log.error("Could not list annotations for target: " + target, e);
      addActionError("Annotation fetching failed with error message: " + e.getMessage());
      return ERROR;
    }
    return SUCCESS;
  }

  /**
   * List annotations.
   * @return status
   * @throws Exception
   */
  @Override
  @Transactional(readOnly = true)
  public String execute() throws Exception {
    return loadAnnotations();
  }

  /**
   * @return Only those annotations that represent formal corrections.
   */
  @Transactional(readOnly = true)
  public String fetchFormalCorrections() {
    if(!SUCCESS.equals(loadAnnotations())) {
      formalCorrections = null;
      return ERROR;
    }
    List<WebAnnotation> list = new ArrayList<WebAnnotation>();
    for(WebAnnotation wa : annotations) {
      if(FormalCorrection.RDF_TYPE.equals(wa.getType())) {
        list.add(wa);
      }
    }
    formalCorrections = list.toArray( new WebAnnotation[list.size()] );
    return SUCCESS;
  }

  /**
   * @return a list of annotations
   */
  public WebAnnotation[] getAnnotations() {
    return annotations;
  }

  /**
   * @return List of associated formal corrections
   */
  public WebAnnotation[] getFormalCorrections() {
    return formalCorrections;
  }

  /**
   * Set the target that it annotates.
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @return the target of the annotation
   */
  @RequiredStringValidator(message="You must specify the target that you want to list the annotations for")
  public String getTarget() {
    return target;
  }

}
