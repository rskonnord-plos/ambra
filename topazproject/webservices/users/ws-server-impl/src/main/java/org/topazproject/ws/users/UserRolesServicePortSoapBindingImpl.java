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
import org.topazproject.ws.users.impl.UserRolesImpl;
import org.topazproject.ws.users.impl.UserRolesPEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the user-roles webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class UserRolesServicePortSoapBindingImpl implements UserRoles, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(UserRolesServicePortSoapBindingImpl.class);

  private UserRoles impl;
  private TopazContext ctx = new WSTopazContext(getClass().getName());

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final UserRolesPEP pep = new WSUserRolesPEP((ServletEndpointContext) context);


      // init topaz context
      ctx.init(context);

      // create the impl
      impl = new UserRolesImpl(pep, ctx);

      // wrap in a proxy
      impl = (UserRoles) ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize UserRolesImpl.", e);
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
   * @see org.topazproject.ws.users.UserRoles#getRoles
   */
  public String[] getRoles(String userId) throws RemoteException, NoSuchUserIdException {
    return impl.getRoles(userId);
  }

  /**
   * @see org.topazproject.ws.users.UserRoles#setRoles
   */
  public void setRoles(String userId, String[] authIds)
      throws RemoteException, NoSuchUserIdException {
    impl.setRoles(userId, authIds);
  }

  /**
   * @see org.topazproject.ws.users.UserRoles#listUsersInRole
   */
  public String[] listUsersInRole(String role) throws RemoteException {
    return impl.listUsersInRole(role);
  }

  private static class WSUserRolesPEP extends UserRolesPEP {
    static {
      init(WSUserRolesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSUserRolesPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.user-roles.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
