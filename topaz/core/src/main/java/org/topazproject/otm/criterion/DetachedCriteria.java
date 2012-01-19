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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.annotations.Embedded;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.event.PostLoadEventListener;
import org.topazproject.otm.event.PreInsertEventListener;
import org.topazproject.otm.mapping.Mapper;
import org.topazproject.otm.mapping.RdfMapper;

/**
 * DetachedCriteria is similar to Criteria except that it can exist without a Session. This
 * makes it ideal for persistance. DetachedCriteria can be converted to an executable Criteria by
 * calling the {@link #getExecutableCriteria} method. DetachedCriteria is built the same way as
 * Criteria, ie. by {@link #add}ing {@link org.topazproject.otm.criterion.Criterion} objects.<p>For
 * persistence, make sure that {@link org.topazproject.otm.criterion.Criterion#GRAPH} is
 * configured in the SessionFactory. Also note that not all of the setter methods are for use by
 * application code. They are there to support a load from a triple-store.</p>
 *
 * @author Pradeep Krishnan
 */
@Entity(types = {Criterion.NS + "Criteria"}, graph = Criterion.GRAPH)
@UriPrefix(Criterion.NS)
public class DetachedCriteria implements PreInsertEventListener, PostLoadEventListener {
  private static final Log       log               = LogFactory.getLog(DetachedCriteria.class);
  private static final String    NL                = System.getProperty("line.separator");
  private String                 alias;
  private String                 type;
  private String                 referrer;
  private boolean                forceType;
  private DetachedCriteria       parent;
  private Integer                maxResults;
  private Integer                firstResult;
  private List<Criterion>        criterionList     = new ArrayList<Criterion>();
  private List<Order>            orderList         = new ArrayList<Order>();
  private List<DetachedCriteria> childCriteriaList = new ArrayList<DetachedCriteria>();

  // Only valid in the root criteria
  private List<Order> rootOrderList = new ArrayList<Order>();

  // Used in persistence; ignored otherwise.
  private DeAliased da = new DeAliased();

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  private URI criteriaId;

  /**
   * Creates a new DetachedCriteria object.
   *
   */
  public DetachedCriteria() {
  }

  /**
   * Creates a new DetachedCriteria object.
   *
   * @param entity the entity for which the criteria is created. It could be an entity name or the
   * class name.
   */
  public DetachedCriteria(String entity) {
    this.alias         = entity;
    this.parent        = null;
    this.type          = null;
    this.referrer      = null;
    this.forceType     = false;
  }

  /**
   * Create a child DetachedCriteria.
   */
  private DetachedCriteria(DetachedCriteria parent, String path, String entity) {
    this.alias       = path;
    this.parent      = parent;
    this.type        = entity;
    this.referrer    = null;
    this.forceType   = false;
  }

  /**
   * Create a referrer DetachedCriteria.
   */
  private DetachedCriteria(DetachedCriteria parent, String referrer, String path, boolean forceType) {
    this.alias       = path;
    this.parent      = parent;
    this.type        = null;
    this.referrer    = referrer;
    this.forceType   = forceType;
  }

  /**
   * Creates an executable criteria in the given session. Must be called only  on a root
   * criteria.
   *
   * @param session the execution context
   *
   * @return a newly created Criteria object
   *
   * @throws OtmException on an error
   * @throws UnsupportedOperationException if called on a child
   */
  public Criteria getExecutableCriteria(Session session)
                                 throws OtmException, UnsupportedOperationException {
    if (getParent() != null)
      throw new UnsupportedOperationException("Can't create executable Criteria from a"
                                              + " detached child criteria");

    ClassMetadata cm = session.getSessionFactory().getClassMetadata(alias);

    if (cm == null)
      throw new OtmException("Entity name '" + alias + "' is not found in session factory");

    Criteria c = session.createCriteria(cm.getName());
    copyTo(c);
    c.getOrderPositions().clear();
    c.getOrderPositions().addAll(getRootOrderList());

    return c;
  }

