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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to mark an embedded class field. All members of the embedded class 
 * will have the same subject as the embedding class.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Embedded {
}
