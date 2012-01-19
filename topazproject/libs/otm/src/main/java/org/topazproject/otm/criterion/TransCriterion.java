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
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.mapping.Mapper;

/**
 * A criterion for a triple pattern where the predicate and value is matched transitively.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE + "/trans")
public class TransCriterion extends AbstractBinaryCriterion {
  /**
   * Creates a new TransCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public TransCriterion(String name, Object value) {
    super(name, value);
  }

  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(getFieldName());

    if (m == null)
      throw new OtmException("'" + getFieldName() + "' does not exist in " + cm);
    if (!m.typeIsUri())
      throw new OtmException("Value must be a uri for trans(): field is "
                             + m.getField().toGenericString());

    String val = serializeValue(getValue(), criteria, getFieldName());
    String model = m.getModel();

    if ((model == null) || model.equals(cm.getModel()))
      model = "";
    else {
      ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

      if (conf == null)
        throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

      model = " in <" + conf.getUri() + ">";
    }

    String subj   = m.hasInverseUri() ? val : subjectVar;
    String obj    = m.hasInverseUri() ? subjectVar : val;
    String triple = subj + " <" + m.getUri() + "> " + obj + model;

    return "(trans(" + triple + ") or " + triple + ")";
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'trans' is not supported by OQL (yet)");
  }
}
