/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.topazproject.mulgara.itql.ItqlHelper;
import org.topazproject.mulgara.itql.Answer;
import org.topazproject.xml.transform.cache.CachedSource;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

// Constants
MULGARA_BASE = "localhost:9090"
MULGARA_LOC = "/mulgara-service/services/ItqlBeanService"
FEDORA_BASE = "localhost:9090"
PREFS       = "<local:///topazproject#filter:model=preferences>"
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
def mulgaraUri  = "http://${mulgaraBase}${MULGARA_LOC}"
def fedoraUri   = "http://${fedoraBase}/fedora/get"
def verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
  println "Fedora URI: $fedoraUri"
}

// Globals
RI_MODEL='<local:///topazproject#ri>'
STR_MODEL='<local:///topazproject#str>'

itql = new ItqlHelper(new URI(mulgaraUri))
def aliases = ItqlHelper.getDefaultAliases()
aliases['a'] = 'http://www.w3.org/2000/10/annotation-ns#'

restrict = ""
if (opt.s)
  restrict += " and \$created <topaz:ge> '${opt.s}' in ${STR_MODEL}"
if (opt.e)
  restrict += " and \$created <topaz:le> '${opt.e}' in ${STR_MODEL}"

query = """
  select \$article
    subquery(select \$article \$title from ${RI_MODEL}
             where \$article <dc:subject> \$title)
    count(select \$ann from ${RI_MODEL}
          where \$ann <rdf:type> <a:Annotation>
            and \$ann <a:annotates> \$article
            and \$ann <a:created> \$created
            ${restrict})
    from ${RI_MODEL}
    where \$s <rdf:type> <a:Annotation>
      and \$s <a:annotates> \$article
      and \$s <a:created> \$created
      ${restrict}
"""

if (verbose)
  println "Query: $query"

def results = itql.doQuery(query + ";", aliases);
if (verbose)
  println "Results: $results"

// Get an XmlSlurper object and use our resolver (without resolver, parsing takes forever)
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

println "article, title, author(s), count"
def ans = new XmlSlurper().parseText(results)
ans.query[0].solution.each() {
  // Get article DOI from mulgara answer and convert to fedora URL
  String doi = it.article.'@resource'
  def articleUrl = "$fedoraUri/doi:${URLEncoder.encode(doi.substring(9))}/XML"

  // Parse article and get list of authors into a string
  def authors = "" // An empty string
  def article = slurper.parse(new URL(articleUrl).getContent())
  def authorList = article.front.'article-meta'.'author-notes'.corresp.email.list()
  if (authorList) authors = authorList.toString()[1..-2] // String of comma-separated emails

  // Dump out one of the articles we found as a line of CSV data
  println """\"${it.article.'@resource'}","${it.k0.solution.title.text()}","$authors", ${it.k1}"""
}
