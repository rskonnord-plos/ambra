#set( $service = $artifactId )
#set( $Svc = "${service.substring(0, 1).toUpperCase()}${service.substring(1)}" )
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

package ${package};

import java.net.URI;
import java.rmi.RemoteException;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;
import org.topazproject.common.ws.ImplInvocationHandler;
import org.topazproject.common.ws.WSTopazContext;

import ${package}.impl.${Svc}Impl;
import ${package}.impl.${Svc}PEP;
import org.topazproject.xacml.ws.WSXacmlUtil;

/** 
 * Implements the server-side of the ${service} webservice. This is really just a wrapper around
 * the actual implementation in core.
 * 
 * @author foo
 */
public class ${Svc}ServicePortSoapBindingImpl implements ${Svc}, ServiceLifecycle {
  private static final Log log = LogFactory.getLog(${Svc}ServicePortSoapBindingImpl.class);

  private ${Svc} impl;
  private TopazContext ctx  = new WSTopazContext(getClass().getName());

  public void init(Object context) throws ServiceException {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#init");

    try {
      // get the pep
      ${Svc}PEP pep = new WS${Svc}PEP((ServletEndpointContext) context);

      // initialize api-call context
      ctx.init(context);

      // create the impl
      impl = (${Svc}) ImplInvocationHandler.newProxy(new ${Svc}Impl(pep, ctx), ctx, log);
    } catch (Exception e) {
      log.error("Failed to initialize ${Svc}Impl.", e);
      throw new ServiceException(e);
    }
  }

  public void destroy() {
    if (log.isTraceEnabled())
      log.trace("ServiceLifecycle#destroy");

    ctx.destroy();
    impl = null;
  }

  // The method delegations go here. You need to implement all methods from ${Svc} and
  // delegate them to the impl instance, converting parameters, return values, and exceptions
  // where necessary.

  private static class WS${Svc}PEP extends ${Svc}PEP {
    static {
      init(WS${Svc}PEP.class, SUPPORTED_ACTIONS, SUPPORTED_OBLIGATIONS);
    }

    public WS${Svc}PEP(ServletEndpointContext context) throws Exception {
      super(WSXacmlUtil.lookupPDP(context, "topaz.${service}.pdpName"),
            WSXacmlUtil.createSubjAttrs(context));
    }
  }
}
