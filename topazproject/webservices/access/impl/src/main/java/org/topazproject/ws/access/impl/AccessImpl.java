/*
 * $HeadURL::                                                                             $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.access.impl;

import java.io.IOException;

import java.net.URI;
import java.net.URLEncoder;

import java.rmi.RemoteException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.common.impl.TopazContext;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.ws.access.Access;
import org.topazproject.ws.access.Attribute;
import org.topazproject.ws.access.Result;
import org.topazproject.ws.access.SubjectAttribute;
import org.topazproject.ws.users.filter.UserAccountsFilter;
import org.topazproject.ws.users.impl.UserAccountsImpl;

import org.topazproject.xacml.DenyBiasedPEP;
import org.topazproject.xacml.PDPFactory;
import org.topazproject.xacml.Util;

import com.sun.xacml.PDP;
import com.sun.xacml.ParsingException;
import com.sun.xacml.UnknownIdentifierException;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.IntegerAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Subject;

/**
 * This provides the implementation of the access service.
 *
 * @author Pradeep Krishnan
 */
public class AccessImpl implements Access {
  private static final Log       log         = LogFactory.getLog(AccessImpl.class);
  private static final String    DEFAULT_PDP = "_default_";
  private final AccessPEP        pep;
  private final TopazContext     ctx;
  private final UserAccountsImpl ual;
  private final PDPFactory       pdpFactory;

