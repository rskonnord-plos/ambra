/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 *
 * Modified from code part of Fedora. It's license reads:
 * License and Copyright: The contents of this file will be subject to the
 * same open source license as the Fedora Repository System at www.fedora.info
 * It is expected to be released with Fedora version 2.2.
 * Copyright 2006 by The Technical University of Denmark.
 * All rights reserved.
 */
package org.topazproject.fedoragsearch.topazlucene;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.Date;
import java.util.HashMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.topazproject.xml.transform.cache.CachedSource;

import dk.defxws.fedoragsearch.server.errors.GenericSearchException; // Wraps RMIException

/**
 * Performs the stylesheet transformations.
 *
 * This is used by OperationsImpl to create xml to insert into lucene and to transform
 * results to xml for clients.
 * 
 * @author  Eric Brown and <a href='mailto:gsp@dtv.dk'>Gert</a>
 * @version $Id$
 */
public class TopazTransformer {
  private static final Log log = LogFactory.getLog(TopazTransformer.class);

  /**
   * An XML-entity caching URIResolver used by the transformer.
   *
   * @see javax.xml.transform.Transformer#setURIResolver
   */
  private static class TopazURIResolver implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
      if (log.isDebugEnabled())
        log.debug("Resolving: " + href + " at " + base);

      try {
        // TODO: Use this once systemids set properly: URL url = new URL(new URL(base), href);
        URL url = new URL(href);
        return new CachedSource(new InputSource(url.toString()));
      } catch (SAXException se) {
        log.warn("Unable to create CachedSource for '" + href + "' relative to '" + base + "'", se);
      } catch (MalformedURLException mue) {
        log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
                 "default URIResolver", mue);
      }
      return null; // Let the processor figure it out!
    }
  }

  /**
   * Build a Transformer object from xslt stored as a resource.
   *
   * @param xsltName is the name of the xslt sheet to fetch. It is assumed that this
   *                 sheet is stored as a resource in /config/<name>.xslt or a number
   *                 of other directories: /config/index or /config/index/common.
   * @return a Transfomer object that can be used to do the transformation.
   *
   * @throws GenericSearchException if there is an error creating the Transformer object
   */
  private static Transformer getTransformer(String xsltName) throws GenericSearchException {
    // TODO: These things *could* be cached, but then we'd have syncronization issues
    try {
      // TODO: The name or actual xslt-stream could easily be cached
      // Try first to read from stylesheet in OUR jar
      String name = xsltName + ".xslt";
      InputStream is = TopazTransformer.class.getResourceAsStream(name);
      // Next look in configured location for this repository / index
      if (is == null) {
        name = (xsltName.startsWith("rest/") ? "/config/" : "/config/index/") + xsltName + ".xslt";
        is = TopazTransformer.class.getResourceAsStream(name);
      }
      if (is == null) { // If can't find specific style-sheet, look for common style-sheets
        name = "/config/index/common" + xsltName.substring(xsltName.indexOf("/")) + ".xslt";
        is = TopazTransformer.class.getResourceAsStream(name);
      }
      if (is == null) // If we still can't find a style-seet, raise an exception
        throw new GenericSearchException("Unable to find stylesheet: " + xsltName);
      TransformerFactory tfactory = TransformerFactory.newInstance();
      if (log.isDebugEnabled())
        log.debug(tfactory.getClass().getName() + " parsing resource " + name);
      StreamSource xslt = new StreamSource(is);
      Transformer transformer = tfactory.newTransformer(xslt);
      transformer.setURIResolver(new TopazURIResolver());
      return transformer;
    } catch (TransformerConfigurationException e) {
      throw new GenericSearchException(xsltName + ": " + e.getMessageAndLocation(), e);
    } catch (TransformerFactoryConfigurationError e) {
      throw new GenericSearchException(xsltName, e);
    }
  }

  public static StringBuffer transform(String xsltName, InputStream is, String[] params)
      throws GenericSearchException {
    // TODO: params should really be a HashMap!
    if (log.isDebugEnabled())
      log.debug("xsltName=" + xsltName);
    
    Transformer transformer = getTransformer(xsltName);
    transformer.setParameter("DATETIME", new Date()); // Always there (not sure how this is used)
    for (int i = 0; i < params.length; i = i+2) {
      Object value = params[i+1];
      if (value == null)
        value = "";
      transformer.setParameter(params[i], value);
    }

    if (log.isDebugEnabled()) log.debug("Transform starting");
    
    try {
      StringWriter result = new StringWriter();
      Source source = new CachedSource(new InputSource(is)); // new StreamSource(is);
      transformer.transform(source, new StreamResult(result));
      return result.getBuffer();
    } catch (SAXException se) {
      // TODO: add more to trace -- problem is input stream afterall
      throw new GenericSearchException(xsltName, se);
    } catch (TransformerException e) {
      // TODO: Add list of strings and part of xml source?
      throw new GenericSearchException(xsltName, e);
    } finally {
      if (log.isDebugEnabled()) log.debug("Transform finished");
    }
  }

  public static StringBuffer transform(String xsltName, StringBuffer sb, String[] params)
      throws GenericSearchException {
    // TODO: StringBufferInputStream is deprecated
    return transform(xsltName, new StringBufferInputStream(sb.toString()), params);
  }
}
