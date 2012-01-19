/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.article;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.BasePlosoneTestCase;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.util.Properties;

public class XSLTransformationTest extends BasePlosoneTestCase {
  public static final Log log = LogFactory.getLog(XSLTransformationTest.class);

  private String BASE_TEST_PATH = "webapp/src/test/resources/";
  private final String XML_SOURCE = BASE_TEST_PATH + "pbio.0000001-embedded-math-dtd.xml";
  private final String XSL_SOURCE = "webapp/src/main/resources/viewnlm-v2.xsl";
  private final String OUTPUT_FILENAME = "foo.html";

  public void testXSLTransformation() throws TransformerException, FileNotFoundException, URISyntaxException {
    final Transformer transformer = getXSLTransformer(XSL_SOURCE);
    
    TimeIt.logTime();
//    final URL resource = getClass().getResource(filename);
//    return new File(resource.toURI());
    final String file = XML_SOURCE;
    transformXML(transformer, new StreamSource(file), OUTPUT_FILENAME);
    TimeIt.logTime();
  }

  public void testXSLTransformationToBeEfficient() throws Exception {
    final String XML_SOURCE = BASE_TEST_PATH + "pone.0000008.xml";
//    final String XML_SOURCE = "pbio.0000001-embedded-math-dtd.xml";
    final Transformer transformer = getXSLTransformer(XSL_SOURCE);

    final Source source = getFetchArticleService().getDOMSource(XML_SOURCE);

    transformXML(transformer, source, OUTPUT_FILENAME);
    TimeIt.logTime();
  }


  private void transformXML(final Transformer transformer, final Source xmlSource, final String outputFileName) throws TransformerException, FileNotFoundException {
    // 3. Use the Transformer to transform an XML_SOURCE Source and send the
    //    output to a Result object.
    final FileOutputStream outputStream = new FileOutputStream(outputFileName);
    transformer.transform(
            xmlSource,
            new StreamResult(outputStream));
  }

  private Transformer getXSLTransformer(final String xslStyleSheet) throws TransformerConfigurationException, URISyntaxException {
    // 1. Instantiate a TransformerFactory.
    final String KEY = "javax.xml.transform.TransformerFactory";
//    final String VALUE = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    final String VALUE = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
    final Properties props = System.getProperties();
    props.put(KEY, VALUE);
    System.setProperties(props);

    final TransformerFactory tFactory = TransformerFactory.newInstance();

// 2. Use the TransformerFactory to process the stylesheet Source and generate a Transformer.
//    final URL resource = getClass().getResource(filename);
//    return new File(resource.toURI());
    return tFactory.newTransformer(new StreamSource(xslStyleSheet));
  }

}

class TimeIt {
  public static void run(final Command command) {
    final long startTime = System.currentTimeMillis();
    command.execute();
    final long endTime = System.currentTimeMillis();
    XSLTransformationTest.log.info("Total time:" + (endTime - startTime)/1000.0 + " secs");
  }

  public static void logTime() {
    XSLTransformationTest.log.info(System.currentTimeMillis());
  }
}

interface Command {
  void execute();
}