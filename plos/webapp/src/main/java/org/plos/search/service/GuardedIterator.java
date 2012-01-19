/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.search.service;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.security.Guard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.article.util.NoSuchArticleIdException;

/**
 * Iterator wrapper that returns the subset of its delegate iterator that are not guarded.
 *
 * @author Eric Brown
 * @version $Id$
 */
public class GuardedIterator implements Iterator {
  private static final Log log = LogFactory.getLog(GuardedIterator.class);
  private Iterator         delegate;
  private Guard            guard;
  private Object           element = null;

  /**
   * Create a new GuardedIterator.
   *
   * @param delegate The sub-iterator that represents all elements both guarded and unguarded.
   * @param guard    The guard the tests each element.
   */
  public GuardedIterator(Iterator delegate, Guard guard) {
    this.delegate = delegate;
    this.guard = guard;
  }

  public boolean hasNext() {
    if (element != null)
      return true; // We have an element, so we have the next value

    // Loop over delegate until we find an unguarded element
    while (delegate.hasNext()) {
      element = delegate.next();
      try {
        guard.checkGuard(element);
        return true;
      } catch (SecurityException se) {
        if (!(se.getCause() instanceof NoSuchArticleIdException)) {
          log.warn("Guard blocked '" + element + "'", se); // Log exception
        } else if (log.isDebugEnabled()) {
          log.debug("Guard blocked '" + element + "' - " + se);
        }
        element = null; // Don't want this element returned
      }
    }

    return false; // Didn't find any non-guarded elements (no more elements)
  }

  public Object next() {
    if (hasNext()) {
      try {
        return element;
      } finally {
        element = null;
      }
    } else
      throw new NoSuchElementException();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
