/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xacml;

import java.io.ByteArrayOutputStream;

import java.net.URI;

import java.security.Principal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xacml.BasicEvaluationCtx;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Obligation;
import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.ctx.Subject;
import com.sun.xacml.finder.AttributeFinder;

/**
 * A base class for Deny-Biased-PEP implementations.
 *
 * @author Pradeep Krishnan
 */
public class DenyBiasedPEP {
  private static final Log log                      = LogFactory.getLog(DenyBiasedPEP.class);
  private static final URI STR_ATTR_TYPE            = URI.create(StringAttribute.identifier);
  private static final URI URI_ATTR_TYPE            = URI.create(AnyURIAttribute.identifier);
  private static final URI SUBJECT_CATEGORY_DEFAULT =
    URI.create(AttributeDesignator.SUBJECT_CATEGORY_DEFAULT);
  private PDP              pdp;
  private AttributeFinder  attrFinder;

  /**
   * Constructs a DenyBiasedPEP with the given PDP.
   *
   * @param pdp The PDP to evaluate requests against
   */
  public DenyBiasedPEP(PDP pdp) {
    this(pdp,
         (pdp instanceof PDPFactory.TopazPDP) ? ((PDPFactory.TopazPDP) pdp).getAttributeFinder()
         : null);
  }

  /**
   * Constructs a DenyBiasedPEP with the given PDP.
   *
   * @param pdp The PDP to evaluate requests against
   * @param attrFinder the attribute finder used by the PDP
   */
  public DenyBiasedPEP(PDP pdp, AttributeFinder attrFinder) {
    this.pdp          = pdp;
    this.attrFinder   = attrFinder;
  }

  /**
   * Get the pdp used by this Pep.
   *
   * @return the pdp
   */
  public PDP getPdp() {
    return pdp;
  }

  /**
   * Evaluates a request. The steps are as follows:
   * 
   * <ul>
   * <li>
   * Evaluates the request against the PDP. Any result other than PERMIT is considered a failure.
   * Any <code>Obligation</code> other than what is known to the PEP is a failure. No explicit
   * PERMIT in the response is considered a failure.
   * </li>
   * <li>
   * Throws a <code>SecurityException</code> on failure.
   * </li>
   * <li>
   * Otherwise collects <code>Obligation</code> objects from all <code>Result</code> objects and
   * returns the Set, ignoring the resource of the <code>Result</code>
   * </li>
   * </ul>
   * 
   *
   * @param request The requet to evaluate.
   * @param knownObligations The Set of known obligation URIs that the PEP is prepared to fulfill.
   *
   * @return The Set of Obligations that the PEP must fulfill.
   *
   * @throws SecurityException to indicate evaluation results other than an explicit PERMIT.
   */
  public Set evaluate(RequestCtx request, Set knownObligations) {
    EvaluationCtx ctx;

    try {
      ctx = new BasicEvaluationCtx(request, attrFinder, true);
    } catch (ParsingException e) {
      throw (SecurityException) new SecurityException("").initCause(e);
    }

    ResponseCtx response = pdp.evaluate(ctx);
    Set         results  = response.getResults();
    Decision    decision = new Decision(knownObligations);

    Iterator    it = results.iterator();

    while (it.hasNext()) {
      Result result = (Result) it.next();

      decision.analyze(result);

      Iterator oit = result.getObligations().iterator();

      while (oit.hasNext()) {
        Obligation o = (Obligation) oit.next();

        if (!o.getId().toString().equals("log"))
          decision.analyze(o);
        else if (log.isInfoEnabled())
          log.info(getLogMsg(o, result.getResource(), ctx));
      }
    }

    if (!decision.isPermit())
      throw new SecurityException(decision.explain());

    if (decision.hasExplanation())
      log.warn(decision.explain());

    return decision.getObligations();
  }

