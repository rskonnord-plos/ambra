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
 * A criterion for a triple pattern where a chain is walked to the end returning all matched
 * triples.
 *
 * @author Pradeep Krishnan
 */
public class WalkCriterion implements Criterion {
  private String name;
  private Object value;

/**
   * Creates a new WalkCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   *
   */
  public WalkCriterion(String name, Object value) {
    this.name    = name;
    this.value   = value;
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
   *   walk ($subject_variable <predicate_URI> <object_URI> and
   *         $subject_variable <predicate_URI> $object_variable)
   *     or
   *   walk (<subject_URI> <predicate_URI> $object_variable and
   *         $subject_variable <predicate_URI> $object_variable)
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(getName());

    if (m == null)
      throw new OtmException("'" + getName() + "' does not exist in " + cm);

    String val;

    if (m.typeIsUri())
      val = "<" + ItqlHelper.validateUri(getValue().toString(), getName()) + ">";
    else
      throw new OtmException("Value must be a uri for walk(): field is "
                             + m.getField().toGenericString());

    if (!m.hasInverseUri())
      return "walk(" + subjectVar + " <" + m.getUri() + "> " + val + " and " + subjectVar + " <"
             + m.getUri() + "> " + varPrefix + ")";

    String model = m.getInverseModel();

    if (model != null) {
      ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

      if (conf == null)
        throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

      model = " in <" + conf.getUri() + ">";
    }

    String query =
      "walk(" + val + " <" + m.getUri() + "> " + subjectVar + " and " + varPrefix + " <"
      + m.getUri() + "> " + subjectVar + ")";

    if (model != null)
      query += model;

    return query;
  }
}
