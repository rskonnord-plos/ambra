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
package org.topazproject.otm.criterion;

import java.net.URI;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

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
   * @param ses the session that is generating this event
   * @param dc the detached criteria that is being persisted
   * @param cm the class metadata to use to resolve fields
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    Mapper r = cm.getMapperByName(name);

    if (!(r instanceof RdfMapper))
      log.warn("onPreInsert: The field '" + name + "' does not exist in " + cm);
    else {
      RdfMapper      m  = (RdfMapper)r;
      da.predicateUri   = URI.create(m.getUri());
      da.inverse        = m.hasInverseUri();

      if (m.isAssociation()) {
        ClassMetadata assoc = ses.getSessionFactory().getClassMetadata(m.getAssociatedEntity());
        if (assoc != null)
          da.rdfType        = assoc.getTypes();
      }

      if (log.isDebugEnabled())
        log.debug("onPreInsert: Converted field '" + name + "' to " + da + " in " + cm);
    }
  }

  /**
   * Do post-load processing. Converting predicate-uri to field name.
   *
   * @param ses the session that is generating this event
   * @param dc the detached criteria that is being loaded
   * @param cm the class metadata to use to resolve fields
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    RdfMapper m =
      (da.predicateUri == null) ? null
      : cm.getMapperByUri(ses.getSessionFactory(), da.predicateUri.toString(), da.inverse, da.rdfType);

    if (m == null)
      log.warn("onPostLoad: " + da + " not found in " + cm);
    else {
      name = m.getName();

     if (log.isDebugEnabled())
        log.debug("onPostLoad: Converted " + da + " to '" + name + "' in " + cm);
    }
  }

  /**
   * A class to hold the predicate-uri and the mapping direction (inverse or not)
   * for a field name supplied in creating this Order by clause. This information is
   * persisted when the Criterion is persisted allowing the re-construction of a
   * field name on retrieval even when the field name has changed in the java class.
   * <p/>
   * This also has the additional advantage that what is stored in the persistence
   * store has some meaning outside of the java class that this Criteria is tied to.
   */
  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    public URI         predicateUri;
    public boolean     inverse;
    @Predicate(type=Predicate.PropType.OBJECT)
    public Set<String> rdfType = new HashSet<String>();

    public String toString() {
      return "[predicateUri: <" + predicateUri + ">, inverse: " + inverse + "]";
    }
  }
}
