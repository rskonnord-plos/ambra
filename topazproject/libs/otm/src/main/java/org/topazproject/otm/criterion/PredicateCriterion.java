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

import org.topazproject.mulgara.itql.ItqlHelper;

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
public class PredicateCriterion implements Criterion {
  private String  name;
  private Object  value;
  private boolean unboundPredicate = false;
  private boolean unboundValue     = false;

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public PredicateCriterion(String name, Object value) {
    this.name    = name;
    this.value   = value;
  }

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   */
  public PredicateCriterion(String name) {
    this.name      = name;
    unboundValue   = true;
  }

  /**
   * Creates a new PredicateCriterion object.
   *
   * @param name field/predicate name
   */
  public PredicateCriterion() {
    unboundPredicate   = true;
    unboundValue       = true;
  }

  /**
   * Gets the field/predicate name.
   *
   * @return field/predicate name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the field/predicate value.
   *
   * @return field/predicate value
   */
  public Object getValue() {
    return value;
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    if (unboundPredicate)
      return subjectVar + " " + varPrefix + "p " + varPrefix + "v";

    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(getName());

    if (m == null)
      throw new OtmException("'" + getName() + "' does not exist in " + cm);

    String val;

    if (unboundValue)
      val = varPrefix + "v";
    else {
      try {
        val = (m.getSerializer() != null) ? m.getSerializer().serialize(getValue())
              : getValue().toString();
      } catch (Exception e) {
        throw new OtmException("Serializer exception", e);
      }

      if (m.typeIsUri())
        val = "<" + ItqlHelper.validateUri(val, getName()) + ">";
      else {
        val = "'" + ItqlHelper.escapeLiteral(val) + "'";

        if (m.getDataType() != null)
          val += (("^^<" + m.getDataType()) + ">");
      }
    }

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
}
