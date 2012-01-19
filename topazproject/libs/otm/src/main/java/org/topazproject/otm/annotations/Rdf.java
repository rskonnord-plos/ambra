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
 * An annotation to annotate classes and fields with a uri which is meant to be interpreted
 * as:<ul>
 * <li>for a class: the rdf:type of the class
 * <li>for a field: the field is thought of as a relation between an instance of the class and the value of the field
 * </ul>
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({TYPE,FIELD})
public @interface Rdf {
    /** help compose xsd literal ranges */
    String xsd="http://www.w3.org/2001/XMLSchema#";
    /** help compose rdf defined URIs */
    String rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    /** help compose owl defined URIs */
    String owl="http://www.w3.org/2002/07/owl#";
    /** help compose mulgara defined URIs */
    String mulgara ="http://mulgara.org/mulgara#";
    String tucana = mulgara;
    /** help compose dc defined URIs */
    String dc="http://purl.org/dc/elements/1.1/";
    /** help compose dc_terms defined URIs */
    String dc_terms="http://purl.org/dc/terms/";
    /** help compose topaz defined URIs */
    String topaz="http://rdf.topazproject.org/RDF/";
    /** help compose fedora defined URIs */
    String fedora="info:fedora/";

    /** the String must be a full URI */
    String value();

    /**
     * @return the range uri (for literal types). Value is "" for default.
     */
    String range() default "";

}