  private void copyTo(Criteria c) throws OtmException {
    if (maxResults != null)
      c.setMaxResults(maxResults);

    if (firstResult != null)
      c.setFirstResult(firstResult);

    for (Criterion cr : getCriterionList())
      c.add(cr);

    for (Order or : getOrderList())
      c.addOrder(or);

    for (DetachedCriteria dc : getChildCriteriaList())
      dc.copyTo((dc.getReferrer() != null)
                ? c.createReferrerCriteria(dc.getReferrer(), dc.getAlias(), dc.isForceType())
                : c.createCriteria(dc.getAlias(), dc.getType()));
  }

  /**
   * Creates a new sub-criteria for an association.
   *
   * @param path to the association
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public DetachedCriteria createCriteria(String path) throws OtmException {
    return createCriteria(path, null);
  }

  /**
   * Creates a new sub-criteria with an explicit given type for an association. This is the
   * same as {@link #createCriteria(java.lang.String)} except that the type of the child is
   * explicitly forced instead of determined from the association metadata.
   *
   * @param path to the association
   * @param childType the entity type of the child criteria
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public DetachedCriteria createCriteria(String path, String childType)
                                  throws OtmException {
    DetachedCriteria c = new DetachedCriteria(this, path, childType);
    getChildCriteriaList().add(c);

    return c;
  }

  /**
   * Creates a new sub-criteria for an association from another object to the current object.
   * This is the same as invoking {@link #createReferrerCriteria(java.lang.String,
   * java.lang.String, boolean) createReferrerCriteria(referrer, path, false)}.
   *
   * @param referrer the entity whose association points to us
   * @param path to the association (in <var>entity</var>); this must point to this criteria's
   *        entity
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public DetachedCriteria createReferrerCriteria(String referrer, String path)
                                          throws OtmException {
    return createReferrerCriteria(referrer, path, false);
  }

  /**
   * Creates a new sub-criteria for an association from another object to the current object.
   * Whereas {@link #createCriteria} allows one to walk down associations to other objects, this
   * allows one to walk up an assocation from another object.
   *
   * @param referrer the entity whose association points to us
   * @param path to the association (in <var>entity</var>); this must point to this criteria's
   *        entity
   * @param forceType if true, force the type of our association end to be the same as this
   *        criteria's type; if false, this criteria's type must be a subtype of the assocation
   *        end's type.
   *
   * @return the newly created sub-criteria
   *
   * @throws OtmException on an error
   */
  public DetachedCriteria createReferrerCriteria(String referrer, String path, boolean forceType)
                                          throws OtmException {
    DetachedCriteria c = new DetachedCriteria(this, referrer, path, forceType);
    getChildCriteriaList().add(c);

    return c;
  }

  /**
   * Get parent.
   *
   * @return parent as Criteria.
   */
  public DetachedCriteria getParent() {
    return parent;
  }

  /**
   * Set parent. For use by persistence. DO NOT USE DIRECTLY. Use {@link #createCriteria}
   * instead on the parent.
   *
   * @param parent the value to set.
   */
  @Predicate
  public void setParent(DetachedCriteria parent) {
    this.parent = parent;
  }

  /**
   * Adds a Criterion.
   *
   * @param criterion the criterion to add
   *
   * @return this for method call chaining
   */
  public DetachedCriteria add(Criterion criterion) {
    getCriterionList().add(criterion);

    return this;
  }

  /**
   * Adds an ordering criterion.
   *
   * @param order the order definition
   *
   * @return this for method call chaining
   */
  public DetachedCriteria addOrder(Order order) {
    getOrderList().add(order);
    getRoot().getRootOrderList().add(order);

    return this;
  }

