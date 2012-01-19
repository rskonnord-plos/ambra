/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.xml.transform;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a {@link Source} that can be used in a {@link javax.xml.transform.Transformer#transform}
 * call with a custom entity resolver.
 *
 * @author Eric Brown and Ronald Tschalär
 * @version $Id$
 */
public class EntityResolvingSource extends SAXSource implements Source {
  private static final Log log = LogFactory.getLog(EntityResolvingSource.class);
  private static final String xmlReaderCName;

  static {
    /* Remember the name of the XMLReader class so we can use it in the XMLReaderFactory
     * call. Note that we don't set the org.xml.sax.driver property because there seem to
     * be cases where that property is removed again.
     */
    String cname = null;
    try {
      cname = SAXParserFactory.newInstance().newSAXParser().getXMLReader().getClass().getName();
      log.info("Using XMLReader: " + cname);
    } catch (Exception e) {
      log.warn("Failed to get the XMLReader class", e);
    }
    xmlReaderCName = cname;
  }

  /**
   * Construct a new EntityResolvingSource object.
   *
   * @param src is the source of the XML document
   * @param resolver is the custom resolver
   * @throws SAXException if we're unable to create an XMLReader (Should never happen
   *         unless there is a serious initialization problem that is likely the result
   *         of a mis-configuration of the JDK. Multiple errors will likely be logged.)
   */
  public EntityResolvingSource(InputSource src, EntityResolver resolver) throws SAXException {
    super(createXMLReader(resolver), src);
    if (log.isDebugEnabled())
      log.debug("Created " + this.getClass().getName() + " w/" + resolver.getClass().getName());
  }

  private static XMLReader createXMLReader(EntityResolver resolver) throws SAXException {
    try {
      XMLReader rdr = XMLReaderFactory.createXMLReader(xmlReaderCName);
      rdr.setEntityResolver(resolver);
      return rdr;
    } catch (SAXException se) {
      log.error("Unable to create XMLReader from " + xmlReaderCName, se);
      throw se;
    }
  }
}
