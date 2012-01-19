/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.criterion;

import java.net.URI;

import java.util.Collections;
import java.util.Set;

import org.topazproject.mulgara.itql.ItqlHelper;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Criteria;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.UriPrefix;
import org.topazproject.otm.mapping.Mapper;

/**
 * An abstract base for all query criterion used as restrictions in a  {@link
 * org.topazproject.otm.Criteria}.
 *
 * <p>Subclasses must either override both {@link #toItql toItql()} and {@link #toOql toOql()},
 * or they must override {@link #toQuery toQuery()}; the default implementation for these is to
 * invoke each other.
 *
 * @author Pradeep Krishnan
 */
@Entity(type = Criterion.RDF_TYPE, model = Criterion.MODEL)
@UriPrefix(Criterion.NS)
public abstract class Criterion {

  /**
   * The graph/model alias for persistence. Unused otherwise.
   */
  public static final String MODEL = "criteria";

  /**
   * Namespace for all URIs for persistence. Unused otherwise.
   */
  public static final String NS = Rdf.topaz + "otm/";

  /**
   * The base rdf:type and also the namespace for sub-class types for persistence. Unused otherwise/
   */
  public static final String RDF_TYPE = NS + "Criterion";

  /**
   * The constants indicating the query language.
   */
  public static enum QL { ITQL, OQL };

  /**
   * The id field used for persistence. Ignored otherwise.
   */
  @Id
  @GeneratedValue(uriPrefix = NS + "Criterion/Id/")
  public URI criterionId;


  /**
   * Creates an ITQL query 'where clause' fragment. The default implementation calls {@link #toQuery
   * toQuery()}.
   *
   * @param criteria the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix namespace for internal variables (ie. not visible on select list)
   *
   * @return the itql query fragment
   *
   * @throws OtmException if an error occurred
   */
  public String toItql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    return toQuery(criteria, subjectVar, varPrefix, QL.ITQL);
  }

  /**
   * Creates an OQL query 'where clause' fragment. The default implementation calls {@link #toQuery
   * toQuery()}.
   *
   * @param criteria the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix namespace for internal variables (ie. not visible on select list)
   *
   * @return the oql query fragment
   * @throws OtmException if an error occurred
   */
  public String toOql(Criteria criteria, String subjectVar, String varPrefix) throws OtmException {
    return toQuery(criteria, subjectVar, varPrefix, QL.OQL);
  }

  /**
   * Creates a query 'where clause' fragment. The default implementation calls {@link #toItql
   * toItql} or {@link #toOql toOql} depending on the specified query-language.
   *
   * @param criteria   the Criteria
   * @param subjectVar the subject designator variable (eg. $s etc.)
   * @param varPrefix  namespace for internal variables (ie. not visible on select list)
   * @param ql         the query language to generate the fragment for
   * @return the query fragment
   * @throws OtmException if an error occurred
   */
  public String toQuery(Criteria criteria, String subjectVar, String varPrefix, QL ql)
      throws OtmException {
    switch (ql) {
      case ITQL:
        return toItql(criteria, subjectVar, varPrefix);
      case OQL:
        return toOql(criteria, subjectVar, varPrefix);
      default:
        throw new OtmException("unknown query language '" + ql + "'");
    }
  }

  /** 
   * Serialize the given value into standard rdf form, i.e "&lt;...&gt;" for URI's and
   * single-quoted strings with optional datatype uri for literals.
   * 
   * @param value    the value to serialize
   * @param criteria the criteria object this criterion belongs to
   * @param field    the name of the field whose value is being serialized
   * @return the serialized value
   * @throws OtmException if the field is not valid or an error occurred getting the string
   *                      representation of the value
   */
  protected static String serializeValue(Object value, Criteria criteria, String field)
      throws OtmException {
    ClassMetadata cm = criteria.getClassMetadata();
    Mapper        m  = cm.getMapperByName(field);

    if (m == null)
      throw new OtmException("'" + field + "' does not exist in " + cm);

    String val;
    if (value instanceof Parameter)
      val = criteria.resolveParameter(((Parameter)value).getParameterName(), field);
    else {
      try {
        val = (m.getSerializer() != null) ? m.getSerializer().serialize(value) : value.toString();
      } catch (Exception e) {
        throw new OtmException("Serializer exception", e);
      }
    }

    if (m.typeIsUri())
      val = "<" + ItqlHelper.validateUri(val, field) + ">";
    else {
      val = "'" + ItqlHelper.escapeLiteral(val) + "'";

      if (m.getDataType() != null)
        val += (("^^<" + m.getDataType()) + ">");
    }

    return val;
  }

  /**
   * Gets the parameter names that is set on this Criterion.
   * The default implementation always returns an emptySet.
   * Sub-classes must override this and return a set if they
   * are parameterizable.
   *
   * @return the parameter names as a set; never null
   */
  public Set<String> getParamNames() {
    return Collections.emptySet();
  }

  /**
   * Do any pre-insert processing. eg. converting field names to predicate-uri
   *
   * @param dc the detached criteria that is being persisted
   * @param cm the class metadata to use to resolve fields
   */
  public abstract void onPreInsert(DetachedCriteria dc, ClassMetadata cm);

  /**
   * Do any post-load processing. eg. converting predicate-uri to field name
   *
   * @param dc the detached criteria that is being loaded
   * @param cm the class metadata to use to resolve fields
   */
  public abstract void onPostLoad(DetachedCriteria dc, ClassMetadata cm);

}
