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

import java.io.ByteArrayOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.topazproject.xml.transform.cache.CachedSource;

/**
 * @author Ronald Tschalär
 * @version $Id$
 */
public class EntityResolverTest extends TestCase {
  /**
   * Test that the resource cache is correct and complete (nothing needed from the network).
   */
  public void testResourceCache() throws Exception {
    // make sure network access will break
    System.setProperty("http.proxyHost", "-dummy-");
    System.setProperty("http.proxyPort", "-1");

    // run tests
    doTestCachedSource("article_v11.xml");
    doTestCachedSource("article_v20.xml");
    doTestCachedSource("article_v21.xml");
    doTestCachedSource("article_v22.xml");
  }

  private void doTestCachedSource(String input) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    InputSource myInputSource = new InputSource(getClass().getResourceAsStream(input));
    ByteArrayOutputStream res = new ByteArrayOutputStream(500);
    transformer.transform(new CachedSource(myInputSource), new StreamResult(res));
  }
}
