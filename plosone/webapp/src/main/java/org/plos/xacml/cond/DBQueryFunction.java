/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.xacml.cond;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AttributeFactory;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;

/**
 * An abstract class that implements a XACML extension function to execute a database query. The
 * arguments to the function are a configuration identifier for the database, the query string,
 * and followed by any bind values.
 *
 * @author Pradeep Krishnan
 */
public abstract class DBQueryFunction implements Function {
  /**
   * A base URI for all DB Query functions.
   */
  public static final String FUNCTION_BASE = "urn:topazproject:names:tc:xacml:1.0:function:query:";

  /**
   * URI version of StringAttribute's identifier
   */
  protected static final URI STRING_TYPE = URI.create(StringAttribute.identifier);

  // shared logger
  private static final Log log = LogFactory.getLog(DBQueryFunction.class);

  // The identifier for this function.
  private URI identifier;

  // The return-type for this function.
  private URI returnType;

  // A List used by makeProcessingError() to save some steps.
  private static List processingErrList = null;

  /**
   * Creates a new DBQueryFunction object.
   *
   * @param functionName The function name as it appears in XACML policies
   */
  public DBQueryFunction(String functionName) {
    identifier   = URI.create(functionName);
    returnType   = STRING_TYPE;
  }

  /**
   * Creates a new DBQueryFunction object.
   *
   * @param functionName The function name as it appears in XACML policies
   * @param returnType The return type for this function
   */
  public DBQueryFunction(String functionName, URI returnType) {
    this.identifier   = URI.create(functionName);
    this.returnType   = returnType;
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputs
   */
  public final void checkInputs(List inputs) {
    if ((inputs == null) || (inputs.size() < 2))
      throw new IllegalArgumentException("not enough arguments to " + identifier);

    Iterator it = inputs.iterator();

    for (int i = 0; it.hasNext(); i++) {
      Evaluatable eval = (Evaluatable) it.next();

      if (eval.evaluatesToBag())
        throw new IllegalArgumentException("illegal argument type. bags are not supported");

      // conf and query must be strings
      if ((i < 2) && (!eval.getType().equals(STRING_TYPE)))
        throw new IllegalArgumentException("illegal argument type. must be "
                                           + StringAttribute.identifier);
    }
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputsNoBag
   */
  public final void checkInputsNoBag(List inputs) {
    checkInputs(inputs);
  }

  /*
   * @see com.sun.xacml.cond.Function#evaluate
   */
  public final EvaluationResult evaluate(List inputs, EvaluationCtx context) {
    Iterator it = inputs.iterator();

    // First parameter is the config id
    EvaluationResult result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String conf = result.getAttributeValue().encode();

    // Second parameter is the query
    result = ((Evaluatable) it.next()).evaluate(context);

    if (result.indeterminate())
      return result;

    String query = result.getAttributeValue().encode();

    // Rest are all bindings
    String[] bindings = new String[inputs.size() - 2];

    for (int i = 0; it.hasNext(); i++) {
      result = ((Evaluatable) it.next()).evaluate(context);

      if (result.indeterminate())
        return result;

      bindings[i] = result.getAttributeValue().encode();
    }

    // Execute the query
    try {
      return executeQuery(context, conf, query, bindings);
    } catch (QueryException e) {
      log.warn(e.getMessage(), e);

      return makeProcessingError(e.getMessage());
    }
  }

  /**
   * Executes a database specific query. The query currently is supposed to return only a single
   * column of results.
   *
   * @param context The xacml evaluation context
   * @param conf A database specific configuration identifier; eg. a connect String
   * @param query A database specific query
   * @param bindings An array of values that may be bound to the query before executing it.
   *
   * @return Returns an <code>EvaluationResult</code> with a bag of values or a processing error
   *         status
   *
   * @throws QueryException to indicate a failure in query execution
   */
  public abstract EvaluationResult executeQuery(EvaluationCtx context, String conf, String query,
                                                String[] bindings)
                                         throws QueryException;

  /*
   * @see com.sun.xacml.cond.Function#getIdentifier
   */
  public final URI getIdentifier() {
    return identifier;
  }

  /**
   * Gets the return type of this function.
   *
   * @return returns a URI
   *
   * @see com.sun.xacml.cond.Function#getReturnType
   */
  public final URI getReturnType() {
    return returnType;
  }

  /**
   * Checks to see if this function returns a bag of results.  Query results are a bag. So always
   * returns true.
   *
   * @return returns true
   *
   * @see com.sun.xacml.cond.Function#returnsBag
   */
  public final boolean returnsBag() {
    return true;
  }

  /**
   * Create an <code>EvaluationResult</code> that indicates a processing error with the specified
   * message.
   *
   * @param message a description of the error (<code>null</code> if none)
   *
   * @return the desired <code>EvaluationResult</code>
   */
  protected EvaluationResult makeProcessingError(String message) {
    if (processingErrList == null) {
      String[] errStrings = { Status.STATUS_PROCESSING_ERROR };
      processingErrList = Arrays.asList(errStrings);
    }

    Status           errStatus       = new Status(processingErrList, message);
    EvaluationResult processingError = new EvaluationResult(errStatus);

    return processingError;
  }

  /**
   * Create an <code>EvaluationResult</code> that contains the results of the query.
   *
   * @param results an array of result strings
   *
   * @return the desired <code>EvaluationResult</code>
   *
   * @throws QueryException when there is an error in converting results to return-type
   */
  protected EvaluationResult makeResult(String[] results)
                                 throws QueryException {
    ArrayList        bag         = new ArrayList(results.length);
    AttributeFactory attrFactory = AttributeFactory.getInstance();
    URI              returnType  = getReturnType();

    try {
      for (int i = 0; i < results.length; i++)
        bag.add(attrFactory.createValue(returnType, results[i]));
    } catch (UnknownIdentifierException e) {
      throw new QueryException("Invalid return-type", e);
    } catch (ParsingException e) {
      throw new QueryException("Type conversion error while converting"
                               + " query results to return-type", e);
    }

    BagAttribute attr = new BagAttribute(returnType, bag);

    return new EvaluationResult(attr);
  }

  /**
   * Indicates an error in parsing and executing a query.
   */
  public static class QueryException extends Exception {
    /**
     * Creates a new instance with the given message.
     *
     * @param msg the error message
     */
    public QueryException(String msg) {
      super(msg);
    }

    /**
     * Creates a new instance with the given message and a cause.
     *
     * @param msg the error message
     * @param cause the cause of this exception
     */
    public QueryException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
