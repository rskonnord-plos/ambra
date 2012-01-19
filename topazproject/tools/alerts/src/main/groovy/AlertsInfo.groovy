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

import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

// Constants
DEFAULT_HOST = "localhost"
DEFAULT_PORT = 9091
DEFAULT_LOC = "/mulgara-service/services/ItqlBeanService"
DEFAULT_URI = "http://${DEFAULT_HOST}:${DEFAULT_PORT}${DEFAULT_LOC}"
PREFS       = "<local:///topazproject#filter:model=preferences>"
LIMIT       = 50000
MAX         = 10000000
EMAIL       = 'alertsEmailAddress'
JOURNALS    = 'alertsJournals'
CATEGORIES  = 'alertsCategories'

// Deal with options
options = new Options()
options.addOption("U", "uri",  true, "the mulgara uri")
options.addOption("h", "host", true, "the mulgara host")
options.addOption("j", "journals", false, "Get journal information")
options.addOption("c", "cats", false, "Get categories")
options.addOption("?", "help", false, "usage information")
cmd = new PosixParser().parse(options, args)
uri = cmd.getOptionValue("U", DEFAULT_URI)
if (!uri) uri = "http://${cmd.getOptionValue("h", DEFAULT_HOST)}:${DEFAULT_PORT}${DEFAULT_LOC}"
if (cmd.hasOption("?")) {
  new HelpFormatter().printHelp("alerts", options)
  System.exit(1)
}

// Globals
itql = new ItqlHelper(new URI(uri))
users = [:]

/**
 * Run a query over the suplied data set and populate users map
 *
 * @param prefName The name of the preference
 */
def doQuery(prefName) {
  // Query sans LIMIT & offset clauses
  query = '''
    select $user $value from ''' + PREFS + '''
      where $user <topaz:hasPreferences> $pref
        and $pref   <dc_terms:mediator>  'topaz-plosone' 
        and $pref   <topaz:preference>   $prefn 
        and $prefn  <topaz:prefName>     ''' + "'$prefName'" + '''
        and $prefn  <topaz:prefValue>    $value
      order by $user '''

  offset = 0
  while (true) {
    result = itql.doQuery("$query limit $LIMIT offset $offset;", null)
    rows = new Answer(result).getAnswers().get(0).getRows();
    rows.each() {
      user = users[it[0]]
      if (user != null)
        user[prefName] = (user[prefName] != null ? user[prefName] + it[1] : [ it[1] ])
      else
        users[it[0]] = [ (prefName) : [ it[1] ] ]
    }
//    println "Retrieved ${offset + rows.size()} $prefName"
    if (rows.size() == 0 || offset >= MAX)
      break;
    offset += LIMIT
  }
}

def getCsv(prefName) {
  doQuery(EMAIL)
  doQuery(prefName)

  users.values().each() {
    if (it[prefName]) {
      values = it[prefName].toString()
      println "${it[EMAIL][0]}, ${values[1..-2]}"
    }
  }
}

if (cmd.hasOption('c'))
  getCsv(CATEGORIES);
else if (cmd.hasOption('j'))
  getCsv(JOURNALS);

