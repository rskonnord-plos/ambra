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
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

// Constants
MULGARA_BASE = "localhost"
MULGARA_LOC = "/topazproject"
LIMIT       = 1000
MAX         = 10000000
JOURNALS    = 'alertsJournals'
CATEGORIES  = 'alertsCategories'
RI_MODEL    = '<rmi://localhost/topazproject#ri>'
STR_MODEL   = '<rmi://localhost/topazproject#str>'
USERS_MODEL = '<rmi://localhost/topazproject#users>'
PREFS_MODEL = '<rmi://localhost/topazproject#preferences>'
PROFILES_MODEL = '<rmi://localhost/topazproject#profiles>'

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
def mulgaraUri  = "rmi://${mulgaraBase}${MULGARA_LOC}"
def itemType    = (opt.c) ? CATEGORIES : JOURNALS
def verbose = opt.v
if (verbose) {
  println "Mulgara URI: $mulgaraUri"
}

sf = new SessionFactoryImpl(tripleStore:new ItqlStore(mulgaraUri.toURI()))
sf.addAlias('foaf', 'http://xmlns.com/foaf/0.1/')
sess = sf.openSession()
sess.beginTransaction()

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

def offset = 0

while (true)
{
  def results = sess.doNativeQuery("$query limit $LIMIT offset $offset;");
  if (verbose)
    println "Results: $results"

  boolean gotSomething = false
  while (results.next()) {
    def email = results.getString('email').substring(7)
    def items = results.getSubQueryResults('k0')
    def itemList = []
    while (items.next())
      itemList << items.getString('item')
    if (itemList)
      println """\"$email", "${itemList.toString()[1..-2]}\""""
    gotSomething = true
  }

  if (!gotSomething || offset >= MAX)
    break
  offset += LIMIT
}

sess.close()
