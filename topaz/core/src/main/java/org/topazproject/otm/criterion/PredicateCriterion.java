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

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A criterion for generating a triple pattern. The field name or value
 * or both could be optional in which case the predicate and/or object
 * in the triple pattern will be a 'wild-card'.
 *
 * @author Pradeep Krishnan
 */
public class PredicateCriterion extends AbstractBinaryCriterion {

  /**
   * Creates a new PredicateCriterion object where both the predicate and object are known.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   */
  public PredicateCriterion(String name, Object value) {
    super(name, value);
  }

  /**
   * Creates a new PredicateCriterion object where the predicate is known and object is 
   * a wild-card.
   *
   * @param name field/predicate name
   */
  public PredicateCriterion(String name) {
    super(name, null);
  }

  /**
   * Creates a new PredicateCriterion object where the predicate and object are wild-cards.
   */
  public PredicateCriterion() {
    super(null, null);
  }


  /**
   * Gets the field/predicate name.
   *
   * @return field/predicate name or null
   */
  public String getName() {
    return getFieldName();
  }


  /*
   * inherited javadoc
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    boolean unboundPredicate = getFieldName() == null;
    boolean unboundValue     = getValue() == null;

    if (unboundPredicate)
      return subjectVar + " " + varPrefix + "p " + varPrefix + "v";

    ClassMetadata cm = criteria.getClassMetadata();
    RdfMapper     m  = getMapper(cm, getFieldName());
    String val;

    if (unboundValue)
      val = varPrefix + "v";
    else
      val = serializeValue(getValue(), criteria, getFieldName());

    String model = m.getModel();
    if ((model != null) && !cm.getModel().equals(model))
      model = " in <" + getModelUri(criteria, model) + ">";
    else
      model = "";

    if (m.hasInverseUri() && (m.getColType() != CollectionType.PREDICATE))
          throw new OtmException("Can't query across a " + m.getColType() 
              + " for an inverse mapped field '" + getFieldName() + "' in " + cm);

    String query;
    switch(m.getColType()) {
      case PREDICATE:
         query = m.hasInverseUri() ? (val + " <" + m.getUri() + "> " + subjectVar)
                                   : (subjectVar + " <" + m.getUri() + "> " + val);
         query += model;
         break;
      case RDFSEQ:
      case RDFBAG:
      case RDFALT:
        String seq = varPrefix + "seqS";
        String seqPred = varPrefix + "seqP";
        query = "(" + subjectVar + " <" + m.getUri() + "> " + seq + model
           + " and " + seq +  " " + seqPred + " " + val + model
           + " and " + seqPred + " <mulgara:prefix> <rdf:_> in <"
           + getPrefixModel(criteria) + ">)";
        break;
      case RDFLIST:
        String list = varPrefix + "list";
        String rest = varPrefix + "rest";
        query = "(" + subjectVar + " <" + m.getUri() + "> " + list + model
           + " and (" + list + " <rdf:first> " + val +  model
           + " or ((trans(" + list + " <rdf:rest> " + rest + ")" + model
           + " or " + list + " <rdf:rest> " + rest + model
           + ") and " + rest +  " <rdf:first> " + val + model + ")))";
        break;
      default:
         throw new OtmException(m.getColType() + " not supported; field = " 
             + getFieldName() + " in " + cm);
    }

    return query;
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    boolean unboundPredicate = getFieldName() == null;
    boolean unboundValue     = getValue() == null;

    if (unboundValue)
      throw new OtmException("unbound value not supported in OQL (yet)");

    String res = subjectVar;

    if (unboundPredicate)
      res += ".{" + varPrefix + "p ->}";
    else
      res += "." + getFieldName();

    if (unboundValue)
      res += " != null";
    else
      res += " = " + serializeValue(getValue(), criteria, getFieldName());

    return res;
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    throw new UnsupportedOperationException("Not meant to be persisted");
  }

}
