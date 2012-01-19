/* $HeadURL::                                                                            $
 * $Id$
 *
 */
package org.plos.service;

import org.plos.registration.User;
import org.plos.service.password.PasswordServiceException;

/**
 * Provides a means for a user account creation, password change, user verification, user activation
 */
public interface RegistrationService {
  /**
   * Create user.
   * @param loginName loginName
   * @param password password
   * @return created user
   * @throws UserAlreadyExistsException UserAlreadyExistsException
   * @throws org.plos.service.password.PasswordServiceException PasswordServiceException
   */
  User createUser(final String loginName, final String password) throws UserAlreadyExistsException, PasswordServiceException;

  
  /**
   * Change password.
   * @param loginName loginName
   * @param password password
   * @param newLogin newLogin
   * @throws PasswordInvalidException PasswordInvalidException
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   * @throws org.plos.service.password.PasswordServiceException PasswordServiceException
   * @throws UserAlreadyExistsException UserAlreadyExistsException
   */
  void changeLogin (final String loginName, final String password, final String newLogin) throws NoUserFoundWithGivenLoginNameException, PasswordInvalidException, PasswordServiceException, UserAlreadyExistsException;

    
  /**
   * Will send an email with an address verification link in it if the user has not already been verified
   * 
   * @param loginName username of the user for whom a registration mail should be sent
   * @throws NoUserFoundWithGivenLoginNameException
   * @throws UserAlreadyVerifiedException
   */
  void sendRegistrationEmail (final String loginName) throws NoUserFoundWithGivenLoginNameException, UserAlreadyVerifiedException;

  
  /**
   * Get user with loginName
   * @param loginName loginName
   * @return user
   */
  User getUserWithLoginName(final String loginName);

  /**
   * Set the user as verified.
   * @param user user
   */
  void setVerified(final User user);

  /**
   * Deactivate the user.
   * @param user user
   */
  void deactivate(final User user);

  /**
   * Verify the users account
   * @param loginName loginName
   * @param emailVerificationToken emailVerificationToken
   * @throws VerificationTokenInvalidException VerificationTokenInvalidException
   * @throws UserAlreadyVerifiedException UserAlreadyVerifiedException
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   */
  void verifyUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, UserAlreadyVerifiedException, NoUserFoundWithGivenLoginNameException;

  /**
   * Verify the users account and changes the login to newLoginName
   * @param loginName loginName
   * @param emailVerificationToken emailVerificationToken
   * @throws VerificationTokenInvalidException VerificationTokenInvalidException
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   * @throws UserAlreadyExistsException UserAlreadyExistsException
   */
  void verifyChangeUser(final String loginName, final String emailVerificationToken) throws VerificationTokenInvalidException, NoUserFoundWithGivenLoginNameException, UserAlreadyExistsException;

  /**
   * Send a forgot password message.
   * @param loginName loginName
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   */
  void sendForgotPasswordMessage(final String loginName) throws NoUserFoundWithGivenLoginNameException;

  /**
   * Change password.
   * @param loginName loginName
   * @param oldPassword oldPassword
   * @param newPassword newPassword
   * @throws PasswordInvalidException PasswordInvalidException
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   * @throws UserNotVerifiedException UserNotVerifiedException
   * @throws org.plos.service.password.PasswordServiceException PasswordServiceException
   */
  void changePassword(final String loginName, final String oldPassword, final String newPassword) throws NoUserFoundWithGivenLoginNameException, PasswordInvalidException, UserNotVerifiedException, PasswordServiceException;

  /**
   * Reset the user's password to a new one.
   * @param loginName login name
   * @param resetPasswordToken reset password token
   * @param newPassword new password
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   * @throws VerificationTokenInvalidException VerificationTokenInvalidException
   * @throws org.plos.service.password.PasswordServiceException PasswordServiceException
   */
  void resetPassword(final String loginName, final String resetPasswordToken, final String newPassword) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException, PasswordServiceException;

  /**
   * Return the user with the given loginName and resetPasswordToken
   * @param loginName loginName
   * @param resetPasswordToken resetPasswordToken
   * @return User
   * @throws NoUserFoundWithGivenLoginNameException NoUserFoundWithGivenLoginNameException
   * @throws VerificationTokenInvalidException VerificationTokenInvalidException
   */
  User getUserWithResetPasswordToken(final String loginName, final String resetPasswordToken) throws NoUserFoundWithGivenLoginNameException, VerificationTokenInvalidException;
}
