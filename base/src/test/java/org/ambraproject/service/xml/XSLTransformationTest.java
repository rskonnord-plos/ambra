/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.service.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;
import org.ambraproject.ApplicationException;
import org.ambraproject.action.BaseTest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.io.BufferedInputStream;

/**
 * Test that the XSL actually works and log the time it takes
 */
public class XSLTransformationTest extends BaseTest {
  public static final Logger log = LoggerFactory.getLogger(XSLTransformationTest.class);

  private final String XML_SOURCE = "pbio.0000001-embedded-math-dtd.xml";

  @Autowired
  @Qualifier("xmlService")
  protected XMLService xmlService;

  @Test
  public void testXSLTransformation() throws TransformerException, IOException, ApplicationException {

    URL fileRef = getClass().getClassLoader().getResource(XML_SOURCE);
    InputStream in = fileRef.openStream();

    InputStream transformedInput = xmlService.getTransformedInputStream(in);

    BufferedInputStream buff = new BufferedInputStream(xmlService.getTransformedInputStream(in));
    String stop;

  }
}
