/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.pap;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;
import org.topazproject.ws.pap.impl.PreferencesImpl;
import org.topazproject.ws.pap.impl.PreferencesPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the preferences webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class PreferencesServicePortSoapBindingImpl implements Preferences, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(PreferencesServicePortSoapBindingImpl.class);

  private final TopazContext ctx = new WSTopazContext(getClass().getName());
  private Preferences impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final PreferencesPEP pep = new WSPreferencesPEP((ServletEndpointContext) context);

      ctx.init(context);
      // create the impl
      impl = new PreferencesImpl(pep, ctx);
      impl = (Preferences)ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize PreferencesImpl.", e);
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
   * @see org.topazproject.ws.pap.Preferences#setPreferences
   */
  public void setPreferences(String appId, String userId, UserPreference[] prefs)
      throws RemoteException, NoSuchUserIdException {
    impl.setPreferences(appId, userId, prefs);
  }

  /**
   * @see org.topazproject.ws.pap.Preferences#getPreferences
   */
  public UserPreference[] getPreferences(String appId, String userId)
      throws RemoteException, NoSuchUserIdException {
    return impl.getPreferences(appId, userId);
  }

  private static class WSPreferencesPEP extends PreferencesPEP {
    static {
      init(WSPreferencesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSPreferencesPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.preferences.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
