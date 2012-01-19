/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * Some simple utilities when dealing with RDF and queries/updates. 
 * 
 * @author Ronald Tschalär
 */
public class RdfUtil {
  /**
   * Not meant to be instantiated.
   */
  private RdfUtil() {
  }

  /**
   * Bind values to a format string containing ${xxx} placeholders.
   *
   * @param fmt    the format string
   * @param values the name value pairs for substitusion. name appears in the fmt string as
   *               ${name} and the value is its replacement
   * @return Returns an the format string with all local values bound
   * @throws IllegalArgumentException if a value is missing for a ${token}
   */
  public static String bindValues(String fmt, Map values) {
    Pattern      p   = Pattern.compile("\\$\\{(\\w*)\\}");
    Matcher      m   = p.matcher(fmt);
    StringBuffer sb  = new StringBuffer(fmt.length() * 2);
    int          pos = 0;

    while (m.find()) {
      int    ts    = m.start();
      int    te    = m.end();
      String token = fmt.substring(ts + 2, te - 1);
      String val   = (String) values.get(token);

      if (val == null)
        throw new IllegalArgumentException("Missing value for ${'" + token + "'}");

      sb.append(fmt.substring(pos, ts));
      sb.append(val);
      pos = te;
    }

    sb.append(fmt.substring(pos));

    return sb.toString();
  }

  /**
   * Bind values to a format string containing ${xxx} placeholders. This is a varargs variant of
   * {@link #bindValues(java.lang.String, java.util.Map) #bindValues(String, Map)}.
   *
   * @param fmt  the format string
   * @param args pairs of name and value strings. name appears in the fmt string as ${name} and
   *             the value is its replacement
   * @return Returns an the format string with all local values bound
   * @throws IllegalArgumentException if <var>args</vars> does not contain an even number of
   *                                  elements, or if a value is missing for a ${token}
   */
  public static String bindValues(String fmt, String... args) {
    Map<String, String> values = new HashMap<String, String>();

    if (args.length % 2 != 0)
      throw new IllegalArgumentException("Uneven number of arguments supplied: " + args.length);

    for (int idx = 0; idx < args.length; idx += 2)
      values.put(args[idx], args[idx+1]);

    return bindValues(fmt, values);
  }

  /**
   * Does input validation for uri parameters. Only absolute (non-relative) URIs are valid.
   *
   * In usage, it essentailly asserts that a URI string is a valid URI and throws a
   * subclass of RuntimeException if not.
   *
   * As a helpful side-effect, this function also returns the uri as a proper java.net.URI
   * that can be used for further processing.
   *
   * @param uri the uri string to validate
   * @param name the name of this uri for use in error messages
   * @return Returns the uri
   * @throws NullPointerException if the uri string is null
   * @throws IllegalArgumentException if the uri is not a valid absolute URI
   */
  public static URI validateUri(String uri, String name) {
    if (uri == null)
      throw new NullPointerException("'" + name + "' cannot be null");

    try {
      URI u = new URI(uri);

      if (!u.isAbsolute())
        throw new URISyntaxException(uri, "missing scheme component", 0);

      return u;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("'" + name + "' must be a valid absolute URI", e);
    }
  }

  /**
   * Inserts escapes in a literal so that it is suitable for binding to an ITQL/OQL/SPARQL
   * statement, or any other language where Literals are bracketted with single-quotes. This
   * function inserts backslash-escapes for backslashes and single-quotes into the literal.
   * 
   * @param val the literal value that is to be escaped
   * @return Returns the escaped literal
   */
  public static String escapeLiteral(String val) {
    return val.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
  }
}
