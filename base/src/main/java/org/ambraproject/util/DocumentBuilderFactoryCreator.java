/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Bean creator for DocumentBuilderFactory.
 *
 * @author Dragisa Krsmanovic
 */
public class DocumentBuilderFactoryCreator {

  private static final Logger log = LoggerFactory.getLogger(DocumentBuilderFactoryCreator.class);

  private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String DOCUMENT_BUILDER_FACTORY_CLASS = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";

  static {
    log.info("Setting system properties for xml transforms");

    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
    System.setProperty("javax.xml.transform.Transformer", "net.sf.saxon.Controller");
  }

  /**
   * Static factory method to create document builders that resolve entities from the classpath
   */
  public static DocumentBuilderFactory createFactory(final ConcurrentMap<String, String> dtdMap) {
    log.info("Creating Document builder factory: " + DOCUMENT_BUILDER_FACTORY_CLASS);

    final DocumentBuilderFactory documentBuilderfactory = DocumentBuilderFactory
        .newInstance(DOCUMENT_BUILDER_FACTORY_CLASS, DocumentBuilderFactory.class.getClassLoader());

    documentBuilderfactory.setNamespaceAware(true);
    documentBuilderfactory.setValidating(false);
    try {
      documentBuilderfactory.setFeature(LOAD_EXTERNAL_DTD, false);
    } catch (ParserConfigurationException e) {
      log.error("Error setting " + LOAD_EXTERNAL_DTD + " feature.", e);
    }

    //decorate the document builder factory to create document builders with custom entity resolvers
    return new DocumentBuilderFactory() {
      @Override
      public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = documentBuilderfactory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new EntityResolver() {
          @Override
          public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            //if we know about this entity, pull it off the classpath
            String classpathName = dtdMap.get(systemId);
            InputStream dtdStream;
            if (classpathName != null &&
                ((dtdStream = this.getClass().getClassLoader().getResourceAsStream(classpathName)) != null)) {
              log.debug("Found entity {} as classpath resource {}", publicId, classpathName);
              InputSource inputSource = new InputSource();
              byte[] dtdBytes = IOUtils.toByteArray(dtdStream);
              inputSource.setByteStream(new ByteArrayInputStream(dtdBytes));
              inputSource.setSystemId(systemId);
              inputSource.setPublicId(publicId);
              return inputSource;
            } else {
              //use the default behaviour
              return null;
            }
          }
        });
        return documentBuilder;
      }

      @Override
      public void setAttribute(String name, Object value) throws IllegalArgumentException {
        documentBuilderfactory.setAttribute(name, value);
      }

      @Override
      public Object getAttribute(String name) throws IllegalArgumentException {
        return documentBuilderfactory.getAttribute(name);
      }

      @Override
      public void setFeature(String name, boolean value) throws ParserConfigurationException {
        documentBuilderfactory.setFeature(name, value);
      }

      @Override
      public boolean getFeature(String name) throws ParserConfigurationException {
        return documentBuilderfactory.getFeature(name);
      }
    };
  }
}
