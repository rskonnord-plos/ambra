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

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;

import org.topazproject.ws.access.impl.AccessImpl;
import org.topazproject.ws.access.impl.AccessPEP;

import org.topazproject.xacml.ws.WSXacmlUtil;

/**
 * Implements the server-side of the access webservice. This is really just a wrapper around the
 * actual implementation in core.
 *
 * @author Pradeep Krishnan
 */
public class AccessServicePortSoapBindingImpl implements Access, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(AccessServicePortSoapBindingImpl.class);
  private Access           impl;
  private TopazContext     ctx = new WSTopazContext(getClass().getName());

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      AccessPEP pep = new WSAccessPEP((ServletEndpointContext) context);

      // initialize api-call context
      ctx.init(context);

      // create the impl
      impl = (Access) ImplInvocationHandler.newProxy(new AccessImpl(pep, ctx), ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize AccessImpl.", e);
      throw new ServiceException(e);
    }
  }

  /*
   * @see javax.xml.rpc.server.ServiceLifecycle#init
   */
  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  // The method delegations go here. You need to implement all methods from Access and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action)
                    throws RemoteException {
    return impl.evaluate(pdpName, authId, resource, action);
  }

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action,
                           SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                           Attribute[] actionAttrs, Attribute[] envAttrs)
                    throws RemoteException {
    return impl.evaluate(pdpName, authId, resource, action, subjectAttrs, resourceAttrs,
                         actionAttrs, envAttrs);
  }

  /*
   * @see org.topazproject.ws.access.Access#checkAccess
   */
  public void checkAccess(String pdpName, String authId, String resource, String action)
                   throws SecurityException, RemoteException {
    impl.checkAccess(pdpName, authId, resource, action);
  }

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public void checkAccess(String pdpName, String authId, String resource, String action,
                          SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                          Attribute[] actionAttrs, Attribute[] envAttrs)
                   throws SecurityException, RemoteException {
    impl.checkAccess(pdpName, authId, resource, action, subjectAttrs, resourceAttrs, actionAttrs,
                     envAttrs);
  }

  private static class WSAccessPEP extends AccessPEP {
    static {
      init(WSAccessPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAccessPEP(ServletEndpointContext context)
                throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.access.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
