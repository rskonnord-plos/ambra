package it.unibo.cs.xpointer;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.Range;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

import it.unibo.cs.xpointer.datatype.LocationList;

/**
 * Tests for string-range function with empty string.
 *
 * @author Pradeep Krishnan
 */
public class EmptyStringRangeTest extends TestCase {
  private static final String testXml =
    "<!DOCTYPE doc [<!ELEMENT testid (testid)*>"
    + " <!ATTLIST testid id ID #REQUIRED > ] > <doc> <chapter> <title>Chapter I</title> "
    + " <para>Hello world, indeed, <em>wonderful</em> world</para></chapter> "
    + " <x:a xmlns:x=\"foo\"> <x:a xmlns:x=\"bar\"/> </x:a> "
    + " <testid id=\"id1\"> <testid id=\"id2\"/> </testid> </doc>";

  //
  private Node document;

  /**
   * Creates a new UniboTest object.
   *
   * @param testName name of the test
   */
  public EmptyStringRangeTest(String testName) {
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
    document = builder.parse(new InputSource(new StringReader(testXml)));
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test01() throws TransformerException {
    String expression = "xpointer(string-range(//para,''))";

    /*
     * An empty string is defined to match before each character of the string-value and after the
     * final character.
     *
     * H e l l o   w o r l d ,   i n d e e d ,  <em> w o n d e r f u l</em>   w o r l d
     *1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1     1 2 3 4 5 6 7 8 9      1 2 3 4 5 6 7
     * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0     0 1 2 3 4 5 6 7 8      0 1 2 3 4 5 + 1 (outside)_
     *
     * Total = 21 + 9 + 7 = 37
     */
    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);

    //printList(list);
    assertEquals(37, list.getLength());

    for (int i = 0; i < 21; i++) {
      Location location = list.item(i);

      assertEquals(Location.RANGE, location.getType());

      Range range = (Range) location.getLocation();

      assertEquals("para", range.getStartContainer().getParentNode().getNodeName());
      assertEquals(i, range.getStartOffset());

      assertEquals("para", range.getEndContainer().getParentNode().getNodeName());
      assertEquals(i, range.getEndOffset());
    }

    for (int i = 0; i < 9; i++) {
      Location location = list.item(i + 21);

      assertEquals(Location.RANGE, location.getType());

      Range range = (Range) location.getLocation();

      assertEquals("em", range.getStartContainer().getParentNode().getNodeName());
      assertEquals(i, range.getStartOffset());

      assertEquals("em", range.getEndContainer().getParentNode().getNodeName());
      assertEquals(i, range.getEndOffset());
    }

    for (int i = 0; i < 6; i++) {
      Location location = list.item(i + 30);

      assertEquals(Location.RANGE, location.getType());

      Range range = (Range) location.getLocation();

      assertEquals("para", range.getStartContainer().getParentNode().getNodeName());
      assertEquals(i, range.getStartOffset());

      assertEquals("para", range.getEndContainer().getParentNode().getNodeName());
      assertEquals(i, range.getEndOffset());
    }

    Location location = list.item(36);

    assertEquals(Location.RANGE, location.getType());

    Range range = (Range) location.getLocation();

    assertEquals("doc", range.getStartContainer().getParentNode().getNodeName());
    assertEquals(0, range.getStartOffset());

    assertEquals("doc", range.getEndContainer().getParentNode().getNodeName());
    assertEquals(0, range.getEndOffset());
  }

