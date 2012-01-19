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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.topazproject.otm.FetchType;

/**
 * Annotation for fields in {@link View @View}'s and {@link SubView @SubView}'s.
 *
 * @author Ronald Tschalär
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Projection {
  /**
   * The projection this field is to be filled with. This must be the name of the (explicit or
   * implicit) variable associated with the projection element. Defaults to the field name.
   */
  String value() default "";

  /**
   * Fetch type preferences for this field. Only valid for fields whose class is an Entity or a
   * View.
   */
  FetchType fetch() default FetchType.eager;
}
