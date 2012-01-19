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
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion for a triple pattern where a chain is walked to the end returning all matched
 * triples.
 *
 * @author Pradeep Krishnan
 */
@Entity(type=Criterion.RDF_TYPE + "/walk")
public class WalkCriterion extends AbstractBinaryCriterion {

  public WalkCriterion() {
  }

  /**
   * Creates a new WalkCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   *
   */
  public WalkCriterion(String name, Object value) {
    super(name, value);
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
    Mapper        m  = cm.getMapperByName(getFieldName());

    if (m == null)
      throw new OtmException("'" + getFieldName() + "' does not exist in " + cm);
    if (!m.typeIsUri())
      throw new OtmException("Value must be a uri for walk(): field is "
                             + m.getField().toGenericString());

    String val = serializeValue(getValue(), criteria, getFieldName());
    String model = m.getModel();

    if ((model != null) && !cm.getModel().equals(model)) {
      ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

      if (conf == null)
        throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

      model = " in <" + conf.getUri() + ">";
    } else
      model = "";

    String query =
      m.hasInverseUri()
      ? ("walk(" + val + " <" + m.getUri() + "> " + subjectVar + model + " and " + varPrefix + " <"
      + m.getUri() + "> " + subjectVar + model + ")")
      : ("walk(" + subjectVar + " <" + m.getUri() + "> " + val + model + " and " + subjectVar + " <"
      + m.getUri() + "> " + varPrefix + model + ")");

    return query;
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'walk' is not supported by OQL (yet)");
  }
}
