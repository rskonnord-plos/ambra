/* $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.alerts;

import java.util.Calendar;
import java.util.Date;
import java.net.URI;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;
import org.topazproject.ws.alerts.impl.AlertsImpl;
import org.topazproject.ws.alerts.impl.AlertsPEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the alerts webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Eric Brown
 */
public class AlertsServicePortSoapBindingImpl implements Alerts, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(AlertsServicePortSoapBindingImpl.class);

  private TopazContext ctx = new WSTopazContext(getClass().getName());
  private Alerts impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      AlertsPEP pep = new WSAlertsPEP((ServletEndpointContext) context);

      ctx.init(context);

      // create the impl
      impl = new AlertsImpl(pep, ctx);
      impl = (Alerts)ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize AlertsImpl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  // The method delegations go here. You need to implement all methods from Alerts and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  public boolean sendAlerts(String endDate, int count) throws RemoteException {
    return impl.sendAlerts(endDate, count);
  }

  public boolean sendAlert(String endDate, String emailAddress) throws RemoteException {
    return impl.sendAlert(endDate, emailAddress);
  }
  
  public boolean sendAllAlerts() throws RemoteException {
    return impl.sendAllAlerts();
  }

  public void startUser(String userId, String date) throws RemoteException {
    impl.startUser(userId, date);
  }

  public void startUser(String userId) throws RemoteException {
    impl.startUser(userId);
  }

  public void clearUser(String userId) throws RemoteException {
    impl.clearUser(userId);
  }

  private static class WSAlertsPEP extends AlertsPEP {
    static {
      init(WSAlertsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSAlertsPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.alerts.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
