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
package org.topazproject.otm.criterion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Predicate;

/**
 * Base class for junctions on Criterions.
 *
 * @author Pradeep Krishnan
 */
public abstract class Junction extends Criterion {
  private List<Criterion>       criterions = new ArrayList<Criterion>();
  private transient String      op;
  private transient Set<String> paramNames = new HashSet<String>();

  /**
   * Creates a new Junction object.
   *
   * @param op the operation
   */
  protected Junction(String op) {
    this.op = op;
  }

  /**
   * Adds a Criterion.
   *
   * @param c the criterion
   *
   * @return this for expression chaining
   */
  public Junction add(Criterion c) {
    getCriterions().add(c);
    paramNames.addAll(c.getParamNames());

    return this;
  }

  /**
   * Gets the list of criterions.
   *
   * @return list or criterions
   */
  public List<Criterion> getCriterions() {
    return criterions;
  }

  /**
   * Sets the list of criterions.
   *
   * @param criterions the criterions to set
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setCriterions(List<Criterion> criterions) {
    this.criterions = criterions;

    paramNames.clear();

    for (Criterion c : criterions)
      paramNames.addAll(c.getParamNames());
  }

  /*
   * inherited javadoc
   */
  public Set<String> getParamNames() {
    return paramNames;
  }

  /**
   * Gets the operation.
   *
   * @return the operation
   */
  public String getOp() {
    return op;
  }

  /*
   * inherited javadoc
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
                 throws OtmException {
    String sep   = "(";
    String query = "";
    int    i     = 0;

    for (Criterion c : getCriterions()) {
      query += (sep + c.toQuery(criteria, subjectVar, varPrefix + "j" + i++, ql));
      sep = " " + getOp() + " ";
    }

    if (i > 0)
      query += ")";

    return query;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    StringBuilder sb = new StringBuilder(getCriterions().size() * 20);
    sb.append(getClass().getName().replace("org.topazproject.otm.criterion.", "")).append("[");

    for (Criterion c : getCriterions())
      sb.append(c).append(", ");

    if (getCriterions().size() > 0)
      sb.setLength(sb.length() - 2);

    sb.append("]");

    return sb.toString();
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    for (Criterion c : getCriterions())
      c.onPreInsert(ses, dc, cm);
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    for (Criterion c : getCriterions())
      c.onPostLoad(ses, dc, cm);
  }
}
