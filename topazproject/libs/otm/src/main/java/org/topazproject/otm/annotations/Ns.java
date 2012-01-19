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
 * An annotation to annotate classes with an id which is meant to be interpreted
 * as the default namespace for fields that does not have an {@link Rdf} annotation.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Ns {

    /** the String is a namespace uri. **/
    String value() default "";


}
