/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion.itql;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.mapping.Mapper;

/**
 * Criterion Builder for comparison operations.
 *
 * @author Eric Brown
 * @author Pradeep Krishnan
 */
public class ComparisonCriterionBuilder implements CriterionBuilder {
  private static final String RSLV_MODEL    = "local:///topazproject#str";
  private String              resolverModel;

  /**
   * Creates a new ComparisonCriterionBuilder object.
   *
   * @param resolverModel the model uri for the resolver that implements comparison
   */
  public ComparisonCriterionBuilder(String resolverModel) {
    this.resolverModel                      = "<" + resolverModel + ">";
  }

  /**
   * Creates a new ComparisonCriterionBuilder object using a default resolver model.
   */
  public ComparisonCriterionBuilder() {
    this(RSLV_MODEL);
  }

  /*
   * inherited javadoc
   */
  public Criterion create(String func, Object[] args) throws OtmException {
    if (args.length < 2)
      throw new IllegalArgumentException(func + ": expecting at least 2 arguments");

    if (!(args[0] instanceof String))
      throw new IllegalArgumentException(func
                                         + ": argument 1 is 'name' and is expected to be a String");

    if (args[1] == null)
      throw new NullPointerException(func + ": argument 2 can not be null");

    if ("gt".equals(func))
      return new ComparisonCriterion((String) args[0], args[1], "<topaz:gt>", resolverModel);

    if ("lt".equals(func))
      return new ComparisonCriterion((String) args[0], args[1], "<topaz:lt>", resolverModel);

    throw new OtmException("Unknown function '" + func + "'");
  }

  /**
   * A criterion for a triple pattern where a predicate values satisfy a comparison operator.
   *
   * @author Eric Brown
   * @author Pradeep Krishnan
   */
  public static class ComparisonCriterion implements Criterion {
    private String resolverModel;
    private String name;
    private Object value;
    private String operator;

  /**
   * Creates a new ComparisonCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   * @param operator the comparison operator
   * @param resolverModel the model uri for the resolver that implements comparison
   */
    public ComparisonCriterion(String name, Object value, String operator, String resolverModel) {
      this.name            = name;
      this.value           = value;
      this.operator        = operator;
      this.resolverModel   = resolverModel;
    }

    /*
     * inherited javadoc
     */
    public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                  throws OtmException {
      ClassMetadata cm = criteria.getClassMetadata();
      Mapper        m  = cm.getMapperByName(name);

      if (m == null)
        throw new OtmException("'" + name + "' does not exist in " + cm);

      if (m.typeIsUri())
        throw new OtmException("'" + name + "' invalid comparison - cannot compare URIs");

      String val;

      try {
        val = (m.getSerializer() != null) ? m.getSerializer().serialize(value) : value.toString();
      } catch (Exception e) {
        throw new OtmException("Serializer exception", e);
      }

      val = "'" + ItqlHelper.escapeLiteral(val) + "'";

      if (m.getDataType() != null)
        val += ("^^<" + m.getDataType() + ">");

      String model = m.getModel();

      if (model == null)
        model = "";
      else {
        ModelConfig conf = criteria.getSession().getSessionFactory().getModel(model);

        if (conf == null)
          throw new OtmException("Model/Graph '" + model + "' is not configured in SessionFactory");

        model = " in <" + conf.getUri() + ">";
      }

      if (!m.hasInverseUri())
        return "(" + subjectVar + " <" + m.getUri() + "> " + varPrefix + model + " and "
               + varPrefix + " " + operator + " " + val + " in " + resolverModel + ")";

      return "(" + val + " <" + m.getUri() + "> " + varPrefix + model + " and " + varPrefix + " "
             + operator + " " + subjectVar + " in " + resolverModel + ")";
    }
  }
}
