/* $HeadURL::                                                                            $
 * $Id:ListAnnotationAction.java 722 2006-10-02 16:42:45Z viru $
 */
package org.plos.annotation.action;

import com.opensymphony.xwork.validator.annotations.RequiredStringValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.annotation.service.Annotation;

/**
 * Action class to get a list of annotations.
 */
public class ListAnnotationAction extends AnnotationActionSupport {
  private String target;
  private Annotation[] annotations;

  private static final Log log = LogFactory.getLog(ListAnnotationAction.class);

  /**
   * List annotations.
   * @return status
   * @throws Exception
   */
  public String execute() throws Exception {
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
   * @return a list of annotations
   */
  public Annotation[] getAnnotations() {
    return annotations;
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