  private ClassMetadata getClassMetadata(SessionFactory sf) {
    if (getParent() == null)
      return sf.getClassMetadata(alias);

    if (referrer != null)
      return sf.getClassMetadata(referrer);

    ClassMetadata cm = getParent().getClassMetadata(sf);

    if (cm == null)
      return null;

    Mapper m = cm.getMapperByName(alias);

    return (!(m instanceof RdfMapper)) ? null
           : sf.getClassMetadata(((RdfMapper) m).getAssociatedEntity());
  }

  /*
   * inherited javadoc
   */
  public void onPreInsert(Session session, Object object) {
    assert this == object;

    SessionFactory sf = session.getSessionFactory();

    if (getParent() == null) {
      ClassMetadata cm = sf.getClassMetadata(alias);

      if (cm == null)
        log.warn("onPreInsert: Entity name '" + alias + "' is not found in session factory.");
      else {
        da.setRdfType(cm.getAllTypes());
        da.setPredicateUri(null);
        da.setInverse(false);

        if (log.isDebugEnabled())
          log.debug("onPreInsert: converted entity '" + alias + "' to " + da.getRdfType());

        for (Criterion cr : getCriterionList())
          cr.onPreInsert(session, this, cm);

        for (Order or : getOrderList())
          or.onPreInsert(session, this, cm);
      }
    } else {
      ClassMetadata cm =
        (referrer != null) ? sf.getClassMetadata(referrer) : getParent().getClassMetadata(sf);

      if (cm == null)
        log.warn("onPreInsert: Parent of '" + alias + "' not found in session factory");
      else {
        Mapper r = cm.getMapperByName(alias);

        if (!(r instanceof RdfMapper))
          log.warn("onPreInsert: Field '" + alias + "' not found in " + cm);
        else {
          RdfMapper m = (RdfMapper) r;

          if (referrer == null)
            cm = sf.getClassMetadata(m.getAssociatedEntity());

          if (cm != null)
            da.setRdfType(cm.getAllTypes());

          da.setPredicateUri(URI.create(m.getUri()));
          da.setInverse(m.hasInverseUri());

          if (log.isDebugEnabled())
            log.debug("onPreInsert: converted field '" + alias + "' to " + da);

          for (Criterion cr : getCriterionList())
            cr.onPreInsert(session, this, cm);

          for (Order or : getOrderList())
            or.onPreInsert(session, this, cm);
        }
      }
    }
  }

  public void onPostLoad(Session session, Object object, Mapper field) {
    // ignore since we are already loading the rest
  }

  /*
   * inherited javadoc
   */
  public void onPostLoad(Session session, Object object) {
    assert this == object;

    SessionFactory sf = session.getSessionFactory();

    if (getParent() == null) {
      ClassMetadata cm = sf.getAnySubClassMetadata(null, da.getRdfType());

      if (cm == null) {
        cm = sf.getClassMetadata(alias);

        if ((cm != null) && !cm.getAllTypes().containsAll(da.getRdfType()))
          cm = null;
      }

      if (cm == null) {
        log.warn("onPostLoad: A class metadata matching the rdf:type <" + da.getRdfType()
                 + "> is not found in session factory. Entity name will remain as '" + alias + "'");
      } else {
        alias = cm.getName();

        if (log.isDebugEnabled())
          log.debug("onPostLoad: converted rdfType " + da.getRdfType() + " to entity '" + alias
                    + "'");

        for (Criterion cr : getCriterionList())
          cr.onPostLoad(session, this, cm);

        for (Order or : getOrderList())
          or.onPostLoad(session, this, cm);
      }
    } else {
      ClassMetadata cm =
        (referrer != null) ? sf.getClassMetadata(referrer) : getParent().getClassMetadata(sf);

      if (cm == null)
        log.warn("onPostLoad: Parent of '" + alias + "' not found in session factory");
      else {
        RdfMapper m =
          cm.getMapperByUri(sf, ser(da.getPredicateUri()), da.isInverse(), da.getRdfType());

        if (m == null)
          log.warn("onPostLoad: A field matching " + da + " not found in " + cm);
        else {
          alias = m.getName();

          if (log.isDebugEnabled())
            log.debug("onPostLoad: converted " + da + " to '" + alias + "'");

          if (referrer == null)
            cm = sf.getClassMetadata(m.getAssociatedEntity());

          for (Criterion cr : getCriterionList())
            cr.onPostLoad(session, this, cm);

          for (Order or : getOrderList())
            or.onPostLoad(session, this, cm);
        }
      }
    }
  }

