/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.dom.ranges;

import java.io.IOException;
import java.io.StringReader;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.XPointerAPI;
import it.unibo.cs.xpointer.datatype.LocationList;

import junit.framework.TestCase;

/**
 * Test for Bug#298.
 *
 * @author Pradeep Krishnan
 */
public class Bug298Test extends TestCase {
  private Document document;
  private Regions  regions;
  private String   expression =
    "xpointer(string-range(/article[1]/body[1]/sec[3]/p[4], '')[511]/range-to(string-range(/article[1]/body[1]/sec[4]/sec[1]/sec[1]/p[1], '')[38]))";
  private String   article    = "/pone.15.xml";

/**
   * Creates a new Bug298Test object.
   *
   * @param testName name of this test
   */
  public Bug298Test(String testName) {
    super(testName);
  }

  /**
   * Sets up the test.
   *
   * @throws SAXException on parse failure
   * @throws ParserConfigurationException on parse failure
   * @throws IOException on parse failure
   */
  protected void setUp() throws SAXException, ParserConfigurationException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder        builder = factory.newDocumentBuilder();
    document                       = builder.parse(getClass().getResourceAsStream(article));
    regions                        = new Regions(document);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void test() throws Exception {
    LocationList list = XPointerAPI.evalFullptr(document, expression);
    regions.addRegion(list, "test");
    regions.surroundContents("http://topazproject.org/aml", "aml:annotated", "aml:id", "aml:first");
  }

  private static class Regions extends SelectionRangeList {
    private Document document;

    public Regions(Document document) {
      this.document = document;
    }

    public void addRegion(LocationList list, Object userData) {
      int length = list.getLength();

      for (int i = 0; i < length; i++)
        addRegion(list.item(i), userData);
    }

    public void addRegion(Location location, Object userData) {
      Range range;

      if (location.getType() == Location.RANGE)
        range = (Range) location.getLocation();
      else {
        range = ((DocumentRange) document).createRange();
        range.selectNode((Node) location.getLocation());
      }

      // Ignore it if this range is collapsed (ie. start == end)
      if (!range.getCollapsed())
        insert(new SelectionRange(range, userData));
    }
  }
}
