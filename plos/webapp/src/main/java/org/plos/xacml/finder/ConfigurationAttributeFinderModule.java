/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.xacml.finder;

import java.net.URI;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import org.plos.configuration.ConfigurationStore;
import org.plos.xacml.Util;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.finder.AttributeFinderModule;

/**
 * An AttributeFinderModule that can lookup attributes from configuration.
 *
 * @author Pradeep Krishnan
 */
public class ConfigurationAttributeFinderModule extends AttributeFinderModule {
  private static final Set supportedDesignatorTypes =
    Collections.singleton(new Integer(AttributeDesignator.ENVIRONMENT_TARGET));

  /**
   * Supports attribute designators.
   *
   * @return Returns true always.
   */
  public boolean isDesignatorSupported() {
    return true;
  }

  /**
   * Returns the attribute designator types supported by this module.
   *
   * @return Returns a singleton set indicating Subject Attribute Designator support.
   */
  public Set getSupportedDesignatorTypes() {
    return supportedDesignatorTypes;
  }

  /**
   * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute
   */
  public EvaluationResult findAttribute(URI type, URI id, URI issuer, URI category,
                                        EvaluationCtx context, int designatorType) {
    try {
      Configuration  conf     = ConfigurationStore.getInstance().getConfiguration();
      Object         property = conf.getString(id.toString());
      AttributeValue value    = Util.toAttributeValue(type, property);

      if (value == null)
        value = BagAttribute.createEmptyBag(type);
      else if (!value.isBag())
        value = new BagAttribute(type, Collections.singleton(value));

      return new EvaluationResult(value);
    } catch (Exception e) {
      // Abort the policy evaluation. For a deny-biased PEP, this will result in an
      // access-denied.
      return Util.processingError(e.getMessage(), type, id);
    }
  }
}
