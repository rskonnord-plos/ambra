/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.models;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;

import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.TripleStore;
import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.Searchable;
import org.topazproject.otm.annotations.SubClassResolver;
import org.topazproject.otm.metadata.RdfDefinition;
import org.topazproject.otm.metadata.SearchableDefinition;
import org.topazproject.otm.search.PreProcessor;
import org.topazproject.otm.search.HtmlTagStripper;
import org.topazproject.otm.search.RtfMarkupStripper;
import org.topazproject.otm.search.XmlTagStripper;
import org.topazproject.xml.transform.cache.CachedSource;

/**
 * This specifies a representation that is text and that should be indexed for text searching.
 * All this does is mark the body as searchable.
 *
 * @author Ronald Tschalär
 */
@Entity
public class TextRepresentation extends Representation {
  private static final long serialVersionUID = 1;

  /**
   * No argument constructor for OTM to instantiate.
   */
  public TextRepresentation() {
  }

  /**
   * Creates a new TextRepresentation object. The id of the object is
   * derived from the doi of the ObjectInfo and the representation name.
   *
   * @param object the object this representation belongs to
   * @param name the name of this representation
   */
  public TextRepresentation(ObjectInfo object, String name) {
    super(object, name);
  }

  @Override
  @Searchable(uri = "topaz:body", index = "lucene", preProcessor = BodyPreProcessor.class)
  @org.topazproject.otm.annotations.Blob  // FIXME: remove
  public void setBody(org.topazproject.otm.Blob body) {
    super.setBody(body);
  }

  /**
   * Set the body. The string is UTF-8 encoded to create the byte[]. This assumes a body
   * already exists and replaces its contents.
   *
   * @param body the body
   */
  public void setBodyAsText(String body) {
    try {
      super.getBody().writeAll(body.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected problem with UTF-8", uee);
    }
  }

  /**
   * Get the body as a String. The encoding is assumed to be UTF-8.
   *
   * @return the body
   */
  public String getBodyAsText() {
    try {
      return new String(super.getBody().readAll(), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Unexpected problem with UTF-8", uee);
    }
  }

  /**
   * A {@link org.topazproject.otm.SubClassResolver} which detects Representation's with a
   * content-type of 'text/...' and selects this class instead.
   * @param superEntity       the root of the entity hierarchy that the resolved class
   *                          must be a sub-class-of
   * @param instantiatableIn  the entity mode that the resolved sub-class must be instantiatable in
   * @param sf                the session factory
   * @param typeUris          the rdf:type values found in the data
   * @param statements        the data
   * @return                  the resolved class or null
   */
  @SubClassResolver
  public static ClassMetadata resolve(ClassMetadata superEntity, EntityMode instantiatableIn,
                                      SessionFactory sf, Collection<String> typeUris,
                                      TripleStore.Result statements) {
    if (instantiatableIn != EntityMode.POJO ||
        !superEntity.isAssignableFrom(sf.getClassMetadata(Representation.class), EntityMode.POJO))
      return null;

    String p = ((RdfDefinition) sf.getClassMetadata(Representation.class).
                                   getMapperByName("contentType").getDefinition()).getUri();

    if (statements.getFValues().get(p).get(0).startsWith("text/"))
      return sf.getClassMetadata(TextRepresentation.class);
    else
      return sf.getClassMetadata(Representation.class);
  }

  /**
   * A search pre-processor that handles text/xml, text/html, and text/rtf bodies. Xml is assumed
   * to be NLM and the 'front' and 'back' are stripped out, i.e. only the 'body' (including those
   * of any sub-articles or responses) is extracted.
   */
  public static class BodyPreProcessor implements PreProcessor {
    public String process(Object o, SearchableDefinition def, String value) {
      String ct = ((TextRepresentation) o).getContentType();
      if (ct.equals("text/xml"))
        return new NlmTagStripper().process(o, def, value);
      else if (ct.equals("text/html"))
        return new HtmlTagStripper().process(o, def, value);
      else if (ct.equals("text/rtf"))
        return new RtfMarkupStripper().process(o, def, value);
      else
        return value;
    }

    private static class NlmTagStripper extends XmlTagStripper {
      protected XMLReader newParser() throws Exception {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setEntityResolver(CachedSource.getResolver());
        return reader;
      }

      protected ContentHandler newContentHandler(Writer out) {
        return new TagStripper(out);
      }

      private static class TagStripper extends XmlTagStripper.TagStripper {
        public TagStripper(Writer out) {
          super(out);
        }

        protected boolean include(String ns, String localName, String qName) {
          return !localName.equals("front") && !localName.equals("back");
        }
      }
    }
  }
}
