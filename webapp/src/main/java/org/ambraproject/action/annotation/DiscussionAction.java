package org.ambraproject.action.annotation;

import org.ambraproject.Constants;
import org.ambraproject.action.BaseSessionAwareActionSupport;
import org.ambraproject.util.ProfanityCheckingService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * An action to create an article discussion entity (either a top-level response to an article, or a reply to another
 * annotation).
 * <p/>
 * TODO: Pull {@link org.ambraproject.action.BaseActionSupport#addProfaneMessages} down
 */
public abstract class DiscussionAction extends BaseSessionAwareActionSupport {

  private ProfanityCheckingService profanityCheckingService;

  protected String commentTitle;
  protected String ciStatement;
  protected String comment;
  protected boolean isCompetingInterest = false;

  /**
   * Persist the discussion entity with the data in this action's fields.
   */
  protected abstract void create();

  /**
   * Set this action's messages in response to an error.
   *
   * @param e exception representing the error
   */
  protected abstract void error(Exception e);

  /**
   * Set this action's messages to indicate success.
   */
  protected abstract void success();

  /**
   * {@inheritDoc} Also does some profanity check for commentTitle and comment before creating the annotation.
   */
  @Override
  public String execute() {
    if (isInvalid())
      return INPUT;

    try {
      final List<String> profaneWordsInTitle = profanityCheckingService.validate(commentTitle);
      final List<String> profaneWordsInBody = profanityCheckingService.validate(comment);
      final List<String> profaneWordsCiStatement = profanityCheckingService.validate(ciStatement);

      if (profaneWordsInBody.isEmpty() && profaneWordsInTitle.isEmpty() && profaneWordsCiStatement.isEmpty()) {
        create();
      } else {
        addProfaneMessages(profaneWordsInBody, "comment", "comment");
        addProfaneMessages(profaneWordsInTitle, "commentTitle", "title");
        addProfaneMessages(profaneWordsCiStatement, "ciStatement", "statement");
        return INPUT;
      }
    } catch (Exception e) {
      error(e);
      return ERROR;
    }

    success();
    return SUCCESS;
  }

  private boolean isInvalid() {
    /**
     * This is a little odd that part of validation happens here and
     * part of it occurs as validators on the object properties
     * TODO: Revisit and recombine?  Or perhaps author a generic validator that can handle
     * the logic defined below
     **/
    boolean invalid = false;
    invalid |= validateField("commentTitle", commentTitle, Constants.Length.COMMENT_TITLE_MAX,
        "title", "A title is required.");
    invalid |= validateField("comment", comment, Constants.Length.COMMENT_BODY_MAX,
        "comment", "You must say something in your comment.");
    if (this.isCompetingInterest) {
      invalid |= validateField("statement", ciStatement, Constants.Length.CI_STATEMENT_MAX,
          "competing interest statement", "You must say something in your competing interest statement.");
    }
    return invalid;
  }

  /**
   * Add a field error if a user-submitted string was invalid.
   *
   * @param fieldName              the identity of the field being checked
   * @param fieldValue             the value that the user submitted
   * @param maxCharacters          the maximum length of a valid value
   * @param humanReadableFieldName how to describe the field to the user
   * @param emptyMessage           the message to show the user if the value is blank
   * @return {@code true} if invalid; {@code false} if valid
   */
  private boolean validateField(String fieldName, String fieldValue, int maxCharacters,
                                String humanReadableFieldName, String emptyMessage) {
    if (StringUtils.isEmpty(fieldValue)) {
      addFieldError(fieldName, emptyMessage);
      return true;
    }
    int length = fieldValue.length();
    if (length > maxCharacters) {
      String lengthMessage = String.format("Your %s is %d characters long; it cannot be longer than %d characters.",
          humanReadableFieldName, length, maxCharacters);
      addFieldError(fieldName, lengthMessage);
      return true;
    }
    return false;
  }

  /**
   * Set the profanityCheckingService
   *
   * @param profanityCheckingService profanityCheckingService
   */
  @Required
  public void setProfanityCheckingService(ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  /**
   * Set the commentTitle of the annotation
   *
   * @param commentTitle commentTitle
   */
  public void setCommentTitle(String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Set the comment of the annotation
   *
   * @param comment comment
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Set the competing interest statement of the annotation
   *
   * @param ciStatement Statement
   */
  public void setCiStatement(String ciStatement) {
    this.ciStatement = ciStatement;
  }

  /**
   * @param isCompetingInterest does this annotation have competing interests?
   */
  public void setIsCompetingInterest(boolean isCompetingInterest) {
    this.isCompetingInterest = isCompetingInterest;
  }

}
