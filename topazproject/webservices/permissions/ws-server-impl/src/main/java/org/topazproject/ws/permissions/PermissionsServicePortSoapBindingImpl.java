/*$HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.permissions;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;

import org.topazproject.ws.permissions.impl.PermissionsImpl;
import org.topazproject.ws.permissions.impl.PermissionsPEP;

import org.topazproject.xacml.ws.WSXacmlUtil;

/**
 * Implements the permissions webservice.
 *
 * @author Pradeep Krishnan
 */
public class PermissionsServicePortSoapBindingImpl implements Permissions, ServiceLifecycle {
  private static final Log log  = LogFactory.getLog(PermissionsServicePortSoapBindingImpl.class);
  private TopazContext     ctx  = new WSTopazContext(getClass().getName());
  private Permissions      impl;

  /**
   * Initialize a permissions impl.
   *
   * @param context the jax-rpc context
   *
   * @throws ServiceException on an error in initialize
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final PermissionsPEP pep = new WSPermissionsPEP((ServletEndpointContext) context);

      ctx.init(context);

      // create the impl
      impl   = new PermissionsImpl(pep, ctx);
      impl   = (Permissions) ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize PermissionsImpl.", e);
      throw new ServiceException(e);
    }
  }

  /*
   * Destroys the permissions impl.
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#grant
   */
  public void grant(String resource, String[] permissions, String[] principals)
             throws RemoteException {
    impl.grant(resource, permissions, principals);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#revoke
   */
  public void revoke(String resource, String[] permissions, String[] principals)
              throws RemoteException {
    impl.revoke(resource, permissions, principals);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#cancelGrants
   */
  public void cancelGrants(String resource, String[] permissions, String[] principals)
                    throws RemoteException {
    impl.cancelGrants(resource, permissions, principals);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#cancelRevokes
   */
  public void cancelRevokes(String resource, String[] permissions, String[] principals)
                     throws RemoteException {
    impl.cancelRevokes(resource, permissions, principals);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listGrants
   */
  public String[] listGrants(String resource, String principal)
                      throws RemoteException {
    return impl.listGrants(resource, principal);
  }

  /**
   * @see org.topazproject.ws.permissions.Permissions#listRevokes
   */
  public String[] listRevokes(String resource, String principal)
                       throws RemoteException {
    return impl.listRevokes(resource, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#implyPermission
   */
  public void implyPermissions(String permission, String[] implies)
                        throws RemoteException {
    impl.implyPermissions(permission, implies);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelImplyPermission
   */
  public void cancelImplyPermissions(String permission, String[] implies)
                              throws RemoteException {
    impl.cancelImplyPermissions(permission, implies);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listImpliedPermissions
   */
  public String[] listImpliedPermissions(String permission, boolean transitive)
                                  throws RemoteException {
    return impl.listImpliedPermissions(permission, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#propagatePermissions
   */
  public void propagatePermissions(String resource, String[] to)
                            throws RemoteException {
    impl.propagatePermissions(resource, to);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#cancelPropagatePermissions
   */
  public void cancelPropagatePermissions(String resource, String[] to)
                                  throws RemoteException {
    impl.cancelPropagatePermissions(resource, to);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#listPermissionPropagations
   */
  public String[] listPermissionPropagations(String resource, boolean transitive)
                                      throws RemoteException {
    return impl.listPermissionPropagations(resource, transitive);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isGranted(String resource, String permission, String principal)
                    throws RemoteException {
    return impl.isGranted(resource, permission, principal);
  }

  /*
   * @see org.topazproject.ws.permissions.Permission#isGranted
   */
  public boolean isRevoked(String resource, String permission, String principal)
                    throws RemoteException {
    return impl.isRevoked(resource, permission, principal);
  }

  private static class WSPermissionsPEP extends PermissionsPEP {
    static {
      init(WSPermissionsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSPermissionsPEP(ServletEndpointContext context)
                     throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.permissions.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
