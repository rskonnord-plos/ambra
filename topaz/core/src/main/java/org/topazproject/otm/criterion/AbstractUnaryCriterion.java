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

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A base class for all operations involving a field name. 
 *
 * @author Pradeep Krishnan
 *
 */
public abstract class AbstractUnaryCriterion extends Criterion {
  private static final Log log             = LogFactory.getLog(AbstractUnaryCriterion.class);
  private String           fieldName;

  /**
   * Used for persistence; ignore otherwise.
   *
   * @see DetachedCriteria
   */
  @Embedded
  public DeAliased da = new DeAliased();

  /**
   * Creates a new AbstractUnaryCriterion object.
   */
  public AbstractUnaryCriterion() {
  }

  /**
   * Creates a new AbstractUnaryCriterion object.
   *
   * @param name field/predicate name
   */
  public AbstractUnaryCriterion(String name) {
    setFieldName(name);
  }

  /**
   * Get fieldName.
   *
   * @return fieldName as String.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Set fieldName.
   *
   * @param fieldName the value to set.
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }


  public String toString() {
    return getClass().getName().replace("org.topazproject.otm.criterion.", "").
           replace("Criterion", "") + "[" + getFieldName() + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    Mapper r = cm.getMapperByName(fieldName);
    if (!(r instanceof RdfMapper))
      log.warn("onPreInsert: The field '" + fieldName + "' does not exist in " + cm);
    else {
      RdfMapper m  = (RdfMapper)r;
      da.predicateUri = URI.create(m.getUri());
      da.inverse = m.hasInverseUri();

      if (m.isAssociation()) {
        ClassMetadata assoc = ses.getSessionFactory().getClassMetadata(m.getAssociatedEntity());
        if (assoc != null)
          da.rdfType        = assoc.getTypes();
      }

      if (log.isDebugEnabled())
        log.debug("onPreInsert: Converted field '" + fieldName + "' to " + da + " in " + cm);
    }
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    RdfMapper m = (da.predicateUri == null) ? null :
                  cm.getMapperByUri(ses.getSessionFactory(), da.predicateUri.toString(),
                      da.inverse, da.rdfType);

    if (m == null)
      log.warn("onPostLoad: " + da + " not found in " + cm);
    else {
      fieldName = m.getName();
      if (log.isDebugEnabled())
        log.debug("onPostLoad: Converted " + da + " to '" + fieldName + "' in " + cm);
    }
  }

  /**
   * A class to hold the predicate-uri and the mapping direction (inverse or not)
   * for a field name supplied in creating this Criterion. This information is
   * persisted when the Criterion is persisted allowing the re-construction of a
   * field name on retrieval even when the field name has changed in the java class.
   * <p/>
   * This also has the additional advantage that what is stored in the persistence
   * store has some meaning outside of the java class that this Criteria is tied to.
   */
  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    public URI              predicateUri;
    public boolean          inverse;
    @Predicate(type=Predicate.PropType.OBJECT)
    public Set<String>      rdfType;

    public String toString() {
      return "[predicateUri: <" + predicateUri + ">, inverse: " + inverse + "]";
    }
  }
}
