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

import java.sql.Timestamp;

/**
 * Interface for Plos Reistration User
 */
public interface User {

  /**
   * Get the Login Name
   * @return Login Name
   */
  String getLoginName();

  /**
    * Set the Users Login Name
    * @param email  Set User Login Name
    */
  void setLoginName(String email);

  /**
   * Get the Login Name
   * @return Login Name
   */
  String getNewLoginName();

  /**
    * Set the User's replacement Login Name
    * @param email  Set User Login Name
    */
  void setNewLoginName(String email);
  
  
  /**
   * Get the User Password
   * @return User Password
   */
  String getPassword();

  /**
    * Set the User Password
    * @param password password
    */
  void setPassword(String password);

  /**
    * Set the User to varified
    * @param  verified verified
    */
  void setVerified(final boolean verified);

  /**
   * Is the user verified
   * @return  Boolean true/false
   */
  boolean isVerified();

  /**
    * Set the User to active
    * @param active active
    */
  void setActive(final boolean active);

  /**
   * Is the user active
   * @return  Boolean true/false
   */
  boolean isActive();

  /**
   * Get the User Id
   * @return User Id
   */
  String getId();

  /**
    * Set the User Id
    * @param id id
    */
  void setId(final String id);

  /**
   * Get the Email Verification Token
   * @return Email Verification Token
   */
  String getEmailVerificationToken();

  /**
    * Set the Email Verification Token
    * @param emailVerificationToken emailVerificationToken
    */
  void setEmailVerificationToken(String emailVerificationToken);

  /**
   * Get the Timestamp the User was created
   * @return Timestamp when the user was created
   */
  Timestamp getCreatedOn();

  /**
    * Set the Timestamp the User was created
    * @param createdOn createdOn
    */
  void setCreatedOn(final Timestamp createdOn);

  /**
   * Get the Timestamp of last update
   * @return Timestamp of last update
   */
  Timestamp getUpdatedOn();

  /**
    * Set the Timestamp of last update
    * @param updatedOn updatedOn
    */
  void setUpdatedOn(final Timestamp updatedOn);

  /**
   * Get the Reset Password Token
   * @return Reset Password Token
   */
  String getResetPasswordToken();

  /**
    * Set the Reset Password Token
    * @param resetPasswordToken resetPasswordToken
    */
  void setResetPasswordToken(final String resetPasswordToken);

}
