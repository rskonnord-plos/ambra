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
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

/**
 * An annotation to mark an inverse association. 
 * Instead of x p y, load/save y inverse(p) x from/to the triple store.
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Inverse {
}
