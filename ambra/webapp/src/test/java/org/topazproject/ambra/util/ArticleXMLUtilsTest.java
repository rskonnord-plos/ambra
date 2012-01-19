/* $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.util;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.BaseConfiguration;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchObjectIdException;
import org.xml.sax.SAXException;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.reset;
import static org.easymock.classextension.EasyMock.expect;

import javax.xml.parsers.ParserConfigurationException;
import javax.activation.URLDataSource;
import java.util.Map;
import java.util.HashMap;
import java.net.URISyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * @author Dragisa Krsmanovic
 *
 */
public class ArticleXMLUtilsTest {
  private static final String OBJINFO_NAMESPACES = "xmlns:mml=\"http://www.w3.org/1998/Math/MathML\" " +
      "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
      "xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" " +
      "xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" " +
      "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
      "xmlns:util=\"http://dtd.nlm.nih.gov/xsl/util\"";

  private ArticleXMLUtils secondaryObjectService;
  private ArticleXMLUtils viewNLMService;
  private ArticleOtmService articleServiceMock;


  @BeforeClass
  protected void setUp() throws Exception {
    Map<String, String> xmlFactoryProperties = new HashMap<String, String>();
    xmlFactoryProperties.put("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
    xmlFactoryProperties.put("javax.xml.transform.Transformer",
        "net.sf.saxon.Controller");
    Configuration configiration = new BaseConfiguration();
    configiration.setProperty("ambra.platform.appContext", "test-context");
    secondaryObjectService = new ArticleXMLUtils();
    secondaryObjectService.setArticleRep("XML");
    secondaryObjectService.setXmlFactoryProperty(xmlFactoryProperties);
    secondaryObjectService.setAmbraConfiguration(configiration);
    secondaryObjectService.setXslTemplate("/objInfo.xsl");
    secondaryObjectService.init();

    articleServiceMock = createMock(ArticleOtmService.class);
    viewNLMService = new ArticleXMLUtils();
    viewNLMService.setArticleService(articleServiceMock);
    viewNLMService.setArticleRep("XML");
    viewNLMService.setXmlFactoryProperty(xmlFactoryProperties);
    viewNLMService.setAmbraConfiguration(configiration);
    viewNLMService.setXslTemplate("/viewnlm-v2.xsl");
    viewNLMService.init();
  }

  @DataProvider(name = "objInfoSamples")
  public String[][] createObjInfoSamples() {
    return new String[][]{
        {"Hello World", "Hello World"},
        {"<sc>hello world</sc>", "<small " + OBJINFO_NAMESPACES + ">HELLO WORLD</small>"},
        {"<bold>Hello World</bold>", "<b " + OBJINFO_NAMESPACES + ">Hello World</b>"},
        {"<abbrev xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
            "xlink:href=\"test\" id=\"123\">Hello World</abbrev>",
            "<a " + OBJINFO_NAMESPACES + " href=\"test\" id=\"123\">Hello World</a>"},
        {"<abbrev id=\"123\">Hello World</abbrev>",
            "<span " + OBJINFO_NAMESPACES + " class=\"capture-id\" id=\"123\">Hello World</span>"}
    };
  }


  @Test(dataProvider = "objInfoSamples")
  public void testObjInfoTransformation(String source, String expected)
      throws URISyntaxException, ApplicationException {
    String result = secondaryObjectService.getTranformedDocument(source);
    assertEquals(result, expected);
  }


  @DataProvider(name = "viewNLMFiles")
  public String[][] createViewNLMSamples() {
    return new String[][]{
        {"article1.xml","result1.txt"}
    };
  }

  @Test(dataProvider = "viewNLMFiles")
  public void testViewNLMTransformation(String articleFilename, String resultFilename)
      throws IOException, SAXException, NoSuchArticleIdException, NoSuchObjectIdException,
      URISyntaxException, ApplicationException, ParserConfigurationException {

    expect(articleServiceMock.getContent("1234", "XML"))
        .andReturn(new URLDataSource(getClass().getResource("/article/" + articleFilename)));

    replay(articleServiceMock);
    String result = viewNLMService.getTransformedArticle("1234");
    verify();

    assertNotSame(result, getFileAsString("/article/"+resultFilename));
    reset(articleServiceMock);
  }

  private String getFileAsString(String resultFilename) throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream(resultFilename)));
    StringBuffer expected = new StringBuffer();

    String line = reader.readLine();
    while (line != null) {
      expected.append(line);
      expected.append('\n');
      line = reader.readLine();
    }

    return expected.toString();
  }
}