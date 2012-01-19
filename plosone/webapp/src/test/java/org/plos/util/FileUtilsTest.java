/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.util;

import junit.framework.TestCase;

import java.net.URISyntaxException;

public class FileUtilsTest extends TestCase {
  public void testFileNameExtraction() throws URISyntaxException {
    assertEquals("TransformerFactory.html", FileUtils.getFileName("http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("/1.4.2/docs/api/javax/xml/transform/TransformerFactory.txt"));
    assertEquals("TransformerFactory.txt", FileUtils.getFileName("C:\\1.4.2\\docs\\api\\javax\\xml\\transform\\TransformerFactory.txt"));
  }

  public void testValidateUrl() {
    assertTrue(FileUtils.isHttpURL("http://java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertFalse(FileUtils.isHttpURL("/java.sun.com/j2se/1.4.2/docs/api/javax/xml/transform/TransformerFactory.html"));
    assertFalse(FileUtils.isHttpURL("C:\\1.4.2\\docs\\api\\javax\\xml\\transform\\TransformerFactory.txt"));
  }

  public void testMimeType() {
    assertEquals("text/plain", FileUtils.getContentType("txt"));
    assertEquals("text/xml", FileUtils.getContentType("xml"));
    assertEquals("image/tiff", FileUtils.getContentType("tif"));
    assertEquals("application/msword", FileUtils.getContentType("doc"));
    assertEquals("application/pdf", FileUtils.getContentType("pdf"));
  }

  public void testFileExtForMimeType() throws Exception {
    assertEquals("tiff", FileUtils.getDefaultFileExtByMimeType("image/tiff"));
    assertEquals("html", FileUtils.getDefaultFileExtByMimeType("text/html"));
    assertEquals("xml", FileUtils.getDefaultFileExtByMimeType("text/xml"));
  }
}
