/* $HeadURL::                                                                                     $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.article.util

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Delete [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

def util = new ArticleUtil()
opt.arguments().each() {
  util.delete(it)
  println "Deleted $it"
}

println "Deleted ${opt.arguments().size()} article(s)"
