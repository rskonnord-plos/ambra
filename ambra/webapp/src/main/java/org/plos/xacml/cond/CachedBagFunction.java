/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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
package org.plos.xacml.cond;

import java.io.Serializable;

import java.net.URI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.Evaluatable;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.Function;
import com.sun.xacml.ctx.Status;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * XACML extension function to pull a bag from cache. The arguments are:
 * 
 * <ul>
 * <li>
 * cache-name: the name of the cache from which to lookup/store
 * </li>
 * <li>
 * key: the key to lookup/store
 * </li>
 * <li>
 * bag: if a cache entry is missing, the bag to store into cache as well as to return
 * </li>
 * </ul>
 * 
 * The cache contents are String[] and not a XACML bag so that cache entries can be added/removed
 * by others.
 *
 * @author Pradeep Krishnan
 */
public class CachedBagFunction implements Function {
  /**
   * XACML function name for this function.
   */
  public static final String FUNCTION_CACHEDBAG =
    "urn:topazproject:names:tc:xacml:1.0:function:cached-bag";

  /**
   * URI version of StringAttribute's identifier
   */
  protected static final URI STRING_TYPE = URI.create(StringAttribute.identifier);

  /**
   * URI version of BooleanAttribute's identifier
   */
  protected static final URI BOOLEAN_TYPE = URI.create(BooleanAttribute.identifier);

  // shared logger
  private static final Log log = LogFactory.getLog(CachedBagFunction.class);

  // The identifier for this function.
  private URI identifier;

  // A List used by makeProcessingError() to save some steps.
  private static List processingErrList = null;

  /**
   * Creates a new PermissionFunction object.
   */
  public CachedBagFunction() {
    identifier = URI.create(FUNCTION_CACHEDBAG);
  }

  /*
   * @see com.sun.xacml.cond.Function#checkInputs
   */
  public final void checkInputs(List inputs) {
    if ((inputs == null) || (inputs.size() < 3))
      throw new IllegalArgumentException("not enough arguments to " + identifier);

    Evaluatable eval = (Evaluatable) inputs.get(0);

    if (eval.evaluatesToBag())
      throw new IllegalArgumentException("illegal argument type for arg1 of " + identifier
                                         + ". Bags are not supported for cache-name");

    eval = (Evaluatable) inputs.get(1);

    if (eval.evaluatesToBag())
      throw new IllegalArgumentException("illegal argument type for arg2 of " + identifier
                                         + ". Bags are not supported for cache-key");

    eval = (Evaluatable) inputs.get(2);

    if (!eval.evaluatesToBag())
      throw new IllegalArgumentException("illegal argument type for arg3 of " + identifier
                                         + ". Expecting a bag for value-bag");
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
    // First parameter is the cache-name
    EvaluationResult result = ((Evaluatable) inputs.get(0)).evaluate(context);

    if (result.indeterminate())
      return result;

    String  name  = result.getAttributeValue().encode();
    Ehcache cache = CacheManager.getInstance().getEhcache(name);

    // No cache configured; evaluate the bag and return
    if (cache == null) {
      if (log.isDebugEnabled())
        log.debug("cache " + name + " not found. evaluating bag function ...");

      return ((Evaluatable) inputs.get(2)).evaluate(context);
    }

    // Second parameter is the key
    result = ((Evaluatable) inputs.get(1)).evaluate(context);

    if (result.indeterminate())
      return result;

    String  key = result.getAttributeValue().encode();

    Element element = cache.get(key);

    if (element != null) {
      result = makeResult((Value) element.getValue());

      if (log.isDebugEnabled())
        log.debug(name + " cache hit(" + key + ")");
    } else {
      // Evaluate the bag only on cache-miss
      result = ((Evaluatable) inputs.get(2)).evaluate(context);

      if (result.indeterminate())
        return result;

      cache.put(new Element(key, makeValue(result)));

      if (log.isDebugEnabled())
        log.debug(name + " cache miss(" + key + ")");
    }

    return result;
  }

  /*
   * @see com.sun.xacml.cond.Function#getIdentifier
   */
  public final URI getIdentifier() {
    return identifier;
  }

  /**
   * Gets the return type of this function.
   *
   * @return returns {@link com.sun.xacml.attr.StringAttribute#identifier}
   *
   * @see com.sun.xacml.cond.Function#getReturnType
   */
  public final URI getReturnType() {
    return STRING_TYPE;
  }

  /**
   * Checks to see if this function returns a bag of results.
   *
   * @return returns false since permission evals are not bags
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
   * Create an <code>EvaluationResult</code> that contains the results from the cache.
   *
   * @param value a value object containing an array of result strings
   *
   * @return the desired <code>EvaluationResult</code>
   */
  protected EvaluationResult makeResult(Value value) {
    String[] results = value.getResults();
    ArrayList bag = new ArrayList(results.length);

    for (int i = 0; i < results.length; i++)
      bag.add(new StringAttribute(results[i]));

    BagAttribute attr = new BagAttribute(STRING_TYPE, bag);

    return new EvaluationResult(attr);
  }

  /**
   * Create a results array from an <code>EvaluationResult</code> suitable for storing in Cache.
   *
   * @param result the <code>EvaluationResult</code>
   *
   * @return returns a Value object containing an array of result strings
   */
  protected Value makeValue(EvaluationResult result) {
    BagAttribute attr    = (BagAttribute) result.getAttributeValue();
    String[]     results = new String[attr.size()];
    Iterator     it      = attr.iterator();

    for (int i = 0; i < results.length; i++)
      results[i] = ((AttributeValue) it.next()).encode();

    return new Value(results);
  }

  protected static class Value implements Serializable {
    private String[] results;

    public Value(String[] results) {
      this.results = results;
    }

    public String[] getResults() {
      return results;
    }

    public String toString() {
      String ret = "[";

      if (results.length > 0)
        ret += results[0];

      for (int i = 1; i < results.length; i++)
        ret += ", " + results[i];

      return ret + "]";
    }
  }
}
