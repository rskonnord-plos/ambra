package org.plos;

import org.plos.exception.UserException;

import java.util.Hashtable;

/**
 * Place holder for User object
 * 
 * @author Stephen Cheng
 * 
 */
public class User {

  private String topazUserId;

  private String signOnId;

  private Hashtable<String, String> userProperties;

  public User() {

  }

  /**
   * @param userToGet
   *          CAS signOnId for the user to retrieve from store
   * @param signOnId
   *          signOnId of user making call
   * @param authToken
   *          CAS token for calling user
   * @return User object from store
   */
  public static User getUser(String userToGet, String signOnId, String authToken) {
    User newUser = new User();
    newUser.setSignOnId(userToGet);
    newUser.setTopazUserId("blahblahuniqid");
    return newUser;
  }

  /**
   * Creates user in Topaz store. Updates topazUserId field with newly created ID.
   * 
   * @param signOnId
   * @param authToken
   * @throws UserException
   * 
   */
  public void createUser(String signOnId, String authToken) throws UserException {

  }

  /**
   * Updates user in Topaz store.
   * 
   * @param signOnId
   * @param authToken
   * @throws UserException
   */
  public void updateUser(String signOnId, String authToken) throws UserException {

  }

  /**
   * Deletes user from Topaz store.
   * 
   * @param signOnId
   * @param authToken
   * @throws UserException
   */
  public void deleteUser(String signOnId, String authToken) throws UserException {

  }

  /**
   * @return Returns the signOnId.
   */
  public String getSignOnId() {
    return signOnId;
  }

  /**
   * @param signOnId
   *          ID used by external authentication source to login a user
   */
  public void setSignOnId(String signOnId) {
    this.signOnId = signOnId;
  }

  /**
   * @return Returns the topazUserId.
   */
  public String getTopazUserId() {
    return topazUserId;
  }

  /**
   * @param topazUserId
   *          internally used unique ID for each user (will not change)
   */
  public void setTopazUserId(String topazUserId) {
    this.topazUserId = topazUserId;
  }

  /**
   * @return Returns the userProperties.
   */
  public Hashtable<String, String> getUserProperties() {
    return userProperties;
  }

  /**
   * @param userProperties
   *          The userProperties to set.
   */
  public void setUserProperties(Hashtable<String, String> userProperties) {
    this.userProperties = userProperties;
  }

  /**
   * Used to store arbitrary user information
   * 
   * @param key
   *          name of property
   * @param value
   *          value of property
   */
  public void setProperty(String key, String value) {
    if ((this.userProperties != null) && (key != null) && (value != null)) {
      userProperties.put(key, value);
    }
  }

  /**
   * Get arbitrary user information item
   * 
   * @param key
   *          name of property
   * @return value
   */
  public String getProperty(String key) {
    if ((this.userProperties != null) && (key != null)) {
      return this.userProperties.get(key);
    }
    return null;
  }

}