  /**
   * Logs a 'log' obligation. Helpful in identifying the policy that effected the decision.
   * 
   * <p>
   * Currently all it does is to look for the action and user from the request and logs them along
   * with the resource from the result and the policy-id supplied by the obligation.
   * </p>
   *
   * @param logObligation the logObligation that contains the policy that is firing this
   * @param resource the resource associated with the result
   * @param ctx the eval context
   *
   * @return Returns a log message
   */
  protected String getLogMsg(Obligation logObligation, String resource, EvaluationCtx ctx) {
    String   user   = getUser(ctx);
    String   action = getAction(ctx);
    String   policy = null;

    Iterator it = logObligation.getAssignments().iterator();

    while (it.hasNext()) {
      Attribute attr = (Attribute) (it.next());

      if ("policy".equals(attr.getId().toString()))
        policy = attr.getValue().encode();
    }

    String effects =
      (logObligation.getFulfillOn() == Result.DECISION_PERMIT) ? "' permits '" : "' denies '";

    return "'" + policy + effects + user + "' to do '" + action + "' on '" + resource + "'";
  }

  private String getAction(EvaluationCtx ctx) {
    EvaluationResult result = ctx.getActionAttribute(STR_ATTR_TYPE, Util.ACTION_ID, null);

    if (result.indeterminate())
      return "[undefined action]";

    BagAttribute bag = (BagAttribute) result.getAttributeValue();

    if (bag.isEmpty())
      return "[undefined action]";

    return ((StringAttribute) bag.iterator().next()).getValue();
  }

  private String getUser(EvaluationCtx ctx) {
    EvaluationResult result =
      ctx.getSubjectAttribute(URI_ATTR_TYPE, Util.SUBJECT_ID, SUBJECT_CATEGORY_DEFAULT);

    if (result.indeterminate())
      return "[undefined user]";

    BagAttribute bag = (BagAttribute) result.getAttributeValue();

    if (bag.isEmpty())
      return "[undefined user]";

    return ((AnyURIAttribute) bag.iterator().next()).getValue().toString();
  }

  /**
   * Decision support class for this pep.
   */
  public static class Decision {
    private int          permit           = 0;
    private int          deny             = 0;
    private int          inapplicable     = 0;
    private int          indeterminate    = 0;
    private int          unfulfillable    = 0;
    private StringBuffer explanation      = new StringBuffer();
    private Set          obligations      = new HashSet();
    private Set          knownObligations;

    public Decision(Set knownObligations) {
      this.knownObligations = knownObligations;
    }

    public boolean isPermit() {
      return (permit > 0) && (deny == 0) && (inapplicable == 0) && (indeterminate == 0)
              && (unfulfillable == 0);
    }

    public Set getObligations() {
      return obligations;
    }

    public boolean hasExplanation() {
      return (explanation.length() > 0) || (permit == 0);
    }

    public String explain() {
      if ((explanation.length() == 0) && (permit == 0))
        return "No explicit permissions";

      return explanation.toString();
    }

    public void analyze(Result result) {
      switch (result.getDecision()) {
      case Result.DECISION_PERMIT:
        permit++;

        break;

      case Result.DECISION_DENY:
        deny++;
        addExplanation("A XACML policy denied acess to ", result.getResource());

        break;

      case Result.DECISION_NOT_APPLICABLE:
        inapplicable++;
        addExplanation("No applicable XACML policies for ", result.getResource());

        break;

      case Result.DECISION_INDETERMINATE:
        indeterminate++;
        addExplanation("XACML policy evaluation error:", result.getStatus());

        break;

      default:
        indeterminate++;
        addExplanation("Unknown decision " + result.getDecision());
      }
    }

    public void analyze(Obligation o) {
      if (!knownObligations.contains(o.getId())) {
        unfulfillable++;
        addExplanation("XACML policy contains an obligation that this PEP cannot"
                       + " fulfill. The obligation id is " + o.getId());

        return;
      }

      if (o.getFulfillOn() != Result.DECISION_PERMIT) {
        unfulfillable++;
        addExplanation("XACML policy contains an obligation that this PEP can "
                       + " fulfill only on a PERMIT. The obligation id is " + o.getId());

        return;
      }

      if (!obligations.add(o)) {
        // xxx: currently duplicates are not an error; revisit later
        addExplanation("XACML policy contains a duplicate obligation. The obligation id is "
                       + o.getId());
      }
    }

    public void addExplanation(String msg) {
      if (explanation.length() != 0)
        explanation.append(System.getProperty("line.separator"));

      explanation.append(msg);
    }

    public void addExplanation(String msg, String arg) {
      addExplanation(msg);
      explanation.append(arg);
    }

    public void addExplanation(String msg, Status status) {
      ByteArrayOutputStream out = new ByteArrayOutputStream(512);
      status.encode(out);
      addExplanation(msg, out.toString());
    }
  }
}
