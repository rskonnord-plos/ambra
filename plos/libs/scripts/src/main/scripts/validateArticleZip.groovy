/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import java.io.FileInputStream

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.plos.article.util.Ingester
import org.plos.article.util.Zip
import org.plos.article.util.Zip2Xml
import org.plos.util.ToolHelper

import org.w3c.dom.Document

private static final String OUT_LOC_P = 'output-loc'

log = LogFactory.getLog(this.getClass())


def cli = new CliBuilder(
  usage: 'validateArticleZip [-c config-overrides.xml] [-t file:/tmp/] -f file:/tmp/article.zip')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.t(args:1, 'file:/tmp/ - location of tmp dir. please add trailing slash.')
cli.f(args:1, 'file:/tmp/article.zip - article.zip')

args = ToolHelper.fixArgs(args)

def opt = cli.parse(args);

// Display help if requested
if (opt.h) { cli.usage(); return }

// file required
if (!opt.f) { cli.usage(); return }

def URI tmpDir = (opt.t) ? URI.create(opt.t) : URI.create('file:/tmp/')
 
// Load configuration
CONF = ToolHelper.loadConfiguration(opt.c)

// Get the article.zip from the command line
articleZip = URI.create(opt.f)

println "processing file: $articleZip"  
println "tmp dir: $tmpDir"
println ""

// Topaz/Fedora/PLoS wrapped zip
def inputStream  = new FileInputStream(new File(articleZip))
def wrappedZip = new Zip.DataSourceZip(new org.apache.axis.attachments.ManagedMemoryDataSource(
  inputStream, 8192, 'application/octet-stream', true))

// get zip info
String zipInfo = Zip2Xml.describeZip(wrappedZip)
println "Extracted zip-description: $zipInfo"

// find ingest format handler
String handler = getClass().getResource("/org/plos/article/util/pmc2obj.xslt").toString()
println "Using ingest handler: $handler"

// use handler to convert zip to fedora-object and RDF descriptions
tFactory = new TransformerFactoryImpl();
tFactory.setURIResolver(new Ingester.URLResolver());
tFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);

/* debug trace
tFactory.setAttribute("http://saxon.sf.net/feature/traceListener",
  new net.sf.saxon.trace.XSLTTraceListener());
*/

Transformer t = tFactory.newTransformer(new StreamSource(handler))
t.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
t.setURIResolver(new Ingester.ZipURIResolver(wrappedZip))
((Controller) t).setBaseOutputURI(tmpDir.toString())
t.setParameter(OUT_LOC_P, tmpDir.toString())

final StringWriter msgs = new StringWriter();
((Controller) t).makeMessageEmitter();
((Controller) t).getMessageEmitter().setWriter(msgs);
t.setErrorListener(new MyErrorListener())

Source    inp = new StreamSource(new StringReader(zipInfo), "zip:/");
DOMResult res = new DOMResult();

try {
  t.transform(inp, res);
} catch (TransformerException te) {
  if (msgs.getBuffer().length() > 0)
    throw new TransformerException(msgs.toString(), te);
  else
    throw te;
}
if (msgs.getBuffer().length() > 0)
  throw new TransformerException(msgs.toString());

println "result: ${res.getSystemId()}, ${res.getNode()}"

/* Note: it would be preferable (and correct according to latest JAXP specs) to use
 * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
 * Hence we need to use Saxon's API's in order to get at those messages.
 */
private class MyErrorListener implements ErrorListener {
  public void warning(TransformerException te) {
    println "Warning received while processing zip: $te"
  }

  public void error(TransformerException te) {
    println "Error received while processing zip: $te"
    msgs.write(te.getMessageAndLocation() + '\n');
  }

  public void fatalError(TransformerException te) {
    println "Fatal error received while processing zip: $te"
    msgs.write(te.getMessageAndLocation() + '\n');
  }
}
