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
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A criterion for a triple pattern where the predicate and value is matched transitively. eg.
 *
 * <pre>
 *   // Suppose Annotation a1 is superseded by a2 and a2 is superseded by a3, the following
 *   // query on a3 will return a list containing a1 and a2.
 *   List&lt;Annotation&gt; l =
 *               session.createCriteria(Annotation.class)
 *                       .add(Restrictions.walk("supersededBy", a3.id)).list();
 * </pre>
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
    RdfMapper     m  = getMapper(cm, getFieldName());

    if (!m.typeIsUri())
      throw new OtmException("Value must be a uri for trans(): field is "
                             + m.getName());

    String val = serializeValue(getValue(), criteria, getFieldName());
    String model = m.getModel();

    if ((model != null) && !cm.getModel().equals(model))
      model = " in <" + getModelUri(criteria, model) + ">";
    else
      model = "";

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
