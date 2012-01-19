/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008 by Topaz, Inc.
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

package org.topazproject.otm.search;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.topazproject.otm.OtmException;
import org.topazproject.otm.metadata.SearchableDefinition;

/**
 * A pre-processor that just strips all tags and replaces each with a space.
 *
 * @author Ronald Tschalär
 */
public class XmlTagStripper implements PreProcessor {
  /**
   * Remove all tags.
   *
   * @param o     the object involved whose property is to be indexed
   * @param def   the searchable definition for the property
   * @param value the xml document
   * @return the stripped document
   * @throws OtmException if an error occurs while parsing the input
   */
  public String process(Object o, SearchableDefinition def, String value) throws OtmException {
    try {
      StringWriter out = new StringWriter(value.length());

      XMLReader parser = newParser();
      parser.setContentHandler(newContentHandler(out));
      parser.parse(new InputSource(new StringReader(value)));

      return out.toString();
    } catch (Exception e) {
      throw new OtmException("Error parsing document", e);
    }
  }

  /**
   * Create a new parser. May be overridden to create different parsers or to customize the default
   * parser.
   *
   * @return the parser to use
   * @throws Exception if a problem occurred creating the parser
   */
  protected XMLReader newParser() throws Exception {
    return XMLReaderFactory.createXMLReader();
  }

  /**
   * Create a new content-handler. This is responsible for generating the text to be indexed. May be
   * overridden to create your own handler to to customize the default one.
   *
   * @param out where the content-handler should write the resulting text
   * @return the content-handler to use
   * @throws Exception if a problem occurred creating the content-handler.
   */
  protected ContentHandler newContentHandler(Writer out) throws Exception {
    return new TagStripper(out);
  }

  /**
   * A {@link ContentHandler} that just prints out the element text separated by space. Optionally,
   * certain elements may be skipped altogether by subclassing this and overriding {@link #include}.
   *
   * @author Ronald Tschalär
   */
  protected static class TagStripper extends DefaultHandler {
    protected final Writer  out;
    protected       boolean skip = false;
    protected       int     skipDepth = 0;

    /**
     * Create a new tag stripper.
     *
     * @param out where to write the resulting text
     */
    public TagStripper(Writer out) {
      this.out = out;
    }

    public void startElement(String ns, String localName, String qName, Attributes attributes) {
      if (!skip && !include(ns, localName, qName))
        skip = true;

      if (skip)
        skipDepth++;
    }

    public void endElement(String ns, String localName, String qName) {
      if (skip && --skipDepth == 0)
        skip = false;
    }

    /**
     * Whether or not include the contents of the given tag. Override this to control what to
     * be indexed. Unless overridden this returns true.
     *
     * @param ns        the namespace uri of the tag
     * @param localName the local-name of the tag
     * @param qName     the qname of the tag
     * @return true if the contents of this tag are to be indexed
     * @see ContentHandler#startElement
     */
    protected boolean include(String ns, String localName, String qName) {
      return true;
    }

    public void characters(char[] text, int start, int length) throws SAXException {
      if (skip)
        return;

      try {
        out.write(text, start, length);
        out.write(' ');
      } catch (IOException ioe) {
        throw new SAXException("Error writing out text", ioe);
      }
    }
  }
}
