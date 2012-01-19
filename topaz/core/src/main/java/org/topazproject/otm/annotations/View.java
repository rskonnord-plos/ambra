/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for view classes. View's are expressed using OQL queries, with every field
 * corresponding to an element in the query's projection list; the fields must be marked using
 * the {@link Projection @Projection} annotation. The OQL query must take exactly one parameter,
 * and it must be named <var>id</var>; the result of running the query must be 0 or 1 rows.
 * Furthermore, as with {@link Entity  Entity} one field must be marked {@link Id @Id} (in addition
 * to the @Projection annotation). This makes a View behave similar to an Entity in that each
 * instance has an id by which it can be retrieved (using {@link org.topazproject.otm.Session#get Session.get()}) and
 * under which it is cached. This also allows View's to be used as fields in other View's and to
 * be used as result objects in OQL and Criteria queries (they may not be dereferenced, though).
 * View's may <em>not</em> be saved or deleted, however.
 *
 * <p>Subqueries may be used to fill in fields with type collection or array. If the subquery has
 * only a single projection element then nothing further is needed and the projection element is
 * directly converted to the array/collection's component type. If the subquery has more than one
 * element in the projection list then the field's component type must be a class that has the
 * {@link SubView @SubView} annotation; {@link Projection @Projection} annotations on the fields
 * of that class are used as normal to tie the subquery's projection elements to that class'
 * fields.
 *
 * <p>Example:
 * <pre>
 *   &#64;View(query = "select a.uri id, (select oi.uri pid, (select oi.representations from ObjectInfo oi2) reps from ObjectInfo oi where oi = a.parts order by pid) parts from Article a where a.uri = :id order by id;")
 *   class MyView {
 *     &#64;Id &#64;Projection("id")
 *     String id;
 *
 *     &#64;Projection("parts")
 *     List&lt;MyPart&gt; parts;
 *   }
 *
 *   &#64;SubView
 *   class MyPart {
 *     &#64;Projection("pid")
 *     String id;
 *
 *     &#64;Projection("reps")
 *     Set&lt;String&gt; representations;
 *   }
 * </pre>
 *
 * @author Ronald Tschalär
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface View {
  /**
   * View name. Defaults to class name (without the package prefix).
   */
  String name() default "";

  /**
   * The OQL query. It must have exactly one parameter, and the parameter's name must be
   * <var>id</var>.
   */
  String query();
}
