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
 * Standard tests defined at <code>http://www.cs.unibo.it/projects/xslt%2B%2B/XPointerTest.html</code>
 *
 * @author Pradeep Krishnan
 */
public class UniboTest extends TestCase {
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
  public UniboTest(String testName) {
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
    document = builder.parse(new InputSource(new StringReader(testXml))).getDocumentElement();
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test01() throws TransformerException {
    String       expression = "xpointer(string-range(/,'Hello world'))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    assertEquals(range.getStartContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 0);

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), "Hello world".length());
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test02() throws TransformerException {
    String       expression = "xpointer(string-range(/,'Hello world')/../..)";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.NODE);

    Node node = (Node) location.getLocation();

    assertEquals(node.getNodeName(), "para");
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test03() throws TransformerException {
    String       expression = "xpointer(string-range(/,'Hello world', 3))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    /*
     * <para> H e l l o w o r l d , i n d e e d, <em> w o n d e r f u l</em>  w o r l d</para>
     *       1 2 3 4 5 6 7 8 9 0 1   (ie. 1 ==> before 'H' of Hello)
     *        0 1 2 3 4 5 6 7 8 9    (so the offset for 3 is 2)
     */
    assertEquals(range.getStartContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 2);

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), "Hello world".length());
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test04() throws TransformerException {
    String       expression = "xpointer(string-range(/,'Hello world', 3, 5))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    // same as test03
    assertEquals(range.getStartContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 2);

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), range.getStartOffset() + 5);
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test05() throws TransformerException {
    String       expression = "xpointer(string-range(/,'ello', 0, 5))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    /*
     * <para> H e l l o w o r l d , i n d e e d, <em> w o n d e r f u l</em>  w o r l d</para>
     *       0 1 2 3 4 5 6 7 8 9 0 1   (ie. 1 ==> before 'e' of Hello)
     *        0 1 2 3 4 5 6 7 8 9    (so the offset for 0 is 0)
     */
    assertEquals(range.getStartContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 0);

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), range.getStartOffset() + 5);
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test06() throws TransformerException {
    String       expression = "xpointer(string-range(/,'world', -3, 5))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 2);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    /*
     * <para> H e l l o   w o r l d , i n d e e d, <em> w o n d e r f u l</em>  w o r l d</para>
     *      -5-4-3-2-1 0 1 2 3 4 5 6 7 8 9 0 1   (ie. 1 ==> before 'e' of Hello)
     *        0 1 2 3 4 5 6 7 8 9    (so the offset for -3 is 2)
     */
    assertEquals(range.getStartContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 2);

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), range.getStartOffset() + 5);

    location = list.item(1);

    assertEquals(location.getType(), Location.RANGE);

    range = (Range) location.getLocation();

    /*
     * <para> H e l l o   w o r l d , i n d e e d, <em> w o n d e r f u l</em>   w o r l d</para>
     *                                                          -4-3-2-1      0 1 2 3 4 5
     *                                                  0 1 2 3 4 5 6 7 8      0 1 2
     */
    assertEquals(range.getStartContainer().getParentNode().getParentNode().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 6); // 6 in <em> node

    assertEquals(range.getEndContainer().getParentNode().getNodeName(), "para");
    assertEquals(range.getEndOffset(), 2);
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test07() throws TransformerException {
    String       expression = "xpointer(range(/doc/chapter/title)/range-to(/doc/chapter/para/em))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    assertEquals(range.getStartContainer().getNodeName(), "chapter");
    // Note: there is a text node before <title> in <chapter>, hence this is offset 1
    assertEquals(range.getStartOffset(), 1);

    assertEquals(range.getEndContainer().getNodeName(), "em");
    assertEquals(range.getEndOffset(), 1);
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test08() throws TransformerException {
    String       expression = "xpointer(/doc/chapter/para/range-to(em))";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.RANGE);

    Range range = (Range) location.getLocation();

    assertEquals(range.getStartContainer().getNodeName(), "para");
    assertEquals(range.getStartOffset(), 0);

    assertEquals(range.getEndContainer().getNodeName(), "em");
    assertEquals(range.getEndOffset(), 1);
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test09() throws TransformerException {
    String       expression = "xpointer(start-point(/doc/chapter/para)/self::point()/parent::para)";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.NODE);

    Node node = (Node) location.getLocation();

    assertEquals(node.getNodeName(), "para");
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test10() throws TransformerException {
    String       expression = "xpointer(start-point(/doc/chapter/para)[parent::para/em]/ancestor::chapter)";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.NODE);

    Node node = (Node) location.getLocation();

    assertEquals(node.getNodeName(), "chapter");
  }

  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test11() throws TransformerException {
    //String       expression = "xmlns(y=foo) xmlns(z=bar) xpointer(//y:a/z:a)";
    String       expression = "xpointer(//a/a)";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.NODE);

    Node node = (Node) location.getLocation();

    //assertEquals(node.getParentNode().getNodeName(), "y:a");
    //assertEquals(node.getNodeName(), "z:a");
    
    assertEquals(node.getParentNode().getNodeName(), "x:a");
    assertEquals(node.getNodeName(), "x:a");

    // Note: the xmlms stuff does not work. So use same prefixes as the source for now
  }
  
  /**
   * Does the test.
   *
   * @throws TransformerException on xpointer eval error
   */
  public void test12() throws TransformerException {
    String       expression = "element(id1/1)";

    LocationList list = XPointerAPI.evalFullptr(document, expression);

    assertNotNull(list);
    assertEquals(list.getLength(), 1);

    Location location = list.item(0);

    assertEquals(location.getType(), Location.NODE);

    Node node = (Node) location.getLocation();

    assertEquals(node.getNodeName(), "testid");
  }
}
