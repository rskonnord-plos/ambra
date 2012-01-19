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
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A criterion for a triple pattern where a chain is walked to the end returning all matched
 * triples. eg.
 *
 * <pre>
 *   // Suppose Annotation a1 is superseded by a2 and a2 is superseded by a3, the following
 *   // query on a3 will return a list containing a1 and a2.
 *   List&lt;Annotation&gt; l =
 *               session.createCriteria(Annotation.class)
 *                       .add(Restrictions.walk("supersededBy", a3.id)).list();
 * </pre>
 *
 * @author Pradeep Krishnan
 */
@Entity(types={Criterion.RDF_TYPE + "/walk"})
public class WalkCriterion extends AbstractBinaryCriterion {

  public WalkCriterion() {
  }

  /**
   * Creates a new WalkCriterion object.
   *
   * @param name field/predicate name
   * @param value field/predicate value
   *
   */
  public WalkCriterion(String name, Object value) {
    super(name, value);
  }


  /*
   * inherited javadoc
   *   walk ($subject_variable <predicate_URI> <object_URI> and
   *         $subject_variable <predicate_URI> $object_variable)
   *     or
   *   walk (<subject_URI> <predicate_URI> $object_variable and
   *         $subject_variable <predicate_URI> $object_variable)
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix)
                throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    RdfMapper     m  = getMapper(cm, getFieldName());

    if (!m.typeIsUri())
      throw new OtmException("Value must be a uri for walk(): field is "
                             + m.getName());

    String val = serializeValue(getValue(), criteria, getFieldName());
    String graph = m.getGraph();

    if ((graph != null) && !cm.getGraph().equals(graph))
      graph = " in <" + getGraphUri(criteria, graph) + ">";
    else
      graph = "";

    String query =
      m.hasInverseUri()
      ? ("walk(" + val + " <" + m.getUri() + "> " + subjectVar + graph + " and " + varPrefix + " <"
      + m.getUri() + "> " + subjectVar + graph + ")")
      : ("walk(" + subjectVar + " <" + m.getUri() + "> " + val + graph + " and " + subjectVar + " <"
      + m.getUri() + "> " + varPrefix + graph + ")");

    return query;
  }

  /*
   * inherited javadoc
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    throw new OtmException("'walk' is not supported by OQL (yet)");
  }
}