  private static String ser(URI u) {
    return (u == null) ? null : u.toString();
  }

  /**
   * Gets the list of child Criteria.
   *
   * @return list of child Criteria
   */
  public List<DetachedCriteria> getChildCriteriaList() {
    return childCriteriaList;
  }

  /**
   * Sets the list of child Criteria. For use by persistence. DO NOT USE DIRECTLY.
   *
   * @param list of child Criteria
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setChildCriteriaList(List<DetachedCriteria> list) {
    childCriteriaList = list;
  }

  /**
   * Gets the list of Criterions.
   *
   * @return list of Criterions
   */
  public List<Criterion> getCriterionList() {
    return criterionList;
  }

  /**
   * Sets the list of Criterions. For use by persitence. DO NOT USE DIRECTLY. Use {@link
   * #add} instead.
   *
   * @param list of Criterions
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setCriterionList(List<Criterion> list) {
    criterionList = list;
  }

  /**
   * Gets the list of Order definitions.
   *
   * @return lis of Order dedinitions
   */
  public List<Order> getOrderList() {
    return orderList;
  }

  /**
   * Sets the list of Order definitions. For use by persistence. DO NOT USE DIRECTLY. Use
   * {@link #addOrder} instead.
   *
   * @param list of Order dedinitions
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setOrderList(List<Order> list) {
    orderList = list;
  }

  /**
   * Gets the root order list.
   *
   * @return the root order list
   */
  public List<Order> getRootOrderList() {
    return rootOrderList;
  }

  /**
   * Gets the root order list. For use by persistence. DO NOT USE DIRECTLY. Use {@link
   * #addOrder} instead.
   *
   * @param list the root order list
   */
  @Predicate(collectionType = CollectionType.RDFSEQ)
  public void setRootOrderList(List<Order> list) {
    rootOrderList = list;
  }

  /**
   * Set a limit upon the number of objects to be retrieved.
   *
   * @param maxResults the maximum number of results
   */
  @Predicate
  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  /**
   * Set the first result to be retrieved.
   *
   * @param firstResult the first result to retrieve, numbered from <tt>0</tt>
   */
  @Predicate
  public void setFirstResult(Integer firstResult) {
    this.firstResult = firstResult;
  }

  /**
   * Get a limit upon the number of objects to be retrieved.
   *
   * @return the maximum number of results
   */
  public Integer getMaxResults() {
    return maxResults;
  }

  /**
   * Get the first result to be retrieved.
   *
   * @return the first result to retrieve, numbered from <tt>0</tt>
   */
  public Integer getFirstResult() {
    return firstResult;
  }

  private DetachedCriteria getRoot() {
    return (getParent() == null) ? this : getParent().getRoot();
  }

  /**
   * Get alias.
   *
   * @return alias as String.
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Set alias. For use by persistence DO NOT USE DIRECTLY. Use the constructor instead.
   *
   * @param alias the value to set.
   */
  @Predicate
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * Get referrer.
   *
   * @return the referrer, or null
   */
  public String getReferrer() {
    return referrer;
  }

  /**
   * Set referrer. For use by persistence. DO NOT USE DIRECTLY. Use {@link
   * #createReferrerCriteria} instead on the parent.
   *
   * @param referrer the value to set.
   */
  @Predicate
  public void setReferrer(String referrer) {
    this.referrer = referrer;
  }

  /**
   * Get this criteria's type. Only valid for non-referrer criteria.
   *
   * @return the type, or null
   */
  public String getType() {
    return type;
  }

