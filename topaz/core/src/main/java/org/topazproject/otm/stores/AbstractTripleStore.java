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
package org.topazproject.otm.stores;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Connection;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.criterion.CriterionBuilder;

/**
 * A common base class for triple-store impls.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractTripleStore implements TripleStore {
  private static final Log log = LogFactory.getLog(AbstractTripleStore.class);
  /**
   * Map of Criterion Builders for store specific functions.
   */
  protected Map<String, CriterionBuilder> critBuilders = new HashMap<String, CriterionBuilder>();

  /*
   * inherited javadoc
   */
  public <T> void insert(ClassMetadata cm, String id, T o, Connection con) throws OtmException {
    insert(cm, cm.getRdfMappers(), id, o, con);
  }

  /*
   * inherited javadoc
   */
  public <T> void delete(ClassMetadata cm, String id, T o, Connection con) throws OtmException {
    delete(cm, cm.getRdfMappers(), id, o, con);
  }

  public void flush(Connection con) throws OtmException {
  }

  /*
   * inherited javadoc
   */
  public CriterionBuilder getCriterionBuilder(String func)
                                       throws OtmException {
    return critBuilders.get(func);
  }

  /*
   * inherited javadoc
   */
  public void setCriterionBuilder(String func, CriterionBuilder builder)
                           throws OtmException {
    critBuilders.put(func, builder);
  }
}
