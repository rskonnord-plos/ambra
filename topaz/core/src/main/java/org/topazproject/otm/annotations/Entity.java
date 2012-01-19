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
 * Annotation for classes to specify the necessary config for controlling persistence to an RDF
 * triplestore.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Entity {
  /**
   * Entity name. Defaults to class name (without the package prefix).
   */
  String name() default "";

  /**
   * The rdf:type for this entity. Defaults to super-class.
   */
  String type() default "";

  /**
   * The graph/model where this entity is to be persisted. Defaults to super-class.
   */
  String model() default "";
}
