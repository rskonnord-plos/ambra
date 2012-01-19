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
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.criterion.Disjunction;

/**
 * Defines a simple collection of objects represented by a list of their ids.<p>
 *
 * For example, if you wanted a collection of articles 3, 4 and 5, you could do the following:
 * <pre>
 *   SimpleCollection&lt;Article> collection = new SimpleCollection&lt;Article>(session, Article.class);
 *   collection.addIds(new String[] { "info:doi/3", "info:doi/4", "info:doi/5" });
 *   for (Article article: collection)
 *     ; // do something
 * </pre>
 *
 * @author Eric Brown
 */
public class SimpleCollection<T> extends BaseCollection<T> {
  /**
   * Create a simple collection
   *
   * @param session the session on which to base the collection on
   * @param clazz   the otm managed class returned by this collection (must match parameterization)
   * @throw OtmException if otm does not manage class or some other otm error occurs
   */
  public SimpleCollection(Session session, Class clazz) {
    super(session, clazz, Restrictions.disjunction());
  }

  /**
   * Add the id of an object to add to this list.
   *
   * @param id the id of an object.
   * @return a reference to this <code>SimpleCollection</code> object.
   */
  public SimpleCollection addId(String id) {
    add(Restrictions.id(id));
    return this;
  }

  /**
   * Add an array of object ids to this list.
   *
   * @param ids an array of object ids.
   */
  public void addIds(String[] ids) {
    for (String id: ids) {
      addId(id);
    }
  }
}
