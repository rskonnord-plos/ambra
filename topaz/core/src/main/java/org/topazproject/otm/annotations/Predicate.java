/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.metadata.RdfDefinition;

/**
 * Annotation for properties to specify the necessary config for controlling persistence to an RDF
 * triplestore.
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Predicate {
  /**
   * References another property configuration defined elsewhere. For example
   * an external config file can be used to define the predicate-uri and other attributes
   * so that they need not be defined here. If this attribute is defined, then rest of the
   * attributes in this annotation is treated as an override to the values defined in the
   * reference.
   */
  String ref() default "";

  /**
   * Predicate uri. Defaults to the value from {@link #ref reference}} or if reference is undefined,
   * a URI is constructed by concatinating the value of {@link UriPrefix @UriPrefix} and the name
   * of this property.
   */
  String uri() default "";

  /**
   * An enum to configure the property type of this predicate URI. This is similar to the
   * owl:DataProperty and owl:ObjectProperty.
   */
  enum PropType {
      /**
       * To specify that this field should be persisted as a literal value.
       */
      DATA,
      /**
       * To specify that this field should be persisted as a URI value.
       */
      OBJECT,
      /**
       * To derive the PropType of this value based on other attributes or to
       * accept the values from any referenced property.
       */
      UNDEFINED
  };

  /**
   * An enum to configure boolean property values. This is to distinguish between undefined
   * vs a defined 'true' or 'false' value in the configuration. The significance to this is
   * in the configuration of overrides to a {@link #ref reference} property. There it is important
   * to have a 'tri-state' for all configuration elements so that the annotation parsing can
   * detect properties that are undefined vs using a default.
   */
  enum BT {
     TRUE,
     FALSE,
     UNDEFINED
  }

  /**
   * The property type of this predicate uri. Defaults to the value specified in the
   * {@link #ref reference}. If no reference is supplied, it defaults to <value>OBJECT</value>
   * for associations and {@link java.net.URI} or {@link java.net.URL} fields when the
   * {@link #dataType} value is unspecified. Otherwise it defaults to <value>DATA</value>.
   * Normally there is no need to configure this. The only place this is needed is to specify
   * an <value>OBJECT</value> type for a field that is serializable.
   */
  PropType type() default PropType.UNDEFINED;

  /**
   * Data type for literals. Defaults to the value specified in the {@link #ref reference}.
   * If no reference is supplied, a default value is guessed based on this property's data
   * type. Use {@link #UNTYPED} for explicitly defining untyped literals.
   */
  String dataType() default "";

  /**
   * A constant to indicate an untyped literal value.
   */
  String UNTYPED = RdfDefinition.UNTYPED;

  /**
   * The graph where this predicate is stored. Defaults to value defined in the containing
   * Entity.
   */
  String graph() default "";

  /**
   * Marks an inverse association. Instead of s p o, load/save as o p s where
   * s is the Id for the containing Entity and p is the uri for this predicate and o the
   * value of this field. Defaults to 'false' if no {@link #ref reference} attribute is configured.
   */
  BT inverse() default BT.UNDEFINED;

  /**
   * Marks the backing triples for this field as not owned by this entity and is therefore used
   * only for load. Updates of the entity will skip the rdf statements corresponding to this
   * property. By default all triples for a property are owned by the entity if no
   * {@link #ref reference} attribute is configured.
   */
  BT notOwned() default BT.UNDEFINED;

  /**
   * Collection Type of this property. Applicable only for arrays and java.util.Collection
   * properties. Default is {@link org.topazproject.otm.CollectionType#PREDICATE} for collections
   * when there is no {@link #ref reference} configured.
   */
  CollectionType collectionType() default CollectionType.UNDEFINED;

  /**
   * Cascading preferences for this field. Applicable only for associations. Default is
   * {@link org.topazproject.otm.CascadeType#peer} when there is no {@link #ref reference}
   * configured.
   */
  CascadeType[] cascade() default {CascadeType.undefined};

  /**
   * Fetch preferences for this field. Default is {@link org.topazproject.otm.FetchType#lazy}
   * when there is no {@link #ref reference} configured for associations and fields with
   * {@link #collectionType} value of {@link org.topazproject.otm.CollectionType#PREDICATE}.
   * For all others this attribute is ignored and the effect is equivalent to specifying
   * {@link.org.topazproject.otm.FetchType#eager}.
   */
  FetchType fetch() default FetchType.undefined;
}
