/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.registration.User;
import org.plos.registration.UserImpl;
import org.plos.service.NoUserFoundWithGivenLoginNameException;
import org.plos.service.PasswordInvalidException;
import org.plos.service.RegistrationMailer;
import org.plos.service.RegistrationService;
import org.plos.service.UserAlreadyExistsException;
import org.plos.service.UserAlreadyVerifiedException;
import org.plos.service.UserDAO;
import org.plos.service.UserNotVerifiedException;
import org.plos.service.VerificationTokenInvalidException;
import org.plos.service.password.PasswordDigestService;
import org.plos.service.password.PasswordServiceException;
import org.plos.util.TokenGenerator;

/**
 * Plos registration service implementation.
 */
public class PlosRegistrationService implements RegistrationService {
  private UserDAO userDAO;
  private PasswordDigestService passwordDigestService;
  private RegistrationMailer mailer;
  private static final Log log = LogFactory.getLog(PlosPersistenceService.class);

  public User createUser(final String loginName, final String password) throws UserAlreadyExistsException, PasswordServiceException {
    if (null == getUserWithLoginName(loginName)) {
      final User user = new UserImpl(
                              loginName,
                              passwordDigestService.getDigestPassword(password));

      user.setEmailVerificationToken(TokenGenerator.getUniqueToken());
      user.setVerified(false);
      user.setActive(true);

      saveUser(user);
      mailer.sendEmailAddressVerificationEmail(user);

      return user;
    } else {
      throw new UserAlreadyExistsException(loginName);
    }
  }

  
  /**
   * @see RegistrationService#sendRegistrationEmail(String)
   */
  public void sendRegistrationEmail (final String loginName) throws NoUserFoundWithGivenLoginNameException, UserAlreadyVerifiedException {
    final User user = findExistingUser(loginName);
    if (user.isVerified()) {
      throw new UserAlreadyVerifiedException(loginName);
    }
    if ((user.getEmailVerificationToken() == null) || ("".equals(user.getEmailVerificationToken()))) {
      user.setEmailVerificationToken(TokenGenerator.getUniqueToken());
      saveUser(user);
    }
    mailer.sendEmailAddressVerificationEmail(user);
  }
  
  
  
  /**
   * @see RegistrationService#changeLogin(String, String, String)
   */
  public void changeLogin (final String loginName, final String password, final String newLogin) throws NoUserFoundWithGivenLoginNameException, PasswordInvalidException,  PasswordServiceException, UserAlreadyExistsException {
    final User user = findExistingUser(loginName);

    final boolean validPassword = passwordDigestService.verifyPassword(password, user.getPassword());
    if (validPassword) {
      if (null == getUserWithLoginName(newLogin)) {
        user.setNewLoginName(newLogin);
        user.setEmailVerificationToken(TokenGenerator.getUniqueToken());
        saveUser(user); 
      } else {
        throw new UserAlreadyExistsException(newLogin);
      }         
    } else {
      throw new PasswordInvalidException(loginName, password);
    }
    mailer.sendNewLoginVerificationEmail(user);
  }

  
  /**
   * @see RegistrationService#getUserWithLoginName(String)
   */
  public User getUserWithLoginName(final String loginName) {
    return getUserDAO().findUserWithLoginName(loginName);
  }
  
  /**
   * @see RegistrationService#setVerified(org.plos.registration.User)
   */
  public void setVerified(final User user) {
    user.setVerified(true);
    saveUser(user);
  }

  /**
   * @see RegistrationService#deactivate(org.plos.registration.User)
   */
  public void deactivate(final User user) {
    user.setActive(false);
    saveUser(user);
  }

  /**
   * @see RegistrationService#verifyUser(String, String)
   */
  public void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException, NoUserFoundWithGivenLoginNameException {
    final User user = getUserWithLoginName(loginName);
    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException(loginName);
    }

    if (user.isVerified()) {
      throw new UserAlreadyVerifiedException(loginName);
    }

    if (!user.getEmailVerificationToken().equals(emailVerificationToken)) {
      throw new VerificationTokenInvalidException(loginName, emailVerificationToken);
    }

