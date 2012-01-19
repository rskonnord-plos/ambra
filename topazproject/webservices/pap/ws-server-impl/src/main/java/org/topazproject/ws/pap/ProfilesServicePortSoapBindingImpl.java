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
import org.topazproject.ws.pap.impl.ProfilesImpl;
import org.topazproject.ws.pap.impl.ProfilesPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the profiles webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class ProfilesServicePortSoapBindingImpl implements Profiles, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(ProfilesServicePortSoapBindingImpl.class);

  private final TopazContext ctx = new WSTopazContext(getClass().getName());
  private Profiles impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      final ProfilesPEP pep = new WSProfilesPEP((ServletEndpointContext) context);

      ctx.init(context);
      // create the impl
      impl = new ProfilesImpl(pep, ctx);
      impl = (Profiles)ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize ProfilesImpl.", e);
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
   * @see org.topazproject.ws.pap.Profiles#getProfile
   */
  public UserProfile getProfile(String userId) throws RemoteException, NoSuchUserIdException {
    return impl.getProfile(userId);
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#setProfile
   */
  public void setProfile(String userId, UserProfile profile)
      throws RemoteException, NoSuchUserIdException {
    impl.setProfile(userId, profile);
  }

  /**
   * @see org.topazproject.ws.pap.Profiles#findUsersByProfile
   */
  public String[] findUsersByProfile(UserProfile[] templates, boolean[] ignoreCase)
      throws RemoteException {
    return impl.findUsersByProfile(templates, ignoreCase);
  }

  private static class WSProfilesPEP extends ProfilesPEP {
    static {
      init(WSProfilesPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSProfilesPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.profiles.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
