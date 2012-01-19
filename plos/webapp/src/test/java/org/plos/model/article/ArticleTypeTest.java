package org.plos.model.article;

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
}
