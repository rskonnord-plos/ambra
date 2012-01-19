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
 * as the model/graph to which the triples of this class belong to.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({TYPE,FIELD})
public @interface Model {

    /** The String is a graph/model identifier. Models with this identifier
     * must be registered with the {@link org.topazproject.otm.SessionFactory} 
     * in order to persist classes. **/
    String value() default "";


}
