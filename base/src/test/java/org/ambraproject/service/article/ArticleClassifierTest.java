/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.service.article;

import org.ambraproject.action.BaseTest;
import org.ambraproject.util.DocumentBuilderFactoryCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Alex Kudlick
 *         Date: 7/3/12
 */
public class ArticleClassifierTest extends BaseTest {
  @Autowired
  protected AIArticleClassifier articleClassifier;

  @Test
  public void testAppendElementIfExists() throws Exception {
    Document article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(ClassLoader.getSystemResource("articles/pone.0048915.xml").toURI()));
    StringBuilder sb = new StringBuilder();
    assertFalse(articleClassifier.appendElementIfExists(sb, article, "elementThatShouldntExist"));
    assertTrue(sb.toString().isEmpty());

    assertTrue(articleClassifier.appendElementIfExists(sb, article, "article-title"));
    String s = sb.toString();
    assertTrue(s.startsWith("Maternal Deprivation Exacerbates the Response to a High Fat Diet"));

    sb = new StringBuilder();
    assertTrue(articleClassifier.appendElementIfExists(sb, article, "abstract"));
    s = sb.toString().trim();
    assertTrue(s.startsWith(
        "Maternal deprivation (MD) during neonatal life has diverse long-term effects"));
  }

  @Test
  public void testAppendSectionIfExists() throws Exception {
    Document article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(ClassLoader.getSystemResource("articles/pone.0048915.xml").toURI()));
    StringBuilder sb = new StringBuilder();
    assertFalse(articleClassifier.appendSectionIfExists(sb, article, "sectionThatShouldntExist"));
    assertTrue(sb.toString().isEmpty());

    assertTrue(articleClassifier.appendSectionIfExists(sb, article, "Materials and Methods"));
    String s = sb.toString().trim();
    assertTrue(s.startsWith("Materials and Methods"), s);

    sb = new StringBuilder();
    assertTrue(articleClassifier.appendSectionIfExists(sb, article, "Results"));
    s = sb.toString().trim();
    assertTrue(s.startsWith("Results"), s);
  }

  @Test
  public void testGetCategorizationContent() throws Exception {

    // Arbitrary minimum number of characters that we should be sending for categorization.
    // This should be longer than the article title.
    int threshold = 500;
    Document article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(ClassLoader.getSystemResource("articles/pone.0048915.xml").toURI()));
    String content = articleClassifier.getCategorizationContent(article);
    assertTrue(content.length() > threshold);

    // Editorial without an abstract, materials/methods, or results section.
    article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(ClassLoader.getSystemResource("articles/pntd.0001008.xml").toURI()));
    content = articleClassifier.getCategorizationContent(article);
    assertTrue(content.length() > threshold);

    // Research article with non-standard section titles.
    article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(ClassLoader.getSystemResource("articles/pone.0040598.xml").toURI()));
    content = articleClassifier.getCategorizationContent(article);

    // Call it good if we have material that's at least twice as long as the abstract.
    assertTrue(content.length()
        > article.getElementsByTagName("abstract").item(0).getTextContent().length() * 2);

    // Article with a very short, one-sentence "TOC" abstract that we don't even
    // display in ambra.
    article = DocumentBuilderFactoryCreator.createFactory()
        .newDocumentBuilder().parse(new File(DATA_DIR + "pbio.0020302.xml"));
    content = articleClassifier.getCategorizationContent(article);
    assertTrue(content.length() > threshold);
  }

  @Test
  public void testParseVectorElement() throws Exception {
    assertEquals(AIArticleClassifier.parseVectorElement(
        "<TERM>/Biology and life sciences/Computational biology/Computational neuroscience/Single neuron function|(1) neuron*(1)</TERM>"),
        "/Biology and life sciences/Computational biology/Computational neuroscience/Single neuron function");

    // This appears to be a bug in the AI server--it sometimes does not return an
    // absolute path to a top-level category.  In these cases, the returned value
    // should be discarded.
    assertNull(AIArticleClassifier.parseVectorElement(
        "<TERM>Background noise (acoustics)|(1) background noise(1)</TERM>"));
  }
}
