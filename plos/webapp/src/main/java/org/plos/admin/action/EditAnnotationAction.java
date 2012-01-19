/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.action;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.action.BaseActionSupport;
import org.plos.annotation.service.WebAnnotation;
import org.plos.annotation.service.AnnotationService;
import org.plos.annotation.service.AnnotationsPEP;
import org.plos.article.util.NoSuchObjectIdException;

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.util.TransactionHelper;

import org.springframework.beans.factory.annotation.Required;

public class EditAnnotationAction extends BaseActionSupport {

  private static final Log log = LogFactory.getLog(EditAnnotationAction.class);

  private String loadAnnotationId;
  private WebAnnotation annotation;
  private String saveAnnotationId;
  private String saveAnnotationContext;
  private AnnotationService annotationService;
  private AnnotationsPEP pep;
  private Session session;

  private AnnotationsPEP getPEP() {
    try {
      if (pep == null) {
        pep = new AnnotationsPEP();
      }
    } catch (Exception e) {
      throw new Error("Failed to create AnnotationsPEP", e);
    }
    return pep;
  }


  public String execute() throws Exception {

    // default action is just to display the template
    return SUCCESS;
  }

  /**
   * Struts Action to load an Annotation.
   */
  public String loadAnnotation() throws Exception {

    annotation = annotationService.getAnnotation(loadAnnotationId);

    // tell Struts to continue
    return SUCCESS;
  }

  /**
   * Struts Action to save an Annotation.
   */
  public String saveAnnotation() throws Exception {

    // ask PEP if allowed
    getPEP().checkAccess(AnnotationsPEP.UPDATE_ANNOTATION, URI.create(saveAnnotationId));

    TransactionHelper.doInTxE(session,
      new TransactionHelper.ActionE<Void, NoSuchObjectIdException>() {

      public Void run(Transaction tx) throws NoSuchObjectIdException {

        org.plos.models.Annotation a = tx.getSession()
          .get(org.plos.models.Annotation.class, saveAnnotationId);
        if (a == null) {
          throw new NoSuchObjectIdException(saveAnnotationId);
        }

        a.setContext(saveAnnotationContext);

        tx.getSession().saveOrUpdate(a);

        return null;
      }
    });

    addActionMessage("Annotation: " + saveAnnotationId
      + ", Updated Context: " + saveAnnotationContext);

    return SUCCESS;
  }

  /**
   * Get Annotation Id.
   */
  public String getAnnotationId() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getId();
  }

  /**
   * Get Annotation type.
   */
  public String getAnnotationType() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getType();
  }

  /**
   * Get Annotation created.
   */
  public String getAnnotationCreated() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getCreated();
  }

  /**
   * Get Annotation creator.
   */
  public String getAnnotationCreator() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getCreator();
  }

  /**
   * Get Annotation annotates
   */
  public String getAnnotationAnnotates() {

    if (annotation == null) {
      return "null";
    }

    return annotation.getAnnotates();
  }

  /**
   * Get Annotation context.
   */
  public String getAnnotationContext() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getContext() != null ? annotation.getContext() : "null");
  }

  /**
   * Set Annotation id to save.
   */
  public void setSaveAnnotationId(String saveAnnotationId) {

    this.saveAnnotationId = saveAnnotationId;
  }

  /**
   * Set Annotation context to save.
   */
  public void setSaveAnnotationContext(String saveAnnotationContext) {

    this.saveAnnotationContext = saveAnnotationContext;
  }

  /**
   * Get Annotation superseded by.
   */
  public String getAnnotationSupersededBy() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getSupersededBy() != null ? annotation.getSupersededBy() : "null");
  }

  /**
   * Get Annotation superseds.
   */
  public String getAnnotationSupersedes() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getSupersedes() != null ? annotation.getSupersedes() : "null");
  }

  /**
   * Get Annotation title.
   */
  public String getAnnotationTitle() {

    if (annotation == null) {
      return "null";
    }

    return (annotation.getCommentTitle() != null ? annotation.getCommentTitle() : "null");
  }

  /**
   * Struts setter for editAnnotation form.
   */
  public void setLoadAnnotationId(String loadAnnotationId) {
    this.loadAnnotationId = loadAnnotationId;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set AnnotationService.
   */
  public void setAnnotationService(AnnotationService annotationService) {
    this.annotationService = annotationService;
  }
}
