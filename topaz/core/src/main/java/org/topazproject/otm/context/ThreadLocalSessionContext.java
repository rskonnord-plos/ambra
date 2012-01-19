/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.impl.SessionImpl;
import org.topazproject.otm.impl.SessionFactoryImpl;

/**
 * A {@link CurrentSessionContext} impl which scopes the notion of current session by the
 * current thread of execution.  Unlike the JTA counterpart, threads do not give us a nice hook to
 * perform any type of cleanup making it questionable for this impl to actually generate Session
 * instances.  In the interest of usability, it was decided to have this default impl actually
 * generate a session upon first request and then clean it up after the {@link
 * org.topazproject.otm.Transaction} associated with that session is committed/rolled-back.  In
 * order for ensuring that happens, the sessions generated here are unusable until after {@link
 * Session#beginTransaction()} has been called. If <tt>close()</tt> is called on a session managed
 * by this class, it will be automatically unbound. p/> Additionally, the static {@link #bind} and
 * {@link #unbind} methods are provided to allow application code to explicitly control opening
 * and closing of these sessions.  This, with some from of interception, is the preferred
 * approach.  It also allows easy framework integration and one possible approach for implementing
 * long-sessions.
 *
 * @author Pradeep Krishnan (borrowed from Hibernate)
 */
public class ThreadLocalSessionContext implements CurrentSessionContext {
  private static final Log log = LogFactory.getLog(ThreadLocalSessionContext.class);

  /**
   * A ThreadLocal maintaining current sessions for the given execution thread. The actual
   * ThreadLocal variable is a java.util.Map to account for the possibility for multiple
   * SessionFactorys being used during execution of the given thread.
   */
  private static final ThreadLocal context = new ThreadLocal();
  private static Constructor proxyConstructor = createProxyConstructor();

  /**
   * The session factory
   */
  protected final SessionFactory factory;

/**
   * Creates a new ThreadLocalSessionContext object.
   *
   * @param factory the session factory
   */
  public ThreadLocalSessionContext(SessionFactory factory) {
    this.factory = factory;
  }

  /*
   * inherited javadoc
   */
  public final Session currentSession() throws OtmException {
    Session current = existingSession(factory);

    if (current != null)
      return current;

    try {
      current = (Session) proxyConstructor.newInstance(factory);
      ((ProxyObject) current).setHandler(new MethodHandler() {
          /*
           * Gets called only for close()
           */
          public Object invoke(Object self, Method m, Method proceed, Object[] args)
                        throws Throwable {
            unbind(factory);

            return proceed.invoke(self, args);
          }
        });
      doBind(current, factory);
    } catch (Exception e) {
      throw new OtmException("Failed to instantiate a proxy instance of " + Session.class, e);
    }

    return current;
  }

  /**
   * Associates the given session with the current thread of execution.
   *
   * @param session The session to bind.
   */
  public static void bind(Session session) {
    SessionFactory factory = session.getSessionFactory();
    cleanupAnyOrphanedSession(factory);
    doBind(session, factory);
  }

  private static void cleanupAnyOrphanedSession(SessionFactory factory) {
    Session orphan = doUnbind(factory, false);

    if (orphan != null) {
      log.warn("Already session bound on call to bind(); make sure you clean up your sessions!");

      try {
        if (orphan.getTransaction() != null) {
          try {
            orphan.getTransaction().rollback();
          } catch (Throwable t) {
            if (log.isDebugEnabled())
              log.debug("Unable to rollback transaction for orphaned session", t);
          }
        }

        orphan.close();
      } catch (Throwable t) {
        if (log.isDebugEnabled())
          log.debug("Unable to close orphaned session", t);
      }
    }
  }

  /**
   * Unassociate a previously bound session from the current thread of execution.
   *
   * @param factory the factory to unbind from
   *
   * @return The session which was unbound.
   */
  public static Session unbind(SessionFactory factory) {
    return doUnbind(factory, true);
  }

  private static Session existingSession(SessionFactory factory) {
    Map sessionMap = sessionMap();

    if (sessionMap == null) {
      return null;
    } else {
      return (Session) sessionMap.get(factory);
    }
  }

  /**
   * Gets the seeion map from thread local context
   *
   * @return the map
   */
  protected static Map sessionMap() {
    return (Map) context.get();
  }

  private static void doBind(Session session, SessionFactory factory) {
    Map sessionMap = sessionMap();

    if (sessionMap == null) {
      sessionMap = new HashMap();
      context.set(sessionMap);
    }

    sessionMap.put(factory, session);
  }

  private static Session doUnbind(SessionFactory factory, boolean releaseMapIfEmpty) {
    Map     sessionMap = sessionMap();
    Session session    = null;

    if (sessionMap != null) {
      session = (Session) sessionMap.remove(factory);

      if (releaseMapIfEmpty && sessionMap.isEmpty()) {
        context.set(null);
      }
    }

    return session;
  }

  private static Constructor createProxyConstructor() throws Error {
    try {
      MethodFilter mf =
        new MethodFilter() {
          public boolean isHandled(Method m) {
            return m.getName().equals("close");
          }
        };

      ProxyFactory f  = new ProxyFactory();
      f.setSuperclass(SessionImpl.class);
      f.setFilter(mf);

      Class clazz = f.createClass();

      return clazz.getConstructor(SessionFactoryImpl.class);
    } catch (Exception e) {
      log.error("Unexpected exception while initializing a proxy class for " + Session.class, e);
      throw new Error(e);
    }
  }
}
