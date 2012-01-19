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

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.FieldBinder;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.RDFXMLOntologyFormat;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
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

  // Map of created OWL classes
  HashMap classMap;

  // Map of created OWL properties
  HashMap propertyMap;

  // Logical URI for generated ontology
  String logicalURI;

  // The OTM side of things
  SessionFactory otmFactory;

  // OWLAPI factories etc.
  OWLOntologyManager ontologyManager;
  OWLOntology ontology;
  OWLDataFactory factory;

  /**
   * Return true if the class metadata is a typed entity
   *
   * @param cm the class metadata
   *
   * @return true if cm is a typed entity
   */
  private boolean isTypedEntity(ClassMetadata cm) {
    if (cm == null)
      return false;
    if (cm.getType() == null)
      return false;

    return true;
  }

  /**
   * Return the list of super-classes
   *
   * @param sourceClass the starting point source class
   *
   * @return superClasses hash map of java super classes for a class
   */
  private HashMap<String, Class> getSuperClasses(Class sourceClass) {
    HashMap<String, Class> superClasses = new HashMap();
    Class clazz = sourceClass;
    while (clazz != Object.class) {
      clazz = clazz.getSuperclass();
      if (clazz != Object.class)
        superClasses.put(clazz.getName(), clazz);
    }

    return superClasses;
  }

  /**
   * Return the OWLClass associated with this metadata information
   *
   * @param type the type for the metadata
   * @param name the name for the class
   *
   * @return the OWLClass either from cache or newly created
   */
  private OWLClass getOWLClass(String type, String name) {
    OWLClass owlClass = (OWLClass)classMap.get(type);
    if (owlClass == null) {
      owlClass = factory.getOWLClass(URI.create(type));
      classMap.put(type, owlClass);
      log.debug("Cached OWL Class " + owlClass.getURI() + " created for " + name);
    }

    return owlClass;
  }

  /**
   * Return the OWLObjectProperty associated with this metadata information
   *
   * @param type the type for the metadata
   * @param name the name for the property
   *
   * @return the ObjectProperty either from cache or newly created
   */
  private OWLObjectProperty getOWLObjectProperty(String type, String name) {
    OWLObjectProperty objectProperty = (OWLObjectProperty)propertyMap.get(type);
    if (objectProperty == null) {
      objectProperty = factory.getOWLObjectProperty(URI.create(type));
      propertyMap.put(type, objectProperty);
      log.debug("Cached OWL Property Class " + objectProperty.getURI() + " created for " + name);
    }

    return objectProperty;
  }

  /**
   * Create the generator class with the SessionFactory and model
   * configuration.
   *
   * @param logicalURI the logical URI for the generated ontology
   * @param metaFactory where OTM class metadata exists
   */
  public OwlGenerator(String logicalURI, SessionFactory metaFactory) throws
    OtmException, OWLException {
      this.logicalURI = logicalURI;
      initialize(metaFactory);
    }

  /**
   * Initialize the OWL generator with the information needed.
   *
   * @param metaFactory where OTM class metadata exists
   */
  public void initialize(SessionFactory metaFactory) throws OtmException,OWLException {
    log.debug("Initialising OwlGenerator...\n");
    classMap = new HashMap();
    propertyMap = new HashMap();

    // Initialize input (OTM) side of things
    otmFactory = metaFactory;

    // Initialize output (OWLAPI) side of things
    ontologyManager = OWLManager.createOWLOntologyManager();
    ontology = ontologyManager.createOntology(URI.create(logicalURI));
    factory = ontologyManager.getOWLDataFactory();
  }

  /**
   * Try and save the OWL triples to a name graph
   *
   * @param graph the named graph where the OWL metadata is to be stored
   */
  public void save(ModelConfig graph) throws OtmException {
    otmFactory.addModel(graph);
    otmFactory.getTripleStore().createModel(graph);
  }

  /**
   * Save the OWL triples to named physical URI
   *
   * @param physicalURI the physical URI for storing the generated ontology
   */
  public void save(String physicalURI) throws OWLException {
    RDFXMLOntologyFormat format = new RDFXMLOntologyFormat();
    format.addPrefixNamespaceMapping("topaz", "http://rdf.topazproject.org/RDF/");
    format.addPrefixNamespaceMapping("plos", "http://rdf.plos.org/RDF/");
    format.addPrefixNamespaceMapping("annotea", "http://www.w3.org/2000/10/annotation-ns#");
    ontologyManager.setOntologyFormat(ontology, format);
    ontologyManager.saveOntology(ontology, URI.create(physicalURI));
  }

  /**
   * Generate the list of properties only. Use this function to dump out the
   * list of predicates with the 'union of' domain and ranges for the
   * predicates.
   */
  public void generateProperties() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata cm: otmFactory.listClassMetadata()) {
      log.debug("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      if (type == null)
        continue;
      OWLClass domain = getOWLClass(type,cm.getName());

      // Now let's iterate over the fields to define 'restrictions' on properties
      for (RdfMapper m: cm.getRdfMappers()) {
        log.debug("Processing field: " + m.getName());
        // Ignore if not annotated
        if (m.getUri() == null)
          continue;

        // See if this field really belongs to our class/type
        Field f = ((FieldBinder)m.getBinder(EntityMode.POJO)).getField();
        log.debug("Field Class extracted: " + f);

        AddAxiom domainAxiom = null;
        AddAxiom rangeAxiom = null;
        if (m.isAssociation()) {
          log.debug("Processing association field: " + m.getName());
          OWLObjectProperty objectProperty = getOWLObjectProperty(m.getUri(), m.getName());
          OWLClass range = null;
          if (getRdfType(m) != null)
            range = getOWLClass(getRdfType(m), m.getAssociatedEntity());
          else
            range = factory.getOWLThing();
          OWLAxiom domainRestriction = null;
          OWLAxiom rangeRestriction = null;
          if (!m.hasInverseUri()) {
            domainRestriction = factory.getOWLObjectPropertyDomainAxiom(objectProperty, domain);
            rangeRestriction = factory.getOWLObjectPropertyRangeAxiom(objectProperty, range);
          } else {
            // domain and range are inverted
            domainRestriction = factory.getOWLObjectPropertyDomainAxiom(objectProperty, range);
            rangeRestriction = factory.getOWLObjectPropertyRangeAxiom(objectProperty, domain);
          }

          domainAxiom = new AddAxiom(ontology, domainRestriction);
          rangeAxiom = new AddAxiom(ontology, rangeRestriction);
        } else {
          log.debug("Processing literal field: " + m.getName());
          OWLDataProperty dataProperty = factory.getOWLDataProperty(URI.create(m.getUri()));
          OWLDataRange range = null;
          if (m.getDataType() != null)
            range = factory.getOWLDataType(URI.create(m.getDataType()));
          else
            range = factory.getOWLDataType(XSDVocabulary.ANY_TYPE.getURI());
          OWLAxiom domainRestriction = factory.getOWLDataPropertyDomainAxiom(dataProperty, domain);
          OWLAxiom rangeRestriction = factory.getOWLDataPropertyRangeAxiom(dataProperty, range);

          domainAxiom = new AddAxiom(ontology, domainRestriction);
          rangeAxiom = new AddAxiom(ontology, rangeRestriction);
        }

        ontologyManager.applyChange(domainAxiom);
        ontologyManager.applyChange(rangeAxiom);
      }
    }
  }

  /**
   * Generate class relationships only. This function will dump only class
   * definitions (not Property classes) and sub class relationships.
   */
  public void generateClasses() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata cm: otmFactory.listClassMetadata()) {
      log.debug("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      // Ignore anonymous classes
      if (type == null) {
        continue;
      }

      // Get the corresponding OWL class
      OWLClass owlClass = getOWLClass(type,cm.getName());
      ontologyManager.applyChange(new AddAxiom(ontology, factory.getOWLDeclarationAxiom(owlClass)));
      HashMap<String, Class> superClasses = getSuperClasses(getSourceClass(cm));

      // Create sub-class relationships
      for (Class c: superClasses.values()) {
        ClassMetadata scm = otmFactory.getClassMetadata(c);
        if (!isTypedEntity(scm))
          continue;
        log.debug("Processing super-class: " + scm.getName());
        OWLClass superOwlClass = getOWLClass(scm.getType(),scm.getName());

        // Because of pre-computed types.
        if (superOwlClass == owlClass)
          continue;

        ontologyManager.applyChange(new AddAxiom(ontology,
              factory.getOWLSubClassAxiom(owlClass, superOwlClass)));
        log.debug("Making " + owlClass.getURI() + " subClass of " + superOwlClass.getURI());
      }
    }
  }

  /**
   * Generate the object property restrictions on each class. This function
   * dumps only the object property restrictions for every class defined.
   */
  public void generateClassObjectProperties() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata cm: otmFactory.listClassMetadata()) {
      log.debug("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      // Ignore anonymous classes
      if (type == null) {
        continue;
      }

      // Get the corresponding OWL class
      OWLClass domain = getOWLClass(type,cm.getName());
      HashMap<String, Class> superClasses = getSuperClasses(getSourceClass(cm));

      // Now let's iterate over the fields to define 'restrictions' on properties
      for (RdfMapper m: cm.getRdfMappers()) {
        log.debug("Processing field: " + m.getName());
        // If not annotated or not association
        if ((m.getUri() == null) || (!m.isAssociation()))
          continue;

        // See if this field really belongs to our class/type
        Field f = ((FieldBinder)m.getBinder(EntityMode.POJO)).getField();
        log.debug("Field Class extracted: " + f);
        Class clazz = f.getDeclaringClass();
        if (clazz != getSourceClass(cm)) {
          // See if it exists in super classes
          Class c = (Class)superClasses.get(clazz.getName());
          if (c != null) {
            ClassMetadata fCm = otmFactory.getClassMetadata(c);
            if (isTypedEntity(fCm))
              // It will be taken care of in the super class
              continue;
          }
        }

        AddAxiom addAxiom = null;
        log.debug("Processing association field: " + m.getName());
        OWLObjectProperty objectProperty = getOWLObjectProperty(m.getUri(), m.getName());
        OWLClass range = null;
        if (getRdfType(m) != null)
          range = getOWLClass(getRdfType(m), m.getAssociatedEntity());
        else
          range = factory.getOWLThing();

        if (!m.hasInverseUri()) {
          OWLDescription rangeRestriction = factory.getOWLObjectAllRestriction(objectProperty, range);
          addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(domain, rangeRestriction));
        } else {
          // domain and range are inverted
          OWLDescription rangeRestriction = factory.getOWLObjectAllRestriction(objectProperty, domain);
          addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(range, rangeRestriction));
        }

        ontologyManager.applyChange(addAxiom);
      }
    }
  }

  /**
   * Generate the data property restrictions on each class.
   */
  public void generateClassDataProperties() throws OtmException, OWLException {
    // Loop over all classes in factory
    for (ClassMetadata cm: otmFactory.listClassMetadata()) {
      log.debug("Parsing OTM Class metadata: " + cm.getName());
      String type = cm.getType();
      // Ignore anonymous classes
      if (type == null) {
        continue;
      }

      // Get the corresponding OWL class
      OWLClass domain = getOWLClass(type,cm.getName());
      HashMap<String, Class> superClasses = getSuperClasses(getSourceClass(cm));

      // Now let's iterate over the fields to define 'restrictions' on properties
      for (RdfMapper m: cm.getRdfMappers()) {
        log.debug("Processing field: " + m.getName());
        // If not annotated or an association
        if ((m.getUri() == null) || (m.isAssociation()))
          continue;

        // See if this field really belongs to our class/type
        Field f = ((FieldBinder)m.getBinder(EntityMode.POJO)).getField();
        log.debug("Field Class extracted: " + f);
        Class clazz = f.getDeclaringClass();
        if (clazz != getSourceClass(cm)) {
          // See if it exists in super classes
          Class c = (Class)superClasses.get(clazz.getName());
          if (c != null) {
            ClassMetadata fCm = otmFactory.getClassMetadata(c);
            // It will be taken care of in the super class
            if (isTypedEntity(fCm))
              continue;
          }
        }

        log.debug("Processing data field: " + m.getName());
        OWLDataProperty dataProperty = factory.getOWLDataProperty(URI.create(m.getUri()));
        OWLDataRange typeRange = null;
        if (m.getDataType() != null)
          typeRange = factory.getOWLDataType(URI.create(m.getDataType()));
        else
          typeRange = factory.getOWLDataType(XSDVocabulary.ANY_TYPE.getURI());
        OWLDescription rangeRestriction = factory.getOWLDataAllRestriction(dataProperty, typeRange);
        AddAxiom addAxiom = new AddAxiom(ontology, factory.getOWLSubClassAxiom(domain, rangeRestriction));

        ontologyManager.applyChange(addAxiom);
      }
    }
  }

  private static Class getSourceClass(ClassMetadata cm) {
    // XXX: temporary
    return ((ClassBinder)cm.getEntityBinder(EntityMode.POJO)).getSourceClass();
  }

  private String getRdfType(RdfMapper m) {
    ClassMetadata cm = otmFactory.getClassMetadata(m.getAssociatedEntity());
    if (cm != null)
      return cm.getType();
    return null;
  }
}
