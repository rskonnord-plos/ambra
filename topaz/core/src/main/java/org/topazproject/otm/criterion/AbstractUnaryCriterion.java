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

import java.net.URI;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Session;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * A base class for all operations involving a field name.
 *
 * @author Pradeep Krishnan
 */
public abstract class AbstractUnaryCriterion extends Criterion {
  private static final Log log       = LogFactory.getLog(AbstractUnaryCriterion.class);
  private String           fieldName;
  private DeAliased        da        = new DeAliased();

  /**
   * Creates a new AbstractUnaryCriterion object.
   */
  public AbstractUnaryCriterion() {
  }

  /**
   * Creates a new AbstractUnaryCriterion object.
   *
   * @param name field/predicate name
   */
  public AbstractUnaryCriterion(String name) {
    setFieldName(name);
  }

  /**
   * Get fieldName.
   *
   * @return fieldName as String.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Set fieldName.
   *
   * @param fieldName the value to set.
   */
  @Predicate
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return getClass().getName().replace("org.topazproject.otm.criterion.", "")
              .replace("Criterion", "") + "[" + getFieldName() + "]";
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    Mapper r = cm.getMapperByName(fieldName);

    if (!(r instanceof RdfMapper))
      log.warn("onPreInsert: The field '" + fieldName + "' does not exist in " + cm);
    else {
      RdfMapper m = (RdfMapper) r;
      da.setPredicateUri(URI.create(m.getUri()));
      da.setInverse(m.hasInverseUri());

      if (m.isAssociation()) {
        ClassMetadata assoc = ses.getSessionFactory().getClassMetadata(m.getAssociatedEntity());

        if (assoc != null)
          da.setRdfType(assoc.getAllTypes());
      }

      if (log.isDebugEnabled())
        log.debug("onPreInsert: Converted field '" + fieldName + "' to " + da + " in " + cm);
    }
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session ses, DetachedCriteria dc, ClassMetadata cm) {
    RdfMapper m =
      (da.getPredicateUri() == null) ? null
      : cm.getMapperByUri(ses.getSessionFactory(), da.getPredicateUri().toString(), da.isInverse(),
                          da.getRdfType());

    if (m == null)
      log.warn("onPostLoad: " + da + " not found in " + cm);
    else {
      fieldName = m.getName();

      if (log.isDebugEnabled())
        log.debug("onPostLoad: Converted " + da + " to '" + fieldName + "' in " + cm);
    }
  }

  /**
   * Get de-aliased form. Used for persistence. Ignored otherwise.
   *
   * @return da as DeAliased.
   */
  public DeAliased getDa() {
    return da;
  }

  /**
   * Get de-aliased form. Used for persistence. Ignored otherwise.
   *
   * @param da the value to set.
   *
   * @see DetachedCriteria
   */
  @Embedded
  public void setDa(DeAliased da) {
    this.da = da;
  }

  /**
   * A class to hold the predicate-uri and the mapping direction (inverse or not) for a field
   * name supplied in creating this Criterion. This information is persisted when the Criterion is
   * persisted allowing the re-construction of a field name on retrieval even when the field name
   * has changed in the java class.<p>This also has the additional advantage that what is
   * stored in the persistence store has some meaning outside of the java class that this Criteria
   * is tied to.</p>
   */
  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    private URI         predicateUri;
    private boolean     inverse;
    private Set<String> rdfType;

    @Predicate
    public void setPredicateUri(URI predicateUri) {
      this.predicateUri = predicateUri;
    }

    public URI getPredicateUri() {
      return predicateUri;
    }

    @Predicate
    public void setInverse(boolean inverse) {
      this.inverse = inverse;
    }

    public boolean isInverse() {
      return inverse;
    }

    @Predicate(type = Predicate.PropType.OBJECT)
    public void setRdfType(Set<String> rdfType) {
      this.rdfType = rdfType;
    }

    public Set<String> getRdfType() {
      return rdfType;
    }

    public String toString() {
      return "[predicateUri: <" + predicateUri + ">, inverse: " + inverse + "]";
    }
  }
}
