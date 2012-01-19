/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import groovy.xml.StreamingMarkupBuilder

import org.plos.util.ToolHelper
import org.plos.models.DublinCore
import org.plos.models.EditorialBoard
import org.plos.models.Journal
import org.plos.models.Article

import org.topazproject.otm.ModelConfig
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.criterion.DetachedCriteria
import org.topazproject.otm.criterion.EQCriterion
import org.topazproject.otm.criterion.Restrictions
import org.topazproject.otm.stores.ItqlStore
import org.topazproject.xml.transform.cache.CachedSource

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

log = LogFactory.getLog(this.getClass());

// Use ToolHelper (currently in wrong place -- article-util) to patch args
args = ToolHelper.fixArgs(args)

def cli = new CliBuilder(usage: 'createjournal [-c config-overrides.xml] -j journal-definition.xml')
cli.h(longOpt:'help', "help (this message)")
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.j(args:1, 'journal-definition.xml - file with journal definition in xml')
cli.t(args:0, 'test (dry-run)')

// Display help if requested
def opt = cli.parse(args); if (opt.h) { cli.usage(); return }

// Stash args
DRYRUN = opt.t

// Load configuration
CONF = ToolHelper.loadConfiguration(opt.c)

// Get the journal-definition.xml file from the command line
journalDefinition = opt.j

// Create the slurper object and add entity resolver
def slurper = new XmlSlurper()
slurper.setEntityResolver(CachedSource.getResolver())

// Ask slurper to read journal definition
def slurpedJournal = slurper.parse(new File(journalDefinition))

println "parsed journal definition input: " + journalDefinition

// Setup OTM
def factory = new SessionFactory();
def itql = new ItqlStore(URI.create(CONF.getString("topaz.services.itql.uri")))
def ri = new ModelConfig("ri", URI.create(CONF.getString("topaz.models.articles")), null);
def p = new ModelConfig("profiles", URI.create(CONF.getString("topaz.models.profiles")), null);
def cModel = new ModelConfig("criteria", URI.create(CONF.getString("topaz.models.criteria")), null);
factory.setTripleStore(itql)
factory.addModel(ri);
factory.addModel(p);
factory.addModel(cModel);
factory.preload(DetachedCriteria.class);
factory.preload(EQCriterion.class);
factory.preload(EditorialBoard.class);
factory.preload(Journal.class);
factory.preload(Article.class);
def session = factory.openSession();
def tx = session.beginTransaction();
def key = slurpedJournal.key

List<Journal> existingJournals = session.createCriteria(Journal.class)
      .add(Restrictions.eq('key', key)).list()

def journal
if(existingJournals.size() == 0) {
  println 'Creating Journal in database: ' + key
  journal = new Journal()
  journal.key   = key
} else {
  journal = existingJournals[0]
  println 'Journal ' + journal.key + ' already exists'
}

// set the journal key, eIssn
journal.eIssn = slurpedJournal.eIssn
journal.dublinCore = new DublinCore()
journal.dublinCore.title = slurpedJournal.dublinCore.title

// create a filter, by eIssn, for this journal
EQCriterion eqEIssn = (EQCriterion)Restrictions.eq("eIssn", journal.eIssn)
eqEIssn.setSerializedValue(journal.eIssn)
DetachedCriteria filterByEIssn = (new DetachedCriteria("Article")).add(eqEIssn)
journal.setSmartCollectionRules([filterByEIssn])

println "Journal.key:   " + journal.key
println "Journal.eIssn: " + journal.eIssn
println "Journal.dublinCore.title: " + journal.dublinCore.title
println "Journal.simpleCollection: " + journal.simpleCollection



if (!DRYRUN) {
  try {
    session.saveOrUpdate(journal)
    tx.commit()
    session.close()
    println 'Database updated with: ' + journal.key
  } catch (Throwable t) {
    log.error("Unable to save journal", t);
    println "Unable to save journal: " + t
    throw(t)
  }
}

// TODO: Deal with errors (i.e. catch exceptions)
