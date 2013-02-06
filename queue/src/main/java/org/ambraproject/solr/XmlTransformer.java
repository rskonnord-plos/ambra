/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.solr;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.SaxonOutputKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Class that transforms an NLM DTD journal article into the document that we send to
 * solr for indexing.
 * <p/>
 * This code is currently not used anywhere in the ambra project proper, but is used
 * by the (old) plos queue and the (new) indexer minion.
 */
public class XmlTransformer {
  private static final Logger log = LoggerFactory.getLogger(XmlTransformer.class);

  private Properties defaultProperties;

  private Map<String, Transformer> transformerMap;

  public void init() {
    defaultProperties = new Properties();
    defaultProperties.put(OutputKeys.ENCODING, "UTF-8");
    defaultProperties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
    defaultProperties.put(OutputKeys.INDENT, "yes");
    defaultProperties.put(OutputKeys.METHOD, "xml");
    defaultProperties.put(OutputKeys.MEDIA_TYPE, "text/xml");
  }

  /**
   * Transform the document to a format solr can understand.
   *
   * This supports multiple versions of the DTD.  This method detects the DTD
   * and uses the appropriate transformer.
   */
  public String transform(Document article)
      throws TransformerException, ParserConfigurationException {
    StreamResult dst = null;
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    try {
      DOMSource src = new DOMSource(article);
      dst = new StreamResult(streamOut);
      Transformer transformer = getTranslet(article);
      transformer.transform(src, dst);
    } catch (TransformerException e ) {
      log.error("Transformation error: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }
    return streamOut.toString();
  }

  /**
   * Get the XSL specific to the NLM DTD Version
   *
   * @param doc article XML
   *
   * @return an XSL transformer that can be used on doc
   * @throws TransformerException
   */
  private Transformer getTranslet(Document doc) throws TransformerException {
    String key = doc.getDocumentElement().getAttribute("dtd-version");
    log.debug("Got a dtd-version of: {}", key);
    if (key == null || key.length() == 0) {
      throw new IllegalArgumentException("Cannot identify DTD version of the article XML");
    } else {
      key = key.trim();
      if (transformerMap.containsKey(key)) {
        return transformerMap.get(key);
      } else {
        throw new IllegalArgumentException("DTD version " + key + " is not supported");
      }
    }
  }

  /**
   * Sets the .xsl files used by this class to transform NLM article documents.
   *
   * @param xslTemplateMap a map from a string indicating a DTD version (e.g. "3.0")
   *     to a string that is a (classpath-relative) filename of the .xsl file for
   *     that version
   * @throws TransformerConfigurationException
   */
  public void setXslTemplateMap(Map<String, String> xslTemplateMap)
      throws TransformerConfigurationException {
    if (xslTemplateMap == null) {
      throw new IllegalArgumentException("transformerMap property not initialized");
    }
    transformerMap = new HashMap<String, Transformer>(xslTemplateMap.size());
    for (String key : xslTemplateMap.keySet()) {
      String filename = xslTemplateMap.get(key);

      // set the Templates
      final TransformerFactory tFactory = TransformerFactoryImpl.newInstance();
      InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
      if (is == null) {
        throw new IllegalArgumentException("Could not locate style sheet: " + filename);
      }
      Templates translet = tFactory.newTemplates(new StreamSource(is));
      Transformer transformer = translet.newTransformer();
      transformer.setOutputProperties(defaultProperties);
      transformerMap.put(key, transformer);
    }
  }
}
