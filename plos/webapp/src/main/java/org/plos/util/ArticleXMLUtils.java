/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.util;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.ApplicationException;
import org.plos.article.util.NoSuchArticleIdException;
import org.plos.article.util.NoSuchObjectIdException;
import org.plos.article.service.ArticleOtmService;
import org.plos.configuration.ConfigurationStore;

import org.springframework.beans.factory.annotation.Required;

import org.topazproject.xml.transform.cache.CachedSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Convenience class to aggregate common methods used to deal with XML transforms on articles.
 * Used to transform article with annotations, captions of tables/figures, and citation information.
 *
 * @author Stephen Cheng
 * @author Alex Worden
 *
 */
public class ArticleXMLUtils {
  /** Pub Configuration */
  private static final Configuration PUB_CONFIG = ConfigurationStore.getInstance().getConfiguration();
  private final String PUB_APP_CONTEXT = PUB_CONFIG.getString("pub.app-context", "");

  private static final Log log = LogFactory.getLog(ArticleXMLUtils.class);

  private File xslTemplate;
  private DocumentBuilderFactory factory;
  private ArticleOtmService articleService;
  private String articleRep;
  private Map<String, String> xmlFactoryProperty;

  // designed for Singleton use, set in init(), then Templates are threadsafe for reuse
  private Templates objInfoXsltTemplate;  // initialized from objInfo.xsl template
  private Templates translet;             // initialized from xslTemplate, per bean property

  /**
   * Initialization method called by Spring.
   *
   * @throws ApplicationException On Template creation Exceptions.
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
    final URL resource = getClass().getResource(System.getProperty("secondaryObjectXslTemplate", "/objInfo.xsl"));
    if (resource == null) {
      throw new ApplicationException("Failed to get stylesheet: /objInfo.xsl");
    }
    try {
      translet = tFactory.newTemplates(new StreamSource(xslTemplate));
      objInfoXsltTemplate = tFactory.newTemplates(new StreamSource(new File(resource.toURI())));
    } catch (TransformerConfigurationException tce) {
      throw new ApplicationException(tce);
    } catch (URISyntaxException use) {
      throw new ApplicationException(use);
    }
}

  /**
   * Pass in an XML string fragment, and will return back a string representing the document after
   * going through the XSL transform.
   *
   * @param description
   * @return Transformed document as a String
   * @throws ApplicationException
   */
  public String getTranformedDocument(String description) throws ApplicationException {
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
   * Given an article URI, will retrieve XML of article and apply an XSL transform to it, returning
   * the resulting document as a String.
   *
   * @param articleUri
   * @return XML String
   * @throws SAXException
   * @throws ApplicationException
   * @throws NoSuchArticleIdException
   * @throws ParserConfigurationException
   * @throws IOException
   */
  public String getTransformedArticle(String articleUri)
      throws SAXException, ApplicationException, NoSuchArticleIdException,
             ParserConfigurationException, IOException {
    return getTransformedDocument(getArticleAsDocument(articleUri));
  }

  /**
   * Given an XML Document as input, will return an XML string representing the document after
   * transformation.
   *
   * @param doc
   * @return XML String of transformed document
   * @throws ApplicationException
   */
  public String getTransformedDocument(Document doc) throws ApplicationException {
    String transformedString = null;
    try {
      final DOMSource domSource = new DOMSource(doc);
      final Transformer transformer = getTranslet();
      final Writer writer = new StringWriter(1000);

      transformer.transform(domSource, new StreamResult(writer));
      transformedString = writer.toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
    return transformedString;
  }

  /**
   * Convenience method to create a DocumentBuilder with the factory configs
   *
   * @return Document Builder
   * @throws ParserConfigurationException
   */
  public DocumentBuilder createDocBuilder() throws ParserConfigurationException {
    // Create the builder and parse the file
    final DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setEntityResolver(CachedSource.getResolver());
    return builder;
  }
  
  /**
   * Get a translet, compiled stylesheet, for the xslTemplate.
   *
   * @return Translet for the xslTemplate.
   * @throws TransformerException TransformerException.
   */
  private Transformer getTranslet() throws TransformerException {

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    Transformer transformer = translet.newTransformer();
    transformer.setParameter("pubAppContext", PUB_APP_CONTEXT);
    return transformer;
  }

  private Document getArticleAsDocument(final String articleUri)
        throws IOException, SAXException, ParserConfigurationException, NoSuchArticleIdException {

    final String contentUrl;
    try {
      contentUrl = articleService.getObjectURL(articleUri, articleRep);
    } catch (NoSuchObjectIdException ex) {
      throw new NoSuchArticleIdException(articleUri, "(representation=" + articleRep + ")", ex);
    }

    final DataHandler content = new DataHandler(new URLDataSource(new URL(contentUrl)));
    final DocumentBuilder builder = createDocBuilder();
    return builder.parse(content.getInputStream());
  }

  /**
   * Setter for XSL Templates.  Takes in a string as the filename and searches for it in resource
   * path and then as a URI.
   *
   * @param xslTemplate The xslTemplate to set.
   */
  @Required
  public void setXslTemplate(String xslTemplate)  throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    log.debug("XSL template location = " + file.getAbsolutePath());
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @throws URISyntaxException URISyntaxException
   * @return the local or remote file or url as a java.io.File
   */
  public File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      //access it as a local file resource
      return new File(FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Setter for article represenation
   *
   * @param articleRep The articleRep to set.
   */
  public void setArticleRep(String articleRep) {
    this.articleRep = articleRep;
  }

  /**
   * @param xmlFactoryProperty The xmlFactoryProperty to set.
   */
  public void setXmlFactoryProperty(Map<String, String> xmlFactoryProperty) {
    this.xmlFactoryProperty = xmlFactoryProperty;
  }

  /**
   * @param articleService The articleService to set.
   */
  public void setArticleService(ArticleOtmService articleService) {
    this.articleService = articleService;
  }

  /**
   * @return Returns the articleService.
   */
  public ArticleOtmService getArticleService() {
    return articleService;
  }

  /**
   * @return Returns the articleRep.
   */
  public String getArticleRep() {
    return articleRep;
  }

  /**
   * @return Returns the factory.
   */
  public DocumentBuilderFactory getFactory() {
    return factory;
  }
  
  
  /**
   * Transform article XML to HTML for inclusion in the feed
   */
  public String transformArticleDescriptionToHtml(String description) throws ApplicationException {
    String transformedString;
    try {
      final DocumentBuilder builder = createDocBuilder();
      Document desc = builder.parse(new InputSource(new StringReader("<desc>" + description + "</desc>")));
      final DOMSource domSource = new DOMSource(desc);
      final Transformer transformer = getSecondaryObjectTranslet();
      final Writer writer = new StringWriter();

      transformer.transform(domSource,new StreamResult(writer));
      transformedString = writer.toString();
    } catch (Exception e) {
      throw new ApplicationException(e);
    }

    // PLoS stylesheet leaves "END_TITLE" as a marker for other processes
    transformedString = transformedString.replace("END_TITLE", "");
    return transformedString;
  }

  /**
   * Get a translet, compiled stylesheet, for the secondary objects.
   *
   * @return Translet for the secondary objects.
   * @throws TransformerException TransformerException.
   */
  public Transformer getSecondaryObjectTranslet() throws TransformerException {

    // For each thread, instantiate a new Transformer, and perform the
    // transformations on that thread from a StreamSource to a StreamResult;
    return objInfoXsltTemplate.newTransformer();
  }  
}
