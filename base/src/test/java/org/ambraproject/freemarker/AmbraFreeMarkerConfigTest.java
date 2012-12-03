package org.ambraproject.freemarker;

import org.ambraproject.action.BaseTest;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

/**
 * @author Joe Osowski
 */
public class AmbraFreeMarkerConfigTest extends BaseTest {
  @Autowired
  protected Configuration ambraConfiguration;

  @Test
  public void testConfig() throws Exception {
    AmbraFreemarkerConfig config = new AmbraFreemarkerConfig(ambraConfiguration);

    String name = config.getDisplayNameByEISSN("1234");
    String name1 = config.getDisplayNameByEISSN("5678");

    assertEquals(name, "test journal");
    assertEquals(name1, "test journal 1");
  }
}
