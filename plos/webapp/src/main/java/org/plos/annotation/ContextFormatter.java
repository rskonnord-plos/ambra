/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.util.XPointerUtils;

/**
 * ContextFormatter - Responsible for String-izing a {@link Context}.
 * 
 * @author jkirton
 */
public abstract class ContextFormatter {
  private static final Log log = LogFactory.getLog(ContextFormatter.class);

  /**
   * Converts a {@link Context} to a valid xpath string.
   * <p>
   * Example xpath strings:
   * ---------------------
   * <p>
   * 1)
   * string-range(/doc/chapter/title,'')[5]/range-to(string-range(/doc/chapter/para/em,'')[3])
   * <p>
   * 2) string-range(/article[1]/body[1]/sec[1]/p[2],"",194,344)
   * <p>
   * ---------------------
   * 
   * @param c The context
   * @return xpath string
   * @throws ApplicationException When an encoding related problem arises
   */
  public static String asXPointer(Context c) throws ApplicationException {
    assert c != null;
    final String startPath = c.getStartPath();
    final int startOffset = c.getStartOffset();
    final String endPath = c.getEndPath();
    final int endOffset = c.getEndOffset();

    if(StringUtils.isBlank(startPath)) return null;
    try {
      String context;
      if (startPath.equals(endPath)) {
        final int length = endOffset - startOffset;
        if (length < 0) {
          // addFieldError("endOffset", errorMessage);
          throw new ApplicationException("Invalid length: " + length + " of the annotated content");
        }
        context = XPointerUtils.createStringRangeFragment(startPath, "", startOffset, length, 1);
      }
      else {
        context = XPointerUtils.createRangeToFragment(XPointerUtils.createStringRangeFragment(
            startPath, "", startOffset), XPointerUtils.createStringRangeFragment(endPath, "",
            endOffset));
      }
      String rval = XPointerUtils.createXPointer(c.getTarget(), context, "UTF-8");
      if(log.isDebugEnabled()) {
        log.debug("xpointer '" + rval + "' created from context: " + c.toString());
      }
      return rval;
    } catch (final UnsupportedEncodingException e) {
      throw new ApplicationException(e);
    }
  }
}
