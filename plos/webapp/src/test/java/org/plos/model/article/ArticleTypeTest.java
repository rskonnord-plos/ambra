package org.plos.model.article;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.plos.configuration.ConfigurationStore;

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
    String imageSetConfigName = "MyImageSet";
    assertEquals("Default imageSetConfigName not as expected", 
        imageSetConfigName, dat.getImageSetConfigName());
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