  /**
   * Set the type. For use by persistence. Only valid for non-referrer criteria. DO NOT USE
   * DIRECTLY. Use {@link #createCriteria} instead on the parent.
   *
   * @param type the value to set.
   */
  @Predicate
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Whether to check the association end's type against the parents type. Only valid for
   * referrer criteria.
   *
   * @return false if the types should be checked, true if not
   */
  public boolean isForceType() {
    return forceType;
  }

  /**
   * Set the force-type flag. For use by persistence. Only valid for referrer criteria. DO
   * NOT USE DIRECTLY. Use {@link #createCriteria} instead on the parent.
   *
   * @param force false if the types should be checked, true if not
   */
  @Predicate
  public void setForceType(boolean force) {
    this.forceType = force;
  }

  /**
   * Get de-aliased. Used in persistence. Ignored otherwise.
   *
   * @return da as DeAliased.
   */
  public DeAliased getDa() {
    return da;
  }

  /**
   * Set de-aliased. Used in persistence. Ignored otherwise
   *
   * @param da the value to set.
   */
  @Embedded
  public void setDa(DeAliased da) {
    this.da = da;
  }

  /**
   * Get criteriaId.
   *
   * @return criteriaId as URI.
   */
  public URI getCriteriaId() {
    return criteriaId;
  }

  /**
   * Set criteriaId.
   *
   * @param criteriaId the value to set.
   */
  @Id
  @GeneratedValue(uriPrefix = Criterion.NS + "Criteria/Id/")
  public void setCriteriaId(URI criteriaId) {
    this.criteriaId = criteriaId;
  }

  /**
   * Return the list of parameter names.
   *
   * @return the set of names; will be empty if there are no parameters
   */
  public Set<String> getParameterNames() {
    if (getParent() != null)
      return getParent().getParameterNames();

    return getParameterNames(new HashSet<String>());
  }

  private Set<String> getParameterNames(Set<String> paramNames) {
    for (DetachedCriteria dc : getChildCriteriaList())
      dc.getParameterNames(paramNames);

    for (Criterion c : getCriterionList())
      paramNames.addAll(c.getParamNames());

    return paramNames;
  }

  /*
   * inherited javadoc
   */
  public String toString() {
    return toString("");
  }

  /**
   * Creates a String representation with a prefix for every line.
   *
   * @param indent the string to prefix every line with
   *
   * @return a string representation of this detached-criteria
   */
  public String toString(String indent) {
    StringBuilder sb = new StringBuilder(50);
    sb.append(getAlias()).append(": ");

    for (Criterion c : getCriterionList())
      sb.append(NL).append(indent).append(c);

    for (DetachedCriteria c : getChildCriteriaList())
      sb.append(NL).append(indent).append(c.toString(indent + "  "));

    for (Order o : getOrderList())
      sb.append(NL).append(indent).append(o);

    return sb.toString();
  }

  /**
   * A class to hold the rdf:type URI, the predicate URI and the mapping direction  (inverse
   * or not) for an entity supplied in creating this Criteria. This information is persisted when
   * the Criteria is persisted allowing the re-construction of an association field name on
   * retrieval even when the field name or the association class name has changed. <p> This also
   * has the additional advantage that what is stored in the persistence store has some meaning
   * outside of the java class that this Criteria is tied to.</p>
   */
  @UriPrefix(Criterion.NS)
  public static class DeAliased {
    private Set<String> rdfType      = new HashSet<String>();
    private URI         predicateUri;
    private boolean     inverse;

    @Predicate(type = Predicate.PropType.OBJECT)
    public void setRdfType(Set<String> rdfType) {
      this.rdfType                   = rdfType;
    }

    public Set<String> getRdfType() {
      return rdfType;
    }

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

    public String toString() {
      return "[predicate: <" + predicateUri + ">, rdf:type: " + getRdfType() + ", inverse: "
             + inverse + "]";
    }
  }
}
