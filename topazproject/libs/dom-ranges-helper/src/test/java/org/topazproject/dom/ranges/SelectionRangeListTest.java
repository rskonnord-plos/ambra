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

import junit.framework.TestCase;

import it.unibo.cs.xpointer.Location;
import it.unibo.cs.xpointer.XPointerAPI;
import it.unibo.cs.xpointer.datatype.LocationList;

/**
 * Tests the SelectionRangeList algorithm.
 *
 * @author Pradeep Krishnan
 */
public class SelectionRangeListTest extends TestCase {
  private static final String testXml =
    "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
    + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
    + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter> "
    + " <para>This is a test <span>for <em>skipped</em><!-- comment --> child</span> nodes"
    + " in sub-range</para>"
    + " <x:a xmlns:x=\"foo\"> <x:a xmlns:x=\"bar\"/> </x:a> "
    + " <testid id=\"id1\"> <testid id=\"id2\"/> </testid> </doc>";

  //
  private Document document;
  private Regions  regions;

  /**
   * Creates a new SelectionRangeListTest object.
   *
   * @param testName name of this test
   */
  public SelectionRangeListTest(String testName) {
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
    document   = builder.parse(new InputSource(new StringReader(testXml)));
    regions    = new Regions(document);
  }

  /**
   * Tests non-overlapping regions added in order.
   */
  public void test01() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello world'))", "xpointer(string-range(/,'wonderful'))" };
    String[] userData = { "test01/0", "test01/1" };
    int[][]  regions  = {
                          { 0 },
                          { 1 }
                        };
    String[][] surrounds = {
                             { "Hello world" },
                             { "wonderful" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests non-overlapping regions added out of order.
   */
  public void test02() {
    String[] expressions =
      { "xpointer(string-range(/,'wonderful'))", "xpointer(string-range(/,'Hello world'))" };
    String[] userData = { "test02/0", "test02/1" };
    int[][]  regions  = {
                          { 1 },
                          { 0 }
                        };
    String[][] surrounds = {
                             { "Hello world" },
                             { "wonderful" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests overlapping regions added in order.
   */
  public void test03() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello'))", "xpointer(string-range(/,'llo world'))" };
    String[] userData = { "test03/0", "test03/1" };
    int[][]  regions  = {
                          { 0 },
                          { 0, 1 },
                          { 1 }
                        };
    String[][] surrounds = {
                             { "He" },
                             { "llo" },
                             { " world" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests overlapping regions added in order.
   */
  public void test04() {
    String[] expressions =
      { "xpointer(string-range(/,'llo world'))", "xpointer(string-range(/,'Hello'))" };
    String[] userData = { "test04/0", "test04/1" };
    int[][]  regions  = {
                          { 1 },
                          { 0, 1 },
                          { 0 }
                        };
    String[][] surrounds = {
                             { "He" },
                             { "llo" },
                             { " world" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests containing region with same start.
   */
  public void test05() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello world'))", "xpointer(string-range(/,'Hello'))" };
    String[] userData = { "test05/0", "test05/1" };
    int[][]  regions  = {
                          { 0, 1 },
                          { 0 },
                        };
    String[][] surrounds = {
                             { "Hello" },
                             { " world" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests containing region with same end.
   */
  public void test06() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello world,'))", "xpointer(string-range(/,'world,'))" };
    String[] userData = { "test06/0", "test06/1" };
    int[][]  regions  = {
                          { 0 },
                          { 0, 1 },
                        };
    String[][] surrounds = {
                             { "Hello " },
                             { "world," }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests containing region with no shared start and end.
   */
  public void test07() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello world,'))", "xpointer(string-range(/,'ello world'))" };
    String[] userData = { "test07/0", "test07/1" };
    int[][]  regions  = {
                          { 0 },
                          { 0, 1 },
                          { 0 }
                        };
    String[][] surrounds = {
                             { "H" },
                             { "ello world" },
                             { "," }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests containing region with shared start and end.
   */
  public void test08() {
    String[] expressions =
      { "xpointer(string-range(/,'Hello world,'))", "xpointer(string-range(/,'Hello world,'))" };
    String[] userData = { "test08/0", "test08/1" };
    int[][]  regions  = {
                          { 0, 1 },
                        };
    String[][] surrounds = {
                             { "Hello world," }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests patially selected regions.
   */
  public void test09() {
    String[] expressions =
      { "xpointer(string-range(/,'indeed, wonder'))", "xpointer(string-range(/,'ful world'))" };
    String[] userData = { "test09/0", "test09/1" };
    int[][]  regions  = {
                          { 0 },
                          { 1 }
                        };
    String[][] surrounds = {
                             { "indeed, ", "wonder" },
                             { "ful", " world" }
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests skipping of child nodes of a fully selected range.
   */
  public void test10() {
    String[] expressions = { "xpointer(string-range(/,'indeed, wonderful world'))" };
    String[] userData = { "test10/0" };
    int[][]  regions  = {
                          { 0 },
                        };
    String[][] surrounds = {
                             { "indeed, wonderful world" },
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests skipping of comment nodes of a fully selected range.
   */
  public void test11() {
    String[] expressions = { "xpointer(string-range(/,'test for skipped child nodes'))" };
    String[] userData = { "test11/0" };
    int[][]  regions  = {
                          { 0 },
                        };
    String[][] surrounds = {
                             { "test for skipped child nodes" },
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Tests skipping of comment nodes of a fully selected range.
   */
  public void test12() {
    String[] expressions = { "xpointer(string-range(/,'for skipped child'))" };
    String[] userData = { "test12/0" };
    int[][]  regions  = {
                          { 0 },
                        };
    String[][] surrounds = {
                             { "for skipped child" },
                           };

    doit(expressions, userData, regions, surrounds);
  }

  /**
   * Executes the test.
   *
   * @param expressions xpointer expressions to select a region
   * @param userData user data associated with each expression
   * @param expectedRegions expected regions (identified by userData)
   * @param expectedSurrounds ranges that can be surrounded by a parent element
   *
   * @throws RuntimeException if an error in evaluating xpointer expression
   */
  public void doit(String[] expressions, String[] userData, int[][] expectedRegions,
                   String[][] expectedSurrounds) {
    int i;
    int j;

    assertEquals(expressions.length, userData.length);
    assertEquals(expectedRegions.length, expectedSurrounds.length);

    try {
      for (i = 0; i < expressions.length; i++) {
        LocationList list = XPointerAPI.evalFullptr(document, expressions[i]);
        regions.addRegion(list, userData[i]);
      }
    } catch (TransformerException e) {
      throw new RuntimeException("", e);
    }

    assertEquals("size of regions don't match", expectedRegions.length, regions.size());

    for (i = 0; i < expectedRegions.length; i++) {
      SelectionRange r = regions.get(i);
      List           u = r.getUserDataList();

      assertEquals(u.size(), expectedRegions[i].length);

      for (j = 0; j < expectedRegions[i].length; j++) {
        String data = userData[expectedRegions[i][j]];
        assertTrue("regions[" + i + "] must contain userData " + data, u.contains(data));
      }
      //System.out.println("regions[" + i + "]=" + r);
      Range[] sub = r.getSurroundableRanges();

      assertEquals("count of elements required to surround region[" + i + "]",
                   expectedSurrounds[i].length, sub.length);

      for (j = 0; j < expectedSurrounds[i].length; j++)
        assertEquals("surroundable[" + i + "][" + j + "]", expectedSurrounds[i][j],
                     sub[j].toString());
    }
    //System.out.println("");
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
