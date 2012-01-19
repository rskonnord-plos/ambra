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

package org.topazproject.ambra.model.article;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.model.article.ArticleType;

import junit.framework.TestCase;

public class ArticleTypeTest extends TestCase {
  private ConfigurationStore store;

  protected void setUp() {
    store = ConfigurationStore.getInstance();
    CombinedConfiguration config = new CombinedConfiguration();
    try {
      config.addConfiguration(new XMLConfiguration(getClass().getResource(
          "/articleTypeTestConfig.xml")));
    } catch (ConfigurationException e) {
      AssertionError ae = new AssertionError(
          "Got Confgiruation Exception loading articlerTypeTestConfig.xml");
      ae.setStackTrace(e.getStackTrace());
      throw ae;
    }
    store.setConfiguration(config);

  }

  public void testDefaultArticleType() {
    ArticleType dat = ArticleType.getDefaultArticleType();
    assertNotNull("Default Article Type was Null", dat);

    String defaultHeading = "DefaultHeading";
    assertEquals("Default ArticleType heading not as expected.", 
        defaultHeading, dat.getHeading());
    String defaultArticleTypeUri = "http://rdf.plos.org/RDF/articleType/Research%20Article";
    assertEquals("Default ArticleType URI not as expected.", 
        defaultArticleTypeUri, dat.getUri().toString());
  }
  
  public void testArticleTypeEquality() throws Exception {
    ArticleType art1 = ArticleType.getArticleTypeForURI(
        new URI("http://rdf.plos.org/RDF/articleType/Interview"), false);
    // Ensure that URI doesn't refer to the same internal String object...
    ArticleType art2 = ArticleType.getArticleTypeForURI(
        new URI("http://rdf.plos.org/RDF/"+"articleType/Interview"), false);
    
    assertTrue("Article 1 == Article 2", art1==art2);
    assertTrue("Article 1 should .equals() Article 2", art1.equals(art2));
  }
  
  public void testDeserializedArticleTypeEquality() throws Exception {
    ArticleType art1 = ArticleType.getArticleTypeForURI(
        new URI("http://rdf.plos.org/RDF/articleType/Interview"), false);
    
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream (bout);
    
    out.writeObject(art1);
    out.flush();
    
    ByteArrayInputStream bin = new ByteArrayInputStream (bout.toByteArray ());
    ObjectInputStream in = new ObjectInputStream (bin);
    
    ArticleType art2 = (ArticleType) in.readObject();
    
    assertTrue("Article 1 == Article 2", art1==art2);
    assertTrue("Article 1 should .equals() Article 2", art1.equals(art2));
  }
}
