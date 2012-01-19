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

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

/**
 * An annotation for configuring a single alias for a uri.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {
    /** the String is a uri. **/
    String value();

    /** the String is an alias. **/
    String alias();
}
