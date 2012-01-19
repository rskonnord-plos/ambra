/* $HeadURL::                                                                                    $
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
 * An annotation to support generated values.
 *
 * @author Eric Brown
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GeneratedValue {
  /**
   * The name of the generator class to use.
   *
   * NOTE: This may be deprecated later when we have a configuration file.
   */
  String generatorClass() default "org.topazproject.otm.id.GUIDGenerator";

  /**
   * The prefix of the uri we should generate. If not set, it will be computed.
   */
  String uriPrefix() default "";
}