    activateUser(user);
    saveUser(user);
  }

  /**
   * @see RegistrationService#verifyUser(String, String)
   */
  public void verifyChangeUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, NoUserFoundWithGivenLoginNameException, UserAlreadyExistsException {
    final User user = getUserWithLoginName(loginName);
    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException(loginName);
    }
    
    final String newLoginName = user.getNewLoginName();
    
    if (getUserWithLoginName(newLoginName) != null) {
      throw new UserAlreadyExistsException (newLoginName);
    }
    
    if (!user.getEmailVerificationToken().equals(emailVerificationToken)) {
      throw new VerificationTokenInvalidException(loginName, emailVerificationToken);
    }
    
    user.setEmailVerificationToken(null);
    user.setLoginName(user.getNewLoginName());
    user.setNewLoginName(null);
    saveUser(user);
  }
  
  private void activateUser(final User user) {
    user.setVerified(true);
    user.setEmailVerificationToken(null);
    //user.setActive(true);
  }

  /**
   * @see RegistrationService#sendForgotPasswordMessage(String)
   */
  public void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException {
    final User user = findExistingUser(loginName);

    user.setResetPasswordToken(TokenGenerator.getUniqueToken());
    saveUser(user);

    mailer.sendForgotPasswordVerificationEmail(user);
  }

  /**
   * @see PlosRegistrationService#changePassword(String, String, String)
   */
  public void changePassword(final String loginName, final String oldPassword, final String newPassword) throws NoUserFoundWithGivenLoginNameException, PasswordInvalidException, UserNotVerifiedException, PasswordServiceException {
    final User user = findExistingUser(loginName);

    if (user.isVerified()) {
      final boolean validPassword = passwordDigestService.verifyPassword(oldPassword, user.getPassword());
      if (validPassword) {
        user.setPassword(passwordDigestService.getDigestPassword(newPassword));
      } else {
        throw new PasswordInvalidException(loginName, oldPassword);
      }
    } else {
      throw new UserNotVerifiedException(loginName);
    }

    saveUser(user);
  }

  private User findExistingUser(String loginName) throws NoUserFoundWithGivenLoginNameException {
    final User user = getUserDAO().findUserWithLoginName(loginName);
    if (null == user) {
      throw new NoUserFoundWithGivenLoginNameException(loginName);
    }
    return user;
  }

  /**
   * @see RegistrationService#resetPassword(String, String, String)
   */
  public void resetPassword(final String loginName, final String resetPasswordToken, final String newPassword) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException, PasswordServiceException {
    final User user = getUserWithResetPasswordToken(loginName, resetPasswordToken);

    user.setPassword(passwordDigestService.getDigestPassword(newPassword));
    user.setResetPasswordToken(null);

    activateUser(user);
    saveUser(user);
  }

  /**
   * @see RegistrationService#getUserWithResetPasswordToken(String, String)
   */
  public User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException {
    final User user = findExistingUser(loginName);

    if (user.getResetPasswordToken().equals(resetPasswordToken)) {
      return user;
    } else {
      throw new VerificationTokenInvalidException(loginName, resetPasswordToken);
    }
  }

  /**
   * Save or update the user.
   * @param user user
   */
  private void saveUser(final User user) {
    getUserDAO().saveOrUpdate(user);
  }

  /**
   * Get a UserDAO.
   * @return UserDAO
   */
  public UserDAO getUserDAO() {
    return userDAO;
  }

  /**
   * Sets the UserDAO.
   * @param userDAO userDAO
   */
  public void setUserDAO(final UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  /**
   * Set the passwordDigestService
   * @param passwordDigestService passwordDigestService
   */
  public void setPasswordDigestService(final PasswordDigestService passwordDigestService) {
    this.passwordDigestService = passwordDigestService;
  }

  /**
   * Set the mailer for emailing the users.
   * @param mailer mailer
   */
  public void setMailer(final RegistrationMailer mailer) {
    this.mailer = mailer;
  }
}
