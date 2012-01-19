/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm;

/**
 * Various URIs defined in the RDFS specification. Please see
 * http://www.w3.org/TR/rdf-schema/
 *
 * @author Amit Kapoor
 */
public interface Rdfs {
  /** RDFS ontology */
  public static final String base = "http://www.w3.org/2000/01/rdf-schema#";

  /**
   * All things described by RDF are called resources, and are instances of
   * the class rdfs:Resource. This is the class of everything. All other
   * classes are subclasses of this class. rdfs:Resource is an instance of
   * rdfs:Class.
   * */
  public static final String Resource  = base + "Resource";

  /** 
   * This is the class of resources that are RDF classes. rdfs:Class is an
   * instance of rdfs:Class.
   */
  public static final String RdfsClass  = base + "Class";

  /**
   * The class rdfs:Literal is the class of literal values such as strings and
   * integers. Property values such as textual strings are examples of RDF
   * literals. Literals may be plain or typed. A typed literal is an instance
   * of a datatype class. This specification does not define the class of plain
   * literals.
   *
   * rdfs:Literal is an instance of rdfs:Class. rdfs:Literal is a subclass of
   * rdfs:Resource.
   */
  public static final String Literal  = base + "Literal";

  /**
   * rdfs:Datatype is the class of datatypes. All instances of rdfs:Datatype
   * correspond to the RDF model of a datatype described in the RDF Concepts
   * specification [RDF-CONCEPTS]. rdfs:Datatype is both an instance of and a
   * subclass of rdfs:Class. Each instance of rdfs:Datatype is a subclass of
   * rdfs:Literal.
   */
  public static final String Datatype  = base + "Datatype";

  /**
   * The rdfs:Container class is a super-class of the RDF Container classes,
   * i.e. rdf:Bag, rdf:Seq, rdf:Alt.
   */
  public static final String Container  = base + "Container";

  /**
   * The rdfs:ContainerMembershipProperty class has as instances the properties
   * rdf:_1, rdf:_2, rdf:_3 ... that are used to state that a resource is a
   * member of a container. rdfs:ContainerMembershipProperty is a subclass of
   * rdf:Property. Each instance of rdfs:ContainerMembershipProperty is an
   * rdfs:subPropertyOf the rdfs:member property.
   */
  public static final String ContainerMembershipProperty  = base + "ContainerMembershipProperty";

  /**
   * rdfs:range is an instance of rdf:Property that is used to state that the
   * values of a property are instances of one or more classes.
   *
   * The triple
   *
   *     P rdfs:range C
   *
   *     states that P is an instance of the class rdf:Property, that C is an
   *     instance of the class rdfs:Class and that the resources denoted by the
   *     objects of triples whose predicate is P are instances of the class C.
   */
  public static final String range  = base + "range";

  /**
   * rdfs:domain is an instance of rdf:Property that is used to state that any
   * resource that has a given property is an instance of one or more classes.
   *
   * A triple of the form:
   *
   *     P rdfs:domain C
   *
   *     states that P is an instance of the class rdf:Property, that C is a
   *     instance of the class rdfs:Class and that the resources denoted by the
   *     subjects of triples whose predicate is P are instances of the class C.
   *
   */
  public static final String domain  = base + "domain";

  /**
   * The property rdfs:subClassOf is an instance of rdf:Property that is used
   * to state that all the instances of one class are instances of another.
   *
   * A triple of the form:
   *
   *     C1 rdfs:subClassOf C2
   *
   *     states that C1 is an instance of rdfs:Class, C2 is an instance of
   *     rdfs:Class and C1 is a subclass of C2. The rdfs:subClassOf property is
   *     transitive.
   */
  public static final String subClassOf  = base + "subClassOf";

  /**
   * The property rdfs:subPropertyOf is an instance of rdf:Property that is
   * used to state that all resources related by one property are also related
   * by another.
   *
   * A triple of the form:
   *
   *     P1 rdfs:subPropertyOf P2
   *
   *     states that P1 is an instance of rdf:Property, P2 is an instance of
   *     rdf:Property and P1 is a subproperty of P2. The rdfs:subPropertyOf
   *     property is transitive.
   */
  public static final String subPropertyOf  = base + "subPropertyOf";

  /**
   * rdfs:label is an instance of rdf:Property that may be used to provide a
   * human-readable version of a resource's name.
   *
   * A triple of the form:
   *
   *     R rdfs:label L
   *
   *     states that L is a human readable label for R.
   */
  public static final String label  = base + "label";

  /**
   * rdfs:comment is an instance of rdf:Property that may be used to provide a
   * human-readable description of a resource.
   *
   * A triple of the form:
   *
   *     R rdfs:comment L
   *
   *     states that L is a human readable description of R.
   */
  public static final String comment  = base + "comment";

  /**
   * rdfs:member is an instance of rdf:Property that is a super-property of all
   * the container membership properties i.e. each container membership
   * property has an rdfs:subPropertyOf relationship to the property
   * rdfs:member.
   */
  public static final String member  = base + "member";

  /**
   * rdfs:seeAlso is an instance of rdf:Property that is used to indicate a
   * resource that might provide additional information about the subject
   * resource.
   *
   * A triple of the form:
   *
   *     S rdfs:seeAlso O
   *
   *     states that the resource O may provide additional information about S.
   */
  public static final String seeAlso  = base + "seeAlso";

  /**
   * rdfs:isDefinedBy is an instance of rdf:Property that is used to indicate a
   * resource defining the subject resource. This property may be used to
   * indicate an RDF vocabulary in which a resource is described.
   *
   * A triple of the form:
   *
   *     S rdfs:isDefinedBy O
   *
   *     states that the resource O defines S.
   */
  public static final String isDefinedBy  = base + "isDefinedBy";
}
