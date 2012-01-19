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
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;

/**
 * A base class for all operations involving a field name. 
 *
 * @author Pradeep Krishnan
 *
 */
public abstract class AbstractUnaryCriterion extends Criterion {
  private static final Log log             = LogFactory.getLog(AbstractUnaryCriterion.class);
  private String           fieldName;

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
  public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
    Mapper m = cm.getMapperByName(fieldName);
    if (m == null)
      log.warn("onPreInsert: The field '" + fieldName + "' does not exist in " + cm);
    else {
      da.predicateUri = URI.create(m.getUri());
      da.inverse = m.hasInverseUri();
      if (log.isDebugEnabled())
        log.debug("onPreInsert: Converted field '" + fieldName + "' to " + da + " in " + cm);
    }
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
    Mapper m = (da.predicateUri == null) ? null :
                          cm.getMapperByUri(da.predicateUri.toString(), da.inverse, null);

    if (m == null)
      log.warn("onPostLoad: " + da + " not found in " + cm);
    else {
      fieldName = m.getName();
      if (log.isDebugEnabled())
        log.debug("onPostLoad: Converted " + da + " to '" + fieldName + "' in " + cm);
    }
  }

  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    public URI              predicateUri;
    public boolean          inverse;

    public String toString() {
      return "[predicateUri: <" + predicateUri + ">, inverse: " + inverse + "]";
    }
  }
}
