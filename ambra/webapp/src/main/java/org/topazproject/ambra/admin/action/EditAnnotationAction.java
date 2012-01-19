/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.admin.action;

import org.springframework.transaction.annotation.Transactional;

import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.annotation.service.AnnotationConverter;
import org.topazproject.ambra.annotation.service.AnnotationService;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.models.ArticleAnnotation;


@SuppressWarnings("serial")
public class EditAnnotationAction extends BaseActionSupport {
  private String loadAnnotationId;
  private WebAnnotation annotation;
  private String saveAnnotationId;
  private String saveAnnotationContext;
  private AnnotationService annotationService;
  private AnnotationConverter converter;

  @Override
  public String execute() throws Exception {

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Struts Action to load an Annotation.
   *
   * @return the struts status
   * @throws Exception on an error
   */
  @Transactional(readOnly = true)
  public String loadAnnotation() throws Exception {

    ArticleAnnotation a = annotationService.getAnnotation(loadAnnotationId);
    annotation = converter.convert(a, true, true);
    // tell Struts to continue
    return SUCCESS;
  }

  /**
   * Struts Action to save an Annotation.
   *
   * @return the struts status
   * @throws Exception on an error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public String saveAnnotation() throws Exception {

    annotationService.updateContext(saveAnnotationId, saveAnnotationContext);

    addActionMessage("Annotation: " + saveAnnotationId
      + ", Updated Context: " + saveAnnotationContext);

    return SUCCESS;
  }

  /**
   * Get Annotation Id.
   * @return the annotation id
   */
  public String getAnnotationId() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getId();
  }

  /**
   * Get Annotation type.
   * @return the annotation type
   */
  public String getAnnotationType() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getType();
  }

  /**
   * Get Annotation created.
   * @return the annotation created
   */
  public String getAnnotationCreated() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getCreated();
  }

  /**
   * Get Annotation creator.
   * @return the annotation creator
   */
  public String getAnnotationCreator() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getCreator();
  }

  /**
   * Get Annotation annotates
   * @return the annotation target
   */
  public String getAnnotationAnnotates() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getAnnotates();
  }

  /**
   * Get Annotation context.
   * @return the annotation context
   */
  public String getAnnotationContext() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getContext() != null ? annotation.getContext() : "null");
  }

  /**
   * Set Annotation id to save.
   * @param saveAnnotationId the annotation to save
   */
  public void setSaveAnnotationId(String saveAnnotationId) {

    this.saveAnnotationId = saveAnnotationId;
  }

  /**
   * Set Annotation context to save.
   * @param saveAnnotationContext the changed context
   */
  public void setSaveAnnotationContext(String saveAnnotationContext) {

    this.saveAnnotationContext = saveAnnotationContext;
  }

  /**
   * Get Annotation superseded by.
   * @return the superseder
   */
  public String getAnnotationSupersededBy() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getSupersededBy() != null ? annotation.getSupersededBy() : "null");
  }

  /**
   * Get Annotation superseds
   * @return the superseded
   */
  public String getAnnotationSupersedes() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getSupersedes() != null ? annotation.getSupersedes() : "null");
  }

  /**
   * Get Annotation title.
   * @return the annotation title
   */
  public String getAnnotationTitle() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getCommentTitle() != null ? annotation.getCommentTitle() : "null");
  }

  /**
   * Struts setter for editAnnotation form.
   * @param loadAnnotationId the annotation to load
   */
  public void setLoadAnnotationId(String loadAnnotationId) {
    this.loadAnnotationId = loadAnnotationId;
  }

  /**
   * Set AnnotationService.
   * @param annotationService the annotation service to set
   */
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
