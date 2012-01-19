/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.mulgara.ws;

import javax.servlet.http.HttpSession;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mulgara.itql.ItqlInterpreterBean;
import org.mulgara.itql.ItqlInterpreterException;

import org.mulgara.query.QueryException;

import org.mulgara.server.SessionFactory;

/**
 * A wrapper around ItqlInterpreterBean to properly initialize it and manage the instances.
 *
 * @author Ronald Tschalär
 */
public class ItqlInterpreterBeanWrapper implements ServiceLifecycle {
  private static final Log    log             = LogFactory.getLog(ItqlInterpreterBeanWrapper.class);
  private static final long   MIN_FREE_MEM    = 10 * 1024 * 1024; // TODO: make this configurable
  private static final String INTERPRETER_KEY =
    ItqlInterpreterBeanWrapper.class.getName() + ".interpreter";

  //
  private ServletEndpointContext context;

  /*
   * @see javax.xml.rpc.server.ServletEndpointContext;
   */
  public void init(Object context) throws ServiceException {
    this.context = (ServletEndpointContext) context;
  }

  private ItqlInterpreterBean getInterpreter() throws QueryException {
    HttpSession         session     = context.getHttpSession();
    ItqlInterpreterBean interpreter = (ItqlInterpreterBean) session.getAttribute(INTERPRETER_KEY);

    if (interpreter != null)
      return interpreter;

    /* Note: this check won't work properly on JVM's that return Long.MAX_VALUE for maxMemory().
     * But on those it's hard to know ahead of time if you're getting low on memory...
     */
    Runtime rt = Runtime.getRuntime();

    if ((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) < MIN_FREE_MEM)
      throw new QueryException("Memory too low (probably too many sessions)");

    SessionFactory sf = WebAppListenerInitMulgara.getSessionFactory();
    interpreter = new ItqlInterpreterBean(sf.newSession(), sf.getSecurityDomain());

    session.setAttribute(INTERPRETER_KEY, interpreter);

    if (log.isDebugEnabled())
      log.debug("Created ItqlInterpreterBean instance " + interpreter);

    return interpreter;
  }

  /*
   * @see javax.xml.rpc.server.ServletEndpointContext;
   */
  public void destroy() {
    context = null;
  }

  /**
   * @see ItqlInterpreterBean#close
   */
  public void close() {
    HttpSession         session     = context.getHttpSession();
    ItqlInterpreterBean interpreter;

    interpreter = (ItqlInterpreterBean) session.getAttribute(INTERPRETER_KEY);

    if (interpreter == null)
      return;

    session.removeAttribute(INTERPRETER_KEY);

    if (log.isDebugEnabled())
      log.debug("Closing ItqlInterpreterBean instance " + interpreter);

    interpreter.close();
  }

  /**
   * @see ItqlInterpreterBean#beginTransaction
   */
  public void beginTransaction(String name) throws QueryException {
    ItqlInterpreterBean interpreter = getInterpreter();

    if (log.isDebugEnabled())
      log.debug("beginTransaction(" + name + ") on " + interpreter);

    try {
      interpreter.beginTransaction(name);
    } catch (QueryException e) {
      if (log.isDebugEnabled())
        log.debug("beginTransaction on " + interpreter + " failed.", e);

      throw e;
    }
  }

  /**
   * @see ItqlInterpreterBean#commit
   */
  public void commit(String name) throws QueryException {
    HttpSession         session     = context.getHttpSession();
    ItqlInterpreterBean interpreter = (ItqlInterpreterBean) session.getAttribute(INTERPRETER_KEY);

    if (interpreter != null) {
      if (log.isDebugEnabled())
        log.debug("commit(" + name + ") on " + interpreter);

      try {
        interpreter.commit(name);
      } catch (QueryException e) {
        if (log.isDebugEnabled())
          log.debug("commit on " + interpreter + " failed.", e);

        throw e;
      }
    }
  }

  /**
   * @see ItqlInterpreterBean#rollback
   */
  public void rollback(String name) throws QueryException {
    HttpSession         session     = context.getHttpSession();
    ItqlInterpreterBean interpreter = (ItqlInterpreterBean) session.getAttribute(INTERPRETER_KEY);

    if (interpreter != null) {
      if (log.isDebugEnabled())
        log.debug("rollback(" + name + ") on " + interpreter);

      try {
        interpreter.rollback(name);
      } catch (QueryException e) {
        if (log.isDebugEnabled())
          log.debug("rollback on " + interpreter + " failed.", e);

        throw e;
      }
    }
  }

  /**
   * @see ItqlInterpreterBean#executeQueryToString
   */
  public String executeQueryToString(String queryString)
                              throws Exception {
    ItqlInterpreterBean interpreter = getInterpreter();

    if (log.isDebugEnabled())
      log.debug("executeQueryToString(" + queryString + ") on " + interpreter);

    try {
      return interpreter.executeQueryToString(queryString);
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("executeQueryToString on " + interpreter + " failed.", e);

      throw e;
    }
  }

  /**
   * @see ItqlInterpreterBean#executeUpdate
   */
  public void executeUpdate(String itql) throws ItqlInterpreterException {
    ItqlInterpreterBean interpreter;

    try {
      interpreter = getInterpreter();
    } catch (QueryException e) {
      throw new ItqlInterpreterException(e);
    }

    if (log.isDebugEnabled())
      log.debug("executeUpdate(" + itql + ") on " + interpreter);

    try {
      interpreter.executeUpdate(itql);
    } catch (ItqlInterpreterException e) {
      if (log.isDebugEnabled())
        log.debug("executeUpdate on " + interpreter + " failed.", e);

      throw e;
    }
  }
}
