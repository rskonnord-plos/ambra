/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;

/**
 * Specification of an order-by on a Criteria.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/Order", model = Criterion.MODEL)
public class Order {
  private static final Log   log    = LogFactory.getLog(Order.class);
  @Predicate(uri = Criterion.NS + "fieldName")
  private String             name;
  @Predicate(uri = Criterion.NS + "order/ascending")
  private boolean            ascending;

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  @Id
  @GeneratedValue(uriPrefix = Criterion.RDF_TYPE + "/Order/Id/")
  public URI orderId;

  /**
   * De aliased field names for persistence. Ignored otherwise.
   */
  @Embedded
  public DeAliased da = new DeAliased();

  /**
   * Creates a new Order object.
   */
  public Order() {
  }

  /**
   * Creates a new Order object.
   *
   * @param name the field name to order by
   * @param ascending ascending/descending order
   */
  public Order(String name, boolean ascending) {
    this.name        = name;
    this.ascending   = ascending;
  }

  /**
   * Gets the name of the field to order by.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the field name.
   *
   * @param name the field name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Tests if ascending order.
   *
   * @return ascending or descending
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * Creates a new ascending order object.
   *
   * @param name the field name to order by
   *
   * @return the newly created Order object
   */
  public static Order asc(String name) {
    return new Order(name, true);
  }

  /**
   * Creates a new descending order object.
   *
   * @param name the field name to order by
   *
   * @return the newly created Order object
   */
  public static Order desc(String name) {
    return new Order(name, false);
  }

  /**
   * Get ascending.
   *
   * @return ascending as boolean.
   */
  public boolean getAscending() {
    return ascending;
  }

  /**
   * Set ascending.
   *
   * @param ascending the value to set.
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return "Order[" + name + (ascending ? " (asc)" : " (desc)") + "]";
  }

  /**
   * Do pre-insert processing. Converts the order by field names to predicate-uri
   *
   * @param dc the detached criteria that is being persisted
   * @param cm the class metadata to use to resolve fields
   */
  public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
    Mapper m = cm.getMapperByName(name);

    if (m == null)
      log.warn("onPreInsert: The field '" + name + "' does not exist in " + cm);
    else {
      da.predicateUri   = URI.create(m.getUri());
      da.inverse        = m.hasInverseUri();

      if (log.isDebugEnabled())
        log.debug("onPreInsert: Converted field '" + name + "' to " + da + " in " + cm);
    }
  }

  /**
   * Do post-load processing. Converting predicate-uri to field name.
   *
   * @param dc the detached criteria that is being loaded
   * @param cm the class metadata to use to resolve fields
   */
  public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
    Mapper m =
      (da.predicateUri == null) ? null
      : cm.getMapperByUri(da.predicateUri.toString(), da.inverse, null);

    if (m == null)
      log.warn("onPostLoad: " + da + " not found in " + cm);
    else {
      name = m.getName();

     if (log.isDebugEnabled())
        log.debug("onPostLoad: Converted " + da + " to '" + name + "' in " + cm);
    }
  }

  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    public URI     predicateUri;
    public boolean inverse;

    public String toString() {
      return "[predicateUri: <" + predicateUri + ">, inverse: " + inverse + "]";
    }
  }
}
