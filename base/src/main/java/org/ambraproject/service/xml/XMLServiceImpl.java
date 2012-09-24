/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
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

package org.ambraproject.service.xml;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.ambraproject.ApplicationException;
import org.ambraproject.util.FileUtils;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.xml.transform.cache.CachedSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Convenience class to aggregate common methods used to deal with XML transforms on articles.
 * Used to transform article with annotations, captions of tables/figures, and citation information.
 *
 * @author Bill OConnor
 * @author Stephen Cheng
 * @author Alex Worden
 * @author Joe Osowski
 *
 */
public class XMLServiceImpl implements XMLService {

  private Configuration configuration;

  private static final Logger log = LoggerFactory.getLogger(XMLServiceImpl.class);

  private String xslDefaultTemplate;
  private Map<String, String> xslTemplateMap;
  private DocumentBuilderFactory factory;
  private String articleRep;
  private Map<String, String> xmlFactoryProperty;

  // designed for Singleton use, set in init(), then Templates are threadsafe for reuse
  private Templates translet;             // initialized from xslTemplate, per bean property

  /**
   * Initialization method called by Spring.
   *
   * @throws org.ambraproject.ApplicationException On Template creation Exceptions.
   */
  public void init() throws ApplicationException {
    // set JAXP properties
    System.getProperties().putAll(xmlFactoryProperty);

    // Create a document builder factory and set the defaults
    factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);

    // set the Templates
    final TransformerFactory tFactory = TransformerFactory.newInstance();

    //Because we have XSL sheets with import statements.  I override the URI resolver
    //here so the factory knows to look inside the jar files for these files
    tFactory.setURIResolver(new XMLServiceURIResolver());

    try {
      log.debug("Loading XSL: {}", xslDefaultTemplate);
      translet = tFactory.newTemplates(getResourceAsStreamSource(xslDefaultTemplate));
    } catch (TransformerConfigurationException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    } catch (IOException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

  /**
   * Pass in an XML string fragment, and will return back a string representing the document after
   * going through the XSL transform.
   *
   * @param description
   * @return Transformed document as a String
   * @throws org.ambraproject.ApplicationException
   */
  @Override
  public String getTransformedDocument(String description) throws ApplicationException {
    try {
      final DocumentBuilder builder = createDocBuilder();
      Document desc =
          builder.parse(new InputSource(new StringReader("<desc>" + description + "</desc>")));
      return getTransformedDocument (desc);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error ("Could not transform document", e);
      }
      throw new ApplicationException(e);
    }
  }

