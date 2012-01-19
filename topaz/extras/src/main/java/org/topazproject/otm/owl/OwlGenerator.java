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
package org.topazproject.otm.owl;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Rdf;
import org.topazproject.otm.SessionFactory;

import org.topazproject.otm.metadata.ClassBinding;
import org.topazproject.otm.metadata.EntityDefinition;
import org.topazproject.otm.metadata.RdfDefinition;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;

import org.semanticweb.owl.apibinding.OWLManager;

import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.io.StreamInputSource;

import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntityAnnotationAxiom;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLObjectPropertyExpression;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLPropertyRange;
import org.semanticweb.owl.model.SetOntologyURI;

import org.semanticweb.owl.util.CollectionFactory;

import org.semanticweb.owl.vocab.NamespaceOWLOntologyFormat;
import org.semanticweb.owl.vocab.XSDVocabulary;

/**
 * Class to generate OWL schema from Topaz class meta data information. OWLAPI
 * is utilized to generate the OWL triples.
 *
 * @author Amit Kapoor
 */
public class OwlGenerator {
  // Initialize logging
  private static final Log log = LogFactory.getLog(OwlGenerator.class);

  // Local namespaces
  private static final String JAVA_PREFIX    = "java";
  private static final String MULGARA_PREFIX = "mulgara";
  private static final String JAVA_NS        = Rdf.topaz + JAVA_PREFIX + "/";
  private static final String MULGARA_NS     = "http://www.mulgara.org/rdf/";
  private static final String CLASS_NS       = Rdf.topaz + "class/";

  // Map of created OWL classes: Key is the rdf:type
  HashMap<String, OWLClass> classMap;

  // Map of created OWL classes for class bindings: Key is the binding name
  HashMap<String, OWLClass> cbMap;

  // The OTM side of things
  SessionFactory topazFactory;

  // OWLAPI factories etc.
  OWLOntologyManager ontologyManager;
  OWLOntology ontology;
  OWLDataFactory factory;

  /**
   * Generate and add the OWLClass axioms for the class meta data
   *
   * @param cb the class binding
   */
  private OWLClass getOWLClassAxiom(ClassBinding cb) throws OWLException {
    OWLClass owlClass = cbMap.get(cb.getName());
    if (owlClass != null)
      return owlClass;
    if (!(cb.getClassDefinition() instanceof EntityDefinition))
      return null;
    log.debug("Generating OWL class axiom for " + cb.getName());

    Set<OWLClass> allClasses = CollectionFactory.createSet();
    EntityDefinition entity = (EntityDefinition)cb.getClassDefinition();
    Set<String> types = entity.getTypes();
    if (types.isEmpty()) {
      // No types and no properties is kind of useless
      if (cb.getProperties().isEmpty())
        return null;
      types = new HashSet<String>();
      types.add(CLASS_NS + entity.getName());
    }
    for (String type : types) {
      owlClass = classMap.get(type);
      if (owlClass == null) {
        owlClass = factory.getOWLClass(URI.create(type));
        classMap.put(type, owlClass);
        ontologyManager.applyChange(new AddAxiom(ontology, factory.getOWLDeclarationAxiom(owlClass)));
      }
      allClasses.add(owlClass);
    }

    // Deal with the fact that the OWL class has multiple types
    if (allClasses.size() > 1) {
      log.debug("OWL intersection class for multiple RDF types " + cb.getName());
      owlClass = factory.getOWLClass(URI.create(CLASS_NS + entity.getName()));
      OWLObjectIntersectionOf intersection = factory.getOWLObjectIntersectionOf(allClasses);
      ontologyManager.applyChange(new AddAxiom(ontology,
                                               factory.getOWLEquivalentClassesAxiom(owlClass,
                                                                                    intersection)));
    }

    // Add useful OWL class annotations
    log.debug("Adding OWL annotations to class axiom for " + cb.getName());
    OWLConstant cnst = null;
    OWLEntityAnnotationAxiom entityAxiom = null;
    if (entity.getGraph() != null) {
      cnst = factory.getOWLTypedConstant(topazFactory.getGraph(entity.getGraph()).getUri().toString());
      entityAxiom = factory.getOWLEntityAnnotationAxiom(owlClass, URI.create(MULGARA_NS + "graph"), cnst);
      ontologyManager.applyChange(new AddAxiom(ontology, entityAxiom));
    }
    String[] names = cb.getBinders().get(EntityMode.POJO).getNames();
    for (int i = 0; i < names.length; i++) {
      cnst = factory.getOWLTypedConstant(names[i]);
      entityAxiom = factory.getOWLEntityAnnotationAxiom(owlClass, URI.create(JAVA_NS + "class"), cnst);
      ontologyManager.applyChange(new AddAxiom(ontology, entityAxiom));
    }

    cbMap.put(cb.getName(), owlClass);
    return owlClass;
  }