  /**
   * Does 2 argument function tests.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test02() throws TransformerException {
    Tester[] testers =
      { new Tester(1, "xpointer(string-range(//para,''))", "para", 0, "para", 0, 37), };
    doTests(testers);
  }

  /**
   * Does 3 argument function tests.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test03() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',1))", "para", 0, "para", 0, 37),
        new Tester(2, "xpointer(string-range(//para,'',0)[2])", "para", 0, "para", 1),
        new Tester(3, "xpointer(string-range(//para,'',-1)[3])", "para", 0, "para", 2),
        new Tester(4, "xpointer(string-range(//para,'',2)[1])", "para", 1, "para", 1),
      };
    doTests(testers);
  }

  /**
   * Does 4 argument function tests.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test04() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',1, 0)[1])", "para", 0, "para", 0),
        new Tester(2, "xpointer(string-range(//para,'',0, 1)[2])", "para", 0, "para", 1),
        new Tester(3, "xpointer(string-range(//para,'',-1, 2)[3])", "para", 0, "para", 2),
        new Tester(4, "xpointer(string-range(//para,'',2, 2)[1])", "para", 1, "para", 3),
      };
    doTests(testers);
  }

  /**
   * Does node boundary tests (forward looking).
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test05() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',1, 36)[1])", "para", 0, "para", 6),
        new Tester(2, "xpointer(string-range(//para,'',1, 37)[1])", "para", 0, "doc", 1),
        new Tester(3, "xpointer(string-range(//para,'',1, 38)[1])", "para", 0, "doc", 2),
        new Tester(4, "xpointer(string-range(//para,'',1, 39)[1])", "para", 0, "x:a", 1),
        new Tester(5, "xpointer(string-range(//para,'',1, 40)[1])", "para", 0, "x:a", 1),
        new Tester(6, "xpointer(string-range(//para,'',1, 41)[1])", "para", 0, "doc", 1),
        new Tester(7, "xpointer(string-range(//para,'',1, 42)[1])", "para", 0, "doc", 2),
        new Tester(8, "xpointer(string-range(//para,'',1, 43)[1])", "para", 0, "testid", 1),
        new Tester(9, "xpointer(string-range(//para,'',1, 44)[1])", "para", 0, "testid", 1),
        new Tester(10, "xpointer(string-range(//para,'',1, 45)[1])", "para", 0, "doc", 1),
        new Tester(11, "xpointer(string-range(//para,'',36, 0)[1])", "para", 5, "para", 5),
      };
    doTests(testers);
  }

  /**
   * Does extending outside the doc test (going beyond end)
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test06() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',1, 46)[1])", "para", 0, "doc", 1),
        new Tester(2, "xpointer(string-range(//para,'',1, 1000)[1])", "para", 0, "doc", 1),
      };
    doTests(testers);
  }

  /**
   * Does node boundary test (backward looking)
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test07() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',0, 2)[1])", "chapter", 1, "para", 1),
        new Tester(2, "xpointer(string-range(//para,'',0, 1)[1])", "chapter", 1, "chapter", 2),
        new Tester(3, "xpointer(string-range(//para,'',0, 0)[1])", "chapter", 1, "chapter", 1),
        new Tester(4, "xpointer(string-range(//para,'',-1, 0)[1])", "chapter", 0, "chapter", 0),
        new Tester(5, "xpointer(string-range(//para,'',-2, 0)[1])", "title", 8, "title", 8),
        new Tester(6, "xpointer(string-range(//para,'',-10, 0)[1])", "title", 0, "title", 0),
        new Tester(7, "xpointer(string-range(//para,'',-10, 8)[1])", "title", 0, "title", 8),
        new Tester(8, "xpointer(string-range(//para,'',-11, 0)[1])", "chapter", 0, "chapter", 0),
        new Tester(9, "xpointer(string-range(//para,'',-11, 1)[1])", "chapter", 0, "chapter", 1),
        new Tester(10, "xpointer(string-range(//para,'',-11, 2)[1])", "chapter", 0, "title", 1),
        new Tester(11, "xpointer(string-range(//para,'',-12, 0)[1])", "doc", 0, "doc", 0),
        new Tester(12, "xpointer(string-range(//para,'',-12, 1)[1])", "doc", 0, "doc", 1),
        new Tester(13, "xpointer(string-range(//para,'',-12, 2)[1])", "doc", 0, "chapter", 1),
        new Tester(14, "xpointer(string-range(//para,'',-12, 3)[1])", "doc", 0, "title", 1),
      };
    doTests(testers);
  }

  /**
   * Does extending outside the doc test (going before start). Note: watch the location list length
   * decrement to 0 as all matches go outside the doc.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test08() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(//para,'',-12, 0))", "doc", 0, "doc", 0, 37),
        new Tester(2, "xpointer(string-range(//para,'',-13, 0))", "doc", 0, "doc", 0, 36),
        new Tester(3, "xpointer(string-range(//para,'',-47, 0))", "doc", 0, "doc", 0, 2),
        new Tester(4, "xpointer(string-range(//para,'',-48, 0))", "doc", 0, "doc", 0, 1),
        new Tester(5, "xpointer(string-range(//para,'',-49, 0))", "doc", 0, "doc", 0, 0),
        new Tester(6, "xpointer(string-range(//para,'',-99, 0))", "doc", 0, "doc", 0, 0),
      };
    doTests(testers);
  }

  /**
   * Does tests within a node fragment.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test09() throws TransformerException {
    Tester[] testers =
      {
        new Tester(1, "xpointer(string-range(string-range(//para,'Hello'),''))", "para", 0, "para",
                   0, 6),
        new Tester(2, "xpointer(string-range(string-range(//para,'ello '),'',1))", "para", 1,
                   "para", 1, 6),
        new Tester(3, "xpointer(string-range(string-range(//para,'ello '),'',2))", "para", 2,
                   "para", 2, 6),
        new Tester(4, "xpointer(string-range(string-range(//para,'ello '),'',1, 0))", "para", 1,
                   "para", 1, 6),
        new Tester(5, "xpointer(string-range(string-range(//para,'ello '),'',2, 0))", "para", 2,
                   "para", 2, 6),
        new Tester(6, "xpointer(string-range(string-range(//para,'ello '),'',1, 1))", "para", 1,
                   "para", 2, 6),
        new Tester(7, "xpointer(string-range(string-range(//para,'ello '),'',2, 1))", "para", 2,
                   "para", 3, 6),
        new Tester(8, "xpointer(string-range(//para,'world')[1])", "para", 6, "para", 11, 1),
        new Tester(9, "xpointer(string-range(//para,'world')[2])", "para", 1, "para", 6, 1),
        new Tester(10, "xpointer(string-range(//para,'world'))", "para", 6, "para", 11, 2),
        new Tester(11, "xpointer(string-range(string-range(//para,'world')[1],'',2, 1))", "para",
                   7, "para", 8, 6),
        new Tester(12, "xpointer(string-range(string-range(//para,'world'),'',2, 1))", "para", 7,
                   "para", 8, 12),
      };
    doTests(testers);
  }

  private void doTests(Tester[] testers) {
    for (int i = 0; i < testers.length; i++) {
      try {
        testers[i].test();
      } catch (Exception e) {
        e.printStackTrace();
        fail(testers[i].tester + ": failed with exception " + e);
      }
    }
  }

  private class Tester {
    private String tester;
    private String expression;
    private String startElem;
    private String endElem;
    private int    oStart;
    private int    oEnd;
    private int    length;

    public Tester(int testNumber, String expression, String startElem, int oStart, String endElem,
                  int oEnd) {
      this(testNumber, expression, startElem, oStart, endElem, oEnd, 1);
    }

    public Tester(int testNumber, String expression, String startElem, int oStart, String endElem,
                  int oEnd, int length) {
      tester            = "tester[" + testNumber + "]";
      this.expression   = expression;
      this.startElem    = startElem;
      this.endElem      = endElem;
      this.oStart       = oStart;
      this.oEnd         = oEnd;
      this.oEnd         = oEnd;
      this.length       = length;
    }

    public void test() throws TransformerException {
      LocationList list = XPointerAPI.evalFullptr(document, expression);

      assertNotNull(tester + ": null location list", list);

      //printList(list);
      assertEquals(tester + ": incorrect location list length", length, list.getLength());

      if (length < 1)
        return;

      Location location = list.item(0);

      assertEquals(tester + ": incorrect location type", Location.RANGE, location.getType());

      Range range = (Range) location.getLocation();

      assertEquals(tester + ": incorrect start element", startElem,
                   range.getStartContainer().getParentNode().getNodeName());
      assertEquals(tester + ": incorrect start offset", oStart, range.getStartOffset());

      assertEquals(tester + ": incorrect end element", endElem,
                   range.getEndContainer().getParentNode().getNodeName());
      assertEquals(tester + ": incorect end offset", oEnd, range.getEndOffset());
    }
  }

  private void printList(LocationList list) {
    for (int i = 0; i < list.getLength(); i++) {
      Location location = list.item(i);

      if (location.getType() == Location.RANGE) {
        Range range = (Range) location.getLocation();
        Node  start = range.getStartContainer();
        Node  end   = range.getEndContainer();
        System.out.println(start.getParentNode().getNodeName() + "/" + start.getNodeName() + ":"
                           + range.getStartOffset() + " --- " + end.getParentNode().getNodeName()
                           + "/" + end.getNodeName() + ":" + range.getEndOffset());
      }

      if (location.getType() == Location.NODE) {
        Node node = (Node) location.getLocation();
        System.out.println(node.getNodeName());
      }
    }
  }
}
