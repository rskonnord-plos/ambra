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

import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;

/**
 * A criterion for doing a 'not' operation.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/not")
public class NotCriterion extends Criterion {
  private Criterion criterion;

  /**
   * Creates a new NotCriterion object.
   */
  public NotCriterion() {
  }

  /**
   * Creates a new NotCriterion object.
   *
   * @param criterion the criterion to NOT
   */
  public NotCriterion(Criterion criterion) {
    setCriterion(criterion);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    Criterion impl = new MinusCriterion(new PredicateCriterion(), criterion);

    return impl.toItql(criteria, subjectVar, varPrefix);
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'not' is not supported by OQL");
  }

  /**
   * Get criterion.
   *
   * @return criterion as Criterion.
   */
  public Criterion getCriterion() {
    return criterion;
  }

  /**
   * Set criterion.
   *
   * @param criterion the value to set.
   */
  public void setCriterion(Criterion criterion) {
    this.criterion = criterion;
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    return criterion.getParamNames();
  }

  public String toString() {
    return "Not[" + criterion + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
    criterion.onPreInsert(dc, cm);
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
    criterion.onPostLoad(dc, cm);
  }
}
