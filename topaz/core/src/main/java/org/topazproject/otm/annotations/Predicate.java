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

import org.topazproject.otm.mapping.Mapper.CascadeType;

/**
 * Annotation for fields to specify the necessary config for controlling persistence to an RDF
 * triplestore.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Predicate {
  /**
   * Predicate uri. Defaults to @UriPrefix + field name.
   */
  String uri() default "";

  /**
   * Data type for literals. Defaults based on the field data type. Use UNTYPED for untyped literals. 
   */
  String dataType() default "";

  /**
   * A constant to indicate an untyped literal value.
   */
  String UNTYPED = "__untyped__";

  /**
   * The graph/model where this predicate is stored. Defaults to value defined in the containing 
   * Entity.
   */
  String model() default "";

  /**
   * Marks an inverse association. Instead of s p o, load/save as o p s where 
   * s is the Id for the containing Entity and p is the uri for this predicate and o the
   * value of this field..
   */
  boolean inverse() default false;

  /**
   * Marks the backing triples for this field as not owned by this entity and is therefore used
   * only for load. Updates of the entity will skip the rdf statements corresponding to this fied. 
   * By default all triples for a field are owned by the entity. 
   */
  boolean notOwned() default false;

  /**
   * Enum defining various storage types.
   */
  enum StoreAs {undefined, predicate, rdfList, rdfBag, rdfSeq, rdfAlt};

  /**
   * Storage preference for this field. Defaults based on the type.
   */
  StoreAs storeAs() default StoreAs.undefined;

  /**
   * Cascading preferences for this field. 
   */
  CascadeType[] cascade() default {CascadeType.all};
}
