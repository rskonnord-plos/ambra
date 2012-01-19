/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import org.topazproject.otm.impl.SessionFactoryImpl;
import org.topazproject.otm.stores.ItqlStore;
import org.topazproject.xml.transform.cache.CachedSource;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

// Constants
MULGARA_BASE = "localhost"
MULGARA_LOC = "/topazproject"
FEDORA_BASE = "localhost:9090"
PREFS       = "<rmi://localhost/topazproject#filter:model=preferences>"
LIMIT       = 50000
MAX         = 10000000
EMAIL       = 'alertsEmailAddress'
JOURNALS    = 'alertsJournals'
CATEGORIES  = 'alertsCategories'

def cli = new CliBuilder(usage: 'annotated --start <date> --end <date>')
cli.h(longOpt:'help', 'usage information')
cli.v(longOpt:'verbose', 'turn on verbose mode')
cli.s(args:1, 'Date to start with')
cli.e(args:1, 'Date to end with')
cli.F(args:1, 'Fedora host:port')
cli.M(args:1, 'Mulgara host:port')

if (args.length == 1 && args[0].indexOf(' ') > 0)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                  .getTokenArray()
def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def mulgaraBase = (opt.M) ? opt.M : MULGARA_BASE
def fedoraBase  = (opt.F) ? opt.F : FEDORA_BASE
def mulgaraUri  = "rmi://${mulgaraBase}${MULGARA_LOC}"
def fedoraUri   = "http://${fedoraBase}/fedora/get"
def verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
  println "Fedora URI: $fedoraUri"
}

// Globals
RI_MODEL='<rmi://localhost/topazproject#ri>'
STR_MODEL='<rmi://localhost/topazproject#str>'
DATETIME='<http://www.w3.org/2001/XMLSchema#dateTime>'

sf = new SessionFactoryImpl(tripleStore:new ItqlStore(mulgaraUri.toURI()))
sf.addAlias('a', 'http://www.w3.org/2000/10/annotation-ns#')
sess = sf.openSession()
sess.beginTransaction()

restrict = ""
if (opt.s) {
  restrict += " and (\$created <topaz:ge> '${opt.s}' in ${STR_MODEL} " + 
                " or \$created <topaz:ge> '${opt.s}T00:00:00'^^${DATETIME} in ${STR_MODEL})"
}
if (opt.e) {
  restrict += " and (\$created <topaz:le> '${opt.e}' in ${STR_MODEL} " +
               " or \$created <topaz:le> '${opt.e}T00:00:00'^^${DATETIME} in ${STR_MODEL})"
}

query = """
  select \$article \$title
    count(select \$ann from ${RI_MODEL}
          where (     \$ann <rdf:type> <a:Annotation>
                  and \$ann <a:annotates> \$article
                  and \$ann <a:created> \$created
                  ${restrict} )
            minus \$ann <rdf:type> <topaz:RatingSummaryAnnotation>)
    from ${RI_MODEL}
    where \$s <rdf:type> <a:Annotation>
      and \$s <a:annotates> \$article
      and \$s <a:created> \$created
      and \$article <rdf:type> <topaz:Article>
      and \$article <dc:title> \$title
      ${restrict}
"""

if (verbose)
  println "Query: $query"

def results = sess.doNativeQuery(query + ";");
if (verbose)
  println "Results: $results"

// Get an XmlSlurper object and use our resolver (without resolver, parsing takes forever)
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

if (verbose)
  println "article, title, author(s), count"
while (results.next()) {
  // Get article DOI from mulgara answer and convert to fedora URL
  String doi = results.getString('article')
  def articleUrl = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"

  // Parse article and get list of authors into a string
  def authors = "" // An empty string
  def article = slurper.parse(new URL(articleUrl).getContent())
  def authorList = article.front.'article-meta'.'author-notes'.corresp.email.list()
  if (authorList) authors = authorList.toString()[1..-2] // String of comma-separated emails

  def title = results.getString('title')
  title = title.replaceAll(/<.*?>/) { it = " " }
  title = title.replaceAll(/\s{1,}/) { it = " " }

  // Dump out one of the articles we found as a line of tab separated data
  println "${doi}\t${title}\t$authors\t${results.getString('k0')}"
}

sess.close()
