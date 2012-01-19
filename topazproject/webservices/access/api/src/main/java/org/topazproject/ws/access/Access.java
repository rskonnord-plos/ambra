/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.access;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This defines the access service. It is somewhat XACML centric. But other implementations are
 * possible.
 *
 * @author Pradeep Krishnan
 */
public interface Access extends Remote {
  /**
   * Evaluate access for the given authenticated-user-id. Can be used to evaluate access
   * permissions on a resource. However there is one catch; the results of this access check will
   * match the access check performed by the service only if they both use the same policies and
   * the same attribute values are available to the pdp and the policies do not contain any
   * temporal decisions. (eg. valid till 3:00 PM and this call is made before 3:00 pm vs the
   * sevice access attempted after 3:00 PM).
   * 
   * <p>
   * There is one other use for this method. Any third-party service or other topaz service may
   * decide to use this API as its primary authorization mechanism.
   * </p>
   *
   * @param pdpName The name of the pdp to use in evaluating this request
   * @param authId The user's raw authenticated id. (Not the topaz user-id)
   * @param resource The resource on which the user wants to perform this action
   * @param action The action that the user is attempting to perform
   *
   * @return Returns the evaluation result
   *
   * @throws RemoteException if some other error ocuured
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action)
                    throws RemoteException;

  /**
   * Evaluate access for the given authenticated-user-id. Same as  {@link
   * #evaluate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)}. The
   * additional attributes will also be used in making the access decision.
   *
   * @param pdpName The name of the pdp to use in evaluating this request
   * @param authId The user's raw authenticated id. (Not the topaz user-id)
   * @param resource The resource on which the user wants to perform this action
   * @param action The action that the user is attempting to perform
   * @param subjectAttrs Additional subject attributes.
   * @param resourceAttrs Additional resource attributes.
   * @param actionAttrs Additional action attributes.
   * @param envAttrs Environment attributes
   *
   * @return Returns the evaluation result
   *
   * @throws RemoteException if some other error ocuured
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action,
                           SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                           Attribute[] actionAttrs, Attribute[] envAttrs)
                    throws RemoteException;

  /**
   * Check access assuming a deny-biased-pep for the given authenticated-user-id. It calls the
   * {@link #evaluate} method and throws a {@link java.lang.SecurityException} if even a single
   * result is not a {@link org.topazproject.ws.access.Result#DECISION_PERMIT DECISION_PERMIT}
   *
   * @param pdpName The name of the pdp to use in evaluating this request
   * @param authId The user's raw authenticated id. (Not the topaz user-id)
   * @param resource The resource on which the user wants to perform this action
   * @param action The action that the user is attempting to perform
   *
   * @throws RemoteException if some other error ocuured
   * @throws SecurityException if not all decision are permits
   */
  public void checkAccess(String pdpName, String authId, String resource, String action)
                   throws RemoteException, SecurityException;

  /**
   * Check access assuming a deny-biased-pep for the given authenticated-user-id. Same as {@link
   * #checkAccess(java.lang.String,java.lang.String,java.lang.String,java.lang.String)}. The
   * additional attributes will also be used in making the access decision.
   *
   * @param pdpName The name of the pdp to use in evaluating this request
   * @param authId The user's raw authenticated id. (Not the topaz user-id)
   * @param resource The resource on which the user wants to perform this action
   * @param action The action that the user is attempting to perform
   * @param subjectAttrs Additional subject attributes.
   * @param resourceAttrs Additional resource attributes.
   * @param actionAttrs Additional action attributes.
   * @param envAttrs Environment attributes
   *
   * @throws RemoteException if some other error ocuured
   * @throws SecurityException if not all decision are permits
   */
  public void checkAccess(String pdpName, String authId, String resource, String action,
                          SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                          Attribute[] actionAttrs, Attribute[] envAttrs)
                   throws RemoteException, SecurityException;
}
