/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.users;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.WSTopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.ws.users.impl.UserAccountsImpl;
import org.topazproject.ws.users.impl.UserAccountsPEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the user-accounts webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class UserAccountsServicePortSoapBindingImpl implements UserAccounts, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(UserAccountsServicePortSoapBindingImpl.class);

  private UserAccounts impl;
  private TopazContext ctx = new WSTopazContext(getClass().getName());

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final UserAccountsPEP pep = new WSUserAccountsPEP((ServletEndpointContext) context);

      // init topaz context
      ctx.init(context);

      // create the impl
      impl = new UserAccountsImpl(pep, ctx);

      // wrap in a proxy
      impl = (UserAccounts) ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize UserAccountsImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#createUser
   */
  public String createUser(String authId) throws DuplicateAuthIdException, RemoteException {
    return impl.createUser(authId);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#deleteUser
   */
  public void deleteUser(String userId) throws RemoteException, NoSuchUserIdException {
    impl.deleteUser(userId);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#getState
   */
  public int getState(String userId) throws RemoteException, NoSuchUserIdException {
    return impl.getState(userId);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#setState
   */
  public void setState(String userId, int state) throws RemoteException, NoSuchUserIdException {
    impl.setState(userId, state);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#getAuthenticationIds
   */
  public String[] getAuthenticationIds(String userId)
      throws RemoteException, NoSuchUserIdException {
    return impl.getAuthenticationIds(userId);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#setAuthenticationIds
   */
  public void setAuthenticationIds(String userId, String[] authIds)
      throws RemoteException, DuplicateAuthIdException, NoSuchUserIdException {
    impl.setAuthenticationIds(userId, authIds);
  }

  /**
   * @see org.topazproject.ws.users.UserAccounts#lookUpUserByAuthId
   */
  public String lookUpUserByAuthId(String authId) throws RemoteException {
    return impl.lookUpUserByAuthId(authId);
  }

  private static class WSUserAccountsPEP extends UserAccountsPEP {
    static {
      init(WSUserAccountsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSUserAccountsPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.user-accounts.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
