/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml.finder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.PolicySet;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;

/**
 * This module represents a collection of resourcess containing polices, each of which will be
 * searched through when trying to find a policy that is applicable to a specific request. Based
 * on  {@link com.sun.xacml.finder.impl.FilePolicyModule}.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractPolicyModule extends PolicyFinderModule implements ErrorHandler {
  /**
   * The property which is used to specify the schema file to validate against (if any)
   */
  public static final String POLICY_SCHEMA_PROPERTY = "com.sun.xacml.PolicySchema";
  public static final String JAXP_SCHEMA_LANGUAGE =
    "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
  public static final String W3C_XML_SCHEMA     = "http://www.w3.org/2001/XMLSchema";
  public static final String JAXP_SCHEMA_SOURCE =
    "http://java.sun.com/xml/jaxp/properties/schemaSource";

  // the finder that is using this module
  private PolicyFinder finder;

  //
  private File schemaFile;

  //
  private Set resources;
  private Set policies;
  private Map policyMap;

  // the logger we'll use for all messages
  private static final Log logger = LogFactory.getLog(AbstractPolicyModule.class);

  /**
   * Constructor which retrieves the schema file to validate policies against from the
   * POLICY_SCHEMA_PROPERTY. If the retrieved property is null, then no schema validation will
   * occur.
   */
  public AbstractPolicyModule() {
    resources   = new HashSet();
    policies    = new HashSet();
    policyMap   = new HashMap();

    String schemaName = System.getProperty(POLICY_SCHEMA_PROPERTY);

    if (schemaName == null)
      schemaFile = null;
    else
      schemaFile = new File(schemaName);
  }

  /**
   * Constructor that uses the specified input as the schema file to validate policies against. If
   * schema validation is not desired, a null value should be used.
   *
   * @param schemaFile the schema file to validate policies against, or null if schema validation
   *        is not desired.
   */
  public AbstractPolicyModule(File schemaFile) {
    resources   = new HashSet();
    policies    = new HashSet();
    policyMap   = new HashMap();

    this.schemaFile = schemaFile;
  }

  /**
   * Constructor that specifies a set of initial policy resources to use. No schema validation is
   * performed.
   *
   * @param resources a <code>List</code> of <code>String</code>s that identify policy resources
   */
  public AbstractPolicyModule(List resources) {
    this();

    if (resources != null)
      this.resources.addAll(resources);
  }

  /**
   * Initializes the <code>ResourcePolicyModule</code> by loading the policies contained in the
   * collection of resources associated with this module. This method also uses the specified
   * <code>PolicyFinder</code> to help in instantiating PolicySets.
   *
   * @param finder a PolicyFinder used to help in instantiating PolicySets
   */
  public void init(PolicyFinder finder) {
    this.finder = finder;

    Iterator it = resources.iterator();

    while (it.hasNext()) {
      String         name   = (String) (it.next());
      AbstractPolicy policy = loadPolicy(name, finder);

      if (policy == null)
        continue;

      policies.add(policy);

      URI    id   = policy.getId();
      Object prev = policyMap.put(id, policy);

      if (prev == null)
        continue;

      String message = "Duplicate policy '" + id + "' found in resource '" + name + "'";
      logger.warn(message);

      // We have to cause an error in policy eval.
      Status status =
        new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR), message);

      // stick this in place of policy
      policyMap.put(id, status);
    }
  }

  /**
   * Adds a resource (containing a policy) to the collection of resources associated with this
   * module.
   *
   * @param resource the resource to add to this module's collection of resources
   *
   * @return true if this module did not already contain the specified resource
   */
  public boolean addPolicy(String resource) {
    return resources.add(resource);
  }

  /**
   * Loads a policy from the specified resource, using the specified <code>PolicyFinder</code> to
   * help with instantiating PolicySets, and using the specified input as the schema file to
   * validate policies against. If schema validation is not desired, a null value should be used
   * for schemaFile
   *
   * @param resource the resource to load the policy from
   * @param finder a PolicyFinder used to help in instantiating PolicySets
   *
   * @return a (potentially schema-validated) policy associated with the  specified resource, or
   *         null if there was an error
   */
  public AbstractPolicy loadPolicy(String resource, PolicyFinder finder) {
    try {
      // create the factory
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringComments(true);

      DocumentBuilder db = null;

      // as of 1.2, we always are namespace aware
      factory.setNamespaceAware(true);

      // set the factory to work the way the system requires
      if (schemaFile == null) {
        // we're not doing any validation
        factory.setValidating(false);

        db = factory.newDocumentBuilder();
      } else {
        // we're using a validating parser
        factory.setValidating(true);

        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        factory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);

        db = factory.newDocumentBuilder();
        db.setErrorHandler(this);
      }

      // try to load the policy resource
      Document doc = db.parse(getPolicyResourceAsStream(resource));

      // handle the policy, if it's a known type
      Element root = doc.getDocumentElement();
      String  name = root.getTagName();

      if (name.equals("Policy")) {
        return Policy.getInstance(root);
      } else if (name.equals("PolicySet")) {
        return PolicySet.getInstance(root, finder);
      } else {
        // this isn't a root type that we know how to handle
        throw new Exception("Unknown root document type: " + name);
      }
    } catch (Exception e) {
      if (logger.isWarnEnabled())
        logger.warn("Error reading policy from resource " + resource, e);
    }

    // a default fall-through in the case of an error
    return null;
  }

  /**
   * Gets the InputStream corresponding to the policy resource.
   *
   * @param resource the policy resource
   *
   * @return the InputStream
   *
   * @throws IOException if an error occured
   */
  public abstract InputStream getPolicyResourceAsStream(String resource)
                                                 throws IOException;

  /**
   * Finds a policy based on a request's context. This may involve using the request data as
   * indexing data to lookup a policy. This will always do a Target match to make sure that the
   * given policy applies. If more than one applicable policy is found, this will return an error.
   * NOTE: this is basically just a subset of the OnlyOneApplicable Policy Combining Alg that
   * skips the evaluation step. See comments in there for details on this algorithm.
   *
   * @param context the representation of the request data
   *
   * @return the result of trying to find an applicable policy
   */
  public PolicyFinderResult findPolicy(EvaluationCtx context) {
    AbstractPolicy selectedPolicy = null;
    Iterator       it = policies.iterator();

    while (it.hasNext()) {
      AbstractPolicy policy = (AbstractPolicy) (it.next());

      // see if we match
      MatchResult match  = policy.match(context);
      int         result = match.getResult();

      // if there was an error, we stop right away
      if (result == MatchResult.INDETERMINATE)
        return new PolicyFinderResult(match.getStatus());

      if (result == MatchResult.MATCH) {
        // if we matched before, this is an error...
        if (selectedPolicy != null) {
          Status status =
            new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR),
                       "too many applicable top-level policies");

          return new PolicyFinderResult(status);
        }

        // ...otherwise remember this policy
        selectedPolicy = policy;
      }
    }

    // if we found a policy, return it, otherwise we're N/A
    if (selectedPolicy != null)
      return new PolicyFinderResult(selectedPolicy);
    else

      return new PolicyFinderResult();
  }

  /**
   * Tries to find one and only one matching policy given the idReference If more than one policy
   * is found, this is an error and must be reported as such. If no policies are found, then an
   * empty result must be returned. By default this method returns an empty result. This method
   * should never return null.
   *
   * @param idReference an identifier specifying some policy
   * @param type type of reference (policy or policySet) as identified by the fields in
   *        <code>PolicyReference</code>
   *
   * @return the result of looking for a matching policy
   */
  public PolicyFinderResult findPolicy(URI idReference, int type) {
    Object o = policyMap.get(idReference);

    if ((o instanceof Policy) && (type == PolicyReference.POLICY_REFERENCE))
      return new PolicyFinderResult((Policy) o);

    if ((o instanceof PolicySet) && (type == PolicyReference.POLICYSET_REFERENCE))
      return new PolicyFinderResult((PolicySet) o);

    if (o instanceof Status)
      return new PolicyFinderResult((Status) o);

    return new PolicyFinderResult();
  }

  /**
   * Standard handler routine for the XML parsing.
   *
   * @param exception information on what caused the problem
   *
   * @throws SAXException DOCUMENT ME!
   */
  public void warning(SAXParseException exception) throws SAXException {
    if (logger.isWarnEnabled())
      logger.warn("Warning on line " + exception.getLineNumber() + ": " + exception.getMessage());
  }

  /**
   * Standard handler routine for the XML parsing.
   *
   * @param exception information on what caused the problem
   *
   * @throws SAXException always to halt parsing on errors
   */
  public void error(SAXParseException exception) throws SAXException {
    if (logger.isWarnEnabled())
      logger.warn("Error on line " + exception.getLineNumber() + ": " + exception.getMessage()
                  + " ... " + "Policy will not be available");

    throw new SAXException("error parsing policy");
  }

  /**
   * Standard handler routine for the XML parsing.
   *
   * @param exception information on what caused the problem
   *
   * @throws SAXException always to halt parsing on errors
   */
  public void fatalError(SAXParseException exception) throws SAXException {
    if (logger.isWarnEnabled())
      logger.warn("Fatal error on line " + exception.getLineNumber() + ": "
                  + exception.getMessage() + " ... " + "Policy will not be available");

    throw new SAXException("fatal error parsing policy");
  }
}
