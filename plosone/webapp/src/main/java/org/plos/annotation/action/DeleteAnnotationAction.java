/* $HeadURL::                                                                            $
 * $Id:DeleteAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 */
package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;

/**
 * Action to delete an annotation
 */
public class DeleteAnnotationAction extends AnnotationActionSupport {
  private String annotationId;
  private boolean deletePreceding;

  private static final Log log = LogFactory.getLog(DeleteAnnotationAction.class);

  /**
   * Annotation deletion action.
   * @return status
   * @throws Exception Exception
   */
  public String deletePrivateAnnotation() throws Exception {
    try {
      getAnnotationService().deletePrivateAnnotation(annotationId, deletePreceding);
    } catch (final ApplicationException e) {
      log.error("Could not delete annotation: " + annotationId, e);
      addActionError("Annotation deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Annotation deleted with id:" + annotationId);
    return SUCCESS;
  }

  /**
   * Delete public annotation.
   * @return status
   * @throws Exception Exception
   */
  public String deletePublicAnnotation() throws Exception {
    try {
      getAnnotationService().deletePublicAnnotation(annotationId);
    } catch (final ApplicationException e) {
      log.error("Could not delete annotation: " + annotationId, e);
      addActionError("Annotation deletion failed with error message: " + e.getMessage());
      return ERROR;
    }
    addActionMessage("Annotation marked as deleted with id:" + annotationId);
    return SUCCESS;
  }

  /**
   * Set the annotation Id.
   * @param annotationId annotationId
   */
  public void setAnnotationId(final String annotationId) {
    this.annotationId = annotationId;
  }

  public void setDeletePreceding(final boolean deletePreceding) {
    this.deletePreceding = deletePreceding;
  }

  /**
   * @return the annotation id
   */
  @RequiredStringValidator(message="You must specify the id of the annotation that you want to delete")
  public String getAnnotationId() {
    return annotationId;
  }
}