  /**
   * Create a new access service instance.
   *
   * @param pep the policy-enforcer to use for access-control
   * @param ctx the topaz api call context
   *
   * @throws IOException if PDP config file could not be located
   * @throws ParsingException if PDP config file  could not be parsed
   * @throws UnknownIdentifierException when an unknown identifier was used in a standard xacml
   *         factory
   */
  public AccessImpl(AccessPEP pep, TopazContext ctx)
             throws IOException, ParsingException, UnknownIdentifierException {
    this.pep     = pep;
    this.ctx     = ctx;
    ual          = new UserAccountsImpl(null, ctx);
    pdpFactory   = PDPFactory.getInstance(ctx.getServletContext());
  }

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action)
                    throws RemoteException {
    return evaluate(pdpName, authId, resource, action, null, null, null, null);
  }

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public Result[] evaluate(String pdpName, String authId, String resource, String action,
                           SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                           Attribute[] actionAttrs, Attribute[] envAttrs)
                    throws RemoteException {
    try {
      PDP         pdp  = lookupPdp(pdpName);
      Set         subs = createSubjectAttrs(authId, subjectAttrs);
      Set         acts = createActionAttrs(action, actionAttrs);
      Set         rsrc = createResourceAttrs(resource, resourceAttrs);
      Set         env  = createEnvAttrs(envAttrs);

      RequestCtx  req   = new RequestCtx(subs, rsrc, acts, env);
      ResponseCtx resp  = pdp.evaluate(req);
      Set         rslts = resp.getResults();

      return createResults(rslts);
    } catch (RemoteException e) {
      throw e;
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  /*
   * @see org.topazproject.ws.access.Access#checkAccess
   */
  public void checkAccess(String pdpName, String authId, String resource, String action)
                   throws SecurityException, RemoteException {
    checkAccess(pdpName, authId, resource, action, null, null, null, null);
  }

  /*
   * @see org.topazproject.ws.access.Access#evaluate
   */
  public void checkAccess(String pdpName, String authId, String resource, String action,
                          SubjectAttribute[] subjectAttrs, Attribute[] resourceAttrs,
                          Attribute[] actionAttrs, Attribute[] envAttrs)
                   throws SecurityException, RemoteException {
    try {
      PDP           pdp  = lookupPdp(pdpName);
      Set           subs = createSubjectAttrs(authId, subjectAttrs);
      Set           acts = createActionAttrs(action, actionAttrs);
      Set           rsrc = createResourceAttrs(resource, resourceAttrs);
      Set           env  = createEnvAttrs(envAttrs);

      RequestCtx    req = new RequestCtx(subs, rsrc, acts, env);
      DenyBiasedPEP pep = new DenyBiasedPEP(pdp);
      pep.evaluate(req, Collections.EMPTY_SET);
    } catch (RemoteException e) {
      throw e;
    } catch (SecurityException e) {
      throw e;
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  private PDP lookupPdp(String pdpName) throws Exception {
    return ((pdpName == null) || DEFAULT_PDP.equals(pdpName)) ? pdpFactory.getDefaultPDP()
           : pdpFactory.getPDP(pdpName);
  }

  private Set createSubjectAttrs(String authId, SubjectAttribute[] attrs)
                          throws Exception {
    HashSet defaultSet = new HashSet();
    HashMap catMap = new HashMap();
    String  user   = ual.lookUpUserByAuthIdNoAC(authId);

    int     state = (user != null) ? ual.getStateNoAC(user) : 0;
    defaultSet.add(new com.sun.xacml.ctx.Attribute(new URI(UserAccountsFilter.STATE_KEY), null,
                                                   null, new IntegerAttribute((long) state)));

    //xxx: move to lookup
    if (user == null)
      user = "anonymous:user/" + ((authId == null) ? "" : URLEncoder.encode(authId));

    // make subject-id available both as string and as uri
    defaultSet.add(new com.sun.xacml.ctx.Attribute(Util.SUBJECT_ID, null, null,
                                                   new AnyURIAttribute(new URI(user))));
    defaultSet.add(new com.sun.xacml.ctx.Attribute(Util.SUBJECT_ID, null, null,
                                                   new StringAttribute(user)));

    if (attrs != null)
      for (int i = 0; i < attrs.length; i++) {
        String category = attrs[i].getCategory();
        Set    set;

        if ((category == null) || AttributeDesignator.SUBJECT_CATEGORY_DEFAULT.equals(category))
          set = defaultSet;
        else {
          set = (Set) catMap.get(category);

          if (set == null) {
            set = new HashSet();
            catMap.put(category, set);
          }
        }

        set.add(new com.sun.xacml.ctx.Attribute(new URI(attrs[i].getName()), null, null,
                                                Util.toAttributeValue(new URI(attrs[i].getType()),
                                                                      attrs[i].getValue())));
      }

    HashSet subjSet = new HashSet();

    Subject subj = new Subject(new URI(AttributeDesignator.SUBJECT_CATEGORY_DEFAULT), defaultSet);
    subjSet.add(subj);

    Iterator it = catMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry e = (Map.Entry) it.next();
      subj = new Subject(new URI((String) e.getKey()), (Set) e.getValue());
      subjSet.add(subj);
    }

    return subjSet;
  }

  private Set createActionAttrs(String action, Attribute[] attrs)
                         throws Exception {
    HashSet        set   = new HashSet();
    AttributeValue value = new StringAttribute(action);
    set.add(new com.sun.xacml.ctx.Attribute(Util.ACTION_ID, null, null, value));

    if (attrs == null)
      return set;

    for (int i = 0; i < attrs.length; i++)
      set.add(new com.sun.xacml.ctx.Attribute(new URI(attrs[i].getName()), null, null,
                                              Util.toAttributeValue(new URI(attrs[i].getType()),
                                                                    attrs[i].getValue())));

    return set;
  }

  private Set createResourceAttrs(String resource, Attribute[] attrs)
                           throws Exception {
    HashSet        set   = new HashSet();
    AttributeValue value = new AnyURIAttribute(new URI(resource));
    set.add(new com.sun.xacml.ctx.Attribute(Util.RESOURCE_ID, null, null, value));

    if (attrs == null)
      return set;

    for (int i = 0; i < attrs.length; i++)
      set.add(new com.sun.xacml.ctx.Attribute(new URI(attrs[i].getName()), null, null,
                                              Util.toAttributeValue(new URI(attrs[i].getType()),
                                                                    attrs[i].getValue())));

    return set;
  }

  private Set createEnvAttrs(Attribute[] attrs) throws Exception {
    HashSet set = new HashSet();

    if (attrs == null)
      return set;

    for (int i = 0; i < attrs.length; i++)
      set.add(new com.sun.xacml.ctx.Attribute(new URI(attrs[i].getName()), null, null,
                                              Util.toAttributeValue(new URI(attrs[i].getType()),
                                                                    attrs[i].getValue())));

    return set;
  }

  private Result[] createResults(Set rslts) throws Exception {
    Result[] results = new Result[rslts.size()];
    Iterator it = rslts.iterator();
    int      i  = 0;

    while (it.hasNext()) {
      com.sun.xacml.ctx.Result xacmlResult = (com.sun.xacml.ctx.Result) it.next();
      com.sun.xacml.ctx.Status xacmlStatus = xacmlResult.getStatus();
      String                   code        = null;
      String                   message     = null;

      if (xacmlStatus != null) {
        code      = (String) xacmlStatus.getCode().get(0);
        message   = xacmlStatus.getMessage();
      }

      results[i++] =
        new Result(xacmlResult.getDecision(), xacmlResult.getResource(), code, message);
    }

    return results;
  }

  private void checkAccess(Result[] results) {
    for (int i = 0; i < results.length; i++) {
      Result result   = results[i];
      int    decision = result.getDecision();

      if (decision != Result.DECISION_PERMIT) {
        StringBuffer msg = new StringBuffer();

        msg.append("decision=").append(com.sun.xacml.ctx.Result.DECISIONS[decision]);

        if (result.getResource() != null)
          msg.append("; resource=").append(result.getResource());

        if (result.getCode() != null)
          msg.append(";status-code=").append(result.getCode());

        if (result.getMessage() != null)
          msg.append(";message=").append(result.getMessage());

        throw new SecurityException(msg.toString());
      }
    }
  }
}
