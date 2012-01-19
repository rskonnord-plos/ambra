/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;

import org.plos.ApplicationException;
import org.plos.article.service.FetchArticleService;
import org.plos.email.impl.FreemarkerTemplateMailer;
import org.plos.models.ObjectInfo;
import org.plos.service.PlosoneMailer;
import org.plos.user.PlosOneUser;
import org.plos.user.action.UserActionSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Email the article to another user.
 */
public class EmailArticleAction extends UserActionSupport {
  private String articleURI;
  private String emailTo;
  private String emailFrom;
  private String senderName;
  private String note;
  private String title;
  private String description;
  private String journalName;
  private PlosoneMailer plosoneMailer;
  private FetchArticleService fetchArticleService;
  private static final Log log = LogFactory.getLog(EmailArticleAction.class);
  private static final int MAX_TO_EMAIL = 5;

  /**
   * Render the page with the values passed in
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeRender() throws Exception {
    final PlosOneUser plosOneUser = PlosOneUser.getCurrentUser();
    if (null != plosOneUser) {
      senderName = plosOneUser.getDisplayName();
      emailFrom = plosOneUser.getEmail();
    }
    setArticleTitleAndDesc(articleURI);
    return SUCCESS;
  }

  /**
   * Send the email
   * @return webwork status
   * @throws Exception Exception
   */
  public String executeSend() throws Exception {
    if (!validates()) return INPUT;
    setArticleTitleAndDesc(articleURI);
    final Map<String, String> mapFields = new HashMap<String, String>();
    mapFields.put("articleURI", articleURI);
    mapFields.put("senderName", senderName);
    mapFields.put(FreemarkerTemplateMailer.USER_NAME_KEY, senderName);
    mapFields.put("note", note);
    mapFields.put("title", title);
    mapFields.put("description", description);
    mapFields.put("journalName", journalName);
    mapFields.put("subject", "An Article from " + journalName);
    plosoneMailer.sendEmailThisArticleEmail(emailTo, emailFrom, mapFields);

    return SUCCESS;
  }

  private void setArticleTitleAndDesc(final String articleURI) throws ApplicationException {
    final ObjectInfo articleInfo = fetchArticleService.getArticleInfo(articleURI);
    title = articleInfo.getDublinCore().getTitle();
    description = articleInfo.getDublinCore().getDescription();
  }

  private boolean validates() {
    boolean isValid = true;
    if (StringUtils.isBlank(articleURI)) {
      addFieldError("articleURI", "Article URI cannot be empty");
      isValid = false;
    }
    if (StringUtils.isBlank(emailFrom)) {
      addFieldError("emailFrom", "Your e-mail address cannot be empty");
      isValid = false;
    }
    if (!EmailValidator.getInstance().isValid(emailFrom)) {
      addFieldError("emailFrom", "Invalid e-mail address");
      isValid = false;
    }
    if (StringUtils.isBlank(emailTo)) {
      addFieldError("emailTo", "To e-mail address cannot be empty");
      isValid = false;
    }
    isValid = checkEmails(emailTo) && isValid;
    if (StringUtils.isBlank(senderName)) {
      addFieldError("senderName", "Your name cannot be empty");
      isValid = false;
    }

    return isValid;
  }

  private boolean checkEmails (String emailList) {
    final StringTokenizer emailTokens = new StringTokenizer(emailList, " \t\n\r\f,");
    if (emailTokens.countTokens() > MAX_TO_EMAIL) {
      addFieldError ("emailTo", "Maximum of " + MAX_TO_EMAIL + " email addresses");
      return false;
    }
    EmailValidator validator = EmailValidator.getInstance();
    ArrayList <String> invalidEmails = new ArrayList<String> ();

    while (emailTokens.hasMoreTokens()) {
      String email = emailTokens.nextToken();
      if (!validator.isValid(email)) {
        invalidEmails.add(email);
      }
    }
    final int numInvalid = invalidEmails.size();
    if (numInvalid != 0) {
      StringBuilder errorMsg = new StringBuilder ("Invalid e-mail address");
      if (numInvalid > 1) {
        errorMsg.append ("es: ");
      } else {
        errorMsg.append(": ");
      }
      Iterator<String> iter = invalidEmails.iterator();
      while (iter.hasNext()){
        errorMsg.append (iter.next());
        if (iter.hasNext()) {
          errorMsg.append(", ");
        }
      }
      addFieldError ("emailTo", errorMsg.toString());
    }
    return (numInvalid == 0);
  }

  /**
   * Getter for articleURI.
   * @return Value of articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Setter for articleURI.
   * @param articleURI Value to set for articleURI.
   */
  public void setArticleURI(final String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * Getter for emailFrom.
   * @return Value of emailFrom.
   */
  public String getEmailFrom() {
    return emailFrom;
  }

  /**
   * Setter for emailFrom.
   * @param emailFrom Value to set for emailFrom.
   */
  public void setEmailFrom(final String emailFrom) {
    this.emailFrom = emailFrom;
  }

  /**
   * Getter for emailTo.
   * @return Value of emailTo.
   */
  public String getEmailTo() {
    return emailTo;
  }

  /**
   * Setter for emailTo.
   * @param emailTo Value to set for emailTo.
   */
  public void setEmailTo(final String emailTo) {
    this.emailTo = emailTo;
  }

  /**
   * Getter for note.
   * @return Value of note.
   */
  public String getNote() {
    return note;
  }

  /**
   * Setter for note.
   * @param note Value to set for note.
   */
  public void setNote(final String note) {
    this.note = note;
  }

  /**
   * Getter for senderName.
   * @return Value of senderName.
   */
  public String getSenderName() {
    return senderName;
  }

  /**
   * Setter for senderName.
   * @param senderName Value to set for senderName.
   */
  public void setSenderName(final String senderName) {
    this.senderName = senderName;
  }

  /**
   * Setter for plosoneMailer.
   * @param plosoneMailer Value to set for plosoneMailer.
   */
  public void setPlosoneMailer(final PlosoneMailer plosoneMailer) {
    this.plosoneMailer = plosoneMailer;
  }

  /**
   * Setter for fetchArticleService.
   * @param fetchArticleService Value to set for fetchArticleService.
   */
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * Getter for description.
   * @return Value of description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Getter for title.
   * @return Value of title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the journalName.
   */
  public String getJournalName() {
    return journalName;
  }

  /**
   * @param journalName The journalName to set.
   */
  public void setJournalName(String journalName) {
    this.journalName = journalName;
  }

  /**
   * @return Returns the mAX_TO_EMAIL.
   */
  public static int getMaxEmails() {
    return MAX_TO_EMAIL;
  }
}
