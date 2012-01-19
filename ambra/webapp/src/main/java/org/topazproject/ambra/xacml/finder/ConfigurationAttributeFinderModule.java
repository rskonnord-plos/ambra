/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.topazproject.ambra.xacml.finder;

import java.net.URI;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.xacml.Util;

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
    Collections.singleton(AttributeDesignator.ENVIRONMENT_TARGET);

  /**
   * Supports attribute designators.
   *
   * @return Returns true always.
   */
  @Override
  public boolean isDesignatorSupported() {
    return true;
  }

  /**
   * Returns the attribute designator types supported by this module.
   *
   * @return Returns a singleton set indicating Subject Attribute Designator support.
   */
  @Override
  public Set getSupportedDesignatorTypes() {
    return supportedDesignatorTypes;
  }

  /**
   * @see com.sun.xacml.finder.AttributeFinderModule#findAttribute
   */
  @Override
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
      /*
       * Abort the policy evaluation. For a deny-biased PEP, this will result in an
       * access-denied.
       */
      return Util.processingError(e.getMessage(), type, id);
    }
  }
}
