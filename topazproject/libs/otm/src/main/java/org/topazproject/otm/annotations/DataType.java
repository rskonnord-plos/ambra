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
 * An annotation to annotate fields with a string which is meant to be interpreted
 * as the data type of the literal while persisting this field.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface DataType {

    String UNTYPED = "__untyped__";
    /** The String is usually an xsd data type. **/
    String value() default UNTYPED;

}
