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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion for a triple pattern where the predicate and value are known.
 *
 * @author Pradeep Krishnan
 */
public class PredicateCriterion extends AbstractBinaryCriterion {

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public PredicateCriterion(String name, Object value) {
    super(name, value);
  }

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   */
  public PredicateCriterion(String name) {
    super(name, null);
  }

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   */
  public PredicateCriterion() {
    super(null, null);
  }


  /**
   * Gets the field/predicate name.
   *
   * @return field/predicate name
   */
  public String getName() {
    return getFieldName();
  }


  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    boolean unboundPredicate = getFieldName() == null;
    boolean unboundValue     = getValue() == null;

    if (unboundPredicate)
      return subjectVar + " " + varPrefix + "p " + varPrefix + "v";

    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(getFieldName());

    if (m == null)
      throw new OtmException("'" + getFieldName() + "' does not exist in " + cm);

    String val;

    if (unboundValue)
      val = varPrefix + "v";
    else
      val = serializeValue(getValue(), criteria, getFieldName());

    String query =
      m.hasInverseUri() ? (val + " <" + m.getUri() + "> " + subjectVar)
      : (subjectVar + " <" + m.getUri() + "> " + val);

    String model = m.getModel();

    if (model != null) {
      ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

      if (conf == null)
        throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

      model = " in <" + conf.getUri() + ">";
    }

    if (model != null)
      query += model;

    return query;
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    boolean unboundPredicate = getFieldName() == null;
    boolean unboundValue     = getValue() == null;

    if (unboundValue)
      throw new OtmException("unbound value not supported in OQL (yet)");

    String res = subjectVar;

    if (unboundPredicate)
      res += ".{" + varPrefix + "p ->}";
    else
      res += "." + getFieldName();

    if (unboundValue)
      res += " != null";
    else
      res += " = " + serializeValue(getValue(), criteria, getFieldName());

    return res;
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }
}