  /**
   * Create the generator class with the SessionFactory and graph
   * configuration. Please note: one of logical or physical URI need to be supplied
   *
   * @param logicalURI the logical URI for the generated ontology (can be null)
   * @param inputStream the input stream to load addtional OWL assertions from (can be null)
   * @param metaFactory where Topaz class metadata exists
   */
  public OwlGenerator(String logicalURI, InputStream inputStream, SessionFactory metaFactory)
    throws OtmException, OWLException {
    initialize(logicalURI, inputStream, metaFactory);
  }

  /**
   * Add namespace prefixes to make the generated OWL file little more readable.
   *
   * @param style      "text" for Manchester. null or otherwise maps to RDF/XML
   * @param namespaces the namespace map
   */
  public void addNamespaces(String style, Map<String, String> namespaces) {
    NamespaceOWLOntologyFormat format = new RDFXMLOntologyFormat();
    if ((style != null) && (style.equals("text"))) {
      format = new ManchesterOWLSyntaxOntologyFormat();
    }
    for (Map.Entry<String, String> entry:namespaces.entrySet()) {
      format.addPrefixNamespaceMapping(entry.getKey(), entry.getValue());
    }
    format.addPrefixNamespaceMapping(JAVA_PREFIX, JAVA_NS);
    format.addPrefixNamespaceMapping(MULGARA_PREFIX, MULGARA_NS);
    ontologyManager.setOntologyFormat(ontology, format);
  }

  /**
   * Initialize the OWL generator with the information needed. Please note: one of logical or
   * physcial URI need to be supplied.
   *
   * @param logicalURI  logical URI for the ontology (can be null)
   * @param inputStream the input stream to load initial OWL statements from (can be null)
   * @param metaFactory where OTM class metadata exists
   */
  public void initialize(String logicalURI, InputStream inputStream, SessionFactory metaFactory)
    throws OtmException, OWLException {
    if ((logicalURI == null) && (inputStream == null))
      throw new OtmException("One of logical URI or input stream need to be specified.");

    log.info("Initialising OwlGenerator...\n");
    classMap = new HashMap<String, OWLClass>();
    cbMap = new HashMap<String, OWLClass>();

    // Initialize input (OTM) side of things
    topazFactory = metaFactory;

    // Initialize output (OWLAPI) side of things
    ontologyManager = OWLManager.createOWLOntologyManager();
    if (inputStream != null) {
      ontology = ontologyManager.loadOntology(new StreamInputSource(inputStream));
      if (logicalURI != null) {
        ontologyManager.applyChange(new SetOntologyURI(ontology, URI.create(logicalURI)));
      }
    } else {
      ontology = ontologyManager.createOntology(URI.create(logicalURI));
    }
    factory = ontologyManager.getOWLDataFactory();
  }

  /**
   * Save the OWL triples to named physical URI
   *
   * @param physicalURI the physical URI for storing the generated ontology
   */
  public void save(String physicalURI) throws OWLException {
    log.info("Saving ontology to " + physicalURI);
    ontologyManager.saveOntology(ontology, URI.create(physicalURI));
  }

