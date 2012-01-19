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
 * Annotation for sub-view classes. SubView's are classes which hold the results of subqueries
 * in a {@link View View}'s query. The fields of the annotated class must be marked using the
 * {@link Projection @Projection} annotation.
 *
 * @author Ronald Tschalär
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SubView {
  /**
   * View name. Defaults to class name (without the package prefix).
   */
  String name() default "";
}
