/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;

/** 
 * Helpers for exception related stuff. 
 * 
 * @author Ronald Tschalär
 */
public class ExceptionUtils {
  private ExceptionUtils() {
  }

  /** 
   * Create a new exception handler. Any method invoked on it will log the given exception and
   * throw a {@link #flattenException flattened} instance of it. This is intended for use by the
   * Axis XxxServicePortSoapBindingImpl's because A) Axis does not log exceptions (so we thereby
   * loose the stack traces) and B) Axis ignores any chained exceptions and only returns the message
   * in the top exception, thereby loosing all the messages in the chained exceptions.
   *
   * <p>This is meant to be used as follows. Given an interface Foo, each method should be wrapped
   * as follows:
   * <pre>
   *   try {
   *     doit(x, y);
   *   } catch (Throwable t) {
   *     ((Foo) newExceptionHandler(Foo.class, t, log)).doit(null, null);
   *   }
   * </pre>
   *
   * <p>While you can invoke any Foo method on the returned instance, you should only invoke same
   * method as you caught the exception on ("doit" in the above template) - otherwise you will get
   * UndeclaredThrowableException's if the declared exceptions differ. However, since the method
   * will always throw an exception it ignores all arguments and hence you can pass in any arguments
   * you want.
   * 
   * <p>Implementation note: the use of a proxy object here is to avoid having to create a method
   * with declares that it throws Throwable and to then have to catch and treat the various
   * subclasses individually. A proxy object does this for us, i.e. we can throw any Throwable and
   * it will check for and cast the exception according to the declared exceptions for the invoked
   * method.
   *
   * @param iface the interface the returned object should implement
   * @param t     the exception being handled
   * @param log   the logger to which to log the exception
   * @return a new exception handler instance which implements <var>iface</var>
   */
  public static Object newExceptionHandler(final Class iface, final Throwable t, final Log log) {
    return Proxy.newProxyInstance(iface.getClassLoader(), new Class[] { iface },
                                  new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          if (t instanceof Error)
            log.error("", t);
          else
            log.debug("", t);

          Throwable nt;
          try {
            nt = flattenException(t, log);
          } catch (Throwable e) {
            log.error("", e);
            nt = t;
          }
          throw nt;
        }
      });
  }

  /** 
   * Merge all the messages in all the chained exceptions. A new exception instance is returned
   * which contains a message consisting of all the messages in the complete exception chain,
   * one per line.
   *
   * <p>Unfortunately it's not quite easy create a new clone of an exception with a different
   * message because there's no setMessage() method and no way to be able to figure out which
   * parameter in a constructor take which value (in general). So this method makes the following
   * assumptions about the exception:
   * <ol>
   *   <li>Either the exception has a two-parameter constructor, both of which are String's,
   *       the first of which is some exception specific param and the second of which is the
   *       exception message, and additionally the exception has a single getXYZ() method (other
   *       than getMessage()) which returns a String and which corresponds to the first parameter
   *       of the constructor; or
   *   <li>the exception has a one-parameter constructor which is the exception message.
   * </ol>
   * Obviously this is a bit limited, but it works for our currently defined exceptions and for
   * most RuntimeException's.
   *
   * <p>This is useful in cases where the exception chain is lost, such as when Axis serializes
   * an exception on the server.
   * 
   * @param t   the exception to "flatten"
   * @param log the logger to use for some debug logging
   * @return a new instance of <var>t</var> with the flattened message
   */
  public static Throwable flattenException(Throwable t, Log log) {
    // merge the messages
    StringBuffer msg = new StringBuffer(t.getMessage() != null ? t.getMessage() : "");
    for (Throwable tt = t.getCause(); tt != null; tt = tt.getCause())
      msg.append('\n').append(tt);

    // create a new exception instance with the merged message
    try {
      try {
        String o_param = getOtherParam(t);
        return (Throwable) t.getClass().getConstructor(new Class[] { String.class, String.class }).
                                        newInstance(new Object[] { o_param, msg.toString() });
      } catch (NoSuchMethodException nsme) {
        log.debug("No two-String constructor found for '" + t.getClass().getName() + "'", nsme);
      }

      try {
        return (Throwable) t.getClass().getConstructor(new Class[] { String.class }).
                                        newInstance(new Object[] { msg.toString() });
      } catch (NoSuchMethodException nsme) {
        throw new Error("No one- or two-String constructor found for '" + t.getClass().getName() +
                        "'", nsme);
      }
    } catch (InvocationTargetException ite) {
      throw new Error("Exception encountered creating exception '" + t.getClass().getName() + "'",
                      ite);
    } catch (IllegalAccessException iae) {
      throw new Error("Internal error", iae);   // can't happen
    } catch (InstantiationException ie) {
      throw new Error("Internal error", ie);    // can't happen
    }
  }

  /** 
   * Find a 'String getXYZ()' method, other than getMessage(), and return the result of
   * invoking it.
   * 
   * @param t the exception to scan
   * @return t.getXYZ(), or null if no appropriate method was found
   */
  private static String getOtherParam(Throwable t)
      throws IllegalAccessException, InvocationTargetException {
    Method[] m_list = t.getClass().getMethods();
    for (int idx = 0; idx < m_list.length; idx++) {
      if (!m_list[idx].getName().startsWith("get"))
        continue;
      if (m_list[idx].getName().equals("getMessage"))
        continue;
      if (m_list[idx].getParameterTypes().length != 0)
        continue;
      if (!m_list[idx].getReturnType().equals(String.class))
        continue;

      return (String) m_list[idx].invoke(t, new Object[0]);
    }

    return null;
  }
}
