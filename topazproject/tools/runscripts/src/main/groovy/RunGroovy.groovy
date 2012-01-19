/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

def cli = new CliBuilder(usage: 'rungroovy ...')
cli.h(longOpt:'help', 'usage: rungroovy script.groovy [args]')

// In case being called from maven, re-parse arguments
if (args.length == 1 && args[0] == null) args = [ ]
if (args.length == 1 && args[0].indexOf(' ') > 0)
  args = new StrTokenizer(args[0], 
                          StrMatcher.trimMatcher(), 
                          StrMatcher.quoteMatcher()).getTokenArray()
def opt = cli.parse(args)
if (!opt) return
if (opt.h) { cli.usage(); return }

// Parse args to pass to sub-script
args = opt.getArgs().toList()

if (args.size() > 0) {
  def prog = args[0]
  args.remove(0)
  new GroovyShell().run(new File(prog), args)
} else
  new GroovyShell().run(System.in, 'stdin', null)
