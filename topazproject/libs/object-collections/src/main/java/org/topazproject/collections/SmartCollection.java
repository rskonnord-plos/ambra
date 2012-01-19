/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.collections;

import org.topazproject.otm.Session;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.Conjunction;
import org.topazproject.otm.criterion.Disjunction;

/**
 * Defines a smart collection of objects defined by a set of criterion that are either
 * anded or ored together. Criterion can be thought of as <i>rules</i> in smart collections
 * defined in other contexts.<p>
 *
 * For example, if you wanted the collection of articles defined by
 * ((author is "Einstein") or (author is "Oppenheimer")) and (category is "dancing"),
 * you'd create the the following collection:
 * <pre>
 *   SmartCollection&lt;Article> collection  = new SamrtCollection&lt;Article>(session, Article.class, ALL);
 *   SmartCollection&lt;Article> authoredBy  = new SmartCollection&lt;Article>(session, Article.class, ANY);
 *
 *   Criterion  einstein    = Restrictions.eq("author", "Einstein");
 *   Criterion  oppenheimer = Restricitons.eq("author", "Oppenheimer");
 *   Criterion  dancing     = Restrictions.eq("category", "dancing");
 *
 *   authoredBy.add(einstein).add(oppenheimer);
 *   collection.add(authoredBy).add(dancing);
 *
 *   for (Article article: collection)
 *     ; // do something
 * </pre>
 *
 * @author Eric Brown
 */
public class SmartCollection<T> extends BaseCollection<T> {
  /** ALL criterion must be satisfied (criterion are ANDed together)
   *  -- use in <code>SmartCollection</code> constructor.
   */
  public static final boolean ALL = true;
  /** ANY criterion can be satisifed for an object to be included (criterion are ORed together)
   *  -- use in <code>SmartCollection</code> constructor.
   */
  public static final boolean ANY = false;

  /**
   * Create a smart collection
   *
   * @param session the session on which to base the collection on
   * @param clazz   the otm managed class returned by this collection (must match parameterization)
   * @param all     true if all criterion must match on the object. {@link #ALL} or {@link #ANY}
   *                constants can also be used instead of true or false.
   * @throw OtmException if otm does not manage class or some other otm error occurs
   */
  public SmartCollection(Session session, Class clazz, boolean all) throws OtmException {
    super(session, clazz, (all ? new Conjunction() : new Disjunction()));
  }

  /**
   * Add another criterion to the collection.
   *
   * @param c the criterion to add
   * @return a reference to this <code>SmartCollection</code> object.
   */
  public SmartCollection<T> add(Criterion c) {
    super.add(c);
    return this;
  }
}
