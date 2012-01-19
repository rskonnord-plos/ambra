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

import javax.activation.DataHandler;
import javax.activation.URLDataSource
import org.plos.article.util.Zip
import org.plos.util.ToolHelper

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Ingest [-c config-overrides.xml] zip')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

def util = new ArticleUtil()

opt.arguments().each() {
  // Zip.FileZip
/*
  def zip = new Zip.FileZip(it)
  def doi = util.ingest(zip)
  println "Ingested $it: $doi w/Zip.FileZip"
*/

  // Zip.DataSource
  def zip = new Zip.DataSourceZip(new URLDataSource(it.toURL()));
  def doi = util.ingest(zip)
  println "Ingested $it: $doi w/Zip.DataSourceZip"

}

println "Ingested ${opt.arguments().size()} article(s)"
