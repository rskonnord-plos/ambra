/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;

import antlr.collections.AST;

/**
 * This implements the generic part of the query processing, i.e. the parsing and transforms that
 * are not specific to the target query language. The first method invoked must be {@link
 * #prepareQuery prepareQuery()}, only after which {@link #getParameters getParameters()} and
 * {@link #applyParameterValues applyParameterValues()} may be invoked.
 *
 * @author Ronald Tschalär
 */
public class GenericQueryImpl extends QueryImplBase implements Cloneable {
  private final String            query;
  private final Log               log;
  private       List<String>      prepareWarnings = new ArrayList<String>();
  private final QueryParser       parsedQuery;
  private       FieldTranslator   preparedQuery;
  private       ParameterResolver resolvedQuery;

  /**
   * Create a new generic query impl instance.
   *
   * @param query the query string
   * @param log   the logger to use
   */
  public GenericQueryImpl(String query, Log log) throws OtmException {
    this.query = query;
    this.log   = log;

    QueryLexer lexer  = new QueryLexer(new StringReader(GenericQueryImpl.this.query));
    parsedQuery = doStep("parsing query", new QueryParser(lexer), new QueryStep<QueryParser>() {
      public void run(QueryParser parser) throws Exception {
        parser.query();
      }
    });
  }

  /**
   * Prepare the query for parameter resolving.
   *
   * @param sf the session-factory to use
   * @throws OtmException if an error occurred
   */
  public void prepareQuery(final SessionFactory sf) throws OtmException {
    if (preparedQuery != null)
      return;

    preparedQuery =
        doStep("parsing query", new FieldTranslator(sf), new QueryStep<FieldTranslator>() {
      public void run(FieldTranslator ft) throws Exception {
        ft.query(parsedQuery.getAST());
      }
    });
    prepareWarnings.addAll(warnings);
  }

  /**
   * Apply the given parameter values to the current query.
   *
   * @param params a map of parameter-name to parameter-value
   * @throws OtmException if an error occurred setting a parameter value
   * @throws IllegalStateException if {@link #prepareQuery prepareQuery()} hasn't been invoked yet
   */
  public void applyParameterValues(final Map<String, Object> params) throws OtmException {
    if (preparedQuery == null)
      throw new IllegalStateException("prepareQuery() hasn't been invoked yet");

    warnings.clear();
    warnings.addAll(prepareWarnings);

    resolvedQuery = doStep("setting parameter values for", new ParameterResolver(),
                           new QueryStep<ParameterResolver>() {
      final FieldTranslator pq = preparedQuery;
      public void run(ParameterResolver pr) throws Exception {
        pr.query(pq.getAST(), params);
      }
    });
  }

  /**
   * Get the list of parameter names found in the query.
   *
   * @return the found parameter names
   */
  public Set<String> getParameterNames() {
    return parsedQuery.getParameterNames();
  }

  /**
   * Get the list of parameters found in the query. This is only valid after invoking
   * {@link #prepareQuery prepareQuery()}.
   *
   * @return the found parameters; the keys are the parameter names.
   * @throws IllegalStateException if {@link #prepareQuery prepareQuery()} hasn't been invoked yet
   */
  public Map<String, ExprType> getParameters() {
    if (preparedQuery == null)
      throw new IllegalStateException("prepareQuery() hasn't been invoked yet");

    return preparedQuery.getParameters();
  }

  /** 
   * Get the AST of the query with all parameters resolved. This is only valid after invoking
   * {@link #applyParameterValues applyParameterValues()}.
   * 
   * @return the query AST
   */
  public AST getResolvedQuery() {
    if (resolvedQuery == null)
      throw new IllegalStateException("applyParameterValues() hasn't been invoked yet");
    return resolvedQuery.getAST();
  }

  /**
   * Turn this query into a Criteria, if possible.
   *
   * @param sess the session
   * @throws OtmException if an error occurred
   */
  public Criteria toCriteria(Session sess) throws OtmException {
    CriteriaGenerator cg = doStep("creating criteria for", new CriteriaGenerator(sess),
                                  new QueryStep<CriteriaGenerator>() {
      public void run(CriteriaGenerator cg) throws Exception {
        cg.query(parsedQuery.getAST());
      }
    });
    return cg.getCriteria();
  }

  @Override
  public String toString() {
    return query;
  }

  @Override
  public GenericQueryImpl clone() {
    try {
      GenericQueryImpl c = (GenericQueryImpl) super.clone();
      c.prepareWarnings = new ArrayList<String>(prepareWarnings);
      return c;
    } catch (CloneNotSupportedException cnse) {
      throw new Error("Unexpected exception", cnse);    // can't happen
    }
  }

  /**
   * Do one step in query processing. This will do the various error handling.
   */
  private <T extends ErrorCollector> T doStep(String op, T parser, QueryStep<T> qStep)
      throws OtmException {
    try {
      qStep.run(parser);
      checkMessages(parser.getErrors(), parser.getWarnings(), query);
      return parser;
    } catch (OtmException oe) {
      throw oe;
    } catch (Exception e) {
      if (parser != null && parser.getErrors().size() > 0) {
        // exceptions are usually the result of aborted parsing due to errors
        log.debug("error parsing query: " + parser.getErrors(null), e);
        throw new QueryException("error " + op + " '" + query + "'", parser.getErrors());
      } else
        throw new QueryException("error " + op + " '" + query + "'", e);
    }
  }

  /** query-step runner used by {@link @doStep doStep()} */
  private static interface QueryStep<T> {
    /** do the single query proecessing step */
    public void run(T parser) throws Exception;
  }
}
