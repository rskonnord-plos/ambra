/* $HeadURL::                                                                                    $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.plos.article.util

import org.plos.util.ToolHelper

/*
 * Prepare a SIP from an AP zip. This goes through the following steps:
 * <ol>
 *   <li>create and add a manifest to the zip
 *   <li>fix up the article links
 *   <li>perform validation checks on the SIP and article
 * </ol>
 *
 * @author Ronald Tschalär
 */

args = ToolHelper.fixArgs(args)

String usage = 'PrepareSIP [-v] [-c <config-overrides.xml>] [-o <output.zip>] <article.zip>'
def cli = new CliBuilder(usage: usage, writer : new PrintWriter(System.out))
cli.h(longOpt:'help', "help (this message)")
cli.o(args:1, 'output.zip - new zip file containing prepared sip; if not specified\n' +
              '             then input file is overwritten')
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz/ambra.xml')
cli.v(args:0, 'verbose')

def opt = cli.parse(args);

String[] otherArgs = opt.arguments()
if (opt.h || otherArgs.size() != 1) {
  cli.usage()
  return
}

def config = ToolHelper.loadConfiguration(opt.c)

println("SIP for " + otherArgs[0])

try {
  new AddManifest().addManifest(otherArgs[0], opt.o ?: null)
  println "  manifest added"
  
  new FixArticle().fixLinks(opt.o ?: otherArgs[0], null)
  println "  article links fixed"
  
  new ProcessImages(config, opt.v).processImages(opt.o ?: otherArgs[0], null)
  println "  images processed"
  
  new ValidateSIP().validate(opt.o ?: otherArgs[0])
  println "  validation: No problems found"
  println "done"

  System.exit(0)
} catch (Exception e) {
  println("  error: " + e)
  System.exit(1)
}
