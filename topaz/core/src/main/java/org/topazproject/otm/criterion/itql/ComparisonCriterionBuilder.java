/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.criterion.itql;

import java.net.URI;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.GraphConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.criterion.Criterion;
import org.topazproject.otm.criterion.CriterionBuilder;
import org.topazproject.otm.criterion.DetachedCriteria;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * Criterion Builder for comparison operations.
 *
 * @author Eric Brown
 * @author Pradeep Krishnan
 */
public class ComparisonCriterionBuilder implements CriterionBuilder {
  private static final URI RSLV_GRAPH_TYPE =
                                      URI.create("http://topazproject.org/graphs#StringCompare");
  private URI              resolverGraphType;

  /**
   * Creates a new ComparisonCriterionBuilder object.
   *
   * @param resolverGraphType the graph type uri for the resolver that implements comparison
   */
  public ComparisonCriterionBuilder(URI resolverGraphType) {
    this.resolverGraphType = resolverGraphType;
  }

  /**
   * Creates a new ComparisonCriterionBuilder object using a default resolver graph.
   */
  public ComparisonCriterionBuilder() {
    this(RSLV_GRAPH_TYPE);
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

    return new ComparisonCriterion((String) args[0], args[1], "<topaz:" + func + ">",
                                   resolverGraphType);
  }

  /**
   * A criterion for a triple pattern where a predicate values satisfy a comparison operator.
   *
   * @author Eric Brown
   * @author Pradeep Krishnan
   */
  public static class ComparisonCriterion extends Criterion {
    private URI    resolverGraphType;
    private String name;
    private Object value;
    private String operator;

    /**
     * Creates a new ComparisonCriterion object.
     *
     * @param name field/predicate name
     * @param value field/predicate value
     * @param operator the comparison operator
     * @param resolverGraphType the graph type uri for the resolver that implements comparison
     */
    public ComparisonCriterion(String name, Object value, String operator, URI resolverGraphType) {
      this.name              = name;
      this.value             = value;
      this.operator          = operator;
      this.resolverGraphType = resolverGraphType;
    }

    /*
     * inherited javadoc
     */
    public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                  throws OtmException {
      ClassMetadata cm = criteria.getClassMetadata();
      RdfMapper m  = getMapper(cm, name);
      String val   = serializeValue(value, criteria, name);
      String graph = m.getGraph();
      if ((graph != null) && !cm.getGraph().equals(graph))
        graph = " in <" + getGraphUri(criteria, graph) + ">";
      else
        graph = "";

      List<GraphConfig> resolverGraphs =
          criteria.getSession().getSessionFactory().getGraphs(resolverGraphType);
      if (resolverGraphs == null)
        throw new OtmException("No graph for type '" + resolverGraphType + "' has been configured" +
                               " in SessionFactory");
      String resolverGraph = "<" + resolverGraphs.get(0).getUri() + ">";

      if (m.hasInverseUri() && (m.getColType() != CollectionType.PREDICATE))
            throw new OtmException("Can't query across a " + m.getColType()
                + " for an inverse mapped field '" + name + "' in " + cm);

      String query;
      switch(m.getColType()) {
        case PREDICATE:
           if ( m.hasInverseUri())
             query = varPrefix + " < " + m.getUri() + "> " + subjectVar + graph;
           else
             query = subjectVar + " <" + m.getUri() + "> " + varPrefix + graph;
         break;
        case RDFSEQ:
        case RDFBAG:
        case RDFALT:
          String seq = varPrefix + "seqS";
          String seqPred = varPrefix + "seqP";
          query = subjectVar + " <" + m.getUri() + "> " + seq + graph
             + " and " + seq +  " " + seqPred + " " + varPrefix + graph
             + " and " + seqPred + " <mulgara:prefix> <rdf:_> in <"
             + getPrefixGraph(criteria) + ">";
          break;
        case RDFLIST:
          String list = varPrefix + "list";
          String rest = varPrefix + "rest";
          query = subjectVar + " <" + m.getUri() + "> " + list + graph
             + " and (" + list + " <rdf:first> " + varPrefix +  graph
             + " or ((trans(" + list + " <rdf:rest> " + rest + ")" + graph
             + " or " + list + " <rdf:rest> " + rest + graph
             + ") and " + rest +  " <rdf:first> " + varPrefix + graph + "))";
          break;
        default:
          throw new OtmException(m.getColType() + " not supported; field = "
             + name + " in " + cm);
      }

      return "(" + query + " and "
               + varPrefix + " " + operator + " " + val + " in " + resolverGraph + ")";
    }

    /*
     * inherited javadoc
     */
    public String toOql(Criteria criteria, String subjectVar, String varPrefix)
                  throws OtmException {
      String res;

      if (operator.equals("<topaz:gt>"))
        res = "gt(";
      else if (operator.equals("<topaz:ge>"))
        res = "ge(";
      else if (operator.equals("<topaz:lt>"))
        res = "lt(";
      else if (operator.equals("<topaz:le>"))
        res = "le(";
      else
        throw new OtmException("Internal error: unknown operator '" + operator + "'");

      res += subjectVar + "." + name + ", " + serializeValue(value, criteria, name) + ")";

      return res;
    }

    /*
     * inherited javadoc
     */
    public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
      throw new UnsupportedOperationException("Not meant to be persisted");
    }

    /*
     * inherited javadoc
     */
    public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
      throw new UnsupportedOperationException("Not meant to be persisted");
    }
  }
}
