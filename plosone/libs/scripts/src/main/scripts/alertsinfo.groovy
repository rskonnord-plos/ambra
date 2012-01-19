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
MULGARA_BASE = "localhost:9091"
MULGARA_LOC = "/mulgara-service/services/ItqlBeanService"
LIMIT       = 1000
MAX         = 10000000
JOURNALS    = 'alertsJournals'
CATEGORIES  = 'alertsCategories'
RI_MODEL    = '<local:///topazproject#ri>'
STR_MODEL   = '<local:///topazproject#str>'
USERS_MODEL = '<local:///topazproject#users>'
PREFS_MODEL = '<local:///topazproject#preferences>'
PROFILES_MODEL = '<local:///topazproject#profiles>'

def cli = new CliBuilder(usage: 'annotated --start <date> --end <date>')
cli.h(longOpt:'help', 'usage information')
cli.v(longOpt:'verbose', 'turn on verbose mode')
cli.M(args:1, 'Mulgara host:port')
cli.j(longOpt:'journals', 'Get journal information')
cli.c(longOpt:'cats', 'Get Categories')

if (args.length == 1 && args[0].indexOf(' ') > 0)
  args = new StrTokenizer(args[0], StrMatcher.trimMatcher(), StrMatcher.quoteMatcher())
                  .getTokenArray()
def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }
def mulgaraBase = (opt.M) ? opt.M : MULGARA_BASE
def mulgaraUri  = "http://${mulgaraBase}${MULGARA_LOC}"
def itemType    = (opt.c) ? CATEGORIES : JOURNALS
def verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
}

itql = new ItqlHelper(new URI(mulgaraUri))
def aliases = ItqlHelper.getDefaultAliases()
aliases['foaf'] = "http://xmlns.com/foaf/0.1/"

query = """
  select \$user \$email
    subquery( select \$item from ${PREFS_MODEL}
               where \$user <topaz:hasPreferences> \$pref
                 and \$pref <dc_terms:mediator> 'topaz-plosone'
                 and \$pref <topaz:preference> \$prefn
                 and \$prefn <topaz:prefName> '${itemType}'
                 and \$prefn <topaz:prefValue> \$item )
    from ${PROFILES_MODEL}
   where \$prof <foaf:holdsAccount> \$user
     and \$prof <rdf:type> <foaf:Person>
     and \$prof <foaf:mbox> \$email
   order by \$email
"""

if (verbose)
  println "Query: $query"

// Get an XmlSlurper object and use our resolver (without resolver, parsing takes forever)
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

def offset = 0

while (true)
{
  def results = itql.doQuery("$query limit $LIMIT offset $offset;", aliases);
  if (verbose)
    println "Results: $results"

  def ans = new XmlSlurper().parseText(results)
  ans.query[0].solution.each() {
    def email = it.email.'@resource'.toString().substring(7)
    def itemList = it.k0.solution.item.list()
    if (itemList) {
      def items = itemList.toString()[1..-2]
      println """\"$email", "$items\""""
    }
  }

  if (ans.query[0].solution.size() == 0 || offset >= MAX)
    break
  offset += LIMIT
}
