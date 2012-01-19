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

import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.ModelConfig;
import org.topazproject.otm.stores.ItqlStore;

import org.plos.models.Article
import org.plos.models.ObjectInfo
import org.plos.models.Category
import org.plos.models.Citation
import org.plos.models.PLoS
import org.plos.models.UserProfile

import org.plos.util.ToolHelper

args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'Delete [-c config-overrides.xml] article-uris ...')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.i(args:0, 'ignore errors')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Load configuration before ArticleUtil is instantiated
CONF = ToolHelper.loadConfiguration(opt.c)

def factory = new SessionFactory();
def itql = new ItqlStore(URI.create(CONF.getString("topaz.services.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("topaz.models.articles")), null)
def p = new ModelConfig("profiles", URI.create(CONF.getString("topaz.models.profiles")), null)
factory.setTripleStore(itql)
factory.addModel(ri)
factory.addModel(p)
factory.preload(Article.class)
factory.preload(Category.class)
factory.preload(Citation.class)
factory.preload(UserProfile.class)
def session = factory.openSession()

// We may want to ignore errors if something needs to be cleaned up
ignore = opt.i
def process(c) {
  try { c() } catch (Exception e) {
    if (ignore) { println "${e.getClass()}: ${e}" }
    else { throw e }
  }
}

// Get directories zip files are stashed in
def queueDir    = CONF.getString('pub.spring.ingest.source', '/var/spool/plosone/ingestion-queue')
def ingestedDir = CONF.getString('pub.spring.ingest.destination', '/var/spool/plosone/ingested')

def util = new ArticleUtil()
opt.arguments().each() { uri ->
  // Call ArticleUtil.delete() to remove from mulgara, fedora & lucene
  print "Deleting article $uri..."
  process() {
    def tx = session.beginTransaction()
    util.delete(uri, tx)
    tx.commit()
  }
  println "done"
}

println "Tried to delete ${opt.arguments().size()} article(s)"
