package org.ambraproject.service.user;

import org.ambraproject.models.UserProfile;

/**
 * Service bean for registering new users
 *
 * @author Alex Kudlick 9/24/12
 */
public interface UserRegistrationService {

  /**
   * Register a new user with the provided email and newPassword, and send a verification email. The user will not be
   * valid until the verification link from the email is clicked.
   *
   *
   *
   * @param userProfile A userProfile object containing the email address, display name, and first and last names of the user to register
   * @param password the unhashed password for the user
   * @return the generated id of the user object
   * @throws DuplicateUserException if a user with the given email already exists
   */
  public Long registerUser(UserProfile userProfile, String password) throws DuplicateUserException;

  /**
   * Verify the user's account
   *
   * @param email             the user's email address
   * @param verificationToken the verification token for the user
   * @throws NoSuchUserException          if no user with the given email exists
   * @throws UserAlreadyVerifiedException if the user was already verified
   */
  public void verifyUser(String email, String verificationToken) throws NoSuchUserException, UserAlreadyVerifiedException;

  /**
   * Resend a verification email to the user with instructions on how to verify their account
   *
   * @param email the user's email
   * @throws NoSuchUserException          if there is no user with the given email
   * @throws UserAlreadyVerifiedException if the specified user has already verified their account
   */
  public void resendVerificationEmail(String email) throws NoSuchUserException, UserAlreadyVerifiedException;

  /**
   * Send an email message to the user with a link to reset their newPassword
   *
   * @param email the user's email
   * @throws NoSuchUserException if there is no user with the given email
   */
  public void sendForgotPasswordMessage(String email) throws NoSuchUserException;

  /**
   * Check whether an email verification token is valid
   *
   * @param email             the user's email
   * @param verificationToken the randomly generated token sent to the user's email
   * @return true if the newPassword reset token is valid, false otherwise
   */
  public boolean validateVerificationToken(String email, String verificationToken);

  /**
   * Reset a user's password. Will not reset the password if the verification token does not match the user
   *
   * @param email             the user's email
   * @param verificationToken the verification token sent to the user's email
   * @param newPassword       the user's new newPassword
   */
  public void resetPassword(String email, String verificationToken, String newPassword);

  /**
   * Send a message to the user with a link to verify their new email address
   *
   * @param oldEmail the user's old email address
   * @param newEmail the new email address for the user
   * @param password the user's password
   */
  public void sendEmailChangeMessage(String oldEmail, String newEmail, String password) throws NoSuchUserException;

  /**
   * Change a user's email address
   *
   *
   * @param oldEmail the user's old email address
   * @param newEmail the new email address to set
   * @param verificationToken the verification token for the user
   * @throws NoSuchUserException if no user with the given email exists
   */
  public void updateEmailAddress(String oldEmail, String newEmail, String verificationToken) throws NoSuchUserException;

}