  /**
   * Get the list of class definitions from Topaz and create corresponding OWL class axioms in the
   * ontology. NOTE: this function does not generate properties. It only generates classes and class
   * relationships.
   */
  public void generateClasses() throws OtmException, OWLException {
    for (ClassBinding cb: topazFactory.listClassBindings()) {
      log.debug("Parsing for class axioms: " + cb.getName());

      // Get the corresponding OWL class
      OWLClass owlClass = getOWLClassAxiom(cb);
      if (owlClass == null)
        continue;

      EntityDefinition entity = (EntityDefinition)cb.getClassDefinition();
      for (String entityName : entity.getSuperEntities()) {
        ClassBinding scb = topazFactory.getClassBinding(entityName);
        log.debug("Creating super class axiom " + scb.getName() + " for " + cb.getName());
        OWLClass superClass = getOWLClassAxiom(scb);
        if (superClass == null)
          continue;
        ontologyManager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassAxiom(owlClass,
                                                                                       superClass)));
      }
    }
  }

  /**
   * @param uri the uri
   *
   * @return either the OWLProperty or null associated with the  URI
   */
  private OWLProperty getProperty(String uri) throws OWLException, OtmException {
    URI propURI = URI.create(uri);

    if (ontology.isPunned(propURI))
      throw new OtmException("Punned URIs not currently supported.");

    if (ontology.containsObjectPropertyReference(propURI)) {
      return factory.getOWLObjectProperty(propURI);
    }

    if (ontology.containsDataPropertyReference(propURI)) {
      return factory.getOWLDataProperty(propURI);
    }

    return null;
  }

  /**
   * @param prop the property
   * @param defn the RDF definition
   *
   * @return the range for the predicate
   */
  private OWLPropertyRange getRange (OWLProperty prop, RdfDefinition defn) throws OWLException {
    if (defn.getColType() != CollectionType.PREDICATE) {
      OWLProperty member = factory.getOWLObjectProperty(URI.create(Rdf.rdf + "member"));
      OWLDescription restrict;
      if (topazFactory.getClassBinding(defn.getAssociatedEntity()) == null) {
        URI memberURI = (defn.getDataType() == null) ? XSDVocabulary.ANY_TYPE.getURI() :
                                                       URI.create(defn.getDataType());
        restrict = factory.getOWLObjectAllRestriction((OWLObjectPropertyExpression)member,
                                                       factory.getOWLClass(memberURI));
      } else {
        restrict = factory.getOWLObjectAllRestriction((OWLObjectPropertyExpression)member,
            getOWLClassAxiom(topazFactory.getClassBinding(defn.getAssociatedEntity())));
      }
      Set<OWLDescription> allClasses = CollectionFactory.createSet();
      allClasses.add(restrict);
      return factory.getOWLObjectIntersectionOf(allClasses);
    }

    if (defn.isAssociation()) {
      if (topazFactory.getClassBinding(defn.getAssociatedEntity()) == null)
        return factory.getOWLClass(URI.create(CLASS_NS + defn.getAssociatedEntity()));
      else
        return getOWLClassAxiom(topazFactory.getClassBinding(defn.getAssociatedEntity()));
    }
    if (prop instanceof OWLObjectProperty) {
      if (defn.getDataType() == null)
        return factory.getOWLClass(XSDVocabulary.ANY_TYPE.getURI());
      else
        return factory.getOWLClass(URI.create(defn.getDataType()));
    }

    if (defn.getDataType() == null)
      return factory.getOWLDataType(XSDVocabulary.ANY_TYPE.getURI());
    else
      return factory.getOWLDataType(URI.create(defn.getDataType()));
  }

  /**
   * Generate the list of properties associated with the class defintions. NOTE: this function can
   * be called in addition to generateClasses() to document the properties of the classes
   */
  public void generateProperties() throws OtmException, OWLException {
    if (cbMap.isEmpty())
      generateClasses();

    for (ClassBinding cb: topazFactory.listClassBindings()) {
      for (String propName : cb.getProperties()) {
        if (!(topazFactory.getDefinition(propName) instanceof RdfDefinition))
          continue;

        RdfDefinition defn = (RdfDefinition)topazFactory.getDefinition(propName);
        URI propURI = URI.create(defn.getUri());
        if (!((!defn.isAssociation()) && (defn.getColType() == CollectionType.PREDICATE))) { // Assoc.
          if (!ontology.containsDataPropertyReference(propURI)) {
            OWLProperty objProp = factory.getOWLObjectProperty(URI.create(defn.getUri()));
            ontologyManager.applyChange(new AddAxiom(ontology, factory.getOWLDeclarationAxiom(objProp)));
          }
        }
      }
    }

    for (ClassBinding cb: topazFactory.listClassBindings()) {
      log.debug("Parsing for property axioms: " + cb.getName());
      for (String propName : cb.getProperties()) {
        if (!(topazFactory.getDefinition(propName) instanceof RdfDefinition))
          continue;

        OWLClass domain = getOWLClassAxiom(cb);
        RdfDefinition defn = (RdfDefinition)topazFactory.getDefinition(propName);
        OWLProperty prop = getProperty(defn.getUri());
        if (prop == null) {
          if ((!defn.isAssociation()) && (defn.getColType() == CollectionType.PREDICATE)) { // Literals
            prop = factory.getOWLDataProperty(URI.create(defn.getUri()));
          } else { // Association should have been created
            throw new OtmException("Internal error: Association not expected here: " + defn.toString());
          }
        }

        // Define the range restriction
        OWLPropertyRange range = getRange(prop, defn);
        OWLDescription rangeRestriction = null;
        if (prop instanceof OWLDataProperty) {
          rangeRestriction = factory.getOWLDataAllRestriction((OWLDataPropertyExpression)prop,
                                                              (OWLDataRange)range);
        } else {
          if (!defn.hasInverseUri()) {
            rangeRestriction = factory.getOWLObjectAllRestriction((OWLObjectPropertyExpression)prop,
                                                                  (OWLDescription)range);
          } else {
            rangeRestriction = factory.getOWLObjectAllRestriction((OWLObjectPropertyExpression)prop,
                                                                  (OWLDescription)domain);
          }
        }
        ontologyManager.applyChange(new AddAxiom(ontology,
                                                 factory.getOWLSubClassAxiom(domain, rangeRestriction)));
      }
    }
  }
}
