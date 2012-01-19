/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.common.ws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;

import org.topazproject.common.ExceptionUtils;
import org.topazproject.common.impl.TopazContext;

/**
 * An invocation handler to invoke calls to a Topaz Service Impl
 *
 * @author Pradeep Krishnan
 */
public class ImplInvocationHandler implements InvocationHandler {
  private Object       target;
  private TopazContext ctx;
  private Log          log;

  /**
   * Creates a Topaz Service Impl proxy
   *
   * @param impl The client stub that we are proxying
   * @param ctx The context associated with the impl
   * @param log the logger to which to log the exception
   *
   * @return the proxy instance of the impl
   */
  public static Object newProxy(Object impl, TopazContext ctx, Log log) {
    return Proxy.newProxyInstance(impl.getClass().getClassLoader(),
                                  impl.getClass().getInterfaces(),
                                  new ImplInvocationHandler(impl, ctx, log));
  }

  private ImplInvocationHandler(Object target, TopazContext ctx, Log log) {
    this.target   = target;
    this.ctx      = ctx;
    this.log      = log;
  }

  /*
   * @see java.lang.reflect.InvocationHandler#invoke
   */
  public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
    try {
      ctx.activate();

      return method.invoke(target, args);
    } catch (Throwable t) {
      if (t instanceof InvocationTargetException && t.getCause() != null)
        t = t.getCause();

      if (t instanceof Error)
        log.error("method " + method.getName() + " threw an error", t);
      else
        log.info("method " + method.getName() + " threw an exception", t);

      Throwable nt;

      try {
        nt = ExceptionUtils.flattenException(t, log);
      } catch (Throwable e) {
        log.error("Error flattening exception", e);
        nt = t;
      }

      throw nt;
    } finally {
      ctx.passivate();
    }
  }
}