  /**
   * Given an XML Document as input, will return an XML string representing the document after
   * transformation.
   *
   * @param doc
   * @return XML String of transformed document
   * @throws org.ambraproject.ApplicationException
   */
  @Override
  public String getTransformedDocument(Document doc) throws ApplicationException {
    String transformedString;
    try {
      if (log.isDebugEnabled())
        log.debug("Applying XSLT transform to the document...");

      final DOMSource domSource = new DOMSource(doc);
      final Transformer transformer = getTranslet(doc);
      final Writer writer = new StringWriter(1000);

      transformer.transform(domSource, new StreamResult(writer));
      transformedString = writer.toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return transformedString;
  }

  @Override
  public byte[] getTransformedByArray(byte[] xml) throws ApplicationException {
    try {

      Document doc = createDocBuilder().parse(new ByteArrayInputStream(xml));
      Transformer transformer = this.getTranslet(doc);
      DOMSource domSource = new DOMSource(doc);

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      transformer.transform(domSource, new StreamResult(bs));
      return bs.toByteArray();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  @Override
  public InputStream getTransformedInputStream(InputStream xml) throws ApplicationException {
    try {
      final Writer writer = new StringWriter(1000);

      Document doc = createDocBuilder().parse(xml);
      Transformer transformer = this.getTranslet(doc);
      DOMSource domSource = new DOMSource(doc);
      transformer.transform(domSource, new StreamResult(writer));
      return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  /**
   * Given a string as input, will return an XML string representing the document after
   * transformation.
   *
   * @param description
   * @return string
   * @throws org.ambraproject.ApplicationException
   */
  @Override
  public String getTransformedDescription(String description) throws ApplicationException {
    String transformedString;
    try {
      final DocumentBuilder builder = createDocBuilder();
      Document desc = builder.parse(new InputSource(new StringReader("<desc>" + description + "</desc>")));
      final DOMSource domSource = new DOMSource(desc);
      final Transformer transformer = getTranslet(desc);
      final Writer writer = new StringWriter();

      transformer.transform(domSource,new StreamResult(writer));
      transformedString = writer.toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }

    // Ambra stylesheet leaves "END_TITLE" as a marker for other processes
    transformedString = transformedString.replace("END_TITLE", "");
    return transformedString;
  }

  /**
   * Convenience method to create a DocumentBuilder with the factory configs
   *
   * @return Document Builder
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  @Override
  public DocumentBuilder createDocBuilder() throws ParserConfigurationException {
    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(CachedSource.getResolver());

    return builder;
  }

  /**
   * Get a translet, compiled stylesheet, for the xslTemplate. If the doc is null
   * use the default template. If the doc is not null then get the DTD version.
   * IF the DTD version does not exist use the default template else use the
   * template associated with that version.
   *
   * @param  doc  the dtd version of document
   * @return Translet for the xslTemplate.
   * @throws javax.xml.transform.TransformerException TransformerException.
   */
  private Transformer getTranslet(Document doc) throws TransformerException, URISyntaxException, IOException {
    Transformer transformer;
    // key is "" if the Attribute does not exist
    String key = (doc == null) ? "default" : doc.getDocumentElement().getAttribute("dtd-version").trim();

    if ((!xslTemplateMap.containsKey(key)) || (key.equalsIgnoreCase(""))) {
      transformer = this.translet.newTransformer();
    } else {
      Templates translet;
      String templateName = xslTemplateMap.get(key);

      // set the Templates
      final TransformerFactory tFactory = TransformerFactory.newInstance();
      //Because we have XSL sheets with import statements.  I override the URI resolver
      //here so the factory knows to look inside the jar files for these files
      tFactory.setURIResolver(new XMLServiceURIResolver());

      //TODO: (performace) We should cache the translets when this class is initialized.  We don't need
      //to parse the XSLs for every transform.

      translet = tFactory.newTemplates(getResourceAsStreamSource(templateName));
      transformer = translet.newTransformer();
    }
    transformer.setParameter("pubAppContext", configuration.getString("ambra.platform.appContext", ""));
    return transformer;
  }

  /**
   * Setter for XSL Templates.  Takes in a string as the filename and searches for it in resource
   * path and then as a URI.
   *
   * @param xslTemplate The xslTemplate to set.
   * @throws java.net.URISyntaxException
   */
  @Required
  public void setXslDefaultTemplate(String xslTemplate)  throws URISyntaxException {
    log.debug("setXslDefaultTemplate called: {}", xslTemplate);
    this.xslDefaultTemplate = xslTemplate;
  }

   /**
   * Setter for XSL Templates.  Takes in a string as the filename and searches for it in resource
   * path and then as a URI.
   *
   * @param xslTemplateMap The xslTemplate to set.
   */
  @Required
  public void setXslTemplateMap(Map<String, String> xslTemplateMap) {
    this.xslTemplateMap = xslTemplateMap;
  }

  /**
   * Setter method for configuration. Injected through Spring.
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Setter for article represenation
   *
   * @param articleRep The articleRep to set.
   */
  @Required
  public void setArticleRep(String articleRep) {
    this.articleRep = articleRep;
  }

  /**
   * @param xmlFactoryProperty The xmlFactoryProperty to set.
   */
  @Required
  public void setXmlFactoryProperty(Map<String, String> xmlFactoryProperty) {
    this.xmlFactoryProperty = xmlFactoryProperty;
  }

  /**
   * @return Returns the articleRep.
   */
  public String getArticleRep() {
    return articleRep;
  }

  private StreamSource getResourceAsStreamSource(String filename) throws IOException {
    log.debug("Loading: {}", filename);

    URL loc = getClass().getClassLoader().getResource(filename);

    //If Loading resource fails, try getting the physical file
    if(loc == null) {
      File xsl = new File(filename);

      log.debug("Found File: {}", xsl.getPath());

      return new StreamSource(xsl);
    } else {
      InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
      StreamSource source = new StreamSource(is);

      log.debug("Found Resource: {}", loc.getFile());

      //Note: http://stackoverflow.com/questions/7236291/saxon-error-with-xslt-import-statement
      source.setSystemId(loc.getFile());

      return source;
    }
  }

  class XMLServiceURIResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
      try {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(href);
        return new StreamSource(inputStream);
      } catch(Exception ex) {
        throw new TransformerException(ex.getMessage(), ex);
      }
    }
  }
}
