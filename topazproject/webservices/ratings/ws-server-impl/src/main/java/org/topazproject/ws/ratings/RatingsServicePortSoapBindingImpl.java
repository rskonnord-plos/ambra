/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.ws.ratings;

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
import org.topazproject.ws.ratings.impl.RatingsImpl;
import org.topazproject.ws.ratings.impl.RatingsPEP;
import org.topazproject.ws.users.NoSuchUserIdException;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the ratings webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author Ronald Tschalär
 */
public class RatingsServicePortSoapBindingImpl implements Ratings, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(RatingsServicePortSoapBindingImpl.class);

  private TopazContext ctx = new WSTopazContext(getClass().getName());
  private Ratings impl;

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      RatingsPEP pep = new WSRatingsPEP((ServletEndpointContext) context);

      ctx.init(context);

      // create the impl
      impl = new RatingsImpl(pep, ctx);
      impl = (Ratings)ImplInvocationHandler.newProxy(impl, ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize RatingsImpl.", e);
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
   * @see org.topazproject.ws.ratings.Ratings#setRatings
   */
  public void setRatings(String appId, String userId, String object, ObjectRating[] ratings)
      throws RemoteException, NoSuchUserIdException {
    impl.setRatings(appId, userId, object, ratings);
  }

  /**
   * @see org.topazproject.ws.ratings.Ratings#getRatings
   */
  public ObjectRating[] getRatings(String appId, String userId, String object)
      throws RemoteException, NoSuchUserIdException {
    return impl.getRatings(appId, userId, object);
  }

  /**
   * @see org.topazproject.ws.ratings.Ratings#getRatingStats
   */
  public ObjectRatingStats[] getRatingStats(String appId, String object) throws RemoteException {
    return impl.getRatingStats(appId, object);
  }

  private static class WSRatingsPEP extends RatingsPEP {
    static {
      init(WSRatingsPEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WSRatingsPEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.ratings.pdpName"),
          WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
