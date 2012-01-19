/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.registration;

import org.hibernate.annotations.Index;
import org.plos.util.TokenGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import java.sql.Timestamp;

/**
 * Implementation of Plos Registered User
 */

@Entity
@Table (name = "plos_user")
public class UserImpl implements User {
  @Id
  private String id;

  @Column  (unique = true, length = 256)
  private String loginName;

  @Column  (unique = true, length = 256)
  private String newLoginName;
  
  @Column (nullable = false, length = 256)
  private String password;

  @Column (nullable = false)
  private boolean verified;

  @Column (nullable = false)
  private boolean active;
  private String emailVerificationToken;

  @Column (insertable = false, updatable = false, columnDefinition = "timestamp DEFAULT now()")
  private Timestamp createdOn;

  @Version
  private Timestamp updatedOn;

  private String resetPasswordToken;

  public UserImpl() {
  }

  /**
    * Creates a new UserImpl object.
    *
    * @param loginName The user login name
    * @param password The user password
    */
  public UserImpl(final String loginName, final String password) {
    this.loginName = loginName;
    this.password = password;
    this.id = TokenGenerator.getUniqueToken();
  }

  /**
   * @see org.plos.registration.User#getLoginName()
   */
  @Transactional(readOnly=true)
  @Index(name="login_name_idx") 
  public String getLoginName() {
    return loginName;
  }

  /**
   * @see User#setLoginName(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setLoginName(final String loginName) {
    this.loginName = loginName;
  }

  /**
   * @see org.plos.registration.User#getPassword()
   */
  @Transactional(readOnly=true)
  public String getPassword() {
    return password;
  }

  /**
   * @see User#setPassword(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setPassword(final String password) {
    this.password = password;
  }

  /**
   * @see User#setVerified(boolean)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setVerified(final boolean verified) {
    this.verified = verified;
  }

  /**
   * @see org.plos.registration.User#isVerified()
   */
  @Transactional(readOnly=true)
  public boolean isVerified() {
    return verified;
  }

  /**
   * @see User#setActive(boolean)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setActive(final boolean active) {
    this.active = active;

  }

  /**
   * @see org.plos.registration.User#isActive()
   */
  @Transactional(readOnly=true)
  public boolean isActive() {
    return active;
  }

  /**
   * @see org.plos.registration.User#getId()
   */
  @Transactional(readOnly=true)
  public String getId() {
    return id;
  }

  /**
   * @see User#setId(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * @see org.plos.registration.User#getEmailVerificationToken()
   */
  @Transactional(readOnly=true)
  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  /**
   * @see User#setEmailVerificationToken(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setEmailVerificationToken(final String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  /**
   * @see org.plos.registration.User#getCreatedOn()
   */
  @Transactional(readOnly=true)
  public Timestamp getCreatedOn() {
    return createdOn;
  }

  /**
   * @see User#setCreatedOn(java.sql.Timestamp)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setCreatedOn(final Timestamp createdOn) {
    this.createdOn = createdOn;
  }

  /**
   * @see org.plos.registration.User#getUpdatedOn()
   */
  @Transactional(readOnly=true)
  public Timestamp getUpdatedOn() {
    return updatedOn;
  }

  /**
   * @see User#setUpdatedOn(java.sql.Timestamp)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setUpdatedOn(final Timestamp updatedOn) {
    this.updatedOn = updatedOn;
  }

  /**
   * @see org.plos.registration.User#getResetPasswordToken()
   */
  @Transactional(readOnly=true)
  public String getResetPasswordToken() {
    return resetPasswordToken;
  }

  /**
   * @see User#setResetPasswordToken(String)
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setResetPasswordToken(String resetPasswordToken) {
    this.resetPasswordToken = resetPasswordToken;
  }

  /**
   * @return Returns the newLoginName.
   */
  @Transactional(readOnly=true)
  public String getNewLoginName() {
    return newLoginName;
  }

  /**
   * @param newLoginName The newLoginName to set.
   */
  @Transactional(propagation= Propagation.MANDATORY)
  public void setNewLoginName(String newLoginName) {
    this.newLoginName = newLoginName;
  }
}
