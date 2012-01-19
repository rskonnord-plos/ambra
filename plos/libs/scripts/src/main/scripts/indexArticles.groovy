/* $HeadURL:: http://gandalf.topazproject.org/svn/head/plosone/libs/article-util/src/main/groovy/#$
 * $Id: Delete.groovy 2686 2007-05-15 08:22:38Z ebrown $
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

import java.rmi.RemoteException
import javax.xml.rpc.ServiceException

import org.apache.commons.configuration.Configuration
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.plos.article.util.ArticleUtil
import org.plos.configuration.ConfigurationStore
import org.plos.models.Article
import org.plos.util.ToolHelper

import org.topazproject.fedoragsearch.service.FgsOperations
import org.topazproject.otm.ModelConfig
import org.topazproject.otm.SessionFactory
import org.topazproject.otm.query.Results
import org.topazproject.otm.stores.ItqlStore

/**
 * Index all Articles.
 *
 * @author jsuttor
 */

log = LogFactory.getLog(this.getClass());

// parse args
def cli = new CliBuilder(usage: 'indexArticles [-c config-overrides.xml] [-s startDoi] [-o onlyDoi]')
cli.h(longOpt:'help', 'help (this message)')
cli.c(args:1, 'config-overrides.xml - overrides /etc/topaz.xml')
cli.s(args:1, 'startDoi - indexing will start with this Doi (alphabetical sort)')
cli.o(args:1, 'onlyDoi - only index this Doi')
args = ToolHelper.fixArgs(args)
def opt = cli.parse(args);

// display help if requested
if (opt.h) { cli.usage(); return }

// Load configuration
def CONF = ToolHelper.loadConfiguration(opt.c)
println "configuration loaded (w/-config-overrides.xml: ${opt.c}): $CONF"

def fgsRepository = CONF.getString('topaz.fedoragsearch.repository', 'Topaz')
def fgsAction     = 'fromPid'

// let ArticleUtil use standard methods to get fedoragsearch operations
def FgsOperations[] fgs
try {
  fgs = ArticleUtil.getFgsOperations()
} catch (ServiceException ex) {
  log.error(ex)
  throw new Exception("Exception creating fedoragsearch operations", ex)
}
println "created Fedora Service: fgs: $fgs, fgsRepository: $fgsRepository, fgsAction: $fgsAction"

// could be 1 & only 1 Doi specified on the cmd line
if (opt.o) {
  print "Only re-indexing Article: ${opt.o}..."
  indexDoi(fgsRepository, fgsAction, opt.o, fgs)
  println ''

  System.exit(0)
}

// get a list of Article dois
// Setup OTM
def factory = new SessionFactory();
def itql = new ItqlStore(URI.create(CONF.getString('topaz.services.itql.uri')))
def ri = new ModelConfig('ri', URI.create(CONF.getString('topaz.models.articles')), null);
factory.setTripleStore(itql)
factory.addModel(ri)
factory.preload(Article.class)
def session = factory.openSession();

// query for all Article dois
def query = 'select a.id doi from Article a order by doi;'
def tx = session.beginTransaction();
def results = session.createQuery(query).execute()
tx.commit()

// re-index each doi
def indexCount = 0
while (results.next()) {
  def doi = "doi:${results.get('doi').toString().substring(9).replace('/', '%2F')}"

  // parms allow skipping of Doi's
  if (opt.s && doi.compareTo(opt.s) < 0) { continue }

  print "re-indexing Article: $doi..."

  // re-index in each index
  indexDoi(fgsRepository, fgsAction, doi, fgs)
  indexCount++
  println ''
}

println ''
println "total re-indexed Articles: $indexCount"

private void indexDoi(String fgsRepository, String fgsAction, String doi, FgsOperations[] fgs) {
  try {
    for (int onFgs = 0; onFgs < fgs.length; onFgs++) {
      def start = (new Date()).getTime()
      def result = fgs[onFgs].updateIndex(fgsAction, doi, fgsRepository, null, null, null)
      def elapsed = (new Date()).getTime() - start
      print " index[$onFgs]($elapsed ms)"
    }
  } catch (RemoteException re) {
    final String errorMessage = "Exception indexing Article: $doi"
    log.error(errorMessage, re)
    throw new Exception(errorMessage, re)
  }
}
